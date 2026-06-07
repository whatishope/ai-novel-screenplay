package com.whatishope.screenplay.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.whatishope.screenplay.config.LlmProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class HttpLlmClientTest {

    @Test
    void generateCallsOpenAiCompatibleChatCompletionApi() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://example.com/v1/chat/completions"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-api-key"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.model").value("test-model"))
                .andExpect(jsonPath("$.messages[0].role").value("system"))
                .andExpect(jsonPath("$.messages[1].role").value("user"))
                .andExpect(jsonPath("$.messages[1].content").value("生成角色"))
                .andExpect(jsonPath("$.response_format.type").value("json_object"))
                .andRespond(withSuccess("""
                        {
                          "choices": [
                            {
                              "message": {
                                "content": "{\\"characters\\":[]}"
                              }
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        LlmProperties properties = properties();
        RestClient restClient = builder
                .baseUrl(properties.getBaseUrl())
                .requestInterceptor((request, body, execution) -> {
                    request.getHeaders().setBearerAuth(properties.getApiKey());
                    return execution.execute(request, body);
                })
                .build();
        HttpLlmClient client = new HttpLlmClient(properties, restClient);

        String content = client.generate("生成角色");

        assertThat(content).isEqualTo("{\"characters\":[]}");
        server.verify();
    }

    @Test
    void generateCanDisableJsonResponseFormatForCompatibleProviders() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://example.com/v1/chat/completions"))
                .andExpect(jsonPath("$.response_format").doesNotExist())
                .andRespond(withSuccess("""
                        {
                          "choices": [
                            {
                              "message": {
                                "content": "{\\"characters\\":[]}"
                              }
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        LlmProperties properties = properties();
        properties.setJsonResponseEnabled(false);
        RestClient restClient = builder
                .baseUrl(properties.getBaseUrl())
                .requestInterceptor((request, body, execution) -> {
                    request.getHeaders().setBearerAuth(properties.getApiKey());
                    return execution.execute(request, body);
                })
                .build();
        HttpLlmClient client = new HttpLlmClient(properties, restClient);

        String content = client.generate("生成角色");

        assertThat(content).isEqualTo("{\"characters\":[]}");
        server.verify();
    }

    private LlmProperties properties() {
        LlmProperties properties = new LlmProperties();
        properties.setApiKey("test-api-key");
        properties.setBaseUrl("https://example.com/v1");
        properties.setModel("test-model");
        properties.setTimeoutSeconds(5);
        return properties;
    }
}
