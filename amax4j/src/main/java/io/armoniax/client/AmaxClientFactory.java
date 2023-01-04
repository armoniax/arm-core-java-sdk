package io.armoniax.client;

import io.armoniax.error.serializationProvider.SerializationProviderError;
import io.armoniax.impl.AmaxClientImpl;
import io.armoniax.rpc.error.RpcInitializerError;
import io.armoniax.sign.error.ImportKeyError;

public class AmaxClientFactory {
    public static AmaxClient getAmaxClient(AmaxOption option) throws RpcInitializerError, SerializationProviderError, ImportKeyError {
        return new AmaxClientImpl(option);
    }
}
