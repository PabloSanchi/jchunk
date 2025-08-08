package jchunk.chunker.semantic.embedder;

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.onnxruntime.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Embeds input text into dense vectors using a HuggingFace tokenizer and an ONNX model.
 *
 * <p>Loads its tokenizer and model from the classpath and applies mean pooling on the last hidden
 * states.
 *
 * <p><i>Inspired by <a href="https://spring.io/projects/spring-ai">Spring AI</a> default Embedding Model impl</i>
 *
 * @author Pablo Sanchidrian Herrera
 */
public class JChunkEmbedder implements Embedder, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(JChunkEmbedder.class);

    private static final String DEFAULT_TOKENIZER_PATH = "onnx/tokenizer.json";
    private static final String DEFAULT_MODEL_PATH = "onnx/model.onnx";
    private static final String DEFAULT_OUTPUT_NAME = "last_hidden_state";

    private final HuggingFaceTokenizer tokenizer;
    private final OrtEnvironment ortEnv;
    private final OrtSession ortSession;
    private final Set<String> modelInputs;
    private final String outputName;

    public JChunkEmbedder() throws IOException, OrtException {
        this(DEFAULT_TOKENIZER_PATH, DEFAULT_MODEL_PATH);
    }

    public JChunkEmbedder(String tokenizerResource, String modelResource) throws IOException, OrtException {
        try (InputStream tokStream = loadResource(tokenizerResource);
                InputStream modelStream = loadResource(modelResource)) {

            this.tokenizer = HuggingFaceTokenizer.newInstance(tokStream, Map.of());
            this.ortEnv = OrtEnvironment.getEnvironment();

            byte[] modelBytes = modelStream.readAllBytes();
            this.ortSession = ortEnv.createSession(modelBytes, new OrtSession.SessionOptions());
            this.modelInputs = ortSession.getInputNames();

            Map<String, NodeInfo> outputs = ortSession.getOutputInfo();
            this.outputName = outputs.containsKey(DEFAULT_OUTPUT_NAME)
                    ? DEFAULT_OUTPUT_NAME
                    : outputs.keySet().iterator().next();
        }
    }

    @Override
    public int getDimension() {
        try {
            TensorInfo tInfo =
                    (TensorInfo) ortSession.getOutputInfo().get(outputName).getInfo();
            long[] shape = tInfo.getShape();
            if (shape.length >= 3 && shape[2] > 0) {
                return (int) shape[2];
            }
        } catch (Exception e) {
            log.warn("Unable to infer embedding dimension directly, running fallback embedding", e);
        }
        return embed("a").length;
    }

    @Override
    @SuppressWarnings("java:S112")
    public List<float[]> embed(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            throw new IllegalArgumentException("Input text list must not be null or empty");
        }

        var encodings = tokenizer.batchEncode(texts);
        long[][] inputIds = new long[encodings.length][];
        long[][] attentionMask = new long[encodings.length][];
        long[][] tokenTypeIds = new long[encodings.length][];

        for (int i = 0; i < encodings.length; i++) {
            inputIds[i] = encodings[i].getIds();
            attentionMask[i] = encodings[i].getAttentionMask();
            long[] typeIds = encodings[i].getTypeIds();
            tokenTypeIds[i] = (typeIds != null && typeIds.length > 0) ? typeIds : new long[inputIds[i].length];
        }

        try (OnnxTensor idsTensor = OnnxTensor.createTensor(ortEnv, inputIds);
                OnnxTensor maskTensor = OnnxTensor.createTensor(ortEnv, attentionMask);
                OnnxTensor typesTensor = OnnxTensor.createTensor(ortEnv, tokenTypeIds);
                NDManager manager = NDManager.newBaseManager()) {

            Map<String, OnnxTensor> allInputs = Map.of(
                    "input_ids", idsTensor,
                    "attention_mask", maskTensor,
                    "token_type_ids", typesTensor);

            Map<String, OnnxTensor> acceptedInputs = filterInputs(allInputs, modelInputs);

            try (OrtSession.Result result = ortSession.run(acceptedInputs)) {
                OnnxValue outputVal = result.get(outputName)
                        .orElseThrow(() -> new IllegalStateException("Model output not found: " + outputName));

                float[][][] tokenEmbeddings = (float[][][]) outputVal.getValue();
                NDArray pooled = meanPool(toNDArray(tokenEmbeddings, manager), manager.create(attentionMask));

                List<float[]> vectors = new ArrayList<>(texts.size());
                for (int i = 0; i < pooled.getShape().get(0); i++) {
                    vectors.add(pooled.get(i).toFloatArray());
                }
                return vectors;
            }

        } catch (OrtException e) {
            throw new RuntimeException("ONNX inference failed", e);
        }
    }

    private static Map<String, OnnxTensor> filterInputs(Map<String, OnnxTensor> provided, Set<String> accepted) {
        return provided.entrySet().stream()
                .filter(e -> accepted.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static NDArray toNDArray(float[][][] data, NDManager manager) {
        var d0 = data.length;
        var d1 = data[0].length;
        var d2 = data[0][0].length;

        var buf = FloatBuffer.allocate(d0 * d1 * d2);

        Arrays.stream(data).flatMap(Arrays::stream).forEach(buf::put);

        buf.rewind();
        return manager.create(buf, new Shape(d0, d1, d2));
    }

    private static NDArray meanPool(NDArray tokenEmbeddings, NDArray attentionMask) {
        var maskExpanded = attentionMask
                .expandDims(-1)
                .broadcast(tokenEmbeddings.getShape())
                .toType(DataType.FLOAT32, false);

        var sumEmbeddings = tokenEmbeddings.mul(maskExpanded).sum(new int[] {1});
        var sumMask = maskExpanded.sum(new int[] {1}).clip(1e-9f, Float.MAX_VALUE);

        return sumEmbeddings.div(sumMask);
    }

    private static InputStream loadResource(String path) throws IOException {
        InputStream in = JChunkEmbedder.class.getClassLoader().getResourceAsStream(path);

        if (in == null) {
            throw new IOException("Resource not found in classpath: " + path);
        }

        return in;
    }

    @Override
    public void close() {
        try {
            ortSession.close();
            ortEnv.close();
            tokenizer.close();
        } catch (Exception e) {
            log.warn("Failed to close", e);
        }
    }
}
