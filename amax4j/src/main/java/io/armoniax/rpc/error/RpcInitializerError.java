package io.armoniax.rpc.error;

import io.armoniax.error.rpcProvider.RpcProviderError;
import org.jetbrains.annotations.NotNull;

//
// Copyright © 2017-2019 block.one.
//

/**
 * Error thrown when there is an issue initializing the RPC Provider.
 */
public class RpcInitializerError extends RpcProviderError {

    public RpcInitializerError() {
    }

    public RpcInitializerError(
            @NotNull String message) {
        super(message);
    }

    public RpcInitializerError(
            @NotNull String message,
            @NotNull Exception exception) {
        super(message, exception);
    }

    public RpcInitializerError(
            @NotNull Exception exception) {
        super(exception);
    }

}
