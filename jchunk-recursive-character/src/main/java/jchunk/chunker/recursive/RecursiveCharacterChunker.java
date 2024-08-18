package jchunk.chunker.recursive;

import jchunk.chunker.core.chunk.Chunk;
import jchunk.chunker.core.chunk.IChunker;

import java.util.List;

/**
 * {@link RecursiveCharacterChunker} is a class that implements the {@link IChunker}
 * interface and splits a text into chunks recursively with the given separators.
 *
 * @author Pablo Sanchidrian Herrera
 */
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
		return Utils.splitContent(content, config.getChunkSize(), config.getChunkOverlap(), config.getKeepDelimiter(),
				config.getDelimiters(), config.getTrimWhitespace());
	}

}
