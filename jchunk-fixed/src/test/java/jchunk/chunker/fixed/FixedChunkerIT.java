package jchunk.chunker.fixed;

import jchunk.chunker.core.chunk.Chunk;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FixedChunkerIT {

	private FixedChunker chunker;

	private static final String CONTENT = "This is the text I would like to chunk up. It is the example text for this exercise";

	@Test
	public void testSplitWithDefaultConfig() {
		chunker = new FixedChunker();
		List<Chunk> expectedChunks = List
			.of(new Chunk(0, "This is the text I would like to chunk up. It is the example text for this exercise"));

		List<Chunk> chunks = chunker.split(CONTENT);

		assertThat(chunks).isNotNull();
		assertThat(chunks.size()).isEqualTo(1);
	}

	@Test
	public void testSplitWithCustomConfig() {
		Config config = Config.builder().chunkSize(35).chunkOverlap(4).separator("").build();

		chunker = new FixedChunker(config);

		List<Chunk> expectedChunks = List.of(new Chunk(0, "This is the text I would like to ch"),
				new Chunk(1, "o chunk up. It is the example text"), new Chunk(2, "ext for this exercise"));

		List<Chunk> chunks = chunker.split(CONTENT);

		assertThat(chunks).isNotNull();
		assertThat(chunks.size()).isEqualTo(3);
		assertThat(chunks).containsExactlyElementsOf(expectedChunks);
	}

	@Test
	public void testSplitWithCustomConfigNoWhiteSpace() {
		Config config = Config.builder().chunkSize(35).chunkOverlap(0).separator("").trimWhitespace(false).build();

		chunker = new FixedChunker(config);

		List<Chunk> expectedChunks = List.of(new Chunk(0, "This is the text I would like to ch"),
				new Chunk(1, "unk up. It is the example text for "), new Chunk(2, "this exercise"));

		List<Chunk> chunks = chunker.split(CONTENT);

		assertThat(chunks).isNotNull();
		assertThat(chunks.size()).isEqualTo(3);
		assertThat(chunks).containsExactlyElementsOf(expectedChunks);
	}

	@Test
	public void testSplitWithCustomConfigWithKeepDelimiterSetToNone() {
		Config config = Config.builder()
			.chunkSize(35)
			.chunkOverlap(0)
			.separator("ch")
			.trimWhitespace(true)
			.keepDelimiter(Config.Delimiter.NONE)
			.build();

		chunker = new FixedChunker(config);

		List<Chunk> expectedChunks = List.of(new Chunk(0, "This is the text I would like to"),
				new Chunk(1, "unk up. It is the example text for this exercise"));

		List<Chunk> chunks = chunker.split(CONTENT);

		assertThat(chunks).isNotNull();
		assertThat(chunks.size()).isEqualTo(2);
		assertThat(chunks).containsExactlyElementsOf(expectedChunks);
	}

}
