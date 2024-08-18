package jchunk.chunker.recursive;

import jchunk.chunker.core.chunk.Chunk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * Utility class for recursive chunker.
 *
 * @author Pablo Sanchidrian Herrera
 */
public class Utils {

	private static final Logger logger = Logger.getLogger(Utils.class.getName());

	public static final String LONGER_THAN_THE_SPECIFIED = "Created a chunk of size %d, which is longer than the specified %d";

	/**
	 * private constructor to hide the implicit public one
	 */
	private Utils() {
	}

	/**
	 * Splits the content into chunks.
	 * @param content the content to split
	 * @param chunkSize the size of the chunk
	 * @param chunkOverlap the overlap between chunks
	 * @param keepDelimiter whether to keep the delimiter at the start or end of the
	 * sentence or not. {@link Config.Delimiter}
	 * @param delimiters the list of delimiters to split the content
	 * @param trimWhitespace whether to trim the whitespace
	 * @param index the index of the chunk
	 * @return the list of chunks {@link Chunk}
	 */
	public static List<Chunk> splitContent(String content, Integer chunkSize, Integer chunkOverlap,
			Config.Delimiter keepDelimiter, List<String> delimiters, Boolean trimWhitespace, AtomicInteger index) {

		List<String> newDelimiters = new ArrayList<>(delimiters);
		String delimiter = getBestMatchingDelimiter(content, newDelimiters);

		List<String> splits = splitWithDelimiter(content, delimiter, keepDelimiter);

		List<String> goodSplits = new ArrayList<>();
		String delimiterToUse = (keepDelimiter != Config.Delimiter.NONE) ? "" : delimiter;

		List<Chunk> chunks = new ArrayList<>();

		for (String split : splits) {
			if (split.length() < chunkSize) {
				goodSplits.add(split);
			}
			else {
				if (!goodSplits.isEmpty()) {
					List<Chunk> generatedChunks = mergeSentences(goodSplits, delimiterToUse, chunkSize, chunkOverlap,
							trimWhitespace, index);
					chunks.addAll(generatedChunks);
					goodSplits.clear();
				}

				if (newDelimiters.isEmpty()) {
					Chunk chunk = Chunk.builder()
							.id(index.getAndIncrement())
							.content(trimWhitespace ? split.trim() : split)
							.build();
					chunks.add(chunk);
				}
				else {
					List<Chunk> generatedChunks = splitContent(split, chunkSize, chunkOverlap, keepDelimiter,
							newDelimiters, trimWhitespace, index);
					chunks.addAll(generatedChunks);
				}
			}
		}

		if (!goodSplits.isEmpty()) {
			List<Chunk> generatedChunks = mergeSentences(goodSplits, delimiterToUse, chunkSize, chunkOverlap,
					trimWhitespace, index);
			chunks.addAll(generatedChunks);
		}

		return chunks;
	}

	/**
	 * Get the best matching delimiter from right to left in the delimiter list from the
	 * given config
	 * @param content the content to split
	 * @param delimiters the list of delimiters to check
	 * @return the best matching delimiter and modifies the reference value of the given
	 * list
	 */
	private static String getBestMatchingDelimiter(String content, List<String> delimiters) {
		for (Iterator<String> iterator = delimiters.iterator(); iterator.hasNext();) {
			String delimiter = iterator.next();

			if (delimiter.isEmpty()) {
				delimiters.clear();
				return delimiter;
			}

			if (Pattern.compile(delimiter).matcher(content).find()) {
				iterator.remove();
				return delimiter;
			}
		}

		return "";
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
		if (delimiter.isEmpty()) {
			return content.chars().mapToObj(c -> String.valueOf((char) c)).toList();
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

		return splits.stream().filter(s -> !s.isBlank()).toList();
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

		return splits.stream().filter(s -> !s.isBlank()).toList();
	}

	/**
	 * Merges the sentences into chunks.
	 * @param sentences the sentences to merge
	 * @return list of chunks
	 */
	static List<Chunk> mergeSentences(List<String> sentences, String delimiter, Integer chunkSize, Integer chunkOverlap,
			Boolean trimWhitespace, AtomicInteger index) {

		int currentLen = 0;
		int delimiterLen = delimiter.length();

		List<Chunk> chunks = new ArrayList<>();
		List<String> currentChunk = new ArrayList<>();

		for (String sentence : sentences) {
			int sentenceLength = sentence.length();

			if (currentLen + sentenceLength + (currentChunk.isEmpty() ? 0 : delimiterLen) > chunkSize) {
				if (currentLen > chunkSize) {
					logger.warning(String.format(LONGER_THAN_THE_SPECIFIED, currentLen, chunkSize));
				}

				if (!currentChunk.isEmpty()) {
					String generatedSentence = joinSentences(currentChunk, delimiter, trimWhitespace);
					Chunk chunk = Chunk.builder()
							.id(index.getAndIncrement())
							.content(generatedSentence)
							.build();
					chunks.add(chunk);

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
			String generatedSentence = joinSentences(currentChunk, delimiter, trimWhitespace);
			Chunk chunk = Chunk.builder()
					.id(index.getAndIncrement())
					.content(generatedSentence)
					.build();
			chunks.add(chunk);
		}

		return chunks;
	}

	/**
	 * Joins the sentences into a single sentence.
	 * @param sentences the sentences to join
	 * @param delimiter the delimiter to join the sentences
	 * @param trimWhitespace whether to trim the whitespace
	 * @return the generated sentence
	 */
	private static String joinSentences(List<String> sentences, String delimiter, Boolean trimWhitespace) {
		String generatedSentence = String.join(delimiter, sentences);
		if (trimWhitespace) {
			generatedSentence = generatedSentence.trim();
		}

		return generatedSentence;
	}

}
