package jchunk.chunker.recursive;

import jchunk.chunker.core.chunk.Chunk;
import jchunk.chunker.core.chunk.IChunker;

import java.util.List;

public class RecursiveCharcterChunker implements IChunker {

	private final Config config;

	public RecursiveCharcterChunker() {
		this(Config.defaultConfig());
	}

	public RecursiveCharcterChunker(Config config) {
		this.config = config;
	}

	@Override
	public List<Chunk> split(String content) {
		return null;
	}

}
