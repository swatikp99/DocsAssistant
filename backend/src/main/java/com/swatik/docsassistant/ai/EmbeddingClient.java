package com.swatik.docsassistant.ai;

import java.util.List;

/**
 *  Embedding turns a piece of text into a fixed-length numeric vector so that
 *  semantically similar texts end up close together in vector space. We store
 *  these vectors in pgvector and later do similarity search against them.
 *
 *  This interface is the "abstraction so a free cloud model can swap in"
 *  i.e. a Groq/Gemini-backed implementation could replace it without touching
 *  the ingestion pipeline.
 */
public interface EmbeddingClient {

    // Embeds a single piece of text.
    float[] embed(String text);

    // Embeds several texts. The default impl calls one piece per item.
    default List<float[]> embed(List<String> texts){
        return texts.stream().map(this::embed).toList();
    }

    // Returns the fixed dimensionality of the vectors this client produces
    int dimensions();
}
