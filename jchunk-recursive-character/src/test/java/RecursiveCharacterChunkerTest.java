import jchunk.chunker.core.chunk.Chunk;
import jchunk.chunker.recursive.RecursiveCharcterChunker;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RecursiveCharacterChunkerTest {

	RecursiveCharcterChunker chunker = new RecursiveCharcterChunker();

	static String content = """
			One of the most important things I didn't understand about the world when I was a child is the degree to which the returns for performance are superlinear.

			Teachers and coaches implicitly told us the returns were linear. "You get out," I heard a thousand times, "what you put in." They meant well, but this is rarely true. If your product is only half as good as your competitor's, you don't get half as many customers. You get no customers, and you go out of business.

			It's obviously true that the returns for performance are superlinear in business. Some think this is a flaw of capitalism, and that if we changed the rules it would stop being true. But superlinear returns for performance are a feature of the world, not an artifact of rules we've invented. We see the same pattern in fame, power, military victories, knowledge, and even benefit to humanity. In all of these, the rich get richer. [1]
			""";

	@Test
	public void testSplit() {
		List<Chunk> expectedChunks = List.of(
				new Chunk(0, "One of the most important things I didn't understand about the"),
				new Chunk(1, "world when I was a child is the degree to which the returns for"),
				new Chunk(2, "performance are superlinear."),
				new Chunk(3, "Teachers and coaches implicitly told us the returns were linear."),
				new Chunk(4, "\"You get out,\" I heard a thousand times, \"what you put in.\" They"),
				new Chunk(5, "meant well, but this is rarely true. If your product is only"),
				new Chunk(6, "half as good as your competitor's, you don't get half as many"),
				new Chunk(7, "customers. You get no customers, and you go out of business."),
				new Chunk(8, "It's obviously true that the returns for performance are"),
				new Chunk(9, "superlinear in business. Some think this is a flaw of"),
				new Chunk(10, "capitalism, and that if we changed the rules it would stop being"),
				new Chunk(11, "true. But superlinear returns for performance are a feature of"),
				new Chunk(12, "the world, not an artifact of rules we've invented. We see the"),
				new Chunk(13, "same pattern in fame, power, military victories, knowledge, and"),
				new Chunk(14, "even benefit to humanity. In all of these, the rich get richer."), new Chunk(15, "[1]"));

		List<Chunk> chunks = chunker.split(content);

		assertThat(chunks).isNotNull();
		assertThat(chunks).hasSize(expectedChunks.size());

		for (int i = 0; i < chunks.size(); i++) {
			assertThat(chunks.get(i).id()).isEqualTo(expectedChunks.get(i).id());
			assertThat(chunks.get(i).content()).isEqualTo(expectedChunks.get(i).content());
		}
	}

}
