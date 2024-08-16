import jchunk.chunker.recursive.Config;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        Config config = Config.builder()
                .chunkSize(50)
                .chunkOverlap(10)
                .separators(List.of("-", "!", "?"))
                .build();

        assertThat(config.getChunkSize()).isEqualTo(50);
        assertThat(config.getChunkOverlap()).isEqualTo(10);
        assertThat(config.getSeparators()).containsExactly("-", "!", "?");
    }

}
