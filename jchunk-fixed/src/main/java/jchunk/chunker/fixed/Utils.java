package jchunk.chunker.fixed;

import jchunk.chunker.core.chunk.Chunk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Utils {

	private static final Logger logger = Logger.getLogger(Utils.class.getName());

	public static final String LONGER_THAN_THE_SPECIFIED_ = "Created a chunk of size %d, which is longer than the specified %d";

	/**
	 * Splits the content into sentences using the delimiter.
	 * @param content the content to split
	 * @param config configuration for the chunker/splitter
	 * @return a list of split sentences
	 */
	public static List<String> splitIntoSentences(String content, Config config) {
		String delimiter = config.getDelimiter();
		Config.Delimiter keepDelimiter = config.getKeepDelimiter();

		if (delimiter.isBlank()) {
			return content.chars().mapToObj(c -> String.valueOf((char) c)).collect(Collectors.toList());
		}

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

			return splits.stream().filter(s -> !s.isBlank()).toList();
		}

		return Arrays.stream(content.split(delimiter)).filter(s -> !s.isBlank()).toList();
	}

	/**
	 * Merges the sentences into chunks.
	 * @param sentences the sentences to merge
	 * @param config configuration for the chunker/splitter
	 * @return list of chunks
	 */
	static List<Chunk> mergeSentences(List<String> sentences, Config config) {
		String delimiter = config.getDelimiter();
		Integer chunkSize = config.getChunkSize();
		Integer chunkOverlap = config.getChunkOverlap();
		Boolean trimWhitespace = config.getTrimWhitespace();

		int currentLen = 0;
		int delimiterLen = delimiter.length();

		List<Chunk> chunks = new ArrayList<>();
		List<String> currentChunk = new ArrayList<>();

		AtomicInteger chunkIndex = new AtomicInteger(0);

		for (String sentence : sentences) {
			int sentenceLength = sentence.length();

			if (currentLen + sentenceLength + (currentChunk.isEmpty() ? 0 : delimiterLen) > chunkSize) {
				if (currentLen > chunkSize) {
					logger.warning(String.format(LONGER_THAN_THE_SPECIFIED_, currentLen, config.getChunkSize()));
				}

				if (!currentChunk.isEmpty()) {
					String generatedSentence = joinSentences(currentChunk, delimiter, trimWhitespace);
					chunks.add(new Chunk(chunkIndex.getAndIncrement(), generatedSentence));

					while (currentLen > chunkOverlap
							|| (currentLen + sentenceLength + (currentChunk.isEmpty() ? 0 : delimiterLen) > chunkSize
									&& currentLen > 0)) {
						currentLen -= currentChunk.removeFirst().length() + (currentChunk.isEmpty() ? 0 : delimiterLen);
					}
				}
			}

			currentChunk.add(sentence);
			currentLen += sentenceLength + (currentChunk.size() > 1 ? delimiterLen : 0);
		}

		if (!currentChunk.isEmpty()) {
			String generatedSentence = joinSentences(currentChunk, config.getDelimiter(), config.getTrimWhitespace());
			chunks.add(new Chunk(chunkIndex.getAndIncrement(), generatedSentence));
		}

		return chunks;
	}

	private static String joinSentences(List<String> sentences, String delimiter, Boolean trimWhitespace) {
		String generatedSentence = String.join(delimiter, sentences);
		return trimWhitespace ? generatedSentence.trim() : generatedSentence;
	}

}
