package jchunk.chunker.semantic;

import jchunk.chunker.core.chunk.Chunk;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.springframework.ai.embedding.EmbeddingModel;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Utility class for semantic chunking. Wraps the different methods needed to chunk a text
 * semantically.
 *
 * @author Pablo Sanchidrian Herrera
 */
public class Utils {

	/**
	 * Split the content into sentences
	 * @param content the content to split
	 * @return the list of sentences
	 */
	public static List<Sentence> splitSentences(String content, SentenceSplitingStrategy splitingStrategy) {
		AtomicInteger index = new AtomicInteger(0);
		return Arrays.stream(content.split(splitingStrategy.getStrategy()))
			.map(sentence -> Sentence.builder().content(sentence).index(index.getAndIncrement()).build())
			.collect(Collectors.toList());
	}

	/**
	 * Combine the sentences based on the buffer size (append the buffer size of sentences
	 * behind and over the current sentence)
	 * <p>
	 * Use the sliding window technique to reduce the time complexity
	 * @param sentences the list of sentences
	 * @param bufferSize the buffer size to use
	 * @return the list of combined sentences
	 */
	public static List<Sentence> combineSentences(List<Sentence> sentences, Integer bufferSize) {
		assert sentences != null : "The list of sentences cannot be null";
		assert !sentences.isEmpty() : "The list of sentences cannot be empty";
		assert bufferSize != null && bufferSize > 0 : "The buffer size cannot be null nor 0";
		assert bufferSize < sentences.size() : "The buffer size cannot be greater equal than the input length";

		int n = sentences.size();
		int windowSize = bufferSize * 2 + 1;
		int currentWindowSize = 0;
		StringBuilder windowBuilder = new StringBuilder();

		for (int i = 0; i <= Math.min(bufferSize, n - 1); i++) {
			windowBuilder.append(sentences.get(i).getContent()).append(" ");
			currentWindowSize++;
		}

		windowBuilder.deleteCharAt(windowBuilder.length() - 1);

		for (int i = 0; i < n; ++i) {
			sentences.get(i).setCombined(windowBuilder.toString());

			if (currentWindowSize < windowSize && i + bufferSize + 1 < n) {
				windowBuilder.append(" ").append(sentences.get(i + bufferSize + 1).getContent());
				currentWindowSize++;
			}
			else {
				windowBuilder.delete(0, sentences.get(i - bufferSize).getContent().length() + 1);
				if (i + bufferSize + 1 < n) {
					windowBuilder.append(" ").append(sentences.get(i + bufferSize + 1).getContent());
				}
				else {
					currentWindowSize--;
				}
			}
		}

		return sentences;
	}

	/**
	 * Embed the sentences using the embedding model
	 * @param sentences the list of sentences
	 * @return the list of sentences with the embeddings
	 */
	public static List<Sentence> embedSentences(EmbeddingModel embeddingModel, List<Sentence> sentences) {

		List<String> sentencesText = sentences.stream().map(Sentence::getContent).toList();

		List<List<Double>> embeddings = embeddingModel.embed(sentencesText);

		return IntStream.range(0, sentences.size()).mapToObj(i -> {
			Sentence sentence = sentences.get(i);
			sentence.setEmbedding(embeddings.get(i));
			return sentence;
		}).collect(Collectors.toList());
	}

	/**
	 * Calculate the similarity between the sentences embeddings
	 * @param sentence1 the first sentence embedding
	 * @param sentence2 the second sentence embedding
	 * @return the cosine similarity between the sentences
	 */
	public static Double cosineSimilarity(List<Double> sentence1, List<Double> sentence2) {
		assert sentence1 != null : "The first sentence embedding cannot be null";
		assert sentence2 != null : "The second sentence embedding cannot be null";
		assert sentence1.size() == sentence2.size() : "The sentence embeddings must have the same size";

		INDArray arrayA = Nd4j.create(sentence1.stream().mapToDouble(Double::doubleValue).toArray());
		INDArray arrayB = Nd4j.create(sentence2.stream().mapToDouble(Double::doubleValue).toArray());

		arrayA = arrayA.div(arrayA.norm2Number());
		arrayB = arrayB.div(arrayB.norm2Number());

		return Nd4j.getBlasWrapper().dot(arrayA, arrayB);
	}

	/**
	 * Calculate the similarity between the sentences embeddings
	 * @param sentences the list of sentences
	 * @return the list of similarities (List of double)
	 */
	public static List<Double> calculateSimilarities(List<Sentence> sentences) {
		return IntStream.range(0, sentences.size() - 1).parallel().mapToObj(i -> {
			Sentence sentence1 = sentences.get(i);
			Sentence sentence2 = sentences.get(i + 1);
			return cosineSimilarity(sentence1.getEmbedding(), sentence2.getEmbedding());
		}).collect(Collectors.toList());
	}

	/**
	 * Calculate the break points indices based on the similarities and the threshold
	 * @param distances the list of cosine similarities between the sentences
	 * @return the list of break points indices
	 */
	public static List<Integer> calculateBreakPoints(List<Double> distances, Integer percentile) {
		assert distances != null : "The list of distances cannot be null";

		double breakpointDistanceThreshold = calculatePercentile(distances, percentile);

		return IntStream.range(0, distances.size())
			.filter(i -> distances.get(i) >= breakpointDistanceThreshold)
			.boxed()
			.toList();
	}

	private static Double calculatePercentile(List<Double> distances, int percentile) {
		assert distances != null : "The list of distances cannot be null";
		assert percentile > 0 && percentile < 100 : "The percentile must be between 0 and 100";

		distances = distances.stream().sorted().toList();

		int rank = (int) Math.ceil(percentile / 100.0 * distances.size());
		return distances.get(rank - 1);
	}

	/**
	 * Generate chunks combining the sentences based on the break points
	 * @param sentences the list of sentences
	 * @param breakPoints the list of break points indices
	 * @return the list of chunks
	 */
	public static List<Chunk> generateChunks(List<Sentence> sentences, List<Integer> breakPoints) {
		assert sentences != null : "The list of sentences cannot be null";
		assert !sentences.isEmpty() : "The list of sentences cannot be empty";
		assert breakPoints != null : "The list of break points cannot be null";

		AtomicInteger index = new AtomicInteger(0);

		return IntStream.range(0, breakPoints.size() + 1).mapToObj(i -> {
			int start = i == 0 ? 0 : breakPoints.get(i - 1) + 1;
			int end = i == breakPoints.size() ? sentences.size() : breakPoints.get(i) + 1;
			String content = sentences.subList(start, end)
				.stream()
				.map(Sentence::getContent)
				.collect(Collectors.joining(" "));
			return new Chunk(index.getAndIncrement(), content);
		}).collect(Collectors.toList());
	}

}
