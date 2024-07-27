package jchunk.chunker.semantic;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.embedding.EmbeddingModel;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class SemanticChunkerTest {
	private final EmbeddingModel embeddingModel;
	private SemanticChunker semanticChunker;

	public SemanticChunkerTest() {
		this.embeddingModel = Mockito.mock(EmbeddingModel.class);
	}

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
				SemanticChunker.Sentence.builder().content("sure").build());

		String content = "This is a test sentence. How are u? I am fine thanks\nI am a test sentence! sure";
		List<SemanticChunker.Sentence> result = this.semanticChunker.splitSentences(content);

		assertThat(result).isNotNull();
		assertThat(result.size()).isEqualTo(expectedResult.size());

		for (int i = 0; i < result.size(); i++) {
			assertThat(result.get(i).getContent()).isEqualTo(expectedResult.get(i).getContent());
		}
	}

	@Test
	public void splitSentenceStrategyTest() {

		configureLineBreakStrategy();

		List<SemanticChunker.Sentence> expectedResult = List.of(SemanticChunker.Sentence.builder()
			.content("This is a test sentence. How are u? I am fine thanks")
			.build(), SemanticChunker.Sentence.builder().content("I am a test sentence! sure").build());

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
				SemanticChunker.Sentence.builder().index(0).content("This is a test sentence.").build(),
				SemanticChunker.Sentence.builder().index(1).content("How are u? I am fine thanks").build(),
				SemanticChunker.Sentence.builder().index(2).content("I am a test sentence!\nsure").build());

		String content = "This is a test sentence.\n\nHow are u? I am fine thanks\n\nI am a test sentence!\nsure";
		List<SemanticChunker.Sentence> result = this.semanticChunker.splitSentences(content);

		assertThat(result).isNotNull();
		assertThat(result.size()).isEqualTo(expectedResult.size());

		assertThat(result.get(0).getContent()).isEqualTo(expectedResult.get(0).getContent());
		assertThat(result.get(1).getContent()).isEqualTo(expectedResult.get(1).getContent());
		assertThat(result.get(2).getContent()).isEqualTo(expectedResult.get(2).getContent());
	}

	@Test
	public void combineSentencesSuccessTest() {
		configure();
		Integer bufferSize = 2;
		List<SemanticChunker.Sentence> input = List.of(
				SemanticChunker.Sentence.builder().index(0).content("This").build(),
				SemanticChunker.Sentence.builder().index(1).content("is").build(),
				SemanticChunker.Sentence.builder().index(2).content("a").build(),
				SemanticChunker.Sentence.builder().index(3).content("sentence").build(),
				SemanticChunker.Sentence.builder().index(4).content("for").build(),
				SemanticChunker.Sentence.builder().index(5).content("you").build(),
				SemanticChunker.Sentence.builder().index(6).content("mate").build());

		List<SemanticChunker.Sentence> expectedResult = List.of(
				SemanticChunker.Sentence.builder().index(0).content("This").combined("This is a").build(),
				SemanticChunker.Sentence.builder().index(1).content("is").combined("This is a sentence").build(),
				SemanticChunker.Sentence.builder().index(2).content("a").combined("This is a sentence for").build(),
				SemanticChunker.Sentence.builder()
					.index(3)
					.content("sentence")
					.combined("is a sentence for you")
					.build(),
				SemanticChunker.Sentence.builder().index(4).content("for").combined("a sentence for you mate").build(),
				SemanticChunker.Sentence.builder().index(5).content("you").combined("sentence for you mate").build(),
				SemanticChunker.Sentence.builder().index(6).content("mate").combined("for you mate").build());

		List<SemanticChunker.Sentence> result = this.semanticChunker.combineSentences(input, bufferSize);

		assertThat(result).isNotNull();
		assertThat(result.size()).isEqualTo(expectedResult.size());

		for (int i = 0; i < result.size(); i++) {
			assertThat(result.get(i).getIndex()).isEqualTo(expectedResult.get(i).getIndex());
			assertThat(result.get(i).getContent()).isEqualTo(expectedResult.get(i).getContent());
			assertThat(result.get(i).getCombined()).isEqualTo(expectedResult.get(i).getCombined());
		}
	}

	@Test
	public void combineSentencesWithBufferSizeEqualZeroTest() {
		configure();
		Integer bufferSize = 0;
		List<SemanticChunker.Sentence> input = List.of(SemanticChunker.Sentence.builder().content("This").build());

		assertThatThrownBy(() -> this.semanticChunker.combineSentences(input, bufferSize))
			.isInstanceOf(AssertionError.class)
			.hasMessage("The buffer size cannot be null nor 0");
	}

	@Test
	public void combineSentencesWithBufferSizeIsNullTest() {
		configure();
		Integer bufferSize = null;
		List<SemanticChunker.Sentence> input = List.of(SemanticChunker.Sentence.builder().content("This").build());

		assertThatThrownBy(() -> this.semanticChunker.combineSentences(input, bufferSize))
			.isInstanceOf(AssertionError.class)
			.hasMessage("The buffer size cannot be null nor 0");
	}

	@Test
	public void combineSentencesWithBufferSizeGreaterThanInputLengthTest() {
		configure();
		Integer bufferSize = 1;
		List<SemanticChunker.Sentence> input = List.of(SemanticChunker.Sentence.builder().content("This").build());

		assertThatThrownBy(() -> this.semanticChunker.combineSentences(input, bufferSize))
			.isInstanceOf(AssertionError.class)
			.hasMessage("The buffer size cannot be greater equal than the input length");
	}

	@Test
	public void combineSentencesWithInputIsNullTest() {
		configure();
		Integer bufferSize = 2;
		List<SemanticChunker.Sentence> input = null;

		assertThatThrownBy(() -> this.semanticChunker.combineSentences(input, bufferSize))
			.isInstanceOf(AssertionError.class)
			.hasMessage("The list of sentences cannot be null");
	}

	@Test
	public void combineSentencesWithInputIsEmptyTest() {
		configure();
		Integer bufferSize = 2;
		List<SemanticChunker.Sentence> input = List.of();

		assertThatThrownBy(() -> this.semanticChunker.combineSentences(input, bufferSize))
			.isInstanceOf(AssertionError.class)
			.hasMessage("The list of sentences cannot be empty");
	}

	@Test
	public void embedSentencesTest() {
		configure();

		Mockito.when(embeddingModel.embed(Mockito.anyList()))
				.thenReturn(List.of(List.of(1.0, 2.0, 3.0), List.of(4.0, 5.0, 6.0)));

		List<SemanticChunker.Sentence> sentences = List.of(
				SemanticChunker.Sentence.builder().combined("This is a test sentence.").build(),
				SemanticChunker.Sentence.builder().combined("How are u?").build());

		List<SemanticChunker.Sentence> expectedResult = List.of(
				SemanticChunker.Sentence.builder()
						.combined("This is a test sentence.")
						.embedding(List.of(1.0, 2.0, 3.0))
						.build(),
				SemanticChunker.Sentence.builder().combined("How are u?").embedding(List.of(4.0, 5.0, 6.0)).build());

		List<SemanticChunker.Sentence> result = this.semanticChunker.embedSentences(sentences);

		assertThat(result).isNotNull();

		for (int i = 0; i < result.size(); i++) {
			assertThat(result.get(i).getCombined()).isEqualTo(expectedResult.get(i).getCombined());
			assertThat(result.get(i).getEmbedding()).isEqualTo(expectedResult.get(i).getEmbedding());
		}

	}

	@Test
	public void testIdenticalVectors() {
		configure();

		List<Double> embedding1 = List.of(1.0, 2.0, 3.0);
		List<Double> embedding2 = List.of(1.0, 2.0, 3.0);

		double result = this.semanticChunker.cosineSimilarity(embedding1, embedding2);

		assertThat(result).isEqualTo(1.0);
	}

	@Test
	public void testOrthogonalVectors() {
		configure();

		List<Double> embedding1 = List.of(1.0, 0.0, 0.0);
		List<Double> embedding2 = List.of(0.0, 1.0, 0.0);

		double result = this.semanticChunker.cosineSimilarity(embedding1, embedding2);

		assertThat(result).isEqualTo(0.0);
	}

	@Test
	public void testOppositeVectors() {
		configure();

		List<Double> embedding1 = List.of(1.0, 2.0, 3.0);
		List<Double> embedding2 = List.of(-1.0, -2.0, -3.0);

		double result = this.semanticChunker.cosineSimilarity(embedding1, embedding2);

		assertThat(result).isEqualTo(-1.0);
	}

	@Test
	public void testDifferentMagnitudeVectors() {
		configure();

		List<Double> embedding1 = List.of(1.0, 2.0, 3.0);
		List<Double> embedding2 = List.of(2.0, 4.0, 6.0);

		double result = this.semanticChunker.cosineSimilarity(embedding1, embedding2);

		assertThat(result).isEqualTo(1.0);
	}

	@Test
	public void testZeroVectors() {
		configure();

		List<Double> embedding1 = List.of(0.0, 0.0, 0.0);
		List<Double> embedding2 = List.of(0.0, 0.0, 0.0);

		double result = this.semanticChunker.cosineSimilarity(embedding1, embedding2);

		assertThat(result).isNaN();
	}

}
