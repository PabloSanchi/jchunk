package jchunk.chunker.semantic;

import java.util.List;

/**
 * Sentence class to represent a sentence during the splitting process
 *
 * @author Pablo Sanchidrian Herrera
 */
public class Sentence {

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