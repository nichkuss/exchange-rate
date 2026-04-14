package com.exchange.rate.webclient;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.netty.http.client.HttpClient;

@Component
public class WebClientFactoryBean implements FactoryBean<WebClient> {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	 @Value("${frankfurter.api.base.url}")
	 private String baseUrl;

	 @Value("${frankfurter.api.timeout}")
	 private int timeout;
	 
	 @Override
	 public WebClient getObject() {
		logger.info("[getObject] Base URL: " + baseUrl);
		logger.info("[getObject] Timeout: " + timeout);
		 
		HttpClient httpClient = HttpClient.create()
		        .responseTimeout(Duration.ofSeconds(timeout));

		    return WebClient.builder()
		        .baseUrl(baseUrl)
		        .clientConnector(new ReactorClientHttpConnector(httpClient))
		        .filter((request, next) -> {
		            logger.info("[getObject] FINAL REQUEST URL: " + request.url());
		            return next.exchange(request);
		        })
		        .build();
	 }

	 @Override
	 public Class<?> getObjectType() {
		 return WebClient.class;
	 }

	 @Override
	 public boolean isSingleton() {
		 return true;
	 }
}
