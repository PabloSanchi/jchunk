package jchunk.chunker.semantic;

import jchunk.chunker.core.chunk.Chunk;
import jchunk.chunker.core.chunk.IChunker;
import jchunk.chunker.core.decorators.VisibleForTesting;
import org.nd4j.linalg.factory.Nd4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A semantic chunker that chunks the content based on the semantic meaning
 *
 * @author Pablo Sanchidrian Herrera
 */
public class SemanticChunker implements IChunker {

	private final EmbeddingModel embeddingModel;

	private final Config config;

	/**
	 * Constructor
	 * @param embeddingModel the embedding model to use
	 */
	public SemanticChunker(final EmbeddingModel embeddingModel) {
		this(embeddingModel, Config.defaultConfig());
	}

	public SemanticChunker(final EmbeddingModel embeddingModel, final Config config) {
		this.embeddingModel = embeddingModel;
		this.config = config;
	}

	@Override
	public List<Chunk> split(String content) {
		var sentences = splitSentences(content, config.sentenceSplittingStrategy());
		sentences = combineSentences(sentences, config.bufferSize());
		sentences = embedSentences(embeddingModel, sentences);
		var similarities = calculateSimilarities(sentences);
		var breakPoints = calculateBreakPoints(similarities, config.percentile());
		return generateChunks(sentences, breakPoints);
	}

	/**
	 * Split the content into sentences
	 * @param content the content to split
	 * @return the list of sentences
	 */
	@VisibleForTesting
	List<Sentence> splitSentences(String content, SentenceSplittingStrategy splittingStrategy) {
		var index = new AtomicInteger(0);
		return Arrays.stream(content.split(splittingStrategy.getStrategy()))
			.map(sentence -> Sentence.builder().content(sentence).index(index.getAndIncrement()).build())
			.toList();
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
	@VisibleForTesting
	List<Sentence> combineSentences(List<Sentence> sentences, Integer bufferSize) {
		Assert.notNull(sentences, "The list of sentences cannot be null");
		Assert.notEmpty(sentences, "The list of sentences cannot be empty");
		Assert.isTrue(bufferSize != null && bufferSize > 0, "The buffer size cannot be null nor 0");
		Assert.isTrue(bufferSize < sentences.size(), "The buffer size cannot be greater equal than the input length");

		var n = sentences.size();
		var windowSize = bufferSize * 2 + 1;
		var currentWindowSize = 0;
		var windowBuilder = new StringBuilder();

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
	@VisibleForTesting
	List<Sentence> embedSentences(final EmbeddingModel embeddingModel, final List<Sentence> sentences) {
		var sentencesText = sentences.stream().map(Sentence::getContent).toList();
		var embeddings = embeddingModel.embed(sentencesText);

		return IntStream.range(0, sentences.size()).mapToObj(i -> {
			var sentence = sentences.get(i);
			sentence.setEmbedding(embeddings.get(i));
			return sentence;
		}).toList();
	}

	/**
	 * Calculate the similarity between the sentences embeddings
	 * @param sentence1 the first sentence embedding
	 * @param sentence2 the second sentence embedding
	 * @return the cosine similarity between the sentences
	 */
	@VisibleForTesting
	double cosineSimilarity(final float[] sentence1, final float[] sentence2) {
		Assert.notNull(sentence1, "The first sentence embedding cannot be null");
		Assert.notNull(sentence2, "The second sentence embedding cannot be null");
		Assert.isTrue(sentence1.length == sentence2.length, "The sentence embeddings must have the same size");

		try (var arrayA = Nd4j.create(sentence1); var arrayB = Nd4j.create(sentence2)) {
			return Nd4j.getBlasWrapper().dot(arrayA.div(arrayA.norm2Number()), arrayB.div(arrayB.norm2Number()));
		}
	}

	/**
	 * Calculate the similarity between the sentences embeddings
	 * @param sentences the list of sentences
	 * @return the list of similarities (List of double)
	 */
	@VisibleForTesting
	List<Double> calculateSimilarities(final List<Sentence> sentences) {
		return IntStream.range(0, sentences.size() - 1).parallel().mapToObj(i -> {
			Sentence sentence1 = sentences.get(i);
			Sentence sentence2 = sentences.get(i + 1);
			return cosineSimilarity(sentence1.getEmbedding(), sentence2.getEmbedding());
		}).toList();
	}

	/**
	 * Calculate the break points indices based on the similarities and the threshold
	 * @param distances the list of cosine similarities between the sentences
	 * @return the list of break points indices
	 */
	@VisibleForTesting
	List<Integer> calculateBreakPoints(final List<Double> distances, final int percentile) {
		Assert.isTrue(distances != null, "The list of distances cannot be null");

		var breakpointDistanceThreshold = calculatePercentile(distances, percentile);

		return IntStream.range(0, distances.size())
			.filter(i -> distances.get(i) >= breakpointDistanceThreshold)
			.boxed()
			.toList();
	}

	private static Double calculatePercentile(final List<Double> distances, final int percentile) {
		Assert.isTrue(distances != null, "The list of distances cannot be null");
		Assert.isTrue(percentile > 0 && percentile < 100, "The percentile must be between 0 and 100");

		var sortedDistances = distances.stream().sorted().toList();

		var rank = (int) Math.ceil(percentile / 100.0 * distances.size());
		return sortedDistances.get(rank - 1);
	}

	/**
	 * Generate chunks combining the sentences based on the break points
	 * @param sentences the list of sentences
	 * @param breakPoints the list of break points indices
	 * @return the list of chunks
	 */
	@VisibleForTesting
	List<Chunk> generateChunks(final List<Sentence> sentences, final List<Integer> breakPoints) {
		Assert.isTrue(sentences != null, "The list of sentences cannot be null");
		Assert.isTrue(!sentences.isEmpty(), "The list of sentences cannot be empty");
		Assert.isTrue(breakPoints != null, "The list of break points cannot be null");

		var index = new AtomicInteger(0);

		return IntStream.range(0, breakPoints.size() + 1).mapToObj(i -> {
			int start = i == 0 ? 0 : breakPoints.get(i - 1) + 1;
			int end = i == breakPoints.size() ? sentences.size() : breakPoints.get(i) + 1;
			String content = sentences.subList(start, end)
				.stream()
				.map(Sentence::getContent)
				.collect(Collectors.joining(" "));
			return new Chunk(index.getAndIncrement(), content);
		}).toList();
	}

}
