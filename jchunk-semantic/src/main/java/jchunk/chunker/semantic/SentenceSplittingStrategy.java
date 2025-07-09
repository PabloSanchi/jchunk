package jchunk.chunker.semantic;

/**
 * Enum to represent the different strategies to split the sentences
 *
 * @author Pablo Sanchidrian Herrera
 */
public enum SentenceSplittingStrategy {

	DEFAULT("(?<=[.?!])\\s+"), LINE_BREAK("\n"), PARAGRAPH("\n\n");

	private final String strategy;

	SentenceSplittingStrategy(String strategy) {
		this.strategy = strategy;
	}

	@Override
	public String toString() {
		return this.strategy;
	}

	public String getStrategy() {
		return this.strategy;
	}

}
