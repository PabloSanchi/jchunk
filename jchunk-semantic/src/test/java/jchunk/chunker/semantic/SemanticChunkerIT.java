package jchunk.chunker.semantic;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import jchunk.chunker.semantic.embedder.Embedder;
import jchunk.chunker.semantic.embedder.JChunkEmbedder;
import org.junit.jupiter.api.Test;

class SemanticChunkerIT {

    private static final String MIT_CONTENT = getText("data/mit.txt");

    private static final Embedder embedder;

    private static final SemanticChunker semanticChunker;

    static {
        try {
            embedder = new JChunkEmbedder();
            semanticChunker = new SemanticChunker(embedder);
        } catch (Exception e) {
            throw new ExceptionInInitializerError();
        }
    }

    @Test
    void documentContentLoaded() {
        assertThat(MIT_CONTENT).isNotBlank();
    }

    @Test
    void getChunks() {
        // when
        var chunks = semanticChunker.split(MIT_CONTENT);

        // then
        assertThat(chunks).isNotEmpty();
    }

    @Test
    void getSentences() {
        // when
        var sentences = semanticChunker.splitSentences(MIT_CONTENT, SentenceSplittingStrategy.DEFAULT);

        // then
        assertThat(sentences).isNotEmpty().hasSize(317);
    }

    @Test
    void combineSentences() {
        // when
        var sentences = semanticChunker.splitSentences(MIT_CONTENT, SentenceSplittingStrategy.DEFAULT);
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
        var sentences = semanticChunker.splitSentences(MIT_CONTENT, SentenceSplittingStrategy.DEFAULT);
        var combined = semanticChunker.combineSentences(sentences, 1);
        var embedded = semanticChunker.embedSentences(embedder, combined);

        // then
        assertThat(embedded).isNotEmpty().hasSize(317);

        assertThat(embedded.getFirst().getIndex()).isZero();
        assertThat(embedded.getFirst().getContent()).isEqualTo("\n\nWant to start a startup?");
        assertThat(embedded.getFirst().getCombined())
                .isEqualTo("\n\nWant to start a startup? Get funded by\nY Combinator.");
        assertThat(embedded.getFirst().getEmbedding()).isNotNull().hasSize(embedder.getDimension());
    }

    @Test
    void getCosineDistancesArray() {
        // when
        var sentences = semanticChunker.splitSentences(MIT_CONTENT, SentenceSplittingStrategy.DEFAULT);
        var combined = semanticChunker.combineSentences(sentences, 1);
        var embedded = semanticChunker.embedSentences(embedder, combined);
        var distances = semanticChunker.calculateSimilarities(embedded);

        // then
        assertThat(distances).hasSize(sentences.size() - 1);
    }

    // HELPERS

    private static String getText(final String resourcePath) {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {

            if (in == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }

            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resource: " + resourcePath, e);
        }
    }
}
