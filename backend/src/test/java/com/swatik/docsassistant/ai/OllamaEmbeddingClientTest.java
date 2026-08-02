package com.swatik.docsassistant.ai;

import com.swatik.docsassistant.exception.StorageException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class OllamaEmbeddingClientTest {

    private record Fixture(OllamaEmbeddingClient client, MockRestServiceServer server){}

    private Fixture newFixture(int dimensions){
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OllamaEmbeddingClient client = new OllamaEmbeddingClient(builder, "http://localhost:11434","nomic-embed-text",dimensions);

        return new Fixture(client,server);
    }

    @Test
    void parseEmbeddingFromResponse(){

        Fixture f = newFixture(3);
        f.server().expect(requestTo("http://localhost:11434/api/embeddings"))
                .andExpect(method(POST))
                .andRespond(withSuccess("{\"embedding\":[0.1,0.2,0.3]}", MediaType.APPLICATION_JSON));

        float[] vector = f.client().embed("hello world");

        assertThat(vector).containsExactly(0.1f,0.2f,0.3f);
        f.server().verify();
    }

    @Test
    void rejectsEmptyText(){
        Fixture f = newFixture(768);

        assertThatThrownBy(() -> f.client().embed(" "))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void failsWhenDimensionsMismatch(){
        Fixture f = newFixture(768);
        f.server().expect(requestTo("http://localhost:11434/api/embeddings"))
                .andRespond(withSuccess("{\"embedding\":[0.1,0.2,0.3]}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> f.client().embed("hello"))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("does not match expected");
    }

    @Test
    void wrapsServerErrors(){
        Fixture f = newFixture(768);
        f.server().expect(requestTo("http://localhost:11434/api/embeddings"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> f.client().embed("hello"))
                .isInstanceOf(Exception.class);
    }
}
