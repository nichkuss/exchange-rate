package com.exchange.rate;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import com.exchange.rate.strategies.LatestIdrRatesStrategy;

import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LatestIdrRatesStrategyTest {

	@Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec uriSpec;

    @Mock
    private WebClient.RequestHeadersSpec headersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private LatestIdrRatesStrategy strategy;

    @BeforeEach
    void setUp() throws Exception {
        strategy = new LatestIdrRatesStrategy(webClient);
        Field field = LatestIdrRatesStrategy.class.getDeclaredField("githubUsername");
        field.setAccessible(true);
        field.set(strategy, "nichkuss"); 
    }

    @Test
    void whenFetchDataSuccessCalculateAndTransformJsonResponse() throws Exception {
        String mockJson = "{\n" +
                "  \"amount\": 1.0,\n" +
                "  \"base\": \"IDR\",\n" +
                "  \"date\": \"2024-01-01\",\n" +
                "  \"rates\": {\n" +
                "    \"USD\": 0.000065,\n" +
                "    \"EUR\": 0.000060\n" +
                "  }\n" +
                "}";

        doReturn(uriSpec).when(webClient).get();
        doReturn(headersSpec).when(uriSpec).uri(anyString());
        doReturn(responseSpec).when(headersSpec).retrieve();
        doReturn(responseSpec).when(responseSpec).onStatus(any(), any());
        doReturn(Mono.just(mockJson)).when(responseSpec).bodyToMono(String.class);

        Object result = strategy.fetchData();
        assertNotNull(result);
        assertTrue(result instanceof ArrayNode);

        ArrayNode array = (ArrayNode) result;
        assertEquals(1, array.size());

        JsonNode root = array.get(0);
        assertEquals(1.0, root.get("amount").asDouble());
        assertEquals("IDR", root.get("base").asString());
        assertEquals("2024-01-01", root.get("date").asString());

        JsonNode rates = root.get("rates");
        assertEquals(2, rates.size());

        boolean hasUsd = false;
        for (JsonNode node : rates) {
            if ("USD".equals(node.get("currency").asString())) {
                hasUsd = true;
            }
        }
        assertTrue(hasUsd);

        double usdBuySpread = root.get("USD_BuySpread_IDR").asDouble();
        double rateUsd = 0.000065;
        double spreadFactor = calculateSpreadFactor("nichkuss");
        double expected = (1.0 / rateUsd) * (1 + spreadFactor);

        assertEquals(expected, usdBuySpread, 0.0001);
    }

    @Test
    void whenFetchDataReturnsErrorJsonThrowsRuntimeException() {
        String invalidJson = "{\"message\":\"not found\"}";

        doReturn(uriSpec).when(webClient).get();
        doReturn(headersSpec).when(uriSpec).uri(anyString());
        doReturn(responseSpec).when(headersSpec).retrieve();
        doReturn(responseSpec).when(responseSpec).onStatus(any(), any());
        doReturn(Mono.just(invalidJson)).when(responseSpec).bodyToMono(String.class);
        assertThrows(RuntimeException.class, () -> strategy.fetchData());
    }

    private double calculateSpreadFactor(String githubUser) {
        String user = githubUser.toLowerCase();
        int sum = 0;
        for (char c : user.toCharArray()) {
            sum += (int) c;
        }
        return (sum % 1000) / 100000.0;
    }
}
