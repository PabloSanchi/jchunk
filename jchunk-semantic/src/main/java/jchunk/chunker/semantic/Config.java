package jchunk.chunker.semantic;

/**
 * Configuration for the semantic chunker
 *
 * @author Pablo Sanchidrian Herrera
 */
public class Config {

	private final SentenceSplitingStrategy sentenceSplitingStrategy;

	private final Integer percentile;

	private final Integer bufferSize;

	public SentenceSplitingStrategy getSentenceSplitingStrategy() {
		return sentenceSplitingStrategy;
	}

	public Integer getPercentile() {
		return percentile;
	}

	public Config(SentenceSplitingStrategy sentenceSplitingStrategy, Integer percentile, Integer bufferSize) {
		this.sentenceSplitingStrategy = sentenceSplitingStrategy;
		this.percentile = percentile;
		this.bufferSize = bufferSize;
	}

	/**
	 * {@return the default config}
	 */
	public static Config defaultConfig() {
		return builder().build();
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private SentenceSplitingStrategy sentenceSplitingStrategy = SentenceSplitingStrategy.DEFAULT;

		private Integer percentile = 95;

		private Integer bufferSize = 1;

		public Builder sentenceSplittingStrategy(SentenceSplitingStrategy sentenceSplitingStrategy) {
			this.sentenceSplitingStrategy = sentenceSplitingStrategy;
			return this;
		}

		public Builder percentile(Integer percentile) {
			this.percentile = percentile;
			return this;
		}

		public Builder bufferSize(Integer bufferSize) {
			this.bufferSize = bufferSize;
			return this;
		}

		public Config build() {
			return new Config(sentenceSplitingStrategy, percentile, bufferSize);
		}

	}

}
