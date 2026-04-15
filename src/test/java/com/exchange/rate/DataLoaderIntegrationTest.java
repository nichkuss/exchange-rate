package com.exchange.rate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.event.EventListener;

import com.exchange.rate.datastore.ExchangeRateDataStore;
import com.exchange.rate.strategies.HistoricalIdrUsdStrategy;
import com.exchange.rate.strategies.LatestIdrRatesStrategy;
import com.exchange.rate.strategies.SupportedCurrenciesStrategy;
import com.exchange.rate.strategies.impl.ExchangeRateFetcherImpl;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = { "spring.main.allow-bean-definition-overriding=true" })
@ComponentScan(excludeFilters = @ComponentScan.Filter(
	           type = FilterType.ASSIGNABLE_TYPE,
	           classes = { HistoricalIdrUsdStrategy.class,
	        		   	   LatestIdrRatesStrategy.class,
	        		   	   SupportedCurrenciesStrategy.class
	        }
	    )
	)
public class DataLoaderIntegrationTest {

	@Autowired
    private ExchangeRateDataStore dataStore;

    @Test
    void shouldLoadDataCorrectly() {

        assertEquals("hist-data", dataStore.get("historical_idr_usd"));
        assertEquals("latest-data", dataStore.get("latest_idr_rates"));
        assertEquals("supported-data", dataStore.get("supported_currencies"));

        assertThrows(IllegalStateException.class, () -> {
            dataStore.put("NEW", "value");
        });
    }

    @TestComponent
    static class TestStartupVerifier {

        @Autowired
        private ExchangeRateDataStore dataStore;

        @EventListener(ApplicationReadyEvent.class)
        public void verify() {

            if (dataStore.getAll().isEmpty()) {
                throw new IllegalStateException("Data not loaded before ready");
            }

            try {
                dataStore.put("FAIL", "FAIL");
                throw new IllegalStateException("Store not locked");
            } catch (IllegalStateException expected) {}
        }
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public ExchangeRateFetcherImpl historicalIdrUsdStrategy() {
            return new ExchangeRateFetcherImpl() {
                public String getResourceType() { return "historical_idr_usd"; }
                public Object fetchData() { return "hist-data"; }
            };
        }

        @Bean
        public ExchangeRateFetcherImpl latestIdrRatesStrategy() {
            return new ExchangeRateFetcherImpl() {
                public String getResourceType() { return "latest_idr_rates"; }
                public Object fetchData() { return "latest-data"; }
            };
        }

        @Bean
        public ExchangeRateFetcherImpl supportedCurrenciesStrategy() {
            return new ExchangeRateFetcherImpl() {
                public String getResourceType() { return "supported_currencies"; }
                public Object fetchData() { return "supported-data"; }
            };
        }
    }
}
