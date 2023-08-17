package io.armoniax.client;

import io.armoniax.error.rpcProvider.GetBlockRpcError;
import io.armoniax.error.rpcProvider.GetInfoRpcError;
import io.armoniax.error.rpcProvider.RpcProviderError;
import io.armoniax.error.session.TransactionPrepareError;
import io.armoniax.error.session.TransactionSignAndBroadCastError;
import io.armoniax.models.rpc.response.ChainBlock;
import io.armoniax.models.rpc.response.ChainInfo;
import io.armoniax.models.rpc.response.CheckerInfo;
import io.armoniax.models.rpc.response.TransactionResponse;
import io.armoniax.models.rpc.response.account.Account;
import io.armoniax.sign.error.ImportKeyError;
import org.jetbrains.annotations.NotNull;

public interface AmaxClient {
    void importKey(@NotNull String privateKey) throws ImportKeyError;

    /**
     * 创建新账号
     * @param option
     * @throws TransactionSignAndBroadCastError
     * @throws TransactionPrepareError
     */
    TransactionResponse createAccount(NewAccountOption option)
            throws TransactionSignAndBroadCastError, TransactionPrepareError;

    // 签名

    /**
     * 获取账号信息
     * @param accountName
     * @return
     * @throws RpcProviderError
     */
    Account getAccount(String accountName) throws RpcProviderError;

    /**
     * 代币转账
     * @param from
     * @param to
     * @param quantity
     * @param memo
     * @throws TransactionSignAndBroadCastError
     * @throws TransactionPrepareError
     */
    TransactionResponse transfer(String from,String to,String quantity,String memo)
            throws TransactionSignAndBroadCastError, TransactionPrepareError;

    /**
     * 购买内存
     * @param payer
     * @param receiver
     * @param bytes
     * @throws TransactionSignAndBroadCastError
     * @throws TransactionPrepareError
     */
    TransactionResponse buyRam(String payer, String receiver, int bytes)
            throws TransactionSignAndBroadCastError, TransactionPrepareError;

    /**
     * 抵押 CPU 和 Net
     * @param from
     * @param receiver
     * @param cpuQuantity
     * @param netQuantity
     * @param transfer
     * @throws TransactionSignAndBroadCastError
     * @throws TransactionPrepareError
     */
    TransactionResponse stakeCpuAndNet(String from,String receiver,String cpuQuantity,String netQuantity,boolean transfer)
            throws TransactionSignAndBroadCastError, TransactionPrepareError;

    String getActions(int pos,int offset,String accountName) throws RpcProviderError;

    TransactionResponse sendTransaction(Trx trx) throws TransactionPrepareError, TransactionSignAndBroadCastError;
    String getTransaction(String id) throws RpcProviderError;


    @NotNull ChainInfo getInfo() throws GetInfoRpcError;

    @NotNull ChainBlock getBlock(String blockNumOrId)
            throws GetBlockRpcError;

    CheckerInfo getAllCheckers() throws RpcProviderError ;

    CheckerInfo getCheckerByUser(String user) throws RpcProviderError;
}
