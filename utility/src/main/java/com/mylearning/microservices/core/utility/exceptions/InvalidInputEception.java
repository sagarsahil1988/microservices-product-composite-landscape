package com.mylearning.microservices.core.utility.exceptions;

public class InvalidInputEception extends RuntimeException {

    public InvalidInputEception() {
    }

    public InvalidInputEception(String message) {
        super(message);
    }

    public InvalidInputEception(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidInputEception(Throwable cause) {
        super(cause);
    }
}
