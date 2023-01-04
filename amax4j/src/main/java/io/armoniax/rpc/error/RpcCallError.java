package io.armoniax.rpc.error;

import io.armoniax.error.rpcProvider.RpcProviderError;
import io.armoniax.models.rpc.response.RPCResponseError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//
// Copyright © 2017-2019 block.one.
//

/**
 * Error thrown when there is a lower level processing error sending or receiving an RPC call.
 */
public class RpcCallError extends RpcProviderError {

    /**
     * Contains additional information about errors coming back from the blockchain, if available.
     */
    @Nullable
    private RPCResponseError rpcResponseError;

    public RpcCallError() {
    }

    public RpcCallError(@NotNull String message) {
        super(message);
    }

    public RpcCallError(@NotNull String message,
                        @Nullable RPCResponseError rpcResponseError) {
        super(message);
        this.rpcResponseError = rpcResponseError;
    }

    public RpcCallError(@NotNull String message,
                        @NotNull Exception exception) {
        super(message, exception);
    }

    public RpcCallError(
            @NotNull Exception exception) {
        super(exception);
    }

    public RpcCallError(@NotNull String message,
                        @NotNull Exception exception,
                        @Nullable RPCResponseError rpcRepsonseError) {
        super(message, exception);
        this.rpcResponseError = rpcRepsonseError;
    }

    public RPCResponseError getRpcResponseError() {
        return rpcResponseError;
    }

    public void setRpcResponseError(
            RPCResponseError rpcResponseError) {
        this.rpcResponseError = rpcResponseError;
    }
}
