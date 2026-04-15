package com.exchange.rate.exceptions;

public class FrankfurterApiException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public FrankfurterApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public FrankfurterApiException(String message) {
        super(message);
    }
}
