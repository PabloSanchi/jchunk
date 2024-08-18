package jchunk.chunker.fixed;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UtilsTest {

	private static final String CONTENT = "This is the text I would like to chunk up. It is the example text for this exercise";

	@Test
	void testSplitIntoSentencesWithBlanckSeparator() {
		Config config = Config.builder().delimiter("").build();

		List<String> sentences = Utils.splitIntoSentences(CONTENT, config);

		assertThat(sentences).isNotNull().hasSize(CONTENT.length());

		for (int i = 0; i < CONTENT.length(); i++) {
			assertThat(sentences.get(i)).isEqualTo(String.valueOf(CONTENT.charAt(i)));
		}
	}

	@Test
	void testSplitIntoSentencesWithNoDelimiter() {
		Config config = Config.builder().delimiter("ch").build();

		List<String> sentences = Utils.splitIntoSentences(CONTENT, config);

		assertThat(sentences).isNotNull().hasSize(2);
		assertThat(sentences.getFirst()).isEqualTo("This is the text I would like to ");
		assertThat(sentences.getLast()).isEqualTo("unk up. It is the example text for this exercise");
	}

	@Test
	void testSplitIntoSentencesWithDelimiterStart() {
		Config config = Config.builder().delimiter("ch").keepDelimiter(Config.Delimiter.START).build();

		List<String> sentences = Utils.splitIntoSentences(CONTENT, config);

		assertThat(sentences).isNotNull().hasSize(2);
		assertThat(sentences.getFirst()).isEqualTo("This is the text I would like to ");
		assertThat(sentences.getLast()).isEqualTo("chunk up. It is the example text for this exercise");
	}

	@Test
	void testSplitIntoSentencesWithDelimiterEnd() {
		Config config = Config.builder().delimiter("ch").keepDelimiter(Config.Delimiter.END).build();

		List<String> sentences = Utils.splitIntoSentences(CONTENT, config);

		assertThat(sentences).isNotNull().hasSize(2);
		assertThat(sentences.getFirst()).isEqualTo("This is the text I would like to ch");
		assertThat(sentences.getLast()).isEqualTo("unk up. It is the example text for this exercise");
	}

}
