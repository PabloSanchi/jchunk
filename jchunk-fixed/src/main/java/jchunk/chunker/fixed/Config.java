package jchunk.chunker.fixed;

/**
 * Configuration for the fixed chunker
 *
 * @author Pablo Sanchidrian Herrera
 */
public class Config {

	private final Integer chunkSize;

	private final Integer chunkOverlap;

	private final String delimiter;

	private final Boolean trimWhitespace;

	private final Delimiter keepDelimiter;

	public Integer getChunkSize() {
		return chunkSize;
	}

	public Integer getChunkOverlap() {
		return chunkOverlap;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public Boolean getTrimWhitespace() {
		return trimWhitespace;
	}

	public Delimiter getKeepDelimiter() {
		return keepDelimiter;
	}

	public Config(Integer chunkSize, Integer chunkOverlap, String delimiter, Boolean trimWhitespace,
			Delimiter keepDelimiter) {
		this.chunkSize = chunkSize;
		this.chunkOverlap = chunkOverlap;
		this.delimiter = delimiter;
		this.trimWhitespace = trimWhitespace;
		this.keepDelimiter = keepDelimiter;
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

		private Integer chunkSize = 1000;

		private Integer chunkOverlap = 100;

		private String separator = " ";

		private Boolean trimWhitespace = true;

		private Delimiter keepDelimiter = Delimiter.NONE;

		public Builder chunkSize(Integer chunkSize) {
			this.chunkSize = chunkSize;
			return this;
		}

		public Builder chunkOverlap(Integer chunkOverlap) {
			this.chunkOverlap = chunkOverlap;
			return this;
		}

		public Builder separator(String separator) {
			this.separator = separator;
			return this;
		}

		public Builder trimWhitespace(Boolean trimWhitespace) {
			this.trimWhitespace = trimWhitespace;
			return this;
		}

		public Builder keepDelimiter(Delimiter keepDelimiter) {
			this.keepDelimiter = keepDelimiter;
			return this;
		}

		public Config build() {
			assert chunkSize > chunkOverlap : "Chunk size must be greater than chunk overlap";
			return new Config(chunkSize, chunkOverlap, separator, trimWhitespace, keepDelimiter);
		}

	}

	/**
	 * Enum to represent the delimiter configuration NONE: No delimiter START: Delimiter
	 * at the start of the chunk END: Delimiter at the end of the chunk
	 */
	public enum Delimiter {

		NONE, START, END

	}

}
