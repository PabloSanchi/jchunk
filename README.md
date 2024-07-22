# JChunk

JChunk project is simple library that enables different types of text chunking

## ⚠️ WARNING - EARLY STAGE

## Developing

### Semantic Chunker
When doing retrieval-augmented generation (RAG) sometimes fixed chunk size is not enough 
as we could be missing or adding info that we really need.

Idea: use embeddings. with the embedding we represent the semantic meaning of a string so then when comparing embedding of different texts we can see the correlation between them. amd then establish a 
chunking criteria based on that.

Inspired by: Greg Kamradt's [chunkin ideas jupyter notebook](https://github.com/FullStackRetrieval-com/RetrievalTutorials/blob/main/tutorials/LevelsOfTextSplitting/5_Levels_Of_Text_Splitting.ipynb)