package jchunk.chunker.fixed;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigTest {

	@Test
	public void testDefaultConfig() {
		Config config = Config.builder().build();

		assertThat(config.getChunkSize()).isEqualTo(1000);
		assertThat(config.getChunkOverlap()).isEqualTo(100);
		assertThat(config.getDelimiter()).isEqualTo(" ");
		assertThat(config.getTrimWhitespace()).isTrue();
		assertThat(config.getKeepDelimiter()).isEqualTo(Config.Delimiter.NONE);
	}

	@Test
	public void testConfigBuilder() {
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

}
