package com.exchange.rate.datastore;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class ExchangeRateDataStore {
	
	private final Map<String, Object> dataStore = new ConcurrentHashMap<>();

    private volatile boolean initialized = false;

    public void put(String key, Object value) {
        if (initialized) {
            throw new IllegalStateException("Data store is already initialized and immutable");
        }
        dataStore.put(key, value);
    }

    public Object get(String key) {
        return dataStore.get(key);
    }

    public void markInitialized() {
        this.initialized = true;
    }

    public Map<String, Object> getAll() {
        return Collections.unmodifiableMap(dataStore);
    }
}
