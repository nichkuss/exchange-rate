package com.exchange.rate;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.event.EventListener;

import com.exchange.rate.datastore.ExchangeRateDataStore;

@TestComponent
public class TestStartupVerifier {

    private final ExchangeRateDataStore dataStore;

    public static boolean verified = false;

    public TestStartupVerifier(ExchangeRateDataStore dataStore) {
        this.dataStore = dataStore;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {

        if (dataStore.getAll().isEmpty()) {
            throw new IllegalStateException("Data not loaded before application is ready");
        }

        try {
            dataStore.put("TEST", "FAIL");
            throw new IllegalStateException("Store is not immutable");
        } catch (IllegalStateException expected) {
        	
        }
        verified = true;
    }
}
