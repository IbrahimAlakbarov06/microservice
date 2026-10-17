package com.example.client.exception;

public class StorageServerException extends RuntimeException {
    public StorageServerException(String message) {
        super(message);
    }
}