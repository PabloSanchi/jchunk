package jchunk.chunker.semantic;

/**
 * Sentence class to represent a sentence during the splitting process
 *
 * @author Pablo Sanchidrian Herrera
 */
public class Sentence {

	private int index;

	private String content;

	private String combined;

	private float[] embedding;

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
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

	public float[] getEmbedding() {
		return embedding;
	}

	public void setEmbedding(float[] embedding) {
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

		public Builder embedding(float[] embedding) {
			this.sentence.setEmbedding(embedding);
			return this;
		}

		public Sentence build() {
			return this.sentence;
		}

	}

}
