package com.wim100.backend.exception;

import java.time.Instant;
import java.util.Map;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String mensaje,
        String path,
        Map<String, String> campos
) {
    public static ApiError of(int status, String error, String mensaje, String path) {
        return new ApiError(Instant.now(), status, error, mensaje, path, Map.of());
    }

    public static ApiError of(int status, String error, String mensaje, String path, Map<String, String> campos) {
        return new ApiError(Instant.now(), status, error, mensaje, path, campos);
    }
}
