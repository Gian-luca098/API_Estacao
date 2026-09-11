package com.estacao.meteorologica.exception;

/** Lançada quando um recurso (estação, leitura, previsão ou sensor) não é encontrado. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
