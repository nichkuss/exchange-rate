package com.exchange.rate.strategies;

import static com.exchange.rate.constants.Constants.*;

import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.exchange.rate.exceptions.FrankfurterApiException;
import com.exchange.rate.strategies.impl.ExchangeRateFetcherImpl;

import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

@Component
public class SupportedCurrenciesStrategy implements ExchangeRateFetcherImpl {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final WebClient webClient;
	
	public SupportedCurrenciesStrategy(WebClient webClient) {
		this.webClient = webClient;
	}
	
	@Override
	public String getResourceType() {
		return SUPPORTED_CURRENCIES;
	} 
	
	@Override
	public Object fetchData() {
		String response = webClient.get()
		        .uri("/v1/currencies")
		        .retrieve()
		        .onStatus(
				        status -> status.is4xxClientError() || status.is5xxServerError(),
				        clientResponse -> clientResponse.bodyToMono(String.class)
				            .defaultIfEmpty("No error body")
				            .flatMap(body -> Mono.<Throwable>error(
				                new FrankfurterApiException(
				                    "API error: " + clientResponse.statusCode() + " - " + body
				                )
				            ))
				    )
		        .bodyToMono(String.class)
		        .onErrorMap(ex -> {
			        logger.error("[fetchData] API call failed", ex);
			        return new FrankfurterApiException("External API failure", ex);
			    })
		        .block();
		
		logger.info("[fetchData] API RESPONSE: " + response);
		
		try {
			ObjectMapper mapper = JsonMapper.builder()
											.enable(SerializationFeature.INDENT_OUTPUT)
											.build();
			
			JsonNode root = mapper.readTree(response);
			ArrayNode arrayNode = mapper.createArrayNode();
	        Iterator<Map.Entry<String, JsonNode>> fields = root.properties().iterator();
	        
	        while (fields.hasNext()) {
	            Map.Entry<String, JsonNode> entry = fields.next();
	            ObjectNode obj = mapper.createObjectNode();
	            obj.put("code", entry.getKey());
	            obj.put("name", entry.getValue().asString());
	            arrayNode.add(obj);
	        }
			
	        return mapper.writeValueAsString(arrayNode);
		} catch (Exception e2) {
			throw new RuntimeException("Failed to parse response", e2);
		}
	}
}
