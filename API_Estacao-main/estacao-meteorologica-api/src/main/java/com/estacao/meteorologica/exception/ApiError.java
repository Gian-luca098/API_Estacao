package com.estacao.meteorologica.exception;

import java.time.OffsetDateTime;
import java.util.List;

/** Formato padronizado de erro devolvido por todos os endpoints da API. */
public record ApiError(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details
) {
}
