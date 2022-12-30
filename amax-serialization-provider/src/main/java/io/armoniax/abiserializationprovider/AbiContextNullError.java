package io.armoniax.abiserializationprovider;

import io.armoniax.error.serializationProvider.SerializationProviderError;
import org.jetbrains.annotations.NotNull;

/**
 * Error class is used when there is an exception when the ABIEOS c++ code attempts to create its
 * working context during initialization.
 */

public class AbiContextNullError extends SerializationProviderError {

    public AbiContextNullError() {
    }

    public AbiContextNullError(@NotNull String message) {
        super(message);
    }

    public AbiContextNullError(@NotNull String message,
                               @NotNull Exception exception) {
        super(message, exception);
    }

    public AbiContextNullError(@NotNull Exception exception) {
        super(exception);
    }
}
