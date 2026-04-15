package com.exchange.rate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import com.exchange.rate.exceptions.FrankfurterApiException;
import com.exchange.rate.strategies.SupportedCurrenciesStrategy;

import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class SupportedCurrenciesStrategyTest {
	
	@Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec uriSpec;

    @Mock
    private WebClient.RequestHeadersSpec headersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private SupportedCurrenciesStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new SupportedCurrenciesStrategy(webClient);
    }

    @Test
    void whenFetchDataSuccessReturnTransformedJson() throws Exception {
        String mockJson = "{\n" +
                "  \"USD\": \"United States Dollar\",\n" +
                "  \"IDR\": \"Indonesian Rupiah\"\n" +
                "}";

        doReturn(uriSpec).when(webClient).get();
        doReturn(headersSpec).when(uriSpec).uri("/v1/currencies");
        doReturn(responseSpec).when(headersSpec).retrieve();
        doReturn(responseSpec).when(responseSpec).onStatus(any(), any());
        doReturn(Mono.just(mockJson)).when(responseSpec).bodyToMono(String.class);

        Object result = strategy.fetchData();
        assertNotNull(result);
        assertTrue(result instanceof String);

        String jsonResult = (String) result;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode array = mapper.readTree(jsonResult);

        assertEquals(2, array.size());

        JsonNode first = array.get(0);
        assertTrue(first.has("code"));
        assertTrue(first.has("name"));

        List<String> codes = new ArrayList<>();
        for (JsonNode node : array) {
            codes.add(node.get("code").asString());
        }

        assertTrue(codes.contains("USD"));
        assertTrue(codes.contains("IDR"));
    }

    @Test
    void whenFetchDataFromApiFailsThrowsFrankfurterApiException() {
        doReturn(uriSpec).when(webClient).get();
        doReturn(headersSpec).when(uriSpec).uri(anyString());
        doReturn(responseSpec).when(headersSpec).retrieve();
        doReturn(responseSpec).when(responseSpec).onStatus(any(), any());

        doReturn(Mono.error(new RuntimeException("API down")))
                .when(responseSpec).bodyToMono(String.class);

        assertThrows(FrankfurterApiException.class, () -> {
            strategy.fetchData();
        });
    }

    @Test
    void whenFetchDataReturnFailedJsonResponseThrowsRuntimeException() {
    	String errorJson = "{\"message\":\"not found\"}";
        doReturn(uriSpec).when(webClient).get();
        doReturn(headersSpec).when(uriSpec).uri(anyString());
        doReturn(responseSpec).when(headersSpec).retrieve();
        doReturn(responseSpec).when(responseSpec).onStatus(any(), any());
        doReturn(Mono.just(errorJson)).when(responseSpec).bodyToMono(String.class);

        Object result = strategy.fetchData();
        assertNotNull(result);

        String json = (String) result;
        ObjectMapper mapper = new ObjectMapper();
        JsonNode array = mapper.readTree(json);

        assertEquals(1, array.size());
        assertEquals("message", array.get(0).get("code").asString());
        assertEquals("not found", array.get(0).get("name").asString());
    }
}
