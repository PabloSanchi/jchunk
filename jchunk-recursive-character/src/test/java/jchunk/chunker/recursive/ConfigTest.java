package jchunk.chunker.recursive;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfigTest {

	@Test
	void testDefaultConfig() {
		Config config = Config.defaultConfig();

		assertThat(config.getChunkSize()).isEqualTo(100);
		assertThat(config.getChunkOverlap()).isEqualTo(20);
		assertThat(config.getDelimiters()).containsExactly("\n\n", "\n", " ", "");
		assertThat(config.getKeepDelimiter()).isEqualTo(Config.Delimiter.START);
		assertThat(config.getTrimWhitespace()).isTrue();
	}

	@Test
	void testCustomConfig() {
		Config config = Config.builder()
			.chunkSize(50)
			.chunkOverlap(10)
			.separators(List.of("-", "!", "?"))
			.keepDelimiter(Config.Delimiter.END)
			.trimWhitespace(false)
			.build();

		assertThat(config.getChunkSize()).isEqualTo(50);
		assertThat(config.getChunkOverlap()).isEqualTo(10);
		assertThat(config.getDelimiters()).containsExactly("-", "!", "?");
		assertThat(config.getKeepDelimiter()).isEqualTo(Config.Delimiter.END);
		assertThat(config.getTrimWhitespace()).isFalse();
	}

	@Test
	void testThrowExceptionWhenChunkSizeIsZero() {
		assertThatThrownBy(() -> Config.builder().chunkSize(0).build()).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Chunk size must be greater than 0");
	}

	@Test
	void testThrowExceptionWhenChunkSizeIsNegative() {
		assertThatThrownBy(() -> Config.builder().chunkSize(-1).build()).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Chunk size must be greater than 0");
	}

	@Test
	void testThrowExceptionWhenChunkOverlapIsNegative() {
		assertThatThrownBy(() -> Config.builder().chunkOverlap(-1).build()).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Chunk overlap must be greater than or equal to 0");
	}

	@Test
	void testThrowExceptionWhenChunkOverlapIsGreaterThanChunkSize() {
		assertThatThrownBy(() -> Config.builder().chunkSize(10).chunkOverlap(20).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Chunk size must be greater than chunk overlap");
	}

}
