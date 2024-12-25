package jchunk.chunker.semantic;

import jchunk.chunker.core.chunk.Chunk;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.embedding.EmbeddingModel;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class SemanticChunkerUtilsTest {

	final static double MARGIN = 0.0001f;

	final EmbeddingModel embeddingModel;

	SemanticChunkerUtilsTest() {
		this.embeddingModel = Mockito.mock(EmbeddingModel.class);
	}

	@Test
	void splitSentenceDefaultStrategyTest() {
		List<Sentence> expectedResult = List.of(Sentence.builder().content("This is a test sentence.").build(),
				Sentence.builder().content("How are u?").build(),
				Sentence.builder().content("I am fine thanks\nI am a test sentence!").build(),
				Sentence.builder().content("sure").build());

		String content = "This is a test sentence. How are u? I am fine thanks\nI am a test sentence! sure";
		List<Sentence> result = Utils.splitSentences(content, SentenceSplitingStrategy.DEFAULT);

		assertThat(result).isNotNull().hasSize(expectedResult.size());

		for (int i = 0; i < result.size(); i++) {
			assertThat(result.get(i).getContent()).isEqualTo(expectedResult.get(i).getContent());
		}
	}

	@Test
	void splitSentenceStrategyTest() {
		List<Sentence> expectedResult = List.of(
				Sentence.builder().content("This is a test sentence. How are u? I am fine thanks").build(),
				Sentence.builder().content("I am a test sentence! sure").build());

		String content = "This is a test sentence. How are u? I am fine thanks\nI am a test sentence! sure";
		List<Sentence> result = Utils.splitSentences(content, SentenceSplitingStrategy.LINE_BREAK);

		assertThat(result).isNotNull().hasSize(expectedResult.size());

		assertThat(result.get(0).getContent()).isEqualTo(expectedResult.get(0).getContent());
		assertThat(result.get(1).getContent()).isEqualTo(expectedResult.get(1).getContent());
	}

	@Test
	void splitSentenceParagraphStrategyTest() {
		List<Sentence> expectedResult = List.of(Sentence.builder().index(0).content("This is a test sentence.").build(),
				Sentence.builder().index(1).content("How are u? I am fine thanks").build(),
				Sentence.builder().index(2).content("I am a test sentence!\nsure").build());

		String content = "This is a test sentence.\n\nHow are u? I am fine thanks\n\nI am a test sentence!\nsure";
		List<Sentence> result = Utils.splitSentences(content, SentenceSplitingStrategy.PARAGRAPH);

		assertThat(result).isNotNull().hasSize(expectedResult.size());

		assertThat(result.get(0).getContent()).isEqualTo(expectedResult.get(0).getContent());
		assertThat(result.get(1).getContent()).isEqualTo(expectedResult.get(1).getContent());
		assertThat(result.get(2).getContent()).isEqualTo(expectedResult.get(2).getContent());
	}

	@Test
	void combineSentencesSuccessTest() {
		Integer bufferSize = 2;
		List<Sentence> input = List.of(Sentence.builder().index(0).content("This").build(),
				Sentence.builder().index(1).content("is").build(), Sentence.builder().index(2).content("a").build(),
				Sentence.builder().index(3).content("sentence").build(),
				Sentence.builder().index(4).content("for").build(), Sentence.builder().index(5).content("you").build(),
				Sentence.builder().index(6).content("mate").build());

		List<Sentence> expectedResult = List.of(
				Sentence.builder().index(0).content("This").combined("This is a").build(),
				Sentence.builder().index(1).content("is").combined("This is a sentence").build(),
				Sentence.builder().index(2).content("a").combined("This is a sentence for").build(),
				Sentence.builder().index(3).content("sentence").combined("is a sentence for you").build(),
				Sentence.builder().index(4).content("for").combined("a sentence for you mate").build(),
				Sentence.builder().index(5).content("you").combined("sentence for you mate").build(),
				Sentence.builder().index(6).content("mate").combined("for you mate").build());

		List<Sentence> result = Utils.combineSentences(input, bufferSize);

		assertThat(result).isNotNull();
		assertThat(result.size()).isEqualTo(expectedResult.size());

		for (int i = 0; i < result.size(); i++) {
			assertThat(result.get(i).getIndex()).isEqualTo(expectedResult.get(i).getIndex());
			assertThat(result.get(i).getContent()).isEqualTo(expectedResult.get(i).getContent());
			assertThat(result.get(i).getCombined()).isEqualTo(expectedResult.get(i).getCombined());
		}
	}

	@Test
	void combineSentencesWithBufferSizeEqualZeroTest() {
		Integer bufferSize = 0;
		List<Sentence> input = List.of(Sentence.builder().content("This").build());

		assertThatThrownBy(() -> Utils.combineSentences(input, bufferSize)).isInstanceOf(AssertionError.class)
			.hasMessage("The buffer size cannot be null nor 0");
	}

	@Test
	void combineSentencesWithBufferSizeIsNullTest() {
		Integer bufferSize = null;
		List<Sentence> input = List.of(Sentence.builder().content("This").build());

		assertThatThrownBy(() -> Utils.combineSentences(input, bufferSize)).isInstanceOf(AssertionError.class)
			.hasMessage("The buffer size cannot be null nor 0");
	}

	@Test
	void combineSentencesWithBufferSizeGreaterThanInputLengthTest() {
		Integer bufferSize = 1;
		List<Sentence> input = List.of(Sentence.builder().content("This").build());

		assertThatThrownBy(() -> Utils.combineSentences(input, bufferSize)).isInstanceOf(AssertionError.class)
			.hasMessage("The buffer size cannot be greater equal than the input length");
	}

	@Test
	void combineSentencesWithInputIsNullTest() {
		Integer bufferSize = 2;
		List<Sentence> input = null;

		assertThatThrownBy(() -> Utils.combineSentences(input, bufferSize)).isInstanceOf(AssertionError.class)
			.hasMessage("The list of sentences cannot be null");
	}

	@Test
	void combineSentencesWithInputIsEmptyTest() {
		Integer bufferSize = 2;
		List<Sentence> input = List.of();

		assertThatThrownBy(() -> Utils.combineSentences(input, bufferSize)).isInstanceOf(AssertionError.class)
			.hasMessage("The list of sentences cannot be empty");
	}

	@Test
	void embedSentencesTest() {
		Mockito.when(embeddingModel.embed(Mockito.anyList()))
			.thenReturn(List.of(new float[] { 1.0f, 2.0f, 3.0f }, new float[] { 4.0f, 5.0f, 6.0f }));

		List<Sentence> sentences = List.of(Sentence.builder().combined("This is a test sentence.").build(),
				Sentence.builder().combined("How are u?").build());

		List<Sentence> expectedResult = List.of(
				Sentence.builder()
					.combined("This is a test sentence.")
					.embedding(new float[] { 1.0f, 2.0f, 3.0f })
					.build(),
				Sentence.builder().combined("How are u?").embedding(new float[] { 4.0f, 5.0f, 6.0f }).build());

		List<Sentence> result = Utils.embedSentences(embeddingModel, sentences);

		assertThat(result).isNotNull();

		for (int i = 0; i < result.size(); i++) {
			assertThat(result.get(i).getCombined()).isEqualTo(expectedResult.get(i).getCombined());
			assertThat(result.get(i).getEmbedding()).isEqualTo(expectedResult.get(i).getEmbedding());
		}

	}

	@Test
	void testIdenticalVectors() {
		float[] embedding1 = new float[] { 1.0f, 2.0f, 3.0f };
		float[] embedding2 = new float[] { 1.0f, 2.0f, 3.0f };

		double result = Utils.cosineSimilarity(embedding1, embedding2);

		assertThat(result).isCloseTo(1.0, within(MARGIN));
	}

	@Test
	void testOrthogonalVectors() {
		float[] embedding1 = new float[] { 1.0f, 0.0f, 0.0f };
		float[] embedding2 = new float[] { 0.0f, 1.0f, 0.0f };

		double result = Utils.cosineSimilarity(embedding1, embedding2);

		assertThat(result).isEqualTo(0.0);
	}

	@Test
	void testOppositeVectors() {
		float[] embedding1 = new float[] { 1.0f, 2.0f, 3.0f };
		float[] embedding2 = new float[] { -1.0f, -2.0f, -3.0f };

		double result = Utils.cosineSimilarity(embedding1, embedding2);

		assertThat(result).isCloseTo(-1.0, within(MARGIN));
	}

	@Test
	void testDifferentMagnitudeVectors() {
		float[] embedding1 = new float[] { 1.0f, 2.0f, 3.0f };
		float[] embedding2 = new float[] { 2.0f, 4.0f, 6.0f };

		double result = Utils.cosineSimilarity(embedding1, embedding2);

		assertThat(result).isCloseTo(1.0, within(MARGIN));
	}

	@Test
	void testZeroVectors() {
		float[] embedding1 = new float[] { 0.0f, 0.0f, 0.0f };
		float[] embedding2 = new float[] { 0.0f, 0.0f, 0.0f };

		double result = Utils.cosineSimilarity(embedding1, embedding2);

		assertThat(result).isNaN();
	}

	@Test
	void testGetIndicesAboveThreshold() {
		Integer percentile = 95;
		List<Double> distances = List.of(10.0, 15.0, 20.0, 25.0, 30.0, 35.0, 40.0, 45.0, 50.0, 55.0, 60.0, 65.0, 70.0,
				75.0);

		List<Integer> expectedIndices = List.of(13);

		List<Integer> actualIndices = Utils.calculateBreakPoints(distances, percentile);

		assertThat(actualIndices).isEqualTo(expectedIndices);
	}

	@Test
	void testGenerateChunks() {
		List<Sentence> sentences = List.of(Sentence.builder().index(0).content("This").build(),
				Sentence.builder().index(1).content("is").build(), Sentence.builder().index(2).content("a").build(),
				Sentence.builder().index(3).content("test.").build(), Sentence.builder().index(4).content("We").build(),
				Sentence.builder().index(5).content("are").build(),
				Sentence.builder().index(6).content("writing").build(),
				Sentence.builder().index(7).content("unit").build(),
				Sentence.builder().index(8).content("tests.").build());

		List<Integer> breakPoints = List.of(2, 4, 6);

		List<Chunk> expectedChunks = List.of(new Chunk(0, "This is a"), new Chunk(1, "test. We"),
				new Chunk(2, "are writing"), new Chunk(3, "unit tests."));

		List<Chunk> actualChunks = Utils.generateChunks(sentences, breakPoints);

		assertThat(actualChunks).isNotNull().hasSize(expectedChunks.size());

		for (int i = 0; i < actualChunks.size(); i++) {
			assertThat(actualChunks.get(i).id()).isEqualTo(expectedChunks.get(i).id());
			assertThat(actualChunks.get(i).content()).isEqualTo(expectedChunks.get(i).content());
		}
	}

}
