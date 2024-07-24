package jchunk.chunker.semantic;

/**
 * Enum to represent the different strategies to split the sentences
 *
 * @author Pablo Sanchidrian Herrera
 */
public enum SentenceSplitingStategy {

	DEFAULT("(?<=[.?!])\\s+"), LINE_BREAK("\n"), PARAGRAPH("\n\n");

	private String strategy;

	SentenceSplitingStategy(String strategy) {
		this.strategy = strategy;
	}

	@Override
	public String toString() {
		return this.strategy;
	}

}
