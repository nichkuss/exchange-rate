package com.exchange.rate.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.rate.datastore.ExchangeRateDataStore;

@RestController
@RequestMapping("/api/exchange-rate")
public class ExchangeRateController {
	private final ExchangeRateDataStore dataStore;
	
	public ExchangeRateController(ExchangeRateDataStore dataStore) {
		this.dataStore = dataStore;
	}
	
	@GetMapping(value = "/data/{resourceType}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getExchangeRate(@PathVariable String resourceType) {
        return dataStore.get(resourceType);
    }
}
