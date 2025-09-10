package dev.tpcoder.routing.exception;

public class NoHostAvailableException extends RuntimeException {
    public NoHostAvailableException(String message) {
        super(message);
    }
}
