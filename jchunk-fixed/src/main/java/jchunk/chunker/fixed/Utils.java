package jchunk.chunker.fixed;

import jchunk.chunker.core.chunk.Chunk;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class Utils {

	/**
	 * private constructor to hide the implicit public one
	 */
	private Utils() {
	}

	private static final Logger logger = Logger.getLogger(Utils.class.getName());

	public static final String LONGER_THAN_THE_SPECIFIED = "Created a chunk of size %d, which is longer than the specified %d";

	/**
	 * Splits the content into sentences using the delimiter.
	 * @param content the content to split
	 * @param config configuration for the chunker/splitter
	 * @return a list of split sentences
	 */
	public static List<String> splitIntoSentences(String content, Config config) {
		String delimiter = config.getDelimiter();
		Config.Delimiter keepDelimiter = config.getKeepDelimiter();

		if (delimiter.isEmpty()) {
			return content.chars().mapToObj(c -> String.valueOf((char) c)).toList();
		}

		return splitWithDelimiter(content, delimiter, keepDelimiter);
	}

	/**
	 * Splits the content into sentences using the delimiter.
	 * @param content the content to split
	 * @param delimiter the delimiter to split the content.
	 * @param keepDelimiter whether to keep the delimiter at the start or end of the
	 * sentence or not. {@link Config.Delimiter}
	 * @return a list of split sentences
	 */
	private static List<String> splitWithDelimiter(String content, String delimiter, Config.Delimiter keepDelimiter) {

		if (keepDelimiter == Config.Delimiter.NONE) {
			return Arrays.stream(content.split(Pattern.quote(delimiter))).filter(s -> !s.isEmpty()).toList();
		}

		String withDelimiter = "((?<=%1$s)|(?=%1$s))";
		List<String> preSplits = new ArrayList<>(List.of(content.split(String.format(withDelimiter, delimiter))));

		return keepDelimiter == Config.Delimiter.START ? splitWithDelimiterStart(preSplits)
				: splitWithDelimiterEnd(preSplits);
	}

	/**
	 * Splits the content into sentences using the delimiter at the start of each
	 * sentence. {@link Config.Delimiter#START}
	 * @param preSplits pre-splits by the delimiter
	 * @return the list of split sentences
	 */
	private static List<String> splitWithDelimiterStart(List<String> preSplits) {
		List<String> splits = new ArrayList<>();

		splits.add(preSplits.getFirst());
		IntStream.range(1, preSplits.size() - 1)
			.filter(i -> i % 2 == 1)
			.forEach(i -> splits.add(preSplits.get(i).concat(preSplits.get(i + 1))));

		return splits.stream().filter(s -> !s.isEmpty()).toList();
	}

	/**
	 * Splits the content into sentences using the delimiter at the end of each sentence.
	 * {@link Config.Delimiter#END}
	 * @param preSplits the pre-splits by the delimiter
	 * @return the list of split sentences
	 */
	private static List<String> splitWithDelimiterEnd(List<String> preSplits) {
		List<String> splits = new ArrayList<>();

		IntStream.range(0, preSplits.size() - 1)
			.filter(i -> i % 2 == 0)
			.forEach(i -> splits.add(preSplits.get(i).concat(preSplits.get(i + 1))));
		splits.add(preSplits.getLast());

		return splits.stream().filter(s -> !s.isEmpty()).toList();
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
		Deque<String> currentChunk = new LinkedList<>();

		AtomicInteger chunkIndex = new AtomicInteger(0);

		for (String sentence : sentences) {
			int sentenceLength = sentence.length();

			if (currentLen + sentenceLength + (currentChunk.isEmpty() ? 0 : delimiterLen) > chunkSize) {
				if (currentLen > chunkSize) {
					logger.warning(String.format(LONGER_THAN_THE_SPECIFIED, currentLen, config.getChunkSize()));
				}

				if (!currentChunk.isEmpty()) {
					addChunk(chunks, currentChunk, delimiter, trimWhitespace, chunkIndex);
					currentLen = adjustCurrentChunkForOverlap(currentChunk, currentLen, chunkOverlap, delimiterLen);
				}
			}

			currentChunk.add(sentence);
			currentLen += sentenceLength + (currentChunk.size() > 1 ? delimiterLen : 0);
		}

		if (!currentChunk.isEmpty()) {
			addChunk(chunks, currentChunk, delimiter, trimWhitespace, chunkIndex);
		}

		return chunks;
	}

	/**
	 * Adds the chunk to the list of chunks.
	 * @param chunks the list of chunks
	 * @param currentChunk the current chunk
	 * @param delimiter the delimiter
	 * @param trimWhitespace whether to trim the whitespace
	 * @param index the index of the chunk
	 */
	private static void addChunk(List<Chunk> chunks, Deque<String> currentChunk, String delimiter,
			boolean trimWhitespace, AtomicInteger index) {
		String generatedSentence = joinSentences(currentChunk, delimiter, trimWhitespace);
		Chunk chunk = Chunk.builder().id(index.getAndIncrement()).content(generatedSentence).build();
		chunks.add(chunk);
	}

	/**
	 * Adjusts the current chunk for overlap.
	 * @param currentChunk the current chunk
	 * @param currentLen the current length of the chunk
	 * @param chunkOverlap the overlap between chunks
	 * @param delimiterLen the length of the delimiter
	 * @return the adjusted length of the chunk
	 */
	private static int adjustCurrentChunkForOverlap(Deque<String> currentChunk, int currentLen, int chunkOverlap,
			int delimiterLen) {
		while (currentLen > chunkOverlap && !currentChunk.isEmpty()) {
			currentLen -= currentChunk.removeFirst().length() + (currentChunk.isEmpty() ? 0 : delimiterLen);
		}
		return currentLen;
	}

	/**
	 * Joins the sentences into a single sentence.
	 * @param sentences the sentences to join
	 * @param delimiter the delimiter to join the sentences
	 * @param trimWhitespace whether to trim the whitespace
	 * @return the generated sentence
	 */
	private static String joinSentences(Deque<String> sentences, String delimiter, Boolean trimWhitespace) {
		String generatedSentence = String.join(delimiter, sentences);
		if (trimWhitespace) {
			generatedSentence = generatedSentence.trim();
		}

		return generatedSentence;
	}

}
