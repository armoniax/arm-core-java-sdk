package io.armoniax.provider;

import io.armoniax.error.rpcProvider.*;
import io.armoniax.models.rpc.request.RawAbiRequest;
import io.armoniax.models.rpc.request.RequiredKeysRequest;
import io.armoniax.models.rpc.response.*;
import io.armoniax.rpc.api.AmaxRpc;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;

/**
 * The interface of an RPC provider.
 */
public interface IRPCProvider extends AmaxRpc {

    @NotNull String getTableRows(RequestBody requestBody) throws RpcProviderError;


    /**
     * Gets raw abi for a given contract.
     *
     * @param rawAbiRequest Info of a specific smart contract.
     * @return the serialized ABI of a smart contract in the request.
     * @throws GetRawAbiRpcError thrown if there are any exceptions/backend error during the
     * getRawAbi() process.
     */
    @NotNull
    RawAbiResponse getRawAbi(RawAbiRequest rawAbiRequest) throws GetRawAbiRpcError;

    /**
     * Returns the required keys needed to sign a transaction.
     *
     * @param getRequiredKeysRequest Info to get required keys
     * @return the required keys to sign a transaction
     * @throws GetRequiredKeysRpcError thrown if there are any exceptions/backend error during the
     * getRequiredKeys() process.
     */
    @NotNull
    RequiredKeysResponse getRequiredKeys(RequiredKeysRequest getRequiredKeysRequest) throws GetRequiredKeysRpcError;
}
