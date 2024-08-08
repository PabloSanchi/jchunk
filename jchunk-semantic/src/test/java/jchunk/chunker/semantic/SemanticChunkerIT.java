package jchunk.chunker.semantic;

import jchunk.chunker.core.chunk.Chunk;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformers.TransformersEmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Disabled("Only for manual testing purposes.")
class SemanticChunkerIT {

	@Autowired
	private SemanticChunker semanticChunker;

	@Autowired
	private EmbeddingModel embeddingModel;

	private final String mitContent = getText("classpath:/data/mit.txt");

	static String getText(String uri) {
		var resource = new DefaultResourceLoader().getResource(uri);
		try {
			return resource.getContentAsString(StandardCharsets.UTF_8);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	void documentContentLoaded() {
		assertThat(mitContent).isNotBlank();
	}

	@Test
	void getSentences() {
		List<Sentence> sentences = Utils.splitSentences(mitContent, SentenceSplitingStrategy.DEFAULT);
		assertThat(sentences).isNotEmpty().hasSize(317);
	}

	@Test
	void combineSentences() {
		List<Sentence> sentences = Utils.splitSentences(mitContent, SentenceSplitingStrategy.DEFAULT);
		List<Sentence> combined = Utils.combineSentences(sentences, 1);

		assertThat(combined).isNotEmpty();
		assertThat(combined).hasSize(317);

		assertThat(combined.getFirst().getIndex()).isEqualTo(0);
		assertThat(combined.getFirst().getContent()).isEqualTo("\n\nWant to start a startup?");
		assertThat(combined.getFirst().getCombined())
			.isEqualTo("\n\nWant to start a startup? Get funded by\nY Combinator.");
	}

	@Test
	void embedChunks() {
		int EMBEDDING_MODEL_DIMENSION = 384;

		List<Sentence> sentences = Utils.splitSentences(mitContent, SentenceSplitingStrategy.DEFAULT);
		List<Sentence> combined = Utils.combineSentences(sentences, 1);
		List<Sentence> embedded = Utils.embedSentences(embeddingModel, combined);

		assertThat(embedded).isNotEmpty().hasSize(317);

		assertThat(embedded.getFirst().getIndex()).isEqualTo(0);
		assertThat(embedded.getFirst().getContent()).isEqualTo("\n\nWant to start a startup?");
		assertThat(embedded.getFirst().getCombined())
			.isEqualTo("\n\nWant to start a startup? Get funded by\nY Combinator.");
		assertThat(embedded.getFirst().getEmbedding()).isNotNull().hasSize(EMBEDDING_MODEL_DIMENSION);
	}

	@Test
	void getCosineDistancesArray() {
		List<Sentence> sentences = Utils.splitSentences(mitContent, SentenceSplitingStrategy.DEFAULT);
		List<Sentence> combined = Utils.combineSentences(sentences, 1);
		List<Sentence> embedded = Utils.embedSentences(embeddingModel, combined);
		List<Double> distances = Utils.calculateSimilarities(embedded);

		assertThat(distances).hasSize(sentences.size() - 1);
	}

	@Test
	void getChunks() {
		List<Chunk> chunks = this.semanticChunker.split(mitContent);
		assertThat(chunks).isNotEmpty();
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class })
	public static class TestApplication {

		@Bean
		public SemanticChunker semanticChunker(EmbeddingModel embeddingModel) {
			return new SemanticChunker(embeddingModel);
		}

		@Bean
		public EmbeddingModel embeddingModel() {
			return new TransformersEmbeddingModel();
		}

	}

}
