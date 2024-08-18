package jchunk.chunker.recursive;

import jchunk.chunker.core.chunk.Chunk;
import jchunk.chunker.recursive.Config;
import jchunk.chunker.recursive.Utils;
import org.junit.jupiter.api.Test;

import java.util.List;

class UtilsTest {

	static String content = """
			This is the first sentence

			Not the first sentence
			not the last as well

			finally, the last sentence, wohoo!
			""";

	@Test
	void splitText() {
		Config config = Config.builder().chunkSize(15).build();

		List<Chunk> sentences = Utils.splitContent(content, config.getChunkSize(), config.getChunkOverlap(),
				config.getKeepDelimiter(), config.getDelimiters(), config.getTrimWhitespace());
	}

}
