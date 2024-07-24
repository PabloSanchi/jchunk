package jchunk.chunker.core.chunk;

import java.util.List;

/**
 * The Chunk record represents a segment of content with an associated identifier and embedding.
 * This record is used to store information about each chunk generated by the IChunker implementation.
 *
 * @param id        the unique identifier for the chunk
 * @param content   the actual content of the chunk
 * @param embedding a list of double values representing the embedding of the chunk
 */
public record Chunk(String id, String content, List<Double> embedding) {
}
