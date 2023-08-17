package io.armoniax.impl;

import com.google.gson.Gson;
import io.armoniax.client.AmaxClient;
import io.armoniax.client.AmaxOption;
import io.armoniax.client.NewAccountOption;
import io.armoniax.client.Trx;
import io.armoniax.enums.AmaxSignKind;
import io.armoniax.error.rpcProvider.GetBlockRpcError;
import io.armoniax.error.rpcProvider.GetInfoRpcError;
import io.armoniax.error.rpcProvider.RpcProviderError;
import io.armoniax.error.session.TransactionPrepareError;
import io.armoniax.error.session.TransactionSignAndBroadCastError;
import io.armoniax.models.NewAccount;
import io.armoniax.models.rpc.Action;
import io.armoniax.models.rpc.Authorization;
import io.armoniax.models.rpc.TransactionConfig;
import io.armoniax.models.rpc.response.*;
import io.armoniax.models.rpc.response.account.Account;
import io.armoniax.provider.IABIProvider;
import io.armoniax.provider.IRPCProvider;
import io.armoniax.provider.ISerializationProvider;
import io.armoniax.provider.ISignatureProvider;
import io.armoniax.rpc.api.impl.AmaxRpcImpl;
import io.armoniax.rpc.error.RpcInitializerError;
import io.armoniax.session.TransactionSession;
import io.armoniax.sign.SoftSignatureProviderImpl;
import io.armoniax.session.TransactionProcessor;
import io.armoniax.sign.error.ImportKeyError;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class AmaxClientImpl implements AmaxClient {
    private final IRPCProvider rpcProvider;
    private final ISerializationProvider serialProvider;
    private final ISignatureProvider signProvider;
    private final IABIProvider abiProvider;
    private final AmaxOption option;

    public AmaxClientImpl(AmaxOption option) throws RpcInitializerError {
        this.option = option;
        rpcProvider = new AmaxRpcImpl(option);
        serialProvider = option.getSerialProvider();
        if(option.getSignKind() == AmaxSignKind.SOFT){
            signProvider = new SoftSignatureProviderImpl();
        }else {
            signProvider = option.getSignProvider();
        }
        abiProvider = new ABIProviderImpl(rpcProvider, serialProvider);
    }

    @Override
    public void importKey(@NotNull String privateKey) throws ImportKeyError{
        if(option.getSignKind() == AmaxSignKind.SOFT){
            ((SoftSignatureProviderImpl)signProvider).importKey(privateKey);
        }
    }

    @Override
    public TransactionResponse sendTransaction(Trx trx) throws TransactionPrepareError, TransactionSignAndBroadCastError {
        TransactionSession session = new TransactionSession(serialProvider, rpcProvider, abiProvider, signProvider);

        TransactionProcessor processor = session.getTransactionProcessor();
        TransactionConfig config = processor.getTransactionConfig();
        config.setUseLastIrreversible(trx.isUseLastIrreversible());
        config.setExpiresSeconds(trx.getExpiresSeconds());
        config.setBlocksBehind(trx.getBlocksBehind());
        processor.setTransactionConfig(config);

        processor.prepare(trx.getActions());
        return processor.signAndBroadcast();
    }

    @Override
    public TransactionResponse createAccount(NewAccountOption option) throws TransactionSignAndBroadCastError, TransactionPrepareError {
        String creator = option.getCreator();
        String newAccount = option.getNewAccount();

        List<Authorization> auth = new ArrayList<>();
        auth.add(new Authorization(creator, "active"));
        List<Action> actions = new ArrayList<>();
        actions.add(new Action("amax", "newaccount", auth, new Gson().toJson(NewAccount.getNewAccount(creator, newAccount, option.getOwnerPubKey(), option.getActivePubKey()))));

        String buyRam = createBuyRamData(creator, newAccount, option.getBuyRamBytes());
        String buyCPUAndNet = createStakeData(creator, newAccount, option.getStakeCpuQuantity(), option.getStakeNetQuantity(), option.isTransfer());

        actions.add(new Action("amax", "buyrambytes", auth, buyRam));
        actions.add(new Action("amax", "delegatebw", auth, buyCPUAndNet));

        return sendTransaction(Trx.builder().setActions(actions).build());
    }


    @Override
    public Account getAccount(String accountName) throws RpcProviderError {
        return rpcProvider.getAccount(accountName);
    }

    @Override
    public CheckerInfo getAllCheckers() throws RpcProviderError {
        HashMap<String, Object> jsonObj = new HashMap<>();
        jsonObj.put("code","realme.dao");
        jsonObj.put("table","auditconf");
        jsonObj.put("scope","realme.dao");

        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                new Gson().toJson(jsonObj));

        String jsonResult = rpcProvider.getTableRows(requestBody);
        return new Gson().fromJson(jsonResult, CheckerInfo.class);
    }

    @Override
    public CheckerInfo getCheckerByUser(String user) throws RpcProviderError {
        HashMap<String, Object> json = new HashMap<>();
        json.put("code","realme.dao");
        json.put("table","regauths");
        json.put("scope",user);

        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                new Gson().toJson(json));

        String jsonResult = rpcProvider.getTableRows(requestBody);
        return new Gson().fromJson(jsonResult, CheckerInfo.class);
    }

    @Override
    public TransactionResponse transfer(String from, String to, String quantity, String memo) throws TransactionSignAndBroadCastError, TransactionPrepareError {
        String data = "{" +
                "\"from\":" + quot(from) + "," +
                "\"to\":" + quot(to) + "," +
                "\"quantity\":" + quot(quantity) + "," +
                "\"memo\":" + quot(memo) +
                "}";

        List<Action> actions = Collections.singletonList(
                new Action("amax.token", "transfer",
                        Collections.singletonList(
                                new Authorization(from, "active")),
                        data));

        return sendTransaction(Trx.builder().setExpiresSeconds(600).setUseLastIrreversible(false).setActions(actions).build());
    }

    @Override
    public TransactionResponse buyRam(String payer, String receiver, int bytes) throws TransactionSignAndBroadCastError, TransactionPrepareError {
        String data = createBuyRamData(payer, receiver, bytes);
        List<Action> actions = Collections.singletonList(
                new Action("amax", "buyrambytes",
                        Collections.singletonList(
                                new Authorization(payer, "active")), data)
        );
        return sendTransaction(Trx.builder().setActions(actions).build());
    }

    @Override
    public TransactionResponse stakeCpuAndNet(String from, String receiver, String cpuQuantity, String netQuantity, boolean transfer) throws TransactionSignAndBroadCastError, TransactionPrepareError {
        String data = createStakeData(from, receiver, cpuQuantity, netQuantity, transfer);
        List<Action> actions = Collections.singletonList(
                new Action("amax", "delegatebw",
                        Collections.singletonList(
                                new Authorization(from, "active")), data)
        );
        return sendTransaction(Trx.builder().setActions(actions).build());
    }

    @Override
    public String getActions(int pos, int offset, String accountName) throws RpcProviderError {
        return rpcProvider.getActions(pos,offset,accountName);
    }

    @Override
    public String getTransaction(String id) throws RpcProviderError {
        return rpcProvider.getTransaction(id);
    }

    @Override
    public @NotNull ChainInfo getInfo() throws GetInfoRpcError {
        return rpcProvider.getInfo();
    }

    @Override
    public @NotNull ChainBlock getBlock(String blockNumOrId) throws GetBlockRpcError {
        return rpcProvider.getBlock(blockNumOrId);
    }

    private StringBuilder quot(String str) {
        return new StringBuilder().append("\"").append(str).append("\"");
    }

    private String createBuyRamData(String payer, String receiver, int bytes) {
        return "{" +
                "\"payer\":" + quot(payer) + "," +
                "\"receiver\":" + quot(receiver) + "," +
                "\"bytes\":" + bytes +
                "}";
    }

    private String createStakeData(String from, String receiver,
                                   String cpuQuantity, String netQuantity,
                                   boolean transfer) {
        return "{" +
                "\"from\":" + quot(from) + "," +
                "\"receiver\":" + quot(receiver) + "," +
                "\"stake_cpu_quantity\":" + quot(cpuQuantity) + "," +
                "\"stake_net_quantity\":" + quot(netQuantity) + "," +
                "\"transfer\":" + transfer +
                "}";
    }
}
