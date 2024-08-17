import jchunk.chunker.recursive.Config;
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
		assertThat(config.getSeparators()).containsExactly("\n\n", "\n", " ", "");
	}

	@Test
	void testCustomConfig() {
		Config config = Config.builder().chunkSize(50).chunkOverlap(10).separators(List.of("-", "!", "?")).build();

		assertThat(config.getChunkSize()).isEqualTo(50);
		assertThat(config.getChunkOverlap()).isEqualTo(10);
		assertThat(config.getSeparators()).containsExactly("-", "!", "?");
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
