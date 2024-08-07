package jchunk.chunker.fixed;

import jchunk.chunker.core.chunk.Chunk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class Utils {

	/**
	 * Splits the content into sentences using the delimiter.
	 * @param content the content to split
	 * @return a list of split sentences
	 */
	public static List<String> splitIntoSentences(String content, String delimiter, Config.Delimiter keepDelimiter) {

		if (keepDelimiter != Config.Delimiter.NONE) {
			String withDelimiter = "((?<=%1$s)|(?=%1$s))";
			List<String> preSplits = new ArrayList<>(List.of(content.split(String.format(withDelimiter, delimiter))));
			List<String> splits = new ArrayList<>();

			if (keepDelimiter == Config.Delimiter.START) {
				splits.add(preSplits.getFirst());
				IntStream.range(1, preSplits.size())
					.filter(i -> i % 2 == 1)
					.forEach(i -> splits.add(preSplits.get(i).concat(preSplits.get(i + 1))));
			}
			else {
				IntStream.range(0, preSplits.size() - 1)
					.filter(i -> i % 2 == 0)
					.forEach(i -> splits.add(preSplits.get(i).concat(preSplits.get(i + 1))));
				splits.add(preSplits.getLast());
			}

			return splits.stream().filter(s -> !s.isBlank()).map(String::trim).toList();
		}

		return Arrays.stream(content.split(delimiter)).filter(s -> !s.isBlank()).map(String::trim).toList();
	}

	/**
	 * Merges the sentences into chunks.
	 * @param sentences the sentences to merge
	 * @param delimiter the delimiter to use
	 * @return list of chunks
	 */
	List<Chunk> mergeSentences(List<String> sentences, String delimiter) {
		return null;
	}

}
