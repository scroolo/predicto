package com.predicto.catalog.cito;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CitoEnvelope<T>(
        boolean success,
        T data
) {}
