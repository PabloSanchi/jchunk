package jchunk.chunker.semantic;

/**
 * Configuration for the semantic chunker
 *
 * @author Pablo Sanchidrian Herrera
 */
public class Config {

	private final SentenceSplitingStrategy sentenceSplitingStrategy;

	private final Integer percentile;

	public SentenceSplitingStrategy getSentenceSplitingStrategy() {
		return sentenceSplitingStrategy;
	}

	public Integer getPercentile() {
		return percentile;
	}

	public Config(SentenceSplitingStrategy sentenceSplitingStrategy, Integer percentile) {
		this.sentenceSplitingStrategy = sentenceSplitingStrategy;
		this.percentile = percentile;
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

		public Builder sentenceSplittingStrategy(SentenceSplitingStrategy sentenceSplitingStrategy) {
			this.sentenceSplitingStrategy = sentenceSplitingStrategy;
			return this;
		}

		public Builder percentile(Integer percentile) {
			this.percentile = percentile;
			return this;
		}

		public Config build() {
			return new Config(sentenceSplitingStrategy, percentile);
		}

	}

}
