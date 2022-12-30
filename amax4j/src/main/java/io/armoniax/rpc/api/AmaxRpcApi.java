package io.armoniax.rpc.api;
import io.armoniax.models.rpc.request.*;
import io.armoniax.models.rpc.response.*;
import io.armoniax.models.rpc.response.account.Account;
import io.armoniax.rpc.api.impl.AmaxRpcImpl;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

import java.util.Map;

/**
 * Retrofit definitions for communication with the EOSIO blockchain.
 */
public interface AmaxRpcApi {

    //region Model supported APIs

    /**
     * Retrofit POST call to "chain/get_info" to an EOSIO blockchain.
     * This method gets called from {@link AmaxRpcImpl#getInfo()} to get latest information about the pointing chain.
     *
     * @return Executable {@link Call} to return {@link ChainInfo} has latest information about a chain.
     */
    @POST("v1/chain/get_info")
    Call<ChainInfo> getInfo();

    /**
     * Retrofit POST call to "chain/get_block" to an EOSIO blockchain.
     * This method gets called from {@link AmaxRpcImpl#getBlock(String)} to get info/status of a specific block in the request.
     *
     * @param fields Info of a specific block.
     * @return Executable {@link Call} to return {@link ChainBlock} has the info/status of a specific block in the request.
     */
    @POST("v1/chain/get_block")
    Call<ChainBlock> getBlock(@Body Map<String,String> fields);

    /**
     * Retrofit POST call to "chain/get_block_info" to an EOSIO blockchain.
     * This method gets called from {@link AmaxRpcImpl#getBlockInfo(BlockInfoRequest)} to get info/status of a specific block in the request.
     *
     * @param blockInfoRequest of a specific block.
     * @return Executable {@link Call} to return {@link ChainBlock} has the info/status of a specific block in the request.
     */
    @POST("v1/chain/get_block_info")
    Call<BlockInfoResponse> getBlockInfo(@Body BlockInfoRequest blockInfoRequest);

    /**
     * Retrofit POST call to "chain/get_raw_abi" to an EOSIO blockchain.
     * This method gets called from {@link AmaxRpcImpl#getRawAbi(RawAbiRequest)} to get serialized ABI of a smart contract in the request.
     *
     * @param rawAbiRequest Info of a specific smart contract.
     * @return Executable {@link Call} to return {@link RawAbiResponse} has the serialized ABI of a smart contract in the request.
     */
    @POST("v1/chain/get_raw_abi")
    Call<RawAbiResponse> getRawAbi(@Body RawAbiRequest rawAbiRequest);

    /**
     * Retrofit POST call to "chain/get_required_keys" to an EOSIO blockchain.
     * This method gets called from {@link AmaxRpcImpl#getRequiredKeys(RequiredKeysRequest)} to get required keys to sign a transaction
     *
     * @param request Info to get required keys
     * @return Executable {@link Call} to return {@link RequiredKeysResponse} has the required keys to sign a transaction
     */
    @POST("v1/chain/get_required_keys")
    Call<RequiredKeysResponse> getRequiredKeys(@Body RequiredKeysRequest request);

    /**
     * Retrofit POST call to "chain/push_transaction" to an EOSIO blockchain.
     * This method gets called from {@link AmaxRpcImpl#pushTransaction(TransactionRequest)} to Push transaction RPC call to broadcast a transaction to backend
     *
     * @param request the transaction to push with signatures.
     * @return Executable {@link Call} to return {@link TransactionResponse} has the push transaction response
     */
    @POST("v1/chain/push_transaction")
    Call<TransactionResponse> pushTransaction(@Body TransactionRequest request);

    /**
     * Retrofit POST call to "chain/send_transaction" to an EOSIO blockchain.
     * This method gets called from {@link AmaxRpcImpl#sendTransaction(TransactionRequest)} to Send transaction RPC call to broadcast a transaction to backend
     *
     * @param transactionRequest the transaction to push with signatures.
     * @return Executable {@link Call} to return {@link TransactionResponse} has the send transaction response
     */
    @POST("v1/chain/send_transaction")
    Call<TransactionResponse> sendTransaction(@Body TransactionRequest transactionRequest);
    //endregion


    //region Extra APIs
    // Chain APIs
    /**
     * Retrofit POST call to "chain/get_account" to an EOSIO blockchain.
     * This method gets called from {@link AmaxRpcImpl#getAccount(String)}
     *
     * @param fields the request body to call 'get_account' API
     * @return Executable {@link Call} to return {@link ResponseBody} of 'get_account' API
     */
    @POST("v1/chain/get_account")
    Call<Account> getAccount(@Body Map<String,String> fields);

    /**
     * Retrofit POST call to "chain/push_transactions" to an EOSIO blockchain.
     * This method gets called from {@link AmaxRpcImpl#pushTransactions(RequestBody)}
     *
     * @param requestBody the request body to call 'push_transactions' API
     * @return Executable {@link Call} to return {@link ResponseBody} of 'push_transactions' API
     */
    @POST("v1/chain/push_transactions")
    Call<ResponseBody> pushTransactions(@Body RequestBody requestBody);

    /**
     * Retrofit POST call to "chain/get_block_header_state" to an EOSIO blockchain.
     * This method get called from {@link AmaxRpcImpl#getBlockHeaderState(RequestBody)}
     *
     * @param requestBody the request body to call 'get_block_header_state' API
     * @return Executable {@link Call} to return {@link ResponseBody} of 'get_block_header_state' API
     */
    @POST("v1/chain/get_block_header_state")
    Call<ResponseBody> getBlockHeaderState(@Body RequestBody requestBody);

    /**
     * Retrofit POST call to "chain/get_abi" to an EOSIO blockchain.
     * This method gets called from {@link AmaxRpcImpl#getAbi(RequestBody)}
     *
     * @param requestBody the request body to call 'get_abi' API
     * @return Executable {@link Call} to return {@link ResponseBody} of 'get_abi' API
     */
    @POST("v1/chain/get_abi")
    Call<ResponseBody> getAbi(@Body RequestBody requestBody);

    /**
     * Retrofit POST call to "chain/get_currency_balance" to an EOSIO blockchain.
     * This method gets called from {@link AmaxRpcImpl#getCurrencyBalance(RequestBody)}
     *
     * @param requestBody the request body to call 'get_currency_balance' API
     * @return Executable {@link Call} to return {@link ResponseBody} of 'get_currency_balance' API
     */
    @POST("v1/chain/get_currency_balance")
    Call<ResponseBody> getCurrencyBalance(@Body RequestBody requestBody);

    /**
     * Retrofit POST call to "chain/get_currency_stats" to an EOSIO blockchain.
     * This method gets called from {@link AmaxRpcImpl#getCurrencyStats(RequestBody)}
     *
     * @param requestBody the request body to call 'get_currency_stats' API
     * @return Executable {@link Call} to return {@link ResponseBody} of 'get_currency_stats' API
     */
    @POST("v1/chain/get_currency_stats")
    Call<ResponseBody> getCurrencyStats(@Body RequestBody requestBody);

    /**
     * Retrofit POST call to "chain/get_producers" to an EOSIO blockchain.
     * This method gets called from {@link AmaxRpcImpl#getProducers(RequestBody)}
     *
     * @param requestBody the request body to call 'get_producers' API
     * @return Executable {@link Call} to return {@link ResponseBody} of 'get_producers' API
     */
    @POST("v1/chain/get_producers")
    Call<ResponseBody> getProducers(@Body RequestBody requestBody);

    /**
     * Retrofit POST call to "chain/get_raw_code_and_abi" to an EOSIO blockchain.
     * This method gets called from {@link AmaxRpcImpl#getRawCodeAndAbi(RequestBody)}
     *
     * @param requestBody the request body to call 'get_raw_code_and_abi' API
     * @return Executable {@link Call} to return {@link ResponseBody} of 'get_raw_code_and_abi' API
     */
    @POST("v1/chain/get_raw_code_and_abi")
    Call<ResponseBody> getRawCodeAndAbi(@Body RequestBody requestBody);

    /**
     * Retrofit POST call to "chain/get_table_by_scope" to an EOSIO blockchain.
     * This method get called from {@link AmaxRpcImpl#getTableByScope(RequestBody)}
     *
     * @param requestBody the request body to call 'get_table_by_scope' API
     * @return Executable {@link Call} to return {@link ResponseBody} of 'get_table_by_scope' API
     */
    @POST("v1/chain/get_table_by_scope")
    Call<ResponseBody> getTableByScope(@Body RequestBody requestBody);

    /**
     * Retrofit POST call to "chain/get_table_rows" to an EOSIO blockchain.
     * This method gets called from {@link AmaxRpcImpl#getTableRows(RequestBody)}
     *
     * @param requestBody the request body to call 'get_table_rows' API
     * @return Executable {@link Call} to return {@link ResponseBody} of 'get_table_rows' API
     */
    @POST("v1/chain/get_table_rows")
    Call<ResponseBody> getTableRows(@Body RequestBody requestBody);

    /**
     * Retrofit POST call to "chain/get_kv_table_rows" to an EOSIO blockchain.
     * This method gets called from {@link AmaxRpcImpl#getKvTableRows(RequestBody)}
     *
     * @param requestBody the request body to call 'get_kv_table_rows' API
     * @return Executable {@link Call} to return {@link ResponseBody} of 'get_table_rows' API
     */
    @POST("v1/chain/get_kv_table_rows")
    Call<ResponseBody> getKvTableRows(@Body RequestBody requestBody);

    /**
     * Retrofit POST call to "chain/get_code" to an EOSIO blockchain.
     * This method gets called from {@link AmaxRpcImpl#getCode(RequestBody)}
     *
     * @param requestBody the request body to call 'get_code' API
     * @return Executable {@link Call} to return {@link ResponseBody} of 'get_code' API
     */
    @POST("v1/chain/get_code")
    Call<ResponseBody> getCode(@Body RequestBody requestBody);

    //History APIs

    /**
     * Retrofit POST call to "chain/get_actions" to an EOSIO blockchain.
     * This method gets called from {@link AmaxRpcImpl#getActions(int, int, String)} ()}
     *
     * @param fields the request body to call 'get_actions' API
     * @return Executable {@link Call} to return {@link ResponseBody} of 'get_actions' API
     */
    @POST("v1/history/get_actions")
    Call<ResponseBody> getActions(@Body Map<String, Object> fields);

    /**
     * Retrofit POST call to "chain/get_transaction" to an EOSIO blockchain.
     * This method gets called from {@link AmaxRpcImpl#getTransaction(String)} ()}
     *
     * @param fields the request body to call 'get_transaction' API
     * @return Executable {@link Call} to return {@link ResponseBody} of 'get_transaction' API
     */
    @POST("v1/history/get_transaction")
    Call<ResponseBody> getTransaction(@Body Map<String, Object> fields);

    /**
     * Retrofit POST call to "chain/get_key_accounts" to an EOSIO blockchain.
     * This method gets called from {@link AmaxRpcImpl#getKeyAccounts(RequestBody)}
     *
     * @param requestBody the request body to call 'get_key_accounts' API
     * @return Executable {@link Call} to return {@link ResponseBody} of 'get_key_accounts' API
     */
    @POST("v1/history/get_key_accounts")
    Call<ResponseBody> getKeyAccounts(@Body RequestBody requestBody);

    /**
     * Retrofit POST call to "chain/get_controlled_accounts" to an EOSIO blockchain.
     * This method gets called from {@link AmaxRpcImpl#getControlledAccounts(RequestBody)}
     *
     * @param requestBody the request body to call 'get_controlled_accounts' API
     * @return Executable {@link Call} to return {@link ResponseBody} of 'get_controlled_accounts' API
     */
    @POST("v1/history/get_controlled_accounts")
    Call<ResponseBody> getControlledAccounts(@Body RequestBody requestBody);

    //endregion
}
