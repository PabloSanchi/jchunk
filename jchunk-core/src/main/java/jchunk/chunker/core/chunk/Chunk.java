package jchunk.chunker.core.chunk;

import java.util.List;

public record Chunk(String id, String content, List<Double> embedding) {
}
