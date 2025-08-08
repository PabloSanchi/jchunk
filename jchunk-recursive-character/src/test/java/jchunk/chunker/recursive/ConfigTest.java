package jchunk.chunker.recursive;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.stream.Stream;
import jchunk.chunker.Delimiter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ConfigTest {

    @Test
    void testDefaultConfig() {
        Config config = Config.defaultConfig();

        assertThat(config.chunkSize()).isEqualTo(100);
        assertThat(config.chunkOverlap()).isEqualTo(20);
        assertThat(config.delimiters()).containsExactly("\n\n", "\n", " ", "");
        assertThat(config.keepDelimiter()).isEqualTo(Delimiter.START);
        assertThat(config.trimWhiteSpace()).isTrue();
    }

    @Test
    void testCustomConfig() {
        Config config = Config.builder()
                .chunkSize(50)
                .chunkOverlap(10)
                .separators(List.of("-", "!", "?"))
                .keepDelimiter(Delimiter.END)
                .trimWhitespace(false)
                .build();

        assertThat(config.chunkSize()).isEqualTo(50);
        assertThat(config.chunkOverlap()).isEqualTo(10);
        assertThat(config.delimiters()).containsExactly("-", "!", "?");
        assertThat(config.keepDelimiter()).isEqualTo(Delimiter.END);
        assertThat(config.trimWhiteSpace()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("provideInvalidConfiguration")
    void testInvalidConfig(Config.Builder invalidConfigToBuild, String expectedMessage) {
        assertThatThrownBy(invalidConfigToBuild::build)
                .isInstanceOf(AssertionError.class)
                .hasMessage(expectedMessage);
    }

    private static Stream<Arguments> provideInvalidConfiguration() {
        return Stream.of(
                Arguments.of(Config.builder().chunkSize(0), "Chunk size must be greater than 0"),
                Arguments.of(Config.builder().chunkSize(-1), "Chunk size must be greater than 0"),
                Arguments.of(Config.builder().chunkOverlap(-1), "Chunk overlap must be greater than or equal to 0"),
                Arguments.of(
                        Config.builder().chunkSize(10).chunkOverlap(20),
                        "Chunk size must be greater than chunk overlap"));
    }
}
