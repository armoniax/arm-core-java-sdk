package io.armoniax.rpc.api;

import io.armoniax.error.rpcProvider.*;
import io.armoniax.models.rpc.request.BlockInfoRequest;
import io.armoniax.models.rpc.request.TransactionRequest;
import io.armoniax.models.rpc.response.BlockInfoResponse;
import io.armoniax.models.rpc.response.ChainBlock;
import io.armoniax.models.rpc.response.ChainInfo;
import io.armoniax.models.rpc.response.TransactionResponse;
import io.armoniax.models.rpc.response.account.Account;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.http.Body;

public interface AmaxRpc{

    @NotNull ChainInfo getInfo() throws GetInfoRpcError;

    @NotNull ChainBlock getBlock(String blockNumOrId)
            throws GetBlockRpcError;

    Account getAccount(String accountName) throws RpcProviderError;

    String getActions(int pos,int offset,String accountName) throws RpcProviderError;
    String getTransaction(String id) throws RpcProviderError;

    /**
     * This method expects a transaction in JSON format and will attempt to apply it to the blockchain.
     *
     * @param transactionRequest the transaction to push with signatures.
     * @return the send transaction response
     * @throws SendTransactionRpcError thrown if there are any exceptions/backend error during the
     * sendTransaction() process.
     */
    @NotNull
    TransactionResponse sendTransaction(TransactionRequest transactionRequest) throws SendTransactionRpcError;
}
