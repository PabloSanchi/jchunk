package jchunk.chunker.semantic;

import jchunk.chunker.core.chunk.Chunk;
import jchunk.chunker.core.chunk.IChunker;
import org.springframework.ai.embedding.EmbeddingModel;

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

	private SentenceSplitingStategy sentenceSplitingStategy = SentenceSplitingStategy.DEFAULT;

	/**
	 * Constructor
	 * @param embeddingModel the embedding model to use
	 */
	public SemanticChunker(EmbeddingModel embeddingModel) {
		this.embeddingModel = embeddingModel;
	}

	/**
	 * Constructor
	 * @param embeddingModel the embedding model to use
	 * @param sentenceSplitingStategy the strategy to split the sentences
	 */
	public SemanticChunker(EmbeddingModel embeddingModel, SentenceSplitingStategy sentenceSplitingStategy) {
		this.embeddingModel = embeddingModel;
		this.sentenceSplitingStategy = sentenceSplitingStategy;
	}

	@Override
	public List<Chunk> split(String content) {
		return null;
	}

	/**
	 * Class to represent a sentence during the splitting process
	 */
	public static class Sentence {

		private Integer index;

		private String content;

		private String combined;

		private List<Double> embedding;

		public Integer getIndex() {
			return index;
		}

		public void setIndex(Integer index) {
			this.index = index;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public String getCombined() {
			return combined;
		}

		public void setCombined(String combined) {
			this.combined = combined;
		}

		public List<Double> getEmbedding() {
			return embedding;
		}

		public void setEmbedding(List<Double> embedding) {
			this.embedding = embedding;
		}

		public static Builder builder() {
			return new Builder();
		}

		public static class Builder {

			private final Sentence sentence = new Sentence();

			public Builder index(Integer index) {
				this.sentence.setIndex(index);
				return this;
			}

			public Builder content(String content) {
				this.sentence.setContent(content);
				return this;
			}

			public Builder combined(String combined) {
				this.sentence.setCombined(combined);
				return this;
			}

			public Builder embedding(List<Double> embedding) {
				this.sentence.setEmbedding(embedding);
				return this;
			}

			public Sentence build() {
				return this.sentence;
			}

		}

	}

	/**
	 * Split the content into sentences
	 * @param content the content to split
	 * @return the list of sentences
	 */
	public List<Sentence> splitSentences(String content) {
		AtomicInteger index = new AtomicInteger(0);
		return Arrays.stream(content.split(sentenceSplitingStategy.toString()))
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
	public List<Sentence> combineSentences(List<Sentence> sentences, Integer bufferSize) {
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
	public List<Sentence> embedSentences(List<Sentence> sentences) {

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
	public Double cosineSimilarity(List<Double> sentence1, List<Double> sentence2) {
		assert sentence1 != null : "The first sentence embedding cannot be null";
		assert sentence2 != null : "The second sentence embedding cannot be null";
		assert sentence1.size() == sentence2.size() : "The sentence embeddings must have the same size";

		double dotProduct = 0.0;
		double norm1 = 0.0;
		double norm2 = 0.0;

		for (int i = 0; i < sentence1.size(); i++) {
			dotProduct += sentence1.get(i) * sentence2.get(i);
			norm1 += Math.pow(sentence1.get(i), 2);
			norm2 += Math.pow(sentence2.get(i), 2);
		}

		norm1 = Math.sqrt(norm1);
		norm2 = Math.sqrt(norm2);

		return dotProduct / (norm1 * norm2);
	}

}
