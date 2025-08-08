package jchunk.chunker.semantic.embedder;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class JChunkEmbedderTest {

    private static final int DEFAULT_EMBEDDER_DIM = 384;

    @Test
    void embedding_model_loads_successfully() throws Exception {
        var embedder = new JChunkEmbedder();
        var embedding = embedder.embed("this is some text to test");

        assertThat(embedding).hasSize(DEFAULT_EMBEDDER_DIM);
    }
}
