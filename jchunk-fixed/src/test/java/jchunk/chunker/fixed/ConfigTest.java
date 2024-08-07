package jchunk.chunker.fixed;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigTest {

	@Test
	public void testDefaultConfig() {
		Config config = Config.builder().build();

		assertThat(config.getChunkSize()).isEqualTo(1000);
		assertThat(config.getChunkOverlap()).isEqualTo(100);
		assertThat(config.getSeparator()).isEqualTo(" ");
		assertThat(config.getTrimWhitespace()).isTrue();
	}

	@Test
	public void testConfigBuilder() {
		Config config = Config.builder().chunkSize(35).chunkOverlap(4).separator("").trimWhitespace(false).build();

		assertThat(config.getChunkSize()).isEqualTo(35);
		assertThat(config.getChunkOverlap()).isEqualTo(4);
		assertThat(config.getSeparator()).isEqualTo("");
		assertThat(config.getTrimWhitespace()).isFalse();
	}

}
