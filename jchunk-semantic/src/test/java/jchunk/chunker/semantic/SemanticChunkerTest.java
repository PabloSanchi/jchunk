package jchunk.chunker.semantic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.util.List;
import java.util.stream.Stream;
import jchunk.chunker.core.chunk.Chunk;
import jchunk.chunker.semantic.embedder.Embedder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

class SemanticChunkerTest {

    private static final double MARGIN = 0.0001d;

    private final Embedder embeddingModel;

    private final SemanticChunker semanticChunker;

    SemanticChunkerTest() {
        this.embeddingModel = Mockito.mock(Embedder.class);
        this.semanticChunker = new SemanticChunker(embeddingModel);
    }

    // @formatter:off

    @Test
    void splitSentenceDefaultStrategyTest() {
        // given
        var expectedResult = List.of(
                Sentence.builder().content("This is a test sentence.").build(),
                Sentence.builder().content("How are u?").build(),
                Sentence.builder()
                        .content("I am fine thanks\nI am a test sentence!")
                        .build(),
                Sentence.builder().content("sure").build());

        var content = "This is a test sentence. How are u? I am fine thanks\nI am a test sentence! sure";

        // when
        List<Sentence> result = semanticChunker.splitSentences(content, SentenceSplittingStrategy.DEFAULT);

        // then
        assertThat(result).isNotNull().hasSize(expectedResult.size());

        for (int i = 0; i < result.size(); i++) {
            assertThat(result.get(i).getContent())
                    .isEqualTo(expectedResult.get(i).getContent());
        }
    }

    @Test
    void splitSentenceStrategyTest() {
        // given
        var expectedResult = List.of(
                Sentence.builder()
                        .content("This is a test sentence. How are u? I am fine thanks")
                        .build(),
                Sentence.builder().content("I am a test sentence! sure").build());

        var content = "This is a test sentence. How are u? I am fine thanks\nI am a test sentence! sure";

        // when
        List<Sentence> result = semanticChunker.splitSentences(content, SentenceSplittingStrategy.LINE_BREAK);

        // then
        assertThat(result).isNotNull().hasSize(expectedResult.size());

        assertThat(result.get(0).getContent()).isEqualTo(expectedResult.get(0).getContent());
        assertThat(result.get(1).getContent()).isEqualTo(expectedResult.get(1).getContent());
    }

    @Test
    void splitSentenceParagraphStrategyTest() {
        // given
        var expectedResult = List.of(
                Sentence.builder().index(0).content("This is a test sentence.").build(),
                Sentence.builder()
                        .index(1)
                        .content("How are u? I am fine thanks")
                        .build(),
                Sentence.builder()
                        .index(2)
                        .content("I am a test sentence!\nsure")
                        .build());

        var content = "This is a test sentence.\n\nHow are u? I am fine thanks\n\nI am a test sentence!\nsure";

        // when
        var result = semanticChunker.splitSentences(content, SentenceSplittingStrategy.PARAGRAPH);

        // then
        assertThat(result).isNotNull().hasSize(expectedResult.size());

        assertThat(result.get(0).getContent()).isEqualTo(expectedResult.get(0).getContent());
        assertThat(result.get(1).getContent()).isEqualTo(expectedResult.get(1).getContent());
        assertThat(result.get(2).getContent()).isEqualTo(expectedResult.get(2).getContent());
    }

    @Test
    void combineSentencesSuccessTest() {
        // given
        var bufferSize = 2;
        var input = List.of(
                Sentence.builder().index(0).content("This").build(),
                Sentence.builder().index(1).content("is").build(),
                Sentence.builder().index(2).content("a").build(),
                Sentence.builder().index(3).content("sentence").build(),
                Sentence.builder().index(4).content("for").build(),
                Sentence.builder().index(5).content("you").build(),
                Sentence.builder().index(6).content("mate").build());

        var expectedResult = List.of(
                Sentence.builder()
                        .index(0)
                        .content("This")
                        .combined("This is a")
                        .build(),
                Sentence.builder()
                        .index(1)
                        .content("is")
                        .combined("This is a sentence")
                        .build(),
                Sentence.builder()
                        .index(2)
                        .content("a")
                        .combined("This is a sentence for")
                        .build(),
                Sentence.builder()
                        .index(3)
                        .content("sentence")
                        .combined("is a sentence for you")
                        .build(),
                Sentence.builder()
                        .index(4)
                        .content("for")
                        .combined("a sentence for you mate")
                        .build(),
                Sentence.builder()
                        .index(5)
                        .content("you")
                        .combined("sentence for you mate")
                        .build(),
                Sentence.builder()
                        .index(6)
                        .content("mate")
                        .combined("for you mate")
                        .build());

        // when
        var result = semanticChunker.combineSentences(input, bufferSize);

        // then
        assertThat(result).isNotNull().hasSameSizeAs(expectedResult);

        for (int i = 0; i < result.size(); i++) {
            assertThat(result.get(i).getIndex()).isEqualTo(expectedResult.get(i).getIndex());
            assertThat(result.get(i).getContent())
                    .isEqualTo(expectedResult.get(i).getContent());
            assertThat(result.get(i).getCombined())
                    .isEqualTo(expectedResult.get(i).getCombined());
        }
    }

    @ParameterizedTest
    @MethodSource("provideCombineSentencesFailureScenarios")
    void combineSentencesFailureScenariosTest(List<Sentence> sentences, Integer bufferSize, String expectedMsg) {
        assertThatThrownBy(() -> semanticChunker.combineSentences(sentences, bufferSize))
                .isInstanceOf(AssertionError.class)
                .hasMessage(expectedMsg);
    }

    @Test
    void embedSentencesTest() {
        // given
        Mockito.when(embeddingModel.embed(Mockito.anyList()))
                .thenReturn(List.of(new float[] {1.0f, 2.0f, 3.0f}, new float[] {4.0f, 5.0f, 6.0f}));

        var sentences = List.of(
                Sentence.builder().combined("This is a test sentence.").build(),
                Sentence.builder().combined("How are u?").build());

        var expectedResult = List.of(
                Sentence.builder()
                        .combined("This is a test sentence.")
                        .embedding(new float[] {1.0f, 2.0f, 3.0f})
                        .build(),
                Sentence.builder()
                        .combined("How are u?")
                        .embedding(new float[] {4.0f, 5.0f, 6.0f})
                        .build());

        // when
        var result = semanticChunker.embedSentences(embeddingModel, sentences);

        // then
        assertThat(result).isNotNull();

        for (int i = 0; i < result.size(); i++) {
            assertThat(result.get(i).getCombined())
                    .isEqualTo(expectedResult.get(i).getCombined());
            assertThat(result.get(i).getEmbedding())
                    .isEqualTo(expectedResult.get(i).getEmbedding());
        }
    }

    @ParameterizedTest
    @MethodSource("provideVectorScenarios")
    void testVectorScenarios(float[] embedding1, float[] embedding2, double expectedResult, boolean isExact) {
        // when
        double result = semanticChunker.cosineSimilarity(embedding1, embedding2);

        // then
        double margin = isExact ? 0 : MARGIN;
        assertThat(result).isCloseTo(expectedResult, within(margin));
    }

    @Test
    void testZeroVectors() {
        // given
        var embedding1 = new float[] {0.0f, 0.0f, 0.0f};
        var embedding2 = new float[] {0.0f, 0.0f, 0.0f};

        // when
        var result = semanticChunker.cosineSimilarity(embedding1, embedding2);

        // given
        assertThat(result).isNaN();
    }

    @Test
    void testGetIndicesAboveThreshold() {
        // given
        var percentile = 95;
        var distances = List.of(10.0, 15.0, 20.0, 25.0, 30.0, 35.0, 40.0, 45.0, 50.0, 55.0, 60.0, 65.0, 70.0, 75.0);
        var expectedIndices = List.of(13);

        // when
        var actualIndices = semanticChunker.calculateBreakPoints(distances, percentile);

        // then
        assertThat(actualIndices).isEqualTo(expectedIndices);
    }

    @Test
    void testGenerateChunks() {
        // given
        var sentences = List.of(
                Sentence.builder().index(0).content("This").build(),
                Sentence.builder().index(1).content("is").build(),
                Sentence.builder().index(2).content("a").build(),
                Sentence.builder().index(3).content("test.").build(),
                Sentence.builder().index(4).content("We").build(),
                Sentence.builder().index(5).content("are").build(),
                Sentence.builder().index(6).content("writing").build(),
                Sentence.builder().index(7).content("unit").build(),
                Sentence.builder().index(8).content("tests.").build());

        var breakPoints = List.of(2, 4, 6);

        var expectedChunks = List.of(
                new Chunk(0, "This is a"),
                new Chunk(1, "test. We"),
                new Chunk(2, "are writing"),
                new Chunk(3, "unit tests."));

        // when
        var actualChunks = semanticChunker.generateChunks(sentences, breakPoints);

        // then
        assertThat(actualChunks).isNotNull().hasSize(expectedChunks.size());

        for (int i = 0; i < actualChunks.size(); i++) {
            assertThat(actualChunks.get(i).id()).isEqualTo(expectedChunks.get(i).id());
            assertThat(actualChunks.get(i).content())
                    .isEqualTo(expectedChunks.get(i).content());
        }
    }

    private static Stream<Arguments> provideCombineSentencesFailureScenarios() {
        final var nonEmptySentences = List.of(Sentence.builder().content("This").build());
        return Stream.of(
                Arguments.of(nonEmptySentences, 0, "The buffer size cannot be null nor 0"),
                Arguments.of(nonEmptySentences, null, "The buffer size cannot be null nor 0"),
                Arguments.of(nonEmptySentences, 1, "The buffer size cannot be greater or equal than the input length"),
                Arguments.of(null, 2, "The list of sentences cannot be null"),
                Arguments.of(List.of(), 2, "The list of sentences cannot be empty"));
    }

    private static Stream<Arguments> provideVectorScenarios() {
        return Stream.of(
                Arguments.of(new float[] {1.0f, 2.0f, 3.0f}, new float[] {1.0f, 2.0f, 3.0f}, 1.0, false),
                Arguments.of(new float[] {1.0f, 0.0f, 0.0f}, new float[] {0.0f, 1.0f, 0.0f}, 0.0, true),
                Arguments.of(new float[] {1.0f, 2.0f, 3.0f}, new float[] {-1.0f, -2.0f, -3.0f}, -1.0, false),
                Arguments.of(new float[] {1.0f, 2.0f, 3.0f}, new float[] {2.0f, 4.0f, 6.0f}, 1.0, false));
    }

    // @formatter:on

}
