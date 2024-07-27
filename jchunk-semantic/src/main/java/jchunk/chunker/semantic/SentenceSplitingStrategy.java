package jchunk.chunker.semantic;

/**
 * Enum to represent the different strategies to split the sentences
 *
 * @author Pablo Sanchidrian Herrera
 */
public enum SentenceSplitingStrategy {

	DEFAULT("(?<=[.?!])\\s+"), LINE_BREAK("\n"), PARAGRAPH("\n\n");

	private final String strategy;

	SentenceSplitingStrategy(String strategy) {
		this.strategy = strategy;
	}

	@Override
	public String toString() {
		return this.strategy;
	}

}
