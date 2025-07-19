package jchunk.chunker.recursive;

import jchunk.chunker.core.chunk.Chunk;
import jchunk.chunker.core.chunk.IChunker;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * {@link RecursiveCharacterChunker} is a class that implements the {@link IChunker}
 * interface and splits a text into chunks recursively with the given separators.
 *
 * @author Pablo Sanchidrian Herrera
 */
public class RecursiveCharacterChunker implements IChunker {

	private static final Logger logger = Logger.getLogger(RecursiveCharacterChunker.class.getName());

	private static final String LONGER_THAN_THE_SPECIFIED = "Created a chunk of size %d, which is longer than the specified %d";

	private final Config config;

	public RecursiveCharacterChunker() {
		this(Config.defaultConfig());
	}

	public RecursiveCharacterChunker(Config config) {
		this.config = config;
	}

	@Override
	public List<Chunk> split(String content) {
		return splitContent(content, config.delimiters(), new AtomicInteger(0));
	}

	/**
	 * Splits the content into chunks.
	 * @param content the content to split
	 * @param delimiters the list of delimiters to split the content
	 * @param index the index of the chunk
	 * @return the list of chunks {@link Chunk}
	 */
	@SuppressWarnings("java:S3776")
	private List<Chunk> splitContent(String content, List<String> delimiters, AtomicInteger index) {
		var newDelimiters = new ArrayList<>(delimiters);
		var delimiter = getBestMatchingDelimiter(content, newDelimiters);

		var splits = splitWithDelimiter(content, delimiter);

		var goodSplits = new ArrayList<String>();
		var delimiterToUse = config.keepDelimiter() != Config.Delimiter.NONE ? "" : delimiter;


		var chunks = new ArrayList<Chunk>();

		for (String split : splits) {
			if (split.length() < config.chunkSize()) {
				goodSplits.add(split);
			}
			else {
				if (!goodSplits.isEmpty()) {
					var generatedChunks = mergeSentences(goodSplits, delimiterToUse, index);
					chunks.addAll(generatedChunks);
					goodSplits.clear();
				}

				if (newDelimiters.isEmpty()) {
					Chunk chunk = Chunk.builder()
							.id(index.getAndIncrement())
							.content(config.trimWhiteSpace() ? split.trim() : split)
							.build();
					chunks.add(chunk);
				}
				else {
					List<Chunk> generatedChunks = splitContent(split, newDelimiters, index);
					chunks.addAll(generatedChunks);
				}
			}
		}

		if (!goodSplits.isEmpty()) {
			List<Chunk> generatedChunks = mergeSentences(goodSplits, delimiterToUse, index);
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
	private String getBestMatchingDelimiter(String content, List<String> delimiters) {
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
	 * @return a list of split sentences
	 */
	private List<String> splitWithDelimiter(String content, String delimiter) {
		if (delimiter.isEmpty()) {
			return content.chars().mapToObj(c -> String.valueOf((char) c)).toList();
		}

		String withDelimiter = "((?<=%1$s)|(?=%1$s))";
		List<String> preSplits = new ArrayList<>(List.of(content.split(String.format(withDelimiter, delimiter))));

		return config.keepDelimiter() == Config.Delimiter.START ? splitWithDelimiterStart(preSplits)
				: splitWithDelimiterEnd(preSplits);
	}

	/**
	 * Splits the content into sentences using the delimiter at the start of each
	 * sentence. {@link Config.Delimiter#START}
	 * @param preSplits pre-splits by the delimiter
	 * @return the list of split sentences
	 */
	private List<String> splitWithDelimiterStart(List<String> preSplits) {
		var splits = new ArrayList<String>();

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
	private List<String> splitWithDelimiterEnd(List<String> preSplits) {
		var splits = new ArrayList<String>();

		IntStream.range(0, preSplits.size() - 1)
				.filter(i -> i % 2 == 0)
				.forEach(i -> splits.add(preSplits.get(i).concat(preSplits.get(i + 1))));
		splits.add(preSplits.getLast());

		return splits.stream()
				.filter(s -> !s.isBlank())
				.toList();
	}

	/**
	 * Merges the sentences into chunks.
	 * @param sentences the sentences to merge
	 * @return list of chunks
	 */
	private List<Chunk> mergeSentences(List<String> sentences, String delimiter, AtomicInteger index) {

		var currentLen = 0;
		var delimiterLen = delimiter.length();
		var chunks = new ArrayList<Chunk>();
		var currentChunk = new LinkedList<String>();

		for (String sentence : sentences) {
			int sentenceLength = sentence.length();

			if (currentLen + sentenceLength + (currentChunk.isEmpty() ? 0 : delimiterLen) > config.chunkSize()) {

				if (currentLen > config.chunkSize()) {
					var msg = String.format(LONGER_THAN_THE_SPECIFIED, currentLen, config.chunkSize());
					logger.warning(msg);
				}

				if (!currentChunk.isEmpty()) {
					addChunk(chunks, currentChunk, delimiter, index);
					currentLen = adjustCurrentChunkForOverlap(currentChunk, currentLen, delimiterLen);
				}
			}

			currentChunk.addLast(sentence);
			currentLen += sentenceLength + (currentChunk.size() > 1 ? delimiterLen : 0);
		}

		if (!currentChunk.isEmpty()) {
			addChunk(chunks, currentChunk, delimiter, index);
		}

		return chunks;
	}

	/**
	 * Adds the chunk to the list of chunks.
	 * @param chunks the list of chunks
	 * @param currentChunk the current chunk
	 * @param delimiter the delimiter
	 * @param index the index of the chunk
	 */
	private void addChunk(List<Chunk> chunks, Deque<String> currentChunk, String delimiter, AtomicInteger index) {
		var generatedSentence = joinSentences(new ArrayList<>(currentChunk), delimiter);
		var chunk = Chunk.builder().id(index.getAndIncrement()).content(generatedSentence).build();
		chunks.add(chunk);
	}

	/**
	 * Adjusts the current chunk for overlap.
	 * @param currentChunk the current chunk
	 * @param currentLen the current length of the chunk
	 * @param delimiterLen the length of the delimiter
	 * @return the adjusted length of the chunk
	 */
	private int adjustCurrentChunkForOverlap(Deque<String> currentChunk, int currentLen, int delimiterLen) {
		while (currentLen > config.chunkOverlap() && !currentChunk.isEmpty()) {
			currentLen -= currentChunk.removeFirst().length() + (currentChunk.isEmpty() ? 0 : delimiterLen);
		}
		return currentLen;
	}

	/**
	 * Joins the sentences into a single sentence.
	 * @param sentences the sentences to join
	 * @param delimiter the delimiter to join the sentences
	 * @return the generated sentence
	 */
	private String joinSentences(List<String> sentences, String delimiter) {
		var generatedSentence = String.join(delimiter, sentences);
		if (config.trimWhiteSpace()) {
			generatedSentence = generatedSentence.trim();
		}

		return generatedSentence;
	}

}
