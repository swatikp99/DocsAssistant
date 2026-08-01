package com.swatik.docsassistant.ingestion;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DocumentChunkerTest {

    @Test
    void blankOrNullTextProducesNoChunks(){
        DocumentChunker chunker = new DocumentChunker(10,2);

        assertThat(chunker.chunk(null)).isEmpty();
        assertThat(chunker.chunk("  ")).isEmpty();
    }

    @Test
    void shortTextFitsInASingleChunk(){
        DocumentChunker chunker = new DocumentChunker(10,2);

        List<String> chunks = chunker.chunk(" one two three");

        assertThat(chunks).containsExactly("one two three");
    }

    @Test
    void longTextIsSplitWithOverlap(){
        // 12 words: w1...w12 Size 5, overlap 2 -> stride 3.
        String text = IntStream.rangeClosed(1,12)
                .mapToObj(i -> "w" + i)
                .collect(Collectors.joining(" "));
        DocumentChunker chunker = new DocumentChunker(5,2);

        List<String> chunks = chunker.chunk(text);

        assertThat(chunks).containsExactly(
                "w1 w2 w3 w4 w5",
                "w4 w5 w6 w7 w8",
                "w7 w8 w9 w10 w11",
                "w10 w11 w12");

        assertThat(chunks.get(0)).endsWith("w4 w5");
        assertThat(chunks.get(1)).startsWith("w4 w5");
    }

    @Test
    void rejectsInvalidConfiguration(){
        assertThrows(IllegalArgumentException.class, () -> new DocumentChunker(5,5));
        assertThrows(IllegalArgumentException.class, () -> new DocumentChunker(0,0));
    }
}
