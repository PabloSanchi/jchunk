package jchunk.chunker.fixed;

import jchunk.chunker.core.chunk.Chunk;
import jchunk.chunker.core.chunk.IChunker;

import java.util.List;

/**
 * {@link FixedChunker} is a chunker that splits the content into fixed size chunks.
 *
 * @author Pablo Sanchidrian Herrera
 */
public class FixedChunker implements IChunker {

	private final Config config;

	public FixedChunker() {
		this(Config.defaultConfig());
	}

	public FixedChunker(Config config) {
		this.config = config;
	}

	@Override
	public List<Chunk> split(String content) {
		return null;
	}

}
