package jchunk.chunker.recursive;

import org.springframework.util.Assert;

import java.util.ArrayList;
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

	private final List<String> delimiters;

	private final Delimiter keepDelimiter;

	private final Boolean trimWhitespace;

	public Integer getChunkSize() {
		return chunkSize;
	}

	public Integer getChunkOverlap() {
		return chunkOverlap;
	}

	public List<String> getDelimiters() {
		return delimiters;
	}

	public Delimiter getKeepDelimiter() {
		return keepDelimiter;
	}

	public Boolean getTrimWhitespace() {
		return trimWhitespace;
	}

	private Config(Integer chunkSize, Integer chunkOverlap, List<String> delimiters, Delimiter keepDelimiter,
			Boolean trimWhitespace) {
		this.chunkSize = chunkSize;
		this.chunkOverlap = chunkOverlap;
		this.delimiters = delimiters;
		this.keepDelimiter = keepDelimiter;
		this.trimWhitespace = trimWhitespace;
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

		private List<String> delimiters = new ArrayList<>(List.of("\n\n", "\n", " ", ""));

		private Delimiter keepDelimiter = Delimiter.START;

		private Boolean trimWhitespace = true;

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

		public Builder separators(List<String> delimiters) {
			this.delimiters = delimiters;
			return this;
		}

		public Builder keepDelimiter(Delimiter keepDelimiter) {
			this.keepDelimiter = keepDelimiter;
			return this;
		}

		public Builder trimWhitespace(Boolean trimWhitespace) {
			this.trimWhitespace = trimWhitespace;
			return this;
		}

		public Config build() {
			Assert.isTrue(chunkSize > chunkOverlap, "Chunk size must be greater than chunk overlap");
			return new Config(chunkSize, chunkOverlap, delimiters, keepDelimiter, trimWhitespace);
		}

	}

	/**
	 * Enum to represent the delimiter configuration
	 * <p>
	 * <ul>
	 * <li>NONE: No delimiter</li>
	 * <li>START: Delimiter at the start of the chunk</li>
	 * <li>END: Delimiter at the end of the chunk</li>
	 * </ul>
	 */
	public enum Delimiter {

		NONE, START, END

	}

}
