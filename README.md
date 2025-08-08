# JChunk
## A Java Library for Text Chunking

JChunk project is simple library that enables different types of text splitting strategies.
This project begun thanks to Greg Kamradt's post [text splitting ideas](https://github.com/FullStackRetrieval-com/RetrievalTutorials/blob/main/tutorials/LevelsOfTextSplitting/5_Levels_Of_Text_Splitting.ipynb)

## ⚠️ WARNING - EARLY PHASE ⚠️

For now there is only [Pablo Sanchidrian](https://github.com/PabloSanchi) developing this project (in free time) so it might take a while to get to a first stable version.

Feel free to contribute!!

## ROAD MAP
- [x] Fixed Character Chunker (DONE)
- [X] Recursive Character Text Chunker (DONE)
- [x] Semantic Chunker (DONE)
- [ ] Agentic Chunker (FUTURE)

## Building

To build with running unit tests

```sh
./mvnw clean package
```

To reformat using the java-format plugin

```sh
./mvnw spotless:apply
```

To update the year on license headers using the license-maven-plugin

```sh
./mvnw license:update-file-header -Plicense
```

To check javadocs using the javadoc:javadoc

```sh
./mvnw javadoc:javadoc -Pjavadoc
```

## Fixed Character Chunker
Character splitting is a basic text processing technique where text is divided into fixed-size chunks of characters. While it's not suitable for most advanced text processing tasks due to its simplicity and rigidity, it serves as an excellent starting point to understand the fundamentals of text splitting. See the following aspects of this chunker including its advantages, disadvantages, and key concepts like chunk size, chunk overlap, and separators.

### 1. Chunk Size 
The chunk size is the number of characters each chunk will contain. For example, if you set a chunk size of 50, each chunk will consist of 50 characters.

**Example:**
- Input Text: "This is an example of character splitting."
- Chunk Size: 10
- Output Chunks: `["This is an", " example o", "f characte", "r splittin", "g."]`

### 2. Chunk Overlap
Chunk overlap refers to the number of characters that will overlap between consecutive chunks. This helps in maintaining context across chunks by ensuring that a portion of the text at the end of one chunk is repeated at the beginning of the next chunk.

**Example:**
- Input Text: "This is an example of character splitting."
- Chunk Size: 10
- Chunk Overlap: 4
- Output Chunks: `["This is an", "s an examp", "xample of", "of charac", "aracter sp", "r splittin", "tting."]`

### 3. Separators
Separators are specific character sequences used to split the text. For instance, you might want to split your text at every comma or period.

**Example:**
- Input Text: "This is an example. Let's split on periods. Okay?"
- Chunk Size: 20
- Separator: ". "
- Output Chunks: ["This is an example", "Let's split on periods", "Okay?"]

### Pros and Cons

**Pros**
- Easy & Simple: Character splitting is straightforward to implement and understand.
- Basic Segmentation: It provides a basic way to segment text into smaller pieces.

**Cons**
- Rigid: Does not consider the structure or context of the text.
- Duplicate Data: Chunk overlap creates duplicate data, which might not be efficient.

## Recursive Character Text Chunker

## Document Specific Chunker

## Semantic Chunker
When performing retrieval-augmented generation (RAG), fixed chunk sizes can sometimes be inadequate,
either missing crucial information or adding extraneous content. To address this, we can use embeddings to represent the semantic meaning of text, allowing us to chunk content based on semantic relationships.

### How it works?

1. Sentence Splitting:

Split the entire text into sentences using delimiters like '.', '?', and '!' (alternative strategies can also be used).

2. Mapping Sentences:

Transform the list of sentences into the following structure: 
```json
[
  { 
    "sentence": "this is the sentence.",
    "index": 0
  },
  {
    "sentence": "this is the next sentence.",
    "index": 1
  },
  {
    "sentence": "this is the last sentence.",
    "index": 2
  }
]
```

3. Combining Sentences:

Combine each sentence with its preceding and succeeding sentences (the number of sentences will be given by a bufferSize variable ) to reduce noise and better capture relationships. Add a key `combined` for this combined text.

Example for buffer size 1: 
```json
[
  { 
    "sentence": "this is the sentence.",
    "combined": "this is the sentence. this is the next sentence.",
    "index": 0
  },
  {
    "sentence": "this is the next sentence.",
    "combined": "this is the sentence. this is the next sentence. this is the last sentence.",
    "index": 1
  },
  {
    "sentence": "this is the last sentence.",
    "combined": "this is the next sentence. this is the last sentence.",
    "index": 2
  }
]
```

4. Generating Embeddings:

Compute the embedding of each `combined`.

```json
[
  { 
    "sentence": "this is the sentence.",
    "combined": "this is the sentence. this is the next sentence.",
    "embedding": [0.002, 0.003, 0.004, ...], // embedding of the combined key text
    "index": 0
  },
  // ...
]
```

5. Calculating Distances:

Compute the cosine distances between sequential pairs.

6. Identifying Breakpoints:

Analyze the distances to identify sections where distances are smaller (indicating related content) and areas with larger distances (indicating less related content).

7. Determining Split Points:

Use the 95th percentile of the distances as the threshold for determining breakpoints (can use any other percentile or threshold technique).

![semantic-chunk](images/semantic-chunk.png)
   
8. Splitting Chunks:

Split the text into chunks at the identified breakpoints.

9. Done!


## Agentic Chunker

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

# FUTURE

Frontend to test the different chunkers and see the results in a more visual way.
