package jchunk.chunker.semantic;

/**
 * Configuration for the semantic chunker
 *
 * @author Pablo Sanchidrian Herrera
 */
public record Config(SentenceSplittingStrategy sentenceSplittingStrategy, int percentile, int bufferSize) {

	/**
	 * @return the default config
	 */
	public static Config defaultConfig() {
		return builder().build();
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private SentenceSplittingStrategy sentenceSplittingStrategy = SentenceSplittingStrategy.DEFAULT;

		private int percentile = 95;

		private int bufferSize = 1;

		public Builder sentenceSplittingStrategy(SentenceSplittingStrategy sentenceSplittingStrategy) {
			this.sentenceSplittingStrategy = sentenceSplittingStrategy;
			return this;
		}

		public Builder percentile(int percentile) {
			this.percentile = percentile;
			return this;
		}

		public Builder bufferSize(int bufferSize) {
			this.bufferSize = bufferSize;
			return this;
		}

		public Config build() {
			return new Config(sentenceSplittingStrategy, percentile, bufferSize);
		}

	}

}
