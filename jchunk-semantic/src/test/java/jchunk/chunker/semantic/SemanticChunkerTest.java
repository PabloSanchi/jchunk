package jchunk.chunker.semantic;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.EmbeddingModel;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class SemanticChunkerTest {

    @Mock
    private EmbeddingModel embeddingModel;

    private SemanticChunker semanticChunker;

    public void configure() {
        this.semanticChunker = new SemanticChunker(embeddingModel);
    }

    public void configureLineBreakStrategy() {
        this.semanticChunker = new SemanticChunker(embeddingModel, SentenceSplitingStategy.LINE_BREAK);
    }

    public void configureParagraphBreakStrategy() {
        this.semanticChunker = new SemanticChunker(embeddingModel, SentenceSplitingStategy.PARAGRAPH);
    }

    @Test
    public void splitSentenceDefaultStrategyTest() {
        configure();

        List<SemanticChunker.Sentence> expectedResult = List.of(
                SemanticChunker.Sentence.builder().content("This is a test sentence.").build(),
                SemanticChunker.Sentence.builder().content("How are u?").build(),
                SemanticChunker.Sentence.builder().content("I am fine thanks\nI am a test sentence!").build(),
                SemanticChunker.Sentence.builder().content("sure").build()
        );

        String content = "This is a test sentence. How are u? I am fine thanks\nI am a test sentence! sure";
        List<SemanticChunker.Sentence> result = this.semanticChunker.splitSentences(content);

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(expectedResult.size());

        assertThat(result.get(0).getContent()).isEqualTo(expectedResult.get(0).getContent());
        assertThat(result.get(1).getContent()).isEqualTo(expectedResult.get(1).getContent());
        assertThat(result.get(2).getContent()).isEqualTo(expectedResult.get(2).getContent());
        assertThat(result.get(3).getContent()).isEqualTo(expectedResult.get(3).getContent());
    }

    @Test
    public void splitSentenceStrategyTest() {

        configureLineBreakStrategy();

        List<SemanticChunker.Sentence> expectedResult = List.of(
                SemanticChunker.Sentence.builder().content("This is a test sentence. How are u? I am fine thanks").build(),
                SemanticChunker.Sentence.builder().content("I am a test sentence! sure").build()
        );

        String content = "This is a test sentence. How are u? I am fine thanks\nI am a test sentence! sure";
        List<SemanticChunker.Sentence> result = this.semanticChunker.splitSentences(content);

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(expectedResult.size());

        assertThat(result.get(0).getContent()).isEqualTo(expectedResult.get(0).getContent());
        assertThat(result.get(1).getContent()).isEqualTo(expectedResult.get(1).getContent());
    }

    @Test
    public void splitSentenceParagraphStrategyTest() {

        configureParagraphBreakStrategy();

        List<SemanticChunker.Sentence> expectedResult = List.of(
                SemanticChunker.Sentence.builder().content("This is a test sentence.").build(),
                SemanticChunker.Sentence.builder().content("How are u? I am fine thanks").build(),
                SemanticChunker.Sentence.builder().content("I am a test sentence!\nsure").build()
        );

        String content = "This is a test sentence.\n\nHow are u? I am fine thanks\n\nI am a test sentence!\nsure";
        List<SemanticChunker.Sentence> result = this.semanticChunker.splitSentences(content);

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(expectedResult.size());

        assertThat(result.get(0).getContent()).isEqualTo(expectedResult.get(0).getContent());
        assertThat(result.get(1).getContent()).isEqualTo(expectedResult.get(1).getContent());
        assertThat(result.get(2).getContent()).isEqualTo(expectedResult.get(2).getContent());
    }

}
