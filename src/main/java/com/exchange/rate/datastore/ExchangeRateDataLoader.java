package com.exchange.rate.datastore;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.exchange.rate.strategies.impl.ExchangeRateFetcherImpl;

@Component
public class ExchangeRateDataLoader implements ApplicationRunner {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final List<ExchangeRateFetcherImpl> fetchers;
	private final ExchangeRateDataStore dataStore;
	
	public ExchangeRateDataLoader(List<ExchangeRateFetcherImpl> fetchers, 
								  ExchangeRateDataStore dataStore) {
		this.fetchers = fetchers;
		this.dataStore = dataStore;
	}
	
	@Override
	public void run(ApplicationArguments args) {
		for(ExchangeRateFetcherImpl fetcher: fetchers) {
			Object data = fetcher.fetchData();
			dataStore.put(fetcher.getResourceType(), data);
		}
		
		dataStore.markInitialized();
		
		logger.info("[run] Data Preloaded Successfully....");
	}
}
