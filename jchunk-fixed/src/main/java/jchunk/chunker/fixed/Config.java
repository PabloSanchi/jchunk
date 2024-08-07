package jchunk.chunker.fixed;

/**
 * Configuration for the fixed chunker
 *
 * @author Pablo Sanchidrian Herrera
 */
public class Config {

	private Integer chunkSize;

	private Integer chunkOverlap;

	private String separator;

	public Integer getChunkSize() {
		return chunkSize;
	}

	public Integer getChunkOverlap() {
		return chunkOverlap;
	}

	public String getSeparator() {
		return separator;
	}

	public Config(Integer chunkSize, Integer chunkOverlap, String separator) {
		this.chunkSize = chunkSize;
		this.chunkOverlap = chunkOverlap;
		this.separator = separator;
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

		public Config build() {
			assert chunkSize > chunkOverlap : "Chunk size must be greater than chunk overlap";
			return new Config(chunkSize, chunkOverlap, separator);
		}

	}

}
