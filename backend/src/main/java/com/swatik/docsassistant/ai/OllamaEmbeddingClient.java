package com.swatik.docsassistant.ai;


import com.swatik.docsassistant.exception.StorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Ollama exposes a simple REST api. We call /api/embeddings with the model name and a prompt,
 * and it returns the embedding vector for that prompt.
 */
@Component
public class OllamaEmbeddingClient implements EmbeddingClient {

    private final RestClient restClient;
    private final String model;
    private final int dimensions;

    public OllamaEmbeddingClient(
            RestClient.Builder builder,
            @Value("${ap.ai.ollama.base-url:http://localhost:11434}") String baseUrl,
            @Value("${app.ai.ollama.embedding-model:nomic-embed-text}") String model,
            @Value("${app.ai.ollama.embedding-dimensions:768}") int dimensions) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.model = model;
        this.dimensions = dimensions;
    }

    @Override
    public float[] embed(String text) {
        // ollama rejects empty prompts.
        if (text == null || text.isBlank()) {
            throw new StorageException("Cannot embed empty text", null);
        }

        // POST the model + prompt as JSON and deserialize the JSON response into our record.
        EmbeddingResponse response = restClient.post()
                .uri("/api/embeddings")
                .body(new EmbeddingRequest(model, text))
                .retrieve()
                .body(EmbeddingResponse.class);

        // Defensive checks: a malformed/empty response would otherwise surface later
        // as a confusing NPE or a worng-sized vector in the DB.
        if (response == null || response.embedding() == null || response.embedding().length == 0) {
            throw new StorageException("Ollama returned no embedding for the text.", null);
        }

        if (response.embedding().length != dimensions) {
            throw new StorageException(
                    "Embedding size " + response.embedding().length
                            + " does not match expected " + dimensions
                            + " (is the model '" + model + "' correct?)", null);
        }

        return response.embedding();
    }

    @Override
    public int dimensions() {
        return dimensions;
    }

    // Wire formats for the Ollama /api/embeddings endpoint

    // Request body: which model to use and text to embed.
    private record EmbeddingRequest(String model, String prompt) {
    }

    // Response body: the embedding vector. Other fields are ignored
    private record EmbeddingResponse(float[] embedding) {
    }

}
