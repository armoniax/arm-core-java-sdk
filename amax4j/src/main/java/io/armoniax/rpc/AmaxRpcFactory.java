package io.armoniax.rpc;

import io.armoniax.client.AmaxOption;
import io.armoniax.rpc.api.AmaxRpc;
import io.armoniax.rpc.api.impl.AmaxRpcImpl;
import io.armoniax.rpc.error.RpcInitializerError;

public class AmaxRpcFactory {
    public static AmaxRpc getAmaxRpc(String baseUrl) throws RpcInitializerError {
        return getAmaxRpc(baseUrl,false);
    }

    public static AmaxRpc getAmaxRpc(String baseUrl,boolean enableDebug) throws RpcInitializerError {
        return new AmaxRpcImpl(AmaxOption.builder()
                .setUrl(baseUrl)
                .setEnableDebug(enableDebug)
                .build());
    }
}
