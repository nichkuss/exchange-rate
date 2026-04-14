package com.exchange.rate.strategies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import tools.jackson.core.StreamWriteFeature;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import reactor.core.publisher.Mono;

import com.exchange.rate.strategies.impl.ExchangeRateFetcherImpl;
import static com.exchange.rate.constants.Constants.*;

@Component
public class HistoricalIdrUsdStrategy implements ExchangeRateFetcherImpl {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final WebClient webClient;
	
	public HistoricalIdrUsdStrategy(WebClient webClient) {
		this.webClient = webClient;
	}
	
	@Override
	public String getResourceType() {
		return HISTORICAL_IDR_USD;
	}
	
	@Override
	public Object fetchData() {		
		String response = webClient.get()
		        .uri("/v1/2024-01-01..2024-01-05?from=IDR&to=USD")
		        .retrieve()
		        .onStatus(status -> status.isError(), clientResponse ->
		            clientResponse.bodyToMono(String.class)
		                .defaultIfEmpty("No error body")
		                .map(body -> new RuntimeException(
		                    "API Error: " + clientResponse.statusCode() + " - " + body
		                ))
		        )
		        .bodyToMono(String.class)
		        .onErrorResume(e -> {
		            logger.error("[fetchData] API call failed", e);
		            return Mono.empty();
		        })
		        .block();
		
		logger.info("[fetchData] API RESPONSE: " + response);
		
		try {			
			ObjectMapper mapper = JsonMapper.builder()
			        .enable(SerializationFeature.INDENT_OUTPUT)
			        .enable(StreamWriteFeature.WRITE_BIGDECIMAL_AS_PLAIN) 
			        .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
			        .build();

			Map<String, Object> jsonResponse = mapper.readValue(response, new TypeReference<Map<String, Object>>() {});

			Map<String, Map<String, Object>> rates = mapper.convertValue(jsonResponse.get("rates"),
			        								 new TypeReference<Map<String, Map<String, Object>>>() {});

			List<Map<String, Object>> ratesList = new ArrayList<>();

			for (Map.Entry<String, Map<String, Object>> entry : rates.entrySet()) {
			    Map<String, Object> item = new LinkedHashMap<>();
			    item.put("date", entry.getKey());
			    item.putAll(entry.getValue());
			    ratesList.add(item);
			}

			jsonResponse.put("rates", ratesList);
			return Collections.singletonList(jsonResponse);
	    } catch (Exception e) {
	        throw new RuntimeException("Failed to parse response", e);
	    }
	}
}
