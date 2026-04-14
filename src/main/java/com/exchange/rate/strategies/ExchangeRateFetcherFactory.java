package com.exchange.rate.strategies;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.exchange.rate.strategies.impl.ExchangeRateFetcherImpl;

@Component
public class ExchangeRateFetcherFactory {
	
	private final Map<String, ExchangeRateFetcherImpl> strategyMap;
	
	public ExchangeRateFetcherFactory(List<ExchangeRateFetcherImpl> fetchers) {
        this.strategyMap = fetchers.stream()
        						   .collect(Collectors.toMap(
        								    ExchangeRateFetcherImpl::getResourceType,
        								    Function.identity()));
	}
	
	public ExchangeRateFetcherImpl getFetcher(String resourceType) {
		ExchangeRateFetcherImpl fetcher = strategyMap.get(resourceType);
        if (fetcher == null) {
            throw new IllegalArgumentException("Invalid resource type: " + resourceType);
        }
        return fetcher;
    }
}
