# JChunk

JChunk project is simple library that enables different types of text splitting strategies.
This project begun thanks to Greg Kamradt's post [text splitting ideas](https://github.com/FullStackRetrieval-com/RetrievalTutorials/blob/main/tutorials/LevelsOfTextSplitting/5_Levels_Of_Text_Splitting.ipynb)

## ⚠️ WARNING - STILL IN DEVELOPMENT ⚠️

For now there is only [Pablo Sanchidrian](https://github.com/PabloSanchi) developing this project (in free time) so it might take a while to get to a first stable version.

Feel free to contribute!!

## ROAD MAP
- [ ] Character Chunker (NOT STARTED)
- [ ] Recursive Character Text Chunker (NOT STARTED)
- [ ] Document Specific Chunker (NOT STARTED)
- [x] Semantic Chunker (IN DEVELOPMENT)
- [ ] Agentic Chunker (NOT STARTED)


## Character Chunker

## Recursive Character Text Chunker

## Document Specific Chunker

## Semantic Chunker
When doing retrieval-augmented generation (RAG) sometimes fixed chunk size is not enough 
as we could be missing or adding info that we really need.

Idea: use embeddings. with the embedding we represent the semantic meaning of a string so then when comparing embedding of different texts we can see the correlation between them. amd then establish a 
chunking criteria based on that.

How it works?

1. We split the entire text into sentences (using '.', '?' and '!') (**NOTE: CAN BE OTHER STRATEGY**).
2. Transform the list of sentences into a list of maps Array<Map<String, Object>> `[{ "sentence": <sentence>, "index": <index> }]`.
3. Combine the sentence before and after so that we reduce noise and capture more of the relationships between sequential sentences, add a key `combinedSentence`.
4. Add a key with the embedding of the `combinedSentence` like `combinedSentenceEmbedding`.
5. Now let's add the cosine distances between sequential embedding pairs to see where the break points are. We'll add `distanceToNext` as another key.
6. Now we see sections where distances are smaller and then areas of larger distances. Meaning that we can actually infer that the higher the numer the less related are those text portions.
7. Let's split those chunks using the 95th percentile of the distances as the threshold.

![semantic-chunk](images/semantic-chunk.png)
   
8. Now split the chunks using the breakpoints
9. Done!


## Agentic Chunker


