package com.exchange.rate.strategies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

import static com.exchange.rate.constants.Constants.*;

import java.math.BigDecimal;

@Component
public class LatestIdrRatesStrategy implements ExchangeRateFetcherImpl {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Value("${github.username}")
	private String githubUsername;
	
	private final WebClient webClient;
	
	public LatestIdrRatesStrategy(WebClient webClient) {
		this.webClient = webClient;
	}
	
	@Override
	public String getResourceType() {
		return LATEST_IDR_RATES;
	}
	
	@Override
	public Object fetchData() {			
		String response = webClient.get()
			    .uri("/v1/latest?base=IDR")
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
			ObjectMapper objectMapper = JsonMapper.builder().enable(SerializationFeature.INDENT_OUTPUT).build();
			JsonNode root = objectMapper.readTree(response);
			double rateUsd = root.path("rates").path("USD").asDouble();
			double spreadFactor = calculateSpreadFactor(githubUsername);
			logger.info("[fetchData] Spread Factor: " + spreadFactor);
			
	        double usdBuySpreadIdr = (1.0 / rateUsd) * (1 + spreadFactor);

	        ObjectNode result = objectMapper.createObjectNode();
	        result.put("amount", root.path("amount").asDouble());
	        result.put("base", root.path("base").asString());
	        result.put("date", root.path("date").asString());

	        ObjectNode ratesNode = (ObjectNode) root.path("rates");
	        ArrayNode ratesArray = objectMapper.createArrayNode();

	        ratesNode.properties().iterator().forEachRemaining(entry -> {
	            ObjectNode rateObj = objectMapper.createObjectNode();
	            rateObj.put("currency", entry.getKey());
	            rateObj.put("rate", new BigDecimal(entry.getValue().asString()));
	            ratesArray.add(rateObj);
	        });

	        result.set("rates", ratesArray);
	        result.put("USD_BuySpread_IDR", usdBuySpreadIdr);
	        
	        ArrayNode jsonResArray = objectMapper.createArrayNode();
	        jsonResArray.add(result);
	        return jsonResArray;
		} catch (Exception e2) {
			throw new RuntimeException("Failed to parse response", e2);
		}
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
