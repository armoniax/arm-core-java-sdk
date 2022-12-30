package io.armoniax.rpc.api.impl;

import com.google.gson.Gson;
import io.armoniax.client.AmaxOption;
import io.armoniax.error.rpcProvider.*;
import io.armoniax.models.rpc.request.*;
import io.armoniax.models.rpc.response.*;
import io.armoniax.models.rpc.response.account.Account;
import io.armoniax.provider.IRPCProvider;
import io.armoniax.rpc.api.AmaxRpc;
import io.armoniax.rpc.api.AmaxRpcApi;
import io.armoniax.rpc.error.RpcCallError;
import io.armoniax.rpc.error.RpcErrorConstants;
import io.armoniax.rpc.error.RpcInitializerError;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

public final class AmaxRpcImpl implements AmaxRpc, IRPCProvider {
    @NotNull
    private final String baseURL;

    @NotNull
    private final Retrofit retrofit;
    private final OkHttpClient client;

    @NotNull
    private final AmaxRpcApi rpcApi;

    public AmaxRpcImpl(@NotNull AmaxOption option) throws RpcInitializerError {
        this(option.getBaseUrl(), option.isEnableDebug());
    }

    public AmaxRpcImpl(@NotNull String baseURL, boolean enableDebug) throws RpcInitializerError {
        if (baseURL.isEmpty()) {
            throw new RpcInitializerError(RpcErrorConstants.RPC_PROVIDER_BASE_URL_EMPTY);
        }

        this.baseURL = baseURL;
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        if (enableDebug) {
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClient.addInterceptor(httpLoggingInterceptor);
        }
        client = httpClient.build();


        this.retrofit = new Retrofit.Builder()
                .baseUrl(this.baseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        this.rpcApi = this.retrofit.create(AmaxRpcApi.class);
    }

    @NotNull
    private <O> O processCall(Call<O> call) throws Exception {
        Response<O> response = call.execute();
        if (!response.isSuccessful()) {
            String additionalErrInfo = RpcErrorConstants.RPC_PROVIDER_NO_FURTHER_ERROR_INFO;

            RPCResponseError rpcResponseError = null;
            if (response.errorBody() != null) {
                Gson gson = new Gson();
                rpcResponseError = gson.fromJson(response.errorBody().charStream(), RPCResponseError.class);
                if (rpcResponseError == null) {
                    additionalErrInfo = response.errorBody().string();
                } else {
                    additionalErrInfo = RpcErrorConstants.RPC_PROVIDER_SEE_FURTHER_ERROR_INFO;
                }
            }

            String msg = String.format(Locale.getDefault(), RpcErrorConstants.RPC_PROVIDER_BAD_STATUS_CODE_RETURNED,
                    response.code(), response.message(), additionalErrInfo);
            throw new RpcCallError(msg, rpcResponseError);
        }
        if (response.body() == null) {
            throw new RpcCallError(RpcErrorConstants.RPC_PROVIDER_EMPTY_RESPONSE_RETURNED);
        }
        return response.body();
    }

    @Override
    public @NotNull ChainInfo getInfo() throws GetInfoRpcError {
        try {
            Call<ChainInfo> syncCall = this.rpcApi.getInfo();
            return processCall(syncCall);
        } catch (Exception ex) {
            throw new GetInfoRpcError(RpcErrorConstants.RPC_PROVIDER_ERROR_GETTING_CHAIN_INFO,
                    ex);
        }
    }

    @Override
    public @NotNull ChainBlock getBlock(String blockNumOrId)
            throws GetBlockRpcError {
        try {
            Call<ChainBlock> syncCall = this.rpcApi.getBlock(Collections.singletonMap("block_num_or_id", blockNumOrId));
            return processCall(syncCall);
        } catch (Exception ex) {
            throw new GetBlockRpcError(RpcErrorConstants.RPC_PROVIDER_ERROR_GETTING_BLOCK_INFO,
                    ex);
        }
    }

    /**
     * Issue a get_account call to the blockchain and process the response.
     * @param accountName request get_account API
     * @return String content of ResponseBody on successful return.
     * @throws RpcProviderError Thrown if any errors occur calling or processing the request.
     */
    public @NotNull Account getAccount(String accountName) throws RpcProviderError {
        try {
            Call<Account> syncCall = this.rpcApi.getAccount(Collections.singletonMap("account_name", accountName));
            return processCall(syncCall);
        } catch (Exception ex) {
            throw new RpcProviderError(RpcErrorConstants.RPC_PROVIDER_ERROR_GET_ACCOUNT, ex);
        }
    }

    /**
     * Issue a getBlockInfo() call to the blockchain and process the response.
     * @param blockInfoRequest Info about a specific block.
     * @return GetBlockInfoResponse on successful return.
     * @throws GetBlockInfoRpcError Thrown if any errors occur calling or processing the request.
     */
    public @NotNull BlockInfoResponse getBlockInfo(BlockInfoRequest blockInfoRequest)
            throws GetBlockInfoRpcError {
        try {
            Call<BlockInfoResponse> syncCall = this.rpcApi.getBlockInfo(blockInfoRequest);
            return processCall(syncCall);
        } catch (Exception ex) {
            throw new GetBlockInfoRpcError(RpcErrorConstants.RPC_PROVIDER_ERROR_GETTING_BLOCK_INFO,
                    ex);
        }
    }

    /**
     * Issue a getRawAbi() request to the blockchain and process the response.
     * @param rawAbiRequest Info about a specific smart contract.
     * @return GetRawAbiResponse on successful return.
     * @throws GetRawAbiRpcError Thrown if any errors occur calling or processing the request.
     */
    @Override
    public @NotNull RawAbiResponse getRawAbi(RawAbiRequest rawAbiRequest)
            throws GetRawAbiRpcError {
        try {
            Call<RawAbiResponse> syncCall = this.rpcApi.getRawAbi(rawAbiRequest);
            return processCall(syncCall);
        } catch (Exception ex) {
            throw new GetRawAbiRpcError(RpcErrorConstants.RPC_PROVIDER_ERROR_GETTING_RAW_ABI,
                    ex);
        }
    }

    /**
     * Issue a getRequiredKeys() request to the blockchain and process the response.
     * @param getRequiredKeysRequest Info to get required keys
     * @return GetRequiredKeysResponse on successful return.
     * @throws GetRequiredKeysRpcError Thrown if any errors occur calling or processing the request.
     */
    @Override
    public @NotNull RequiredKeysResponse getRequiredKeys(
            RequiredKeysRequest getRequiredKeysRequest) throws GetRequiredKeysRpcError {
        try {
            Call<RequiredKeysResponse> syncCall = this.rpcApi.getRequiredKeys(getRequiredKeysRequest);
            return processCall(syncCall);
        } catch (Exception ex) {
            throw new GetRequiredKeysRpcError(RpcErrorConstants.RPC_PROVIDER_ERROR_GETTING_REQUIRED_KEYS,
                    ex);
        }
    }

    /**
     * Send a given transaction to the blockchain and process the response.
     * @param transactionRequest the transaction to send with signatures.
     * @return SendTransactionResponse on successful return.
     * @throws SendTransactionRpcError Thrown if any errors occur calling or processing the request.
     */
    public @NotNull TransactionResponse sendTransaction(
            TransactionRequest transactionRequest) throws SendTransactionRpcError {
        try {
            Call<TransactionResponse> syncCall = this.rpcApi.sendTransaction(transactionRequest);
            return processCall(syncCall);
        } catch (Exception ex) {
            throw new SendTransactionRpcError(RpcErrorConstants.RPC_PROVIDER_ERROR_SENDING_TRANSACTION,
                    ex);
        }
    }

    /**
     * Push a given transaction to the blockchain and process the response.
     * @param request the transaction to push with signatures.
     * @return PushTransactionResponse on successful return.
     * @throws PushTransactionRpcError Thrown if any errors occur calling or processing the request.
     */
    public @NotNull TransactionResponse pushTransaction(
            TransactionRequest request) throws PushTransactionRpcError {
        try {
            Call<TransactionResponse> syncCall = this.rpcApi.pushTransaction(request);
            return processCall(syncCall);
        } catch (Exception ex) {
            throw new PushTransactionRpcError(RpcErrorConstants.RPC_PROVIDER_ERROR_PUSHING_TRANSACTION,
                    ex);
        }
    }

    /**
     * Issue a pushTransactions() call to the blockchain and process the response.
     * @param requestBody request body of push_transactions API
     * @return String content of ResponseBody on successful return.
     * @throws RpcProviderError Thrown if any errors occur calling or processing the request.
     */
    public @NotNull String pushTransactions(RequestBody requestBody) throws RpcProviderError {
        try {
            Call<ResponseBody> syncCall = this.rpcApi.pushTransactions(requestBody);
            try(ResponseBody responseBody = processCall(syncCall)) {
                return responseBody.string();
            }
        } catch (Exception ex) {
            throw new RpcProviderError(RpcErrorConstants.RPC_PROVIDER_ERROR_PUSHING_TRANSACTIONS, ex);
        }
    }

    /**
     * Issue a getBlockHeaderState() call to the blockchain and process the response.
     * @param requestBody request body of get_block_header_state API
     * @return String content of ResponseBody on successful return.
     * @throws RpcProviderError Thrown if any errors occur calling or processing the request.
     */
    public @NotNull String getBlockHeaderState(RequestBody requestBody) throws RpcProviderError {
        try {
            Call<ResponseBody> syncCall = this.rpcApi.getBlockHeaderState(requestBody);
            try(ResponseBody responseBody = processCall(syncCall)) {
                return responseBody.string();
            }
        } catch (Exception ex) {
            throw new RpcProviderError(RpcErrorConstants.RPC_PROVIDER_ERROR_GET_BLOCK_HEADER_STATE, ex);
        }
    }

    /**
     * Issue a getAbi() call to the blockchain and process the response.
     * @param requestBody request body of get_abi API
     * @return String content of ResponseBody on successful return.
     * @throws RpcProviderError Thrown if any errors occur calling or processing the request.
     */
    public @NotNull String getAbi(RequestBody requestBody) throws RpcProviderError {
        try {
            Call<ResponseBody> syncCall = this.rpcApi.getAbi(requestBody);
            try(ResponseBody responseBody = processCall(syncCall)) {
                return responseBody.string();
            }
        } catch (Exception ex) {
            throw new RpcProviderError(RpcErrorConstants.RPC_PROVIDER_ERROR_GET_ABI, ex);
        }
    }

    /**
     * Issue a getCurrencyBalance call to the blockchain and process the response.
     * @param requestBody request body of get_currency_balance API
     * @return String content of ResponseBody on successful return.
     * @throws RpcProviderError Thrown if any errors occur calling or processing the request.
     */
    public @NotNull String getCurrencyBalance(RequestBody requestBody) throws RpcProviderError {
        try {
            Call<ResponseBody> syncCall = this.rpcApi.getCurrencyBalance(requestBody);
            try(ResponseBody responseBody = processCall(syncCall)) {
                return responseBody.string();
            }
        } catch (Exception ex) {
            throw new RpcProviderError(RpcErrorConstants.RPC_PROVIDER_ERROR_GET_CURRENT_BALANCE, ex);
        }
    }

    /**
     * Issue a getCurrencyStats() call to the blockchain and process the response.
     * @param requestBody request body of get_currency_stats API
     * @return String content of ResponseBody on successful return.
     * @throws RpcProviderError Thrown if any errors occur calling or processing the request.
     */
    public @NotNull String getCurrencyStats(RequestBody requestBody) throws RpcProviderError {
        try {
            Call<ResponseBody> syncCall = this.rpcApi.getCurrencyStats(requestBody);
            try(ResponseBody responseBody = processCall(syncCall)) {
                return responseBody.string();
            }
        } catch (Exception ex) {
            throw new RpcProviderError(RpcErrorConstants.RPC_PROVIDER_ERROR_GET_CURRENT_STATS, ex);
        }
    }

    /**
     * Issue a getProducers() call to the blockchain and process the response.
     * @param requestBody request body of get_producers API
     * @return String content of ResponseBody on successful return.
     * @throws RpcProviderError Thrown if any errors occur calling or processing the request.
     */
    public @NotNull String getProducers(RequestBody requestBody) throws RpcProviderError {
        try {
            Call<ResponseBody> syncCall = this.rpcApi.getProducers(requestBody);
            try(ResponseBody responseBody = processCall(syncCall)) {
                return responseBody.string();
            }
        } catch (Exception ex) {
            throw new RpcProviderError(RpcErrorConstants.RPC_PROVIDER_ERROR_GET_PRODUCERS, ex);
        }
    }

    /**
     * Issue a getRawCodeAndAbi() call to the blockchain and process the response.
     * @param requestBody request body of get_raw_code_and_abi API
     * @return String content of ResponseBody on successful return.
     * @throws RpcProviderError Thrown if any errors occur calling or processing the request.
     */
    public @NotNull String getRawCodeAndAbi(RequestBody requestBody) throws RpcProviderError {
        try {
            Call<ResponseBody> syncCall = this.rpcApi.getRawCodeAndAbi(requestBody);
            try(ResponseBody responseBody = processCall(syncCall)) {
                return responseBody.string();
            }
        } catch (Exception ex) {
            throw new RpcProviderError(RpcErrorConstants.RPC_PROVIDER_ERROR_GET_RAW_CODE_AND_ABI, ex);
        }
    }

    /**
     * Issue a getTableByScope() call to the blockchain and process the response.
     * @param requestBody request body of get_table_by_scope API
     * @return String content of ResponseBody on successful return.
     * @throws RpcProviderError Thrown if any errors occur calling or processing the request.
     */
    public @NotNull String getTableByScope(RequestBody requestBody) throws RpcProviderError {
        try {
            Call<ResponseBody> syncCall = this.rpcApi.getTableByScope(requestBody);
            try(ResponseBody responseBody = processCall(syncCall)) {
                return responseBody.string();
            }
        } catch (Exception ex) {
            throw new RpcProviderError(RpcErrorConstants.RPC_PROVIDER_ERROR_GET_TABLE_BY_SCOPE, ex);
        }
    }

    /**
     * Issue a getTableRows() call to the blockchain and process the response.
     * @param requestBody request body of get_table_rows API
     * @return String content of ResponseBody on successful return.
     * @throws RpcProviderError Thrown if any errors occur calling or processing the request.
     */
    public @NotNull String getTableRows(RequestBody requestBody) throws RpcProviderError {
        try {
            Call<ResponseBody> syncCall = this.rpcApi.getTableRows(requestBody);
            try(ResponseBody responseBody = processCall(syncCall)) {
                return responseBody.string();
            }
        } catch (Exception ex) {
            throw new RpcProviderError(RpcErrorConstants.RPC_PROVIDER_ERROR_GET_TABLE_ROWS, ex);
        }
    }

    /**
     * Issue a getKvTableRows() call to the blockchain and process the response.
     * @param requestBody request body of get_kv_table_rows API
     * @return String content of ResponseBody on successful return.
     * @throws RpcProviderError Thrown if any errors occur calling or processing the request.
     */
    public @NotNull String getKvTableRows(RequestBody requestBody) throws RpcProviderError {
        try {
            Call<ResponseBody> syncCall = this.rpcApi.getKvTableRows(requestBody);
            try(ResponseBody responseBody = processCall(syncCall)) {
                return responseBody.string();
            }
        } catch (Exception ex) {
            throw new RpcProviderError(RpcErrorConstants.RPC_PROVIDER_ERROR_GET_KV_TABLE_ROWS, ex);
        }
    }

    /**
     * Issue a getCode() call to the blockchain and process the response.
     * @param requestBody request body of get_code API
     * @return String content of ResponseBody on successful return.
     * @throws RpcProviderError Thrown if any errors occur calling or processing the request.
     */
    public @NotNull String getCode(RequestBody requestBody) throws RpcProviderError {
        try {
            Call<ResponseBody> syncCall = this.rpcApi.getCode(requestBody);
            try(ResponseBody responseBody = processCall(syncCall)) {
                return responseBody.string();
            }
        } catch (Exception ex) {
            throw new RpcProviderError(RpcErrorConstants.RPC_PROVIDER_ERROR_GET_CODE, ex);
        }
    }

    /**
     * Issue a getActions() call to the blockchain and process the response.
     * @param  pos,offset, accountName request body of get_actions API
     * @return String content of ResponseBody on successful return.
     * @throws RpcProviderError Thrown if any errors occur calling or processing the request.
     */
    @Override
    public String getActions(int pos, int offset, String accountName) throws RpcProviderError {
        try {
            HashMap<String, Object> requestBody = new HashMap<>();
            requestBody.put("pos",pos);
            requestBody.put("offset",offset);
            requestBody.put("account_name",accountName);
            Call<ResponseBody> syncCall = this.rpcApi.getActions(requestBody);
            try(ResponseBody responseBody = processCall(syncCall)) {
                return responseBody.string();
            }
        } catch (Exception ex) {
            throw new RpcProviderError(RpcErrorConstants.RPC_PROVIDER_ERROR_GET_ACTION, ex);
        }
    }

    /**
     * Issue a getTransaction() call to the blockchain and process the response.
     * @param id request body of get_transaction API
     * @return String content of ResponseBody on successful return.
     * @throws RpcProviderError Thrown if any errors occur calling or processing the request.
     */
    public @NotNull String getTransaction(String id) throws RpcProviderError {
        try {
            Call<ResponseBody> syncCall = this.rpcApi.getTransaction(Collections.singletonMap("id",id));
            try(ResponseBody responseBody = processCall(syncCall)) {
                return responseBody.string();
            }
        } catch (Exception ex) {
            throw new RpcProviderError(RpcErrorConstants.RPC_PROVIDER_ERROR_GET_TRANSACTION, ex);
        }
    }

    /**
     * Issue a getKeyAccounts() call to the blockchain and process the response.
     * @param requestBody request body of get_key_accounts API
     * @return String content of ResponseBody on successful return.
     * @throws RpcProviderError Thrown if any errors occur calling or processing the request.
     */
    public @NotNull String getKeyAccounts(RequestBody requestBody) throws RpcProviderError {
        try {
            Call<ResponseBody> syncCall = this.rpcApi.getKeyAccounts(requestBody);
            try(ResponseBody responseBody = processCall(syncCall)) {
                return responseBody.string();
            }
        } catch (Exception ex) {
            throw new RpcProviderError(RpcErrorConstants.RPC_PROVIDER_ERROR_GET_KEY_ACCOUNTS, ex);
        }
    }

    /**
     * Issue a getControlledAccounts() call to the blockchain and process the response.
     * @param requestBody request body of get_controlled_accounts API
     * @return String content of ResponseBody on successful return.
     * @throws RpcProviderError Thrown if any errors occur calling or processing the request.
     */
    public @NotNull String getControlledAccounts(RequestBody requestBody) throws RpcProviderError {
        try {
            Call<ResponseBody> syncCall = this.rpcApi.getControlledAccounts(requestBody);
            try(ResponseBody responseBody = processCall(syncCall)) {
                return responseBody.string();
            }
        } catch (Exception ex) {
            throw new RpcProviderError(RpcErrorConstants.RPC_PROVIDER_ERROR_GET_CONTROLLED_ACCOUNTS, ex);
        }
    }
}
