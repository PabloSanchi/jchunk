package jchunk.chunker.fixed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;
import jchunk.chunker.Delimiter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ConfigTest {

    @Test
    void testDefaultConfig() {
        Config config = Config.builder().build();

        assertThat(config.getChunkSize()).isEqualTo(1000);
        assertThat(config.getChunkOverlap()).isEqualTo(100);
        assertThat(config.getDelimiter()).isEqualTo(" ");
        assertThat(config.getTrimWhitespace()).isTrue();
        assertThat(config.getKeepDelimiter()).isEqualTo(Delimiter.NONE);
    }

    @Test
    void testConfigBuilder() {
        Config config = Config.builder()
                .chunkSize(35)
                .chunkOverlap(4)
                .delimiter("")
                .trimWhitespace(false)
                .keepDelimiter(Delimiter.START)
                .build();

        assertThat(config.getChunkSize()).isEqualTo(35);
        assertThat(config.getChunkOverlap()).isEqualTo(4);
        assertThat(config.getDelimiter()).isBlank();
        assertThat(config.getTrimWhitespace()).isFalse();
        assertThat(config.getKeepDelimiter()).isEqualTo(Delimiter.START);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidConfig")
    void testInvalidConfig(Config.Builder invalidConfigToBuild, String expectedMessage) {
        assertThatThrownBy(invalidConfigToBuild::build)
                .isInstanceOf(AssertionError.class)
                .hasMessage(expectedMessage);
    }

    private static Stream<Arguments> provideInvalidConfig() {
        return Stream.of(
                Arguments.of(Config.builder().chunkSize(-1), "Chunk size must be greater than 0"),
                Arguments.of(Config.builder().chunkOverlap(-1), "Chunk overlap must be greater than or equal to 0"),
                Arguments.of(
                        Config.builder().chunkSize(10).chunkOverlap(11),
                        "Chunk size must be greater than chunk overlap"));
    }
}
