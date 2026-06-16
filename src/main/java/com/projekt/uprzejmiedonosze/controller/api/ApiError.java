package com.projekt.uprzejmiedonosze.controller.api;

import java.util.Map;

public record ApiError(
        int status,
        String message,
        Map<String, String> fields
) {
}