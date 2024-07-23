package jchunk.chunker.core.chunk;

import java.util.List;

public interface IChunker {

	List<Chunk> split(String content);

}
