package io.armoniax.abiserializationprovider;

import io.armoniax.error.serializationProvider.SerializationProviderError;

/**
 * Error class is used when there is an exception when the ABIEOS c++ code attempts to create its
 * working context during initialization.
 */
public class AbiContextNullError extends SerializationProviderError {

    public AbiContextNullError() {
    }

    public AbiContextNullError(String message) {
        super(message);
    }

    public AbiContextNullError(String message, Exception exception) {
        super(message, exception);
    }

    public AbiContextNullError(Exception exception) {
        super(exception);
    }
}
