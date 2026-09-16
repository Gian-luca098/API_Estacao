package com.estacao.meteorologica.exception;

/** Lançada em conflitos de unicidade, ex.: criar uma estação com um código já existente. */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
