package com.swatik.docsassistant.ingestion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits a document's extracted text into overlapping chunks.
 * Embedding models and LLM have a limited context window, and retrieval works best on small, focussed passages.
 * We therefore break the full text into pieces of sroughly
 */
@Service
public class DocumentChunker {

    // Target chunk size, in words. Configurable via app.ingestion.chunk-size.
    private final int chunkSizeWords;

    // How many words each chunk shares with the previous one.
    private final int overlapWords;

    public DocumentChunker(
            @Value("${app.ingestion.chunk-size:375}") int chunkSizeWords,
            @Value("${app.ingestion.chunk-overlap:60}") int overlapWords) {

        //Guard against misconfiguration that would break the sliding window
        if(chunkSizeWords <= 0){
            throw new IllegalArgumentException("chunkSizeWords must be > 0");
        }
        if(overlapWords < 0 || overlapWords >= chunkSizeWords){
            throw new IllegalArgumentException("chunk-overlap must be >= 0 and < chunk-size");
        }
        this.chunkSizeWords = chunkSizeWords;
        this.overlapWords = overlapWords;
    }

    // Splits text into overlapping chunks, preserving reading order.
    public List<String> chunk(String text){
        List<String> chunks = new ArrayList<>();

        // Nothing to chunk; return an empty list
        if(text == null || text.isBlank()){
            return chunks;
        }

        // Split on any run of whitespace into words. trim() first so we don't get a leading empty token.
        String[] words = text.trim().split("\\s+");

        // The window advances by words each step.
        int stride = chunkSizeWords - overlapWords;

        // Slide a window [start,end) across the word array.
        for(int start = 0; start < words.length; start += stride){
            int end = Math.min(start+chunkSizeWords, words.length);

            // Join this window back into a single string chunk.
            StringBuilder sb = new StringBuilder();
            for(int i = start; i < end; i++){
                if(i>start){
                    sb.append(' ');
                }
                sb.append(words[i]);
            }
            chunks.add(sb.toString());

            // If this window already reached the end, stop, otherwise the loop
            // could emit a final tiny chunk that is fully contained in the overlap of the previous one.
            if(end == words.length){
                break;
            }
        }
        return chunks;
    }
}
