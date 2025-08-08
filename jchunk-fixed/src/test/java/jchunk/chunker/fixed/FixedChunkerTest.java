package jchunk.chunker.fixed;

import jchunk.chunker.Delimiter;
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
		// given
		chunker = new FixedChunker();
		List<Chunk> expectedChunks = List.of(
				new Chunk(0, "This is the text I would like to chunk up. It is the example text for this exercise")
		);

		// when
		List<Chunk> chunks = chunker.split(CONTENT);

		// then
		assertThat(chunks)
				.isNotNull()
				.hasSize(1)
				.containsExactlyElementsOf(expectedChunks);
	}

	@Test
	void testSplitWithCustomDelimiter() {
		// given
		Config config = Config.builder().chunkSize(20).chunkOverlap(0).delimiter(".").build();
		chunker = new FixedChunker(config);

		List<Chunk> expectedChunks = List.of(
				new Chunk(0, "This is an example"),
				new Chunk(1, "Let's split on periods"),
				new Chunk(2, "Okay?")
		);

		// when
		List<Chunk> chunks = chunker.split("This is an example. Let's split on periods. Okay?");

		// then
		assertThat(chunks).isNotNull().hasSize(3).containsExactlyElementsOf(expectedChunks);
	}

	@Test
	void testSplitWithCustomConfig() {
		// given
		Config config = Config.builder().chunkSize(35).chunkOverlap(4).delimiter("").build();
		chunker = new FixedChunker(config);

		List<Chunk> expectedChunks = List.of(
				new Chunk(0, "This is the text I would like to ch"),
				new Chunk(1, "o chunk up. It is the example text"),
				new Chunk(2, "ext for this exercise")
		);

		// when
		List<Chunk> chunks = chunker.split(CONTENT);

		// then
		assertThat(chunks)
				.isNotNull()
				.hasSize(3)
				.containsExactlyElementsOf(expectedChunks);
	}

	@Test
	void testSplitWithCustomConfigNoWhiteSpace() {
		// given
		Config config = Config.builder().chunkSize(35).chunkOverlap(0).delimiter("").trimWhitespace(false).build();
		chunker = new FixedChunker(config);

		List<Chunk> expectedChunks = List.of(
				new Chunk(0, "This is the text I would like to ch"),
				new Chunk(1, "unk up. It is the example text for "),
				new Chunk(2, "this exercise")
		);

		// when
		List<Chunk> chunks = chunker.split(CONTENT);

		// then
		assertThat(chunks).isNotNull().hasSize(3).containsExactlyElementsOf(expectedChunks);
	}

	@Test
	void testSplitWithCustomConfigWithKeepDelimiterSetToNone() {
		// given
		Config config = Config.builder()
			.chunkSize(35)
			.chunkOverlap(0)
			.delimiter("ch")
			.trimWhitespace(true)
			.keepDelimiter(Delimiter.NONE)
			.build();
		chunker = new FixedChunker(config);

		List<Chunk> expectedChunks = List.of(
				new Chunk(0, "This is the text I would like to"),
				new Chunk(1, "unk up. It is the example text for this exercise")
		);

		// when
		List<Chunk> chunks = chunker.split(CONTENT);

		// then
		assertThat(chunks).isNotNull().hasSize(2).containsExactlyElementsOf(expectedChunks);
	}

	@Test
	void testSplitIntoSentencesWithBlankSeparator() {
		// given
		chunker = new FixedChunker();
		Config config = Config.builder().delimiter("").build();

		// when
		List<String> sentences = chunker.splitIntoSentences(CONTENT, config);

		// then
		assertThat(sentences).isNotNull().hasSize(CONTENT.length());

		for (int i = 0; i < CONTENT.length(); i++) {
			assertThat(sentences.get(i)).isEqualTo(String.valueOf(CONTENT.charAt(i)));
		}
	}

	@Test
	void testSplitIntoSentencesWithNoDelimiter() {
		// given
		chunker = new FixedChunker();
		Config config = Config.builder().delimiter("ch").build();

		// when
		List<String> sentences = chunker.splitIntoSentences(CONTENT, config);

		// then
		assertThat(sentences).isNotNull().hasSize(2);
		assertThat(sentences.getFirst()).isEqualTo("This is the text I would like to ");
		assertThat(sentences.getLast()).isEqualTo("unk up. It is the example text for this exercise");
	}

	@Test
	void testSplitIntoSentencesWithDelimiterStart() {
		// given
		chunker = new FixedChunker();
		Config config = Config.builder().delimiter("ch").keepDelimiter(Delimiter.START).build();

		// when
		List<String> sentences = chunker.splitIntoSentences(CONTENT, config);

		// then
		assertThat(sentences).isNotNull().hasSize(2);
		assertThat(sentences.getFirst()).isEqualTo("This is the text I would like to ");
		assertThat(sentences.getLast()).isEqualTo("chunk up. It is the example text for this exercise");
	}

	@Test
	void testSplitIntoSentencesWithDelimiterEnd() {
		// given
		chunker = new FixedChunker();
		Config config = Config.builder().delimiter("ch").keepDelimiter(Delimiter.END).build();

		// when
		List<String> sentences = chunker.splitIntoSentences(CONTENT, config);

		// then
		assertThat(sentences).isNotNull().hasSize(2);
		assertThat(sentences.getFirst()).isEqualTo("This is the text I would like to ch");
		assertThat(sentences.getLast()).isEqualTo("unk up. It is the example text for this exercise");
	}

	// @formatter:on

}
