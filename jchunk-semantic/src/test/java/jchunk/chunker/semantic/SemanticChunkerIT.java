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
public class SemanticChunkerIT {

	@Autowired
	private SemanticChunker semanticChunker;

	private final Integer EMBEDDING_MODEL_DIMENSION = 384;

	private String mitContent = getText("classpath:/data/mit.txt");

	public static String getText(String uri) {
		var resource = new DefaultResourceLoader().getResource(uri);
		try {
			return resource.getContentAsString(StandardCharsets.UTF_8);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void documentContentLoaded() {
		assertThat(mitContent).isNotBlank();
	}

	@Test
	public void getSentences() {
		List<SemanticChunker.Sentence> sentences = this.semanticChunker.splitSentences(mitContent);
		assertThat(sentences).isNotEmpty();
		assertThat(sentences).hasSize(317);
	}

	@Test
	public void combineSentences() {
		List<SemanticChunker.Sentence> sentences = this.semanticChunker.splitSentences(mitContent);
		List<SemanticChunker.Sentence> combined = this.semanticChunker.combineSentences(sentences, 1);

		assertThat(combined).isNotEmpty();
		assertThat(combined).hasSize(317);

		assertThat(combined.getFirst().getIndex()).isEqualTo(0);
		assertThat(combined.getFirst().getContent()).isEqualTo("\n\nWant to start a startup?");
		assertThat(combined.getFirst().getCombined())
			.isEqualTo("\n\nWant to start a startup? Get funded by\nY Combinator.");
	}

	@Test
	public void embedChunks() {
		List<SemanticChunker.Sentence> sentences = this.semanticChunker.splitSentences(mitContent);
		List<SemanticChunker.Sentence> combined = this.semanticChunker.combineSentences(sentences, 1);
		List<SemanticChunker.Sentence> embedded = this.semanticChunker.embedSentences(combined);

		assertThat(embedded).isNotEmpty();
		assertThat(embedded).hasSize(317);

		assertThat(embedded.getFirst().getIndex()).isEqualTo(0);
		assertThat(embedded.getFirst().getContent()).isEqualTo("\n\nWant to start a startup?");
		assertThat(embedded.getFirst().getCombined())
			.isEqualTo("\n\nWant to start a startup? Get funded by\nY Combinator.");
		assertThat(embedded.getFirst().getEmbedding()).isNotNull();
		assertThat(embedded.getFirst().getEmbedding()).hasSize(EMBEDDING_MODEL_DIMENSION);
	}

	@Test
	public void getCosineDistancesArray() {
		List<SemanticChunker.Sentence> sentences = this.semanticChunker.splitSentences(mitContent);
		List<SemanticChunker.Sentence> combined = this.semanticChunker.combineSentences(sentences, 1);
		List<SemanticChunker.Sentence> embedded = this.semanticChunker.embedSentences(combined);
		List<Double> distances = this.semanticChunker.calculateSimilarities(embedded);

		assertThat(distances).hasSize(sentences.size() - 1);
	}

	@Test
	public void getChunks() {
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
