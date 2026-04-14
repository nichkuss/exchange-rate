package com.exchange.rate.strategies.impl;

public interface ExchangeRateFetcherImpl {
	public String getResourceType();
	public Object fetchData();
}
