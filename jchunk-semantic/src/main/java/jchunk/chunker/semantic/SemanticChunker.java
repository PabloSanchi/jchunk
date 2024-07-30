package jchunk.chunker.semantic;

import jchunk.chunker.core.chunk.Chunk;
import jchunk.chunker.core.chunk.IChunker;
import org.springframework.ai.embedding.EmbeddingModel;

import java.util.List;

/**
 * A semantic chunker that chunks the content based on the semantic meaning
 *
 * @author Pablo Sanchidrian Herrera
 */
public class SemanticChunker implements IChunker {

	private final EmbeddingModel embeddingModel;

	private final Config config;

	/**
	 * Constructor
	 * @param embeddingModel the embedding model to use
	 */
	public SemanticChunker(EmbeddingModel embeddingModel) {
		this(embeddingModel, Config.defaultConfig());
	}

	public SemanticChunker(EmbeddingModel embeddingModel, Config config) {
		this.embeddingModel = embeddingModel;
		this.config = config;
	}

	@Override
	public List<Chunk> split(String content) {
		List<Sentence> sentences = Utils.splitSentences(content, config.getSentenceSplitingStrategy());
		sentences = Utils.combineSentences(sentences, 1);
		sentences = Utils.embedSentences(embeddingModel, sentences);
		List<Double> similarities = Utils.calculateSimilarities(sentences);
		List<Integer> breakPoints = Utils.calculateBreakPoints(similarities, config.getPercentile());
		return Utils.generateChunks(sentences, breakPoints);
	}

}
