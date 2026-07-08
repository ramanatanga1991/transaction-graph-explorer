package com.example.graph.exception;

public class CycleDetectedException extends RuntimeException {
    public CycleDetectedException(String message) {
        super(message);
    }
}
