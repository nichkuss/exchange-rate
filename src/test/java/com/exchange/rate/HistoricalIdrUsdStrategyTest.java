package com.exchange.rate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import com.exchange.rate.exceptions.FrankfurterApiException;
import com.exchange.rate.strategies.HistoricalIdrUsdStrategy;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
public class HistoricalIdrUsdStrategyTest {

	@Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec uriSpec;

    @Mock
    private WebClient.RequestHeadersSpec headersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private HistoricalIdrUsdStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new HistoricalIdrUsdStrategy(webClient);
    }

    
    @Test
    void whenFetchDataSuccessReturnTransformedJsonResponse() {
    	  String mockJson = "{\n" +
                  "  \"rates\": {\n" +
                  "    \"2024-01-01\": { \"USD\": 0.000065 },\n" +
                  "    \"2024-01-02\": { \"USD\": 0.000066 }\n" +
                  "  }\n" +
                  "}";

        doReturn(uriSpec).when(webClient).get();
        doReturn(headersSpec).when(uriSpec).uri(anyString());
        doReturn(responseSpec).when(headersSpec).retrieve();
        doReturn(responseSpec).when(responseSpec).onStatus(any(), any());
        doReturn(Mono.just(mockJson)).when(responseSpec).bodyToMono(String.class);

        Object result = strategy.fetchData();
        assertNotNull(result);
        assertTrue(result instanceof List);

        List<?> outerList = (List<?>) result;
        assertEquals(1, outerList.size());

        Map<?, ?> response = (Map<?, ?>) outerList.get(0);
        List<?> rates = (List<?>) response.get("rates");

        assertEquals(2, rates.size());

        Map<?, ?> first = (Map<?, ?>) rates.get(0);

        assertEquals("2024-01-01", first.get("date"));
        assertEquals(0.000065, ((Number) first.get("USD")).doubleValue());
    }

    @Test
    void whenFetchDataFailedThrowsFrankfurterApiException() {
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
    void whenFetchDataReturnFailedJsonResponseThrowsRunTimeException() {
        String errorJson = "{\"message\":\"not found\"}";
        doReturn(uriSpec).when(webClient).get();
        doReturn(headersSpec).when(uriSpec).uri(anyString());
        doReturn(responseSpec).when(headersSpec).retrieve();
        doReturn(responseSpec).when(responseSpec).onStatus(any(), any());
        doReturn(Mono.just(errorJson))
                .when(responseSpec).bodyToMono(String.class);
        assertThrows(RuntimeException.class, () -> {
            strategy.fetchData();
        });
    }
}
