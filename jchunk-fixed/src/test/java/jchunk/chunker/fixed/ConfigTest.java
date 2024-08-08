package jchunk.chunker.fixed;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfigTest {

	@Test
	void testDefaultConfig() {
		Config config = Config.builder().build();

		assertThat(config.getChunkSize()).isEqualTo(1000);
		assertThat(config.getChunkOverlap()).isEqualTo(100);
		assertThat(config.getDelimiter()).isEqualTo(" ");
		assertThat(config.getTrimWhitespace()).isTrue();
		assertThat(config.getKeepDelimiter()).isEqualTo(Config.Delimiter.NONE);
	}

	@Test
	void testConfigBuilder() {
		Config config = Config.builder()
			.chunkSize(35)
			.chunkOverlap(4)
			.delimiter("")
			.trimWhitespace(false)
			.keepDelimiter(Config.Delimiter.START)
			.build();

		assertThat(config.getChunkSize()).isEqualTo(35);
		assertThat(config.getChunkOverlap()).isEqualTo(4);
		assertThat(config.getDelimiter()).isBlank();
		assertThat(config.getTrimWhitespace()).isFalse();
		assertThat(config.getKeepDelimiter()).isEqualTo(Config.Delimiter.START);
	}

	@Test
	void testConfigThrowErrorWhenChunkSizeIsNegative() {
		assertThatThrownBy(() -> Config.builder().chunkSize(-1).build()).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Chunk size must be greater than 0");
	}

	@Test
	void testConfigThrowErrorWhenChunkOverlapIsNegative() {
		assertThatThrownBy(() -> Config.builder().chunkOverlap(-1).build()).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Chunk overlap must be greater than or equal to 0");
	}

	@Test
	void testConfigThrowErrorWhenChunkOverlapIsGreaterThanChunkSize() {
		assertThatThrownBy(() -> Config.builder().chunkSize(10).chunkOverlap(11).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Chunk size must be greater than chunk overlap");
	}

}
