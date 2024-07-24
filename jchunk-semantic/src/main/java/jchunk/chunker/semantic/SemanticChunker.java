package jchunk.chunker.semantic;

import jchunk.chunker.core.chunk.Chunk;
import jchunk.chunker.core.chunk.IChunker;
import org.springframework.ai.embedding.EmbeddingModel;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

	public List<Sentence> splitSentences(String content) {
		return Arrays.stream(content.split(sentenceSplitingStategy.toString()))
			.map(sentence -> Sentence.builder().content(sentence).build())
			.collect(Collectors.toList());
	}

}
