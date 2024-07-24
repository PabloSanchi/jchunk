package jchunk.chunker.core.chunk;

import java.util.List;

/**
 * The IChunker interface defines the contract that all chunker implementations must
 * follow. Implementations of this interface are responsible for splitting a given content
 * string into a list of Chunk objects.
 *
 * @author Pablo Sanhidrian Herrera
 */
public interface IChunker {

	/**
	 * Splits the provided content string into a list of Chunk objects.
	 * @param content the content string to be split into chunks
	 * @return a list of Chunk {@link Chunk} objects representing the split content
	 */
	List<Chunk> split(String content);

}
