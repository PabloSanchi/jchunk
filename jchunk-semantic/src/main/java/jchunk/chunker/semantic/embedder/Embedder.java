package jchunk.chunker.semantic.embedder;

import java.util.List;

/**
 * Embedder interface
 *
 * @author Pablo Sanchidrian Herrera
 */
public interface Embedder {

    default float[] embed(String text) {
        return this.embed(List.of(text)).getFirst();
    }

    List<float[]> embed(List<String> text);

    default int getDimension() {
        return this.embed("a").length;
    }
}
