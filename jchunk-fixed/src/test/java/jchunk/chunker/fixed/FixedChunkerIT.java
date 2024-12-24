package jchunk.chunker.fixed;

import jchunk.chunker.core.chunk.Chunk;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FixedChunkerIT {

	private FixedChunker chunker;

	private static final String CONTENT = "This is the text I would like to chunk up. It is the example text for this exercise";

	// @formatter:off

	@Test
	void testSplitWithDefaultConfig() {
		chunker = new FixedChunker();
		List<Chunk> expectedChunks = List.of(
				new Chunk(0, "This is the text I would like to chunk up. It is the example text for this exercise")
		);

		List<Chunk> chunks = chunker.split(CONTENT);

		assertThat(chunks).isNotNull().hasSize(1);

	}

	@Test
	void testSplitWithCustomDelimiter() {
		Config config = Config.builder().chunkSize(20).chunkOverlap(0).delimiter(".").build();

		chunker = new FixedChunker(config);

		List<Chunk> expectedChunks = List.of(
				new Chunk(0, "This is an example"),
				new Chunk(1, "Let's split on periods"),
				new Chunk(2, "Okay?")
		);

		List<Chunk> chunks = chunker.split("This is an example. Let's split on periods. Okay?");

		assertThat(chunks).isNotNull().hasSize(3).containsExactlyElementsOf(expectedChunks);
	}

	@Test
	void testSplitWithCustomConfig() {
		Config config = Config.builder().chunkSize(35).chunkOverlap(4).delimiter("").build();

		chunker = new FixedChunker(config);

		List<Chunk> expectedChunks = List.of(
				new Chunk(0, "This is the text I would like to ch"),
				new Chunk(1, "o chunk up. It is the example text"),
				new Chunk(2, "ext for this exercise")
		);

		List<Chunk> chunks = chunker.split(CONTENT);

		assertThat(chunks).isNotNull().hasSize(3).containsExactlyElementsOf(expectedChunks);
	}

	@Test
	void testSplitWithCustomConfigNoWhiteSpace() {
		Config config = Config.builder().chunkSize(35).chunkOverlap(0).delimiter("").trimWhitespace(false).build();

		chunker = new FixedChunker(config);

		List<Chunk> expectedChunks = List.of(
				new Chunk(0, "This is the text I would like to ch"),
				new Chunk(1, "unk up. It is the example text for "),
				new Chunk(2, "this exercise")
		);

		List<Chunk> chunks = chunker.split(CONTENT);

		assertThat(chunks).isNotNull().hasSize(3).containsExactlyElementsOf(expectedChunks);
	}

	@Test
	void testSplitWithCustomConfigWithKeepDelimiterSetToNone() {
		Config config = Config.builder()
			.chunkSize(35)
			.chunkOverlap(0)
			.delimiter("ch")
			.trimWhitespace(true)
			.keepDelimiter(Config.Delimiter.NONE)
			.build();

		chunker = new FixedChunker(config);

		List<Chunk> expectedChunks = List.of(
				new Chunk(0, "This is the text I would like to"),
				new Chunk(1, "unk up. It is the example text for this exercise")
		);

		List<Chunk> chunks = chunker.split(CONTENT);

		assertThat(chunks).isNotNull().hasSize(2).containsExactlyElementsOf(expectedChunks);
	}

	// @formatter:on

}
