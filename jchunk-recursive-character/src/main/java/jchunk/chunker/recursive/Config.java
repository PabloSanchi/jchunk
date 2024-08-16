package jchunk.chunker.recursive;

import org.springframework.util.Assert;

import java.util.List;

/**
 * {@link Config} is a class that holds the configuration for the
 * {@link RecursiveCharacterChunker}.
 *
 * @author Pablo Sanchidrian Herrera
 */
public class Config {

	private final Integer chunkSize;

	private final Integer chunkOverlap;

	private final List<String> separators;

	public Integer getChunkSize() {
		return chunkSize;
	}

	public Integer getChunkOverlap() {
		return chunkOverlap;
	}

	public List<String> getSeparators() {
		return separators;
	}

	public Config(Integer chunkSize, Integer chunkOverlap, List<String> separators) {
		assert chunkOverlap < chunkSize;

		this.chunkSize = chunkSize;
		this.chunkOverlap = chunkOverlap;
		this.separators = separators;
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

		private Integer chunkSize = 100;

		private Integer chunkOverlap = 20;

		private List<String> separators = List.of("\n\n", "\n", " ", "");

		public Builder chunkSize(Integer chunkSize) {
			Assert.isTrue(chunkSize > 0, "Chunk size must be greater than 0");
			this.chunkSize = chunkSize;
			return this;
		}

		public Builder chunkOverlap(Integer chunkOverlap) {
			Assert.isTrue(chunkOverlap >= 0, "Chunk overlap must be greater than or equal to 0");
			this.chunkOverlap = chunkOverlap;
			return this;
		}

		public Builder separators(List<String> separators) {
			this.separators = separators;
			return this;
		}

		public Config build() {
			Assert.isTrue(chunkSize > chunkOverlap, "Chunk size must be greater than chunk overlap");
			return new Config(chunkSize, chunkOverlap, separators);
		}

	}

}
