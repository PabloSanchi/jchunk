package jchunk.chunker.semantic;

import jchunk.chunker.core.chunk.Chunk;
import jchunk.chunker.core.chunk.IChunker;
import org.springframework.ai.embedding.EmbeddingModel;

import java.util.List;

public class SemanticChunker implements IChunker {

	private final EmbeddingModel embeddingModel;

	public SemanticChunker(EmbeddingModel embeddingModel) {
		this.embeddingModel = embeddingModel;
	}

	@Override
	public List<Chunk> split(String content) {
		return null;
	}

}
