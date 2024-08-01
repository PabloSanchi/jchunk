package jchunk.chunker.recursive;

import jchunk.chunker.core.chunk.Chunk;
import jchunk.chunker.core.chunk.IChunker;

import java.util.List;

public class RecursiveCharacterChunker implements IChunker {

	private final Config config;

	public RecursiveCharacterChunker() {
		this(Config.defaultConfig());
	}

	public RecursiveCharacterChunker(Config config) {
		this.config = config;
	}

	@Override
	public List<Chunk> split(String content) {
		return null;
	}

}
