package jchunk.chunker.semantic;

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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SemanticChunkerIT {

	private final String mitContent = getText("classpath:/data/mit.txt");

	@Autowired
	private SemanticChunker semanticChunker;

	@Autowired
	private EmbeddingModel embeddingModel;

	@Test
	void documentContentLoaded() {
		assertThat(mitContent).isNotBlank();
	}

	@Test
	void getChunks() {
		// when
		var chunks = semanticChunker.split(mitContent);

		// then
		assertThat(chunks).isNotEmpty();
	}

	@Test
	void getSentences() {
		// when
		var sentences = semanticChunker.splitSentences(mitContent, SentenceSplittingStrategy.DEFAULT);

		// then
		assertThat(sentences).isNotEmpty().hasSize(317);
	}

	@Test
	void combineSentences() {
		// when
		var sentences = semanticChunker.splitSentences(mitContent, SentenceSplittingStrategy.DEFAULT);
		var combined = semanticChunker.combineSentences(sentences, 1);

		// then
		assertThat(combined).isNotEmpty().hasSize(317);

		assertThat(combined.getFirst().getIndex()).isZero();
		assertThat(combined.getFirst().getContent()).isEqualTo("\n\nWant to start a startup?");
		assertThat(combined.getFirst().getCombined())
			.isEqualTo("\n\nWant to start a startup? Get funded by\nY Combinator.");
	}

	@Test
	void embedChunks() {
		// when
		var sentences = semanticChunker.splitSentences(mitContent, SentenceSplittingStrategy.DEFAULT);
		var combined = semanticChunker.combineSentences(sentences, 1);
		var embedded = semanticChunker.embedSentences(embeddingModel, combined);

		// then
		assertThat(embedded).isNotEmpty().hasSize(317);

		assertThat(embedded.getFirst().getIndex()).isZero();
		assertThat(embedded.getFirst().getContent()).isEqualTo("\n\nWant to start a startup?");
		assertThat(embedded.getFirst().getCombined())
			.isEqualTo("\n\nWant to start a startup? Get funded by\nY Combinator.");
		assertThat(embedded.getFirst().getEmbedding()).isNotNull().hasSize(embeddingModel.dimensions());
	}

	@Test
	void getCosineDistancesArray() {
		// when
		var sentences = semanticChunker.splitSentences(mitContent, SentenceSplittingStrategy.DEFAULT);
		var combined = semanticChunker.combineSentences(sentences, 1);
		var embedded = semanticChunker.embedSentences(embeddingModel, combined);
		var distances = semanticChunker.calculateSimilarities(embedded);

		// then
		assertThat(distances).hasSize(sentences.size() - 1);
	}

	// HELPERS

	private static String getText(String uri) {
		var resource = new DefaultResourceLoader().getResource(uri);
		try {
			return resource.getContentAsString(StandardCharsets.UTF_8);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// SPRING BOOT APP SETUP

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
