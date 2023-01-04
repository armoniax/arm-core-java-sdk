package io.armoniax.session;

import com.google.common.base.Strings;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import io.armoniax.error.ErrorConstants;
import io.armoniax.error.abiProvider.GetAbiError;
import io.armoniax.error.rpcProvider.*;
import io.armoniax.error.serializationProvider.DeserializeTransactionError;
import io.armoniax.error.serializationProvider.SerializeError;
import io.armoniax.error.serializationProvider.SerializePackedTransactionError;
import io.armoniax.error.serializationProvider.SerializeTransactionError;
import io.armoniax.error.session.TransactionBroadCastEmptySignatureError;
import io.armoniax.error.session.TransactionBroadCastError;
import io.armoniax.error.session.TransactionCreateSignatureRequestAbiError;
import io.armoniax.error.session.TransactionCreateSignatureRequestEmptyAvailableKeyError;
import io.armoniax.error.session.TransactionCreateSignatureRequestError;
import io.armoniax.error.session.TransactionCreateSignatureRequestKeyError;
import io.armoniax.error.session.TransactionCreateSignatureRequestRequiredKeysEmptyError;
import io.armoniax.error.session.TransactionCreateSignatureRequestRpcError;
import io.armoniax.error.session.TransactionCreateSignatureRequestSerializationError;
import io.armoniax.error.session.TransactionGetSignatureDeserializationError;
import io.armoniax.error.session.TransactionGetSignatureError;
import io.armoniax.error.session.TransactionGetSignatureNotAllowModifyTransactionError;
import io.armoniax.error.session.TransactionGetSignatureSigningError;
import io.armoniax.error.session.TransactionPrepareError;
import io.armoniax.error.session.TransactionPrepareInputError;
import io.armoniax.error.session.TransactionPrepareRpcError;
import io.armoniax.error.session.TransactionProcessorConstructorInputError;
import io.armoniax.error.session.TransactionSendTransactionError;
import io.armoniax.error.session.TransactionSerializeError;
import io.armoniax.error.session.TransactionSignAndBroadCastError;
import io.armoniax.error.session.TransactionSignError;
import io.armoniax.error.signatureProvider.GetAvailableKeysError;
import io.armoniax.error.signatureProvider.SignatureProviderError;
import io.armoniax.models.AbiSerializationObject;
import io.armoniax.models.AMAXName;
import io.armoniax.models.rpc.Action;
import io.armoniax.models.rpc.Transaction;
import io.armoniax.models.rpc.ContextFreeData;
import io.armoniax.models.rpc.TransactionConfig;
import io.armoniax.models.rpc.request.RequiredKeysRequest;
import io.armoniax.models.rpc.request.TransactionRequest;
import io.armoniax.models.rpc.response.*;
import io.armoniax.models.signature.TransactionSignatureRequest;
import io.armoniax.models.signature.TransactionSignatureResponse;
import io.armoniax.provider.IABIProvider;
import io.armoniax.provider.IRPCProvider;
import io.armoniax.provider.ISerializationProvider;
import io.armoniax.provider.ISignatureProvider;
import io.armoniax.tools.DateFormatter;
import io.armoniax.tools.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Wrapper class for an EOS Transaction.  This class allows the developer to use the Transaction
 * object in several ways.
 * <p>
 * The TransactionProcessor allows the developer to:
 * <p>
 * - Get input {@link io.armoniax.models.rpc.Action}
 * <p>
 * - Create transaction
 * <p>
 * - Serialize Transaction
 * <p>
 * - Sign Transaction
 * <p>
 * - Broadcast Transaction
 */
public class TransactionProcessor {

    /**
     * Reference of Serialization Provider from TransactionSession
     */
    @NotNull
    private ISerializationProvider serializationProvider;

    /**
     * Reference of RPC Provider from TransactionSession
     */
    @NotNull
    private IRPCProvider rpcProvider;

    /**
     * Reference of ABI Provider from TransactionSession
     */
    @NotNull
    private IABIProvider abiProvider;

    /**
     * Reference of Signature Provider from TransactionSession
     */
    @NotNull
    private ISignatureProvider signatureProvider;

    /**
     * Transaction instance that holds all data relating to an EOS Transaction.
     * <p>
     * This object holds the non-serialized version of the transaction.  However, the serialized
     * version can be extracted by calling the serialize() {@link TransactionProcessor#serialize()}
     * method.
     */
    @Nullable
    private Transaction transaction;

    /**
     * This is an original instance of the transaction that is populated once the signature
     * provider returns the signed transaction.
     * Check getSignature() flow in "complete workflow" doc for more detail
     */
    @Nullable
    private Transaction originalTransaction;

    /**
     * Context Free Data instance that holds all raw, serialized, and packed CFD.
     */
    @NotNull
    private ContextFreeData contextFreeData;

    /**
     * List of signatures used to sign the transaction.  This is populated after the transaction
     * has been signed and the signatures have been return by the signature provider.
     * <p>
     * Check getSignature() flow in "Complete Workflow" document for more detail.
     */
    @NotNull
    private List<String> signatures = new ArrayList<>();

    /**
     * The serialized version of the Transaction that is used to determine whether or not the
     * signature provider modified the transaction.
     * <p>
     *     If the transaction is updated, this object needs to be cleared or updated.
     * <p>
     *     Check getSignature() flow in "Complete Workflow" document for more detail
     * about the value assigned and usages.
     */
    @Nullable
    private String serializedTransaction;

    /**
     * List of available keys that may be provided by SignatureProvider.
     * <p>
     * If this list is already set, the TransactionProcessor won't ask for available keys from
     * the signature provider and use this list.
     * <p> Check createSignatureRequest() flow in "Complete Workflow" doc for more
     * detail.
     */
    @Nullable
    private List<String> availableKeys;

    /**
     * List of required keys to sign the transaction.  See
     * {@link IRPCProvider#getRequiredKeys(RequiredKeysRequest)}
     * <p>
     * If this list is set, TransactionProcessor will use this list instead of a making an RPC
     * call to get the required keys.
     * <p>
     * Check the createSignatureRequest() flow in "Complete Workflow" doc for more detail.
     */
    @Nullable
    private List<String> requiredKeys;

    /**
     * Configuration for transaction which offers ability to set:
     * <p>
     * - The expiration period for the transaction in seconds
     * <p>
     * - Whether to use the last irreversible block or
     * <p>
     * - How many blocks behind the head block
     */
    @NotNull
    private TransactionConfig transactionConfig = new TransactionConfig();

    /**
     * Chain id of target blockchain that will be used in createSignatureRequest()
     * {@link TransactionProcessor#createSignatureRequest()}
     */
    @Nullable
    private String chainId;

    /**
     * Whether or not to allow the signature provider to modify the transaction.
     * <p>
     * Default is false.
     */
    private boolean isTransactionModificationAllowed;

    /**
     * Prefix for packed transaction v0
     */
    private static final String PACKED_TRANSACTION_V0_PREFIX = "00";

    /**
     * Constructor with all provider references from {@link TransactionSession}
     * @param serializationProvider the serialization provider.
     * @param rpcProvider the rpc provider.
     * @param abiProvider the abi provider.
     * @param signatureProvider the signature provider.
     */
    public TransactionProcessor(
            @NotNull ISerializationProvider serializationProvider,
            @NotNull IRPCProvider rpcProvider,
            @NotNull IABIProvider abiProvider,
            @NotNull ISignatureProvider signatureProvider) {
        this.serializationProvider = serializationProvider;
        this.rpcProvider = rpcProvider;
        this.abiProvider = abiProvider;
        this.signatureProvider = signatureProvider;
    }

    /**
     * Constructor with all provider references from {@link TransactionSession} and preset
     * Transaction
     * @param serializationProvider the serialization provider.
     * @param rpcProvider the rpc provider.
     * @param abiProvider the abi provider.
     * @param signatureProvider the signature provider.
     * @param transaction - preset Transaction
     * @throws TransactionProcessorConstructorInputError thrown if the input transaction has an empty action list.
     */
    public TransactionProcessor(
            @NotNull ISerializationProvider serializationProvider,
            @NotNull IRPCProvider rpcProvider,
            @NotNull IABIProvider abiProvider,
            @NotNull ISignatureProvider signatureProvider,
            @NotNull Transaction transaction) throws TransactionProcessorConstructorInputError {
        this(serializationProvider, rpcProvider, abiProvider, signatureProvider);
        this.transaction = transaction;
        if (this.transaction.getActions().isEmpty()) {
            throw new TransactionProcessorConstructorInputError(
                    ErrorConstants.TRANSACTION_PROCESSOR_ACTIONS_EMPTY_ERROR_MSG);
        }
    }

    //region public methods

    /**
     * Prepare action's data from input and create new instance of Transaction if it is not set.
     * <p>
     *     Use this method if you don't want to provide context free actions or context free data.
     * <p>
     * Check prepare() flow in "Complete Workflow" doc for more detail.
     *
     * @param actions - List of actions with data. If the transaction is preset or has a value and it
     * has its own actions, that list will be over-ridden by this input list.
     * @throws TransactionPrepareError thrown if:
     *          <br>
     *              - chainId from {@link IRPCProvider#getInfo()} is blank
     *          <br>
     *              - chainId returned from the chain does not match with input chainId
     *          <br>
     *              - There is a problem with parsing head block time from {@link ChainInfo#getHeadBlockTime()}
     *          <br>
     *          It throws a base error class if:
     *          <br>
     *              {@link TransactionPrepareInputError} thrown if input is invalid
     *              <br>
     *              {@link TransactionPrepareRpcError} thrown if any RPC call ({@link IRPCProvider#getInfo()})
     *              return or throw an error
     */
    public void prepare(@NotNull List<Action> actions) throws TransactionPrepareError {
        this.prepare(actions, new ArrayList<>());
    }

    /**
     * Prepare action's data from input and create new instance of Transaction if it is not set.
     * <p>
     *     Use this method if you don't want to provide context free data.
     * <p>
     * Check prepare() flow in "Complete Workflow" doc for more detail.
     *
     * @param actions - List of actions with data. If the transaction is preset or has a value and it
     * has its own actions, that list will be over-ridden by this input list.
     * @param contextFreeActions - List of context free actions with data.
     * @throws TransactionPrepareError thrown if:
     *          <br>
     *              - chainId from {@link IRPCProvider#getInfo()} is blank
     *          <br>
     *              - chainId returned from the chain does not match with input chainId
     *          <br>
     *              - There is a problem with parsing head block time from {@link ChainInfo#getHeadBlockTime()}
     *          <br>
     *          It throws a base error class if:
     *          <br>
     *              {@link TransactionPrepareInputError} thrown if input is invalid
     *              <br>
     *              {@link TransactionPrepareRpcError} thrown if any RPC call ({@link IRPCProvider#getInfo()})
     *              return or throw an error
     */
    public void prepare(@NotNull List<Action> actions, @NotNull List<Action> contextFreeActions) throws TransactionPrepareError {
        prepare(actions, contextFreeActions, new ArrayList<>());
    }

    /**
     * Prepare action's data from input and create new instance of Transaction if it is not set.
     * <p>
     * Check prepare() flow in "Complete Workflow" doc for more detail
     *
     * @param actions - List of actions with data. If the transaction is preset or has a value and it has its own actions, that list will be over-ridden by this input list.
     * @param contextFreeActions - List of context free actions with data.
     * @param contextFreeData - List of context free data as raw strings.
     *
     * @throws TransactionPrepareError thrown if:
     *          <br>
     *              - chainId from {@link IRPCProvider#getInfo()} is blank
     *          <br>
     *              - chainId returned from the chain does not match with input chainId
     *          <br>
     *              - There is a problem with parsing head block time from {@link ChainInfo#getHeadBlockTime()}
     *          <br>
     *          It throws a base error class if:
     *          <br>
     *              {@link TransactionPrepareInputError} thrown if inputs are invalid
     *              <br>
     *              {@link TransactionPrepareRpcError} thrown if any RPC call ({@link IRPCProvider#getInfo()})
     *              return or throw an error
     */
    public void prepare(@NotNull List<Action> actions, @NotNull List<Action> contextFreeActions, @NotNull List<String> contextFreeData) throws TransactionPrepareError {
        if (actions.isEmpty()) {
            throw new TransactionPrepareInputError(
                    ErrorConstants.TRANSACTION_PROCESSOR_ACTIONS_EMPTY_ERROR_MSG);
        }

        /*
         Create new instance of Transaction.
         Final value will be assigned to transaction object in class level and override preset
         Transaction if it was set by constructor.  Modifying a new transaction avoids corrupting the
         original if an exception is encountered during the modification process.
        */
        Transaction preparingTransaction = new Transaction("", BigInteger.ZERO, BigInteger.ZERO,
                BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, contextFreeActions, actions,
                new ArrayList<>());

        ContextFreeData preparingContextFreeData = new ContextFreeData(contextFreeData);

        // Assigning values for transaction expiration, refBlockNum and refBlockPrefix
        ChainInfo chainInfo;

        try {
            chainInfo = this.rpcProvider.getInfo();
        } catch (GetInfoRpcError getInfoRpcError) {
            throw new TransactionPrepareRpcError(ErrorConstants.TRANSACTION_PROCESSOR_RPC_GET_INFO,
                    getInfoRpcError);
        }

        if (Strings.isNullOrEmpty(this.chainId)) {
            if (Strings.isNullOrEmpty(chainInfo.getChainId())) {
                // Throw exception if both provided chain id and RPC chain id are empty
                throw new TransactionPrepareError(
                        ErrorConstants.TRANSACTION_PROCESSOR_PREPARE_CHAINID_RPC_EMPTY);
            }

            // Assign value to chain id only if chain id is not provided and RPC's chain id is valid
            this.chainId = chainInfo.getChainId();
        } else if (!Strings.isNullOrEmpty(chainInfo.getChainId()) && !chainInfo
                .getChainId().equals(chainId)) {
            // Throw error if both are not empty but one does not match with another
            throw new TransactionPrepareError(
                    String.format(ErrorConstants.TRANSACTION_PROCESSOR_PREPARE_CHAINID_NOT_MATCH,
                            this.chainId,
                            chainInfo.getChainId()));
        }

        // Assigning value to refBlockNum and refBlockPrefix

        BigInteger taposBlockNum;

        boolean useLastIrreversibleConfig = this.transactionConfig.isUseLastIrreversible();
        int blockBehindConfig = this.transactionConfig.getBlocksBehind();

        if (useLastIrreversibleConfig) {
            taposBlockNum = chainInfo.getLastIrreversibleBlockNum();
        } else {
            if (chainInfo.getHeadBlockNum().compareTo(BigInteger.valueOf(blockBehindConfig))
                    > 0) {
                taposBlockNum = chainInfo.getHeadBlockNum()
                        .subtract(BigInteger.valueOf(blockBehindConfig));
            } else {
                taposBlockNum = BigInteger.valueOf(blockBehindConfig);
            }
        }

        ChainBlock block ;
        try {
            block = this.rpcProvider.getBlock(taposBlockNum.toString());
        } catch (GetBlockRpcError error) {
            throw new TransactionPrepareRpcError(
                    ErrorConstants.TRANSACTION_PROCESSOR_PREPARE_RPC_GET_BLOCK_INFO, error);
        }

        // Calculate the expiration based on the taposBlockNum expiration
        if (preparingTransaction.getExpiration().isEmpty()) {
            String strHeadBlockTime = block.getTimestamp();
            long taposBlockTime;

            try {
                taposBlockTime = DateFormatter.convertBackendTimeToMilli(strHeadBlockTime);
            } catch (ParseException e) {
                throw new TransactionPrepareError(
                        ErrorConstants.TRANSACTION_PROCESSOR_TAPOS_BLOCK_TIME_PARSE_ERROR, e);
            }

            int expiresSeconds = this.transactionConfig.getExpiresSeconds();

            long expirationTimeInMilliseconds = taposBlockTime + expiresSeconds * 1000;
            preparingTransaction.setExpiration(DateFormatter
                    .convertMilliSecondToBackendTimeString(expirationTimeInMilliseconds));
        }

        // Restrict the refBlockNum to 32 bit unsigned value
        BigInteger refBlockNum = block.getBlockNum().and(BigInteger.valueOf(0xffff));
        BigInteger refBlockPrefix = block.getRefBlockPrefix();

        preparingTransaction.setRefBlockNum(refBlockNum);
        preparingTransaction.setRefBlockPrefix(refBlockPrefix);

        this.finishPreparing(preparingTransaction, preparingContextFreeData);
    }

    /**
     * Sign the transaction by passing {@link TransactionSignatureRequest} to signature
     * provider
     * <p>
     * Check sign() flow in "Complete Workflow" document for more detail
     *
     * @return success or not
     * @throws TransactionSignError thrown if there are any exceptions during the following:
     *      <br>
     *          - Creating signature. Cause: {@link TransactionCreateSignatureRequestError}
     *      <br>
     *          - Signing. Cause: {@link TransactionGetSignatureError} or {@link SignatureProviderError}
     */
    public boolean sign() throws TransactionSignError {
        TransactionSignatureRequest transactionSignatureRequest;
        try {
            transactionSignatureRequest = this.createSignatureRequest();
        } catch (TransactionCreateSignatureRequestError transactionCreateSignatureRequestError) {
            throw new TransactionSignError(
                    ErrorConstants.TRANSACTION_PROCESSOR_SIGN_CREATE_SIGN_REQUEST_ERROR,
                    transactionCreateSignatureRequestError);
        }

        TransactionSignatureResponse transactionSignatureResponse;
        try {
            transactionSignatureResponse = this.getSignature(transactionSignatureRequest);
            if (transactionSignatureResponse.getError() != null) {
                throw transactionSignatureResponse.getError();
            }
        } catch (TransactionGetSignatureError transactionGetSignatureError) {
            throw new TransactionSignError(transactionGetSignatureError);
        } catch (@Nullable SignatureProviderError signatureProviderError) {
            throw new TransactionSignError(
                    ErrorConstants.TRANSACTION_PROCESSOR_SIGN_SIGNATURE_RESPONSE_ERROR,
                    signatureProviderError);
        }

        return true;
    }

    /**
     * Broadcast transaction to blockchain. <p> Check broadcast() flow in "Complete Workflow"
     * document for more detail.
     *
     * @return broadcast result from chain
     * @throws TransactionBroadCastError thrown under the following conditions:
     *      <br>
     *          - The transaction has not been prepared or serialized yet.
     *      <br>
     *          - The transaction has not been signed yet (no signature).
     *      <br>
     *          - An error has been returned from the blockchain. Cause: {@link TransactionSendTransactionError}
     */
    @NotNull
    public TransactionResponse broadcast() throws TransactionBroadCastError {
        if (this.serializedTransaction == null || this.serializedTransaction.isEmpty()) {
            throw new TransactionBroadCastError(
                    ErrorConstants.TRANSACTION_PROCESSOR_BROADCAST_SERIALIZED_TRANSACTION_EMPTY);
        }

        if (this.signatures.isEmpty()) {
            throw new TransactionBroadCastEmptySignatureError(
                    ErrorConstants.TRANSACTION_PROCESSOR_BROADCAST_SIGN_EMPTY);
        }

        TransactionRequest transactionRequest = new TransactionRequest(this.signatures,
                0, this.contextFreeData.getHexed(), this.serializedTransaction);
        try {
            return this.sendTransaction(transactionRequest);
        } catch (TransactionSendTransactionError transactionSendTransactionError) {
            throw new TransactionBroadCastError(
                    ErrorConstants.TRANSACTION_PROCESSOR_BROADCAST_TRANS_ERROR,
                    transactionSendTransactionError);
        }
    }

    /**
     * Sign and broadcast the transaction and signature/s to chain
     *
     * @return SendTransactionResponse from blockchain.
     * @throws TransactionSignAndBroadCastError thrown under the following conditions:
     *      <br>
     *          - Exception while creating signature. Cause: {@link TransactionCreateSignatureRequestError}
     *      <br>
     *          - Exception while signing. Cause: {@link TransactionGetSignatureError} or {@link SignatureProviderError}
     *      <br>
     *          - The transaction has not been prepared or serialized yet.
     *      <br>
     *          - The transaction has not been signed yet (no signature).
     *      <br>
     *          - An error has been returned from the blockchain. Cause: {@link TransactionSendTransactionError}
     */
    @NotNull
    public TransactionResponse signAndBroadcast() throws TransactionSignAndBroadCastError {
        TransactionSignatureRequest transactionSignatureRequest;
        try {
            transactionSignatureRequest = this.createSignatureRequest();
        } catch (TransactionCreateSignatureRequestError transactionCreateSignatureRequestError) {
            throw new TransactionSignAndBroadCastError(transactionCreateSignatureRequestError);
        }

        try {
            this.getSignature(transactionSignatureRequest);
        } catch (TransactionGetSignatureError transactionGetSignatureError) {
            throw new TransactionSignAndBroadCastError(transactionGetSignatureError);
        }

        if (this.serializedTransaction == null || this.serializedTransaction.isEmpty()) {
            throw new TransactionSignAndBroadCastError(
                    ErrorConstants.TRANSACTION_PROCESSOR_SIGN_BROADCAST_SERIALIZED_TRANSACTION_EMPTY);
        }

        if (this.signatures.isEmpty()) {
            throw new TransactionSignAndBroadCastError(
                    ErrorConstants.TRANSACTION_PROCESSOR_SIGN_BROADCAST_SIGN_EMPTY);
        }

        // Signatures and serializedTransaction are assigned and finalized in getSignature() method
        TransactionRequest transactionRequest = new TransactionRequest(this.signatures,
                0, this.contextFreeData.getHexed(), this.serializedTransaction);
        try {
            return this.sendTransaction(transactionRequest);
        } catch (TransactionSendTransactionError transactionSendTransactionError) {
            throw new TransactionSignAndBroadCastError(transactionSendTransactionError);
        }
    }

    /**
     * Return transaction in JSON string format.
     *
     * @return JSON String of transaction
     */
    @Nullable
    public String toJSON() {
        return Utils.getGson(DateFormatter.BACKEND_DATE_PATTERN).toJson(this.transaction);
    }

    /**
     * Getting serialized version of Transaction <p> Check serialize() flow in "Complete Workflow"
     * document for more detail.
     *
     * @return serialized Transaction
     * @throws TransactionSerializeError thrown if there are any exceptions while serializing
     * transaction. Cause: {@link TransactionCreateSignatureRequestError}
     *      <br>
     *          - Exception while trying to clone transaction for runtime handling.
     *          Method used {@link Utils#clone(Serializable)}
     *      <br>
     *      Thrown as parent error class for:
     *      <br>
     *          - {@link TransactionCreateSignatureRequestRpcError}, which is thrown if any Rpc call
     *          ({@link IRPCProvider#getInfo()}) causes an exception
     *      <br>
     *          - {@link TransactionCreateSignatureRequestAbiError} thrown if any error occurs while
     *          calling {@link IABIProvider#getAbi(String, AMAXName)} to get the ABI needed to
     *          serialize each action
     *      <br>
     *          - {@link TransactionCreateSignatureRequestSerializationError}, which is thrown if any
     *          error happens while calling
     *          {@link ISerializationProvider#serialize(AbiSerializationObject)} to serialize
     *          each action or calling {@link ISerializationProvider#serializeTransaction(String)}
     *          to serialize the whole transaction.
     */
    @Nullable
    public String serialize() throws TransactionSerializeError {
        // Return serialized version of the Transaction, if it exists, otherwise serialize the
        // transaction and return the result.
        if (this.serializedTransaction != null && !this.serializedTransaction.isEmpty()) {
            return this.serializedTransaction;
        }

        try {
            return this.serializeTransaction();
        } catch (TransactionCreateSignatureRequestError transactionCreateSignatureRequestError) {
            throw new TransactionSerializeError(
                    ErrorConstants.TRANSACTION_PROCESSOR_SERIALIZE_ERROR,
                    transactionCreateSignatureRequestError);
        }
    }

    //endregion

    //region private methods

    /**
     * Create signature request which will be sent to signature provider to be signed.
     * <p>
     * Check createSignatureRequest() flow in "Complete Workflow" document for more details.
     */
    @NotNull
    private TransactionSignatureRequest createSignatureRequest()
            throws TransactionCreateSignatureRequestError {
        if (this.transaction == null) {
            throw new TransactionCreateSignatureRequestError(
                    ErrorConstants.TRANSACTION_PROCESSOR_TRANSACTION_HAS_TO_BE_INITIALIZED);
        }

        if (this.transaction.getActions().isEmpty()) {
            throw new TransactionCreateSignatureRequestError(
                    ErrorConstants.TRANSACTION_PROCESSOR_ACTIONS_EMPTY_ERROR_MSG);
        }

        // Cache the serialized version of transaction in the TransactionProcessor
        this.serializedTransaction = this.serializeTransaction();

        TransactionSignatureRequest transactionSignatureRequest = new TransactionSignatureRequest(
                this.serializedTransaction,
                null,
                this.chainId,
                null,
                this.isTransactionModificationAllowed,
                this.contextFreeData.getSerialized());

        // Assign required keys to signing public keys if it was set.
        if (this.requiredKeys != null && !this.requiredKeys.isEmpty()) {
            transactionSignatureRequest.setSigningPublicKeys(this.requiredKeys);
            return transactionSignatureRequest;
        }

        // Getting required keys
        // 1.Getting available keys
        if (this.availableKeys == null || this.availableKeys.isEmpty()) {
            try {
                this.availableKeys = this.signatureProvider.getAvailableKeys();
            } catch (GetAvailableKeysError getAvailableKeysError) {
                throw new TransactionCreateSignatureRequestKeyError(
                        ErrorConstants.TRANSACTION_PROCESSOR_GET_AVAILABLE_KEY_ERROR,
                        getAvailableKeysError);
            }

            if (this.availableKeys.isEmpty()) {
                throw new TransactionCreateSignatureRequestEmptyAvailableKeyError(
                        ErrorConstants.TRANSACTION_PROCESSOR_GET_AVAILABLE_KEY_EMPTY);
            }
        }

        // 2.Getting required keys by getRequiredKeys() RPC call
        try {
            RequiredKeysResponse getRequiredKeysResponse = this.rpcProvider
                    .getRequiredKeys(
                            new RequiredKeysRequest(
                                    this.availableKeys,
                                    this.transaction));
            if (getRequiredKeysResponse.getRequiredKeys() == null || getRequiredKeysResponse
                    .getRequiredKeys().isEmpty()) {
                throw new TransactionCreateSignatureRequestRequiredKeysEmptyError(
                        ErrorConstants.GET_REQUIRED_KEY_RPC_EMPTY_RESULT);
            }

            List<String> backendRequiredKeys = getRequiredKeysResponse.getRequiredKeys();
            if (!this.availableKeys.containsAll(backendRequiredKeys)) {
                throw new TransactionCreateSignatureRequestRequiredKeysEmptyError(
                        ErrorConstants.TRANSACTION_PROCESSOR_REQUIRED_KEY_NOT_SUBSET);
            }

            this.requiredKeys = backendRequiredKeys;
        } catch (GetRequiredKeysRpcError getRequiredKeysRpcError) {
            throw new TransactionCreateSignatureRequestRpcError(
                    ErrorConstants.TRANSACTION_PROCESSOR_RPC_GET_REQUIRED_KEYS,
                    getRequiredKeysRpcError);
        }

        transactionSignatureRequest.setSigningPublicKeys(this.requiredKeys);
        return transactionSignatureRequest;
    }

    /**
     * Passing {@link TransactionSignatureRequest} to signature provider for signing.
     * <p>
     * Check getSignature() flow in "Complete Workflow" documents for more details.
     *
     * @param transactionSignatureRequest The request for signature
     * @return Response from Signature provider
     */
    @NotNull
    private TransactionSignatureResponse getSignature(
            TransactionSignatureRequest transactionSignatureRequest)
            throws TransactionGetSignatureError {
        TransactionSignatureResponse transactionSignatureResponse;
        try {
            transactionSignatureResponse = this.signatureProvider
                    .signTransaction(transactionSignatureRequest);
            if (transactionSignatureResponse.getError() != null) {
                throw transactionSignatureResponse.getError();
            }
        } catch (SignatureProviderError signatureProviderError) {
            throw new TransactionGetSignatureSigningError(
                    ErrorConstants.TRANSACTION_PROCESSOR_SIGN_TRANSACTION_ERROR,
                    signatureProviderError);
        }

        if (Strings.isNullOrEmpty(transactionSignatureResponse.getSerializedTransaction())) {
            throw new TransactionGetSignatureSigningError(
                    ErrorConstants.TRANSACTION_PROCESSOR_SIGN_TRANSACTION_TRANS_EMPTY_ERROR);
        }

        if (transactionSignatureResponse.getSignatures().isEmpty()) {
            throw new TransactionGetSignatureSigningError(
                    ErrorConstants.TRANSACTION_PROCESSOR_SIGN_TRANSACTION_SIGN_EMPTY_ERROR);
        }

        // Store current transaction as original transaction
        this.originalTransaction = this.transaction;

        if (this.serializedTransaction != null
                && !this.serializedTransaction
                .equals(transactionSignatureResponse.getSerializedTransaction())) {
            // Throw error if an unmodifiable transaction is modified
            if (!this.isTransactionModificationAllowed) {
                throw new TransactionGetSignatureNotAllowModifyTransactionError(
                        ErrorConstants.TRANSACTION_IS_NOT_ALLOWED_TOBE_MODIFIED);
            }

            /* Deserialize and update new transaction to the current transaction if it was
               and modification is allowed.*/
            String transactionJSON;
            try {
                transactionJSON = this.serializationProvider
                        .deserializeTransaction(
                                transactionSignatureResponse.getSerializedTransaction());
                if (transactionJSON == null || transactionJSON.isEmpty()) {
                    throw new DeserializeTransactionError(
                            ErrorConstants.TRANSACTION_PROCESSOR_GET_SIGN_DESERIALIZE_TRANS_EMPTY_ERROR);
                }
            } catch (DeserializeTransactionError deserializeTransactionError) {
                throw new TransactionGetSignatureDeserializationError(
                        ErrorConstants.TRANSACTION_PROCESSOR_GET_SIGN_DESERIALIZE_TRANS_ERROR,
                        deserializeTransactionError);
            }

            this.transaction = Utils.getGson(DateFormatter.BACKEND_DATE_PATTERN).fromJson(transactionJSON, Transaction.class);
        }

        this.signatures = new ArrayList<>();
        this.signatures.addAll(transactionSignatureResponse.getSignatures());
        this.serializedTransaction = transactionSignatureResponse.getSerializedTransaction();
        return transactionSignatureResponse;
    }

    /**
     * Send signed transaction to blockchain.
     * <p>
     * Check sendTransaction() flow in "Complete Workflow" document for more details.
     *
     * @param transactionRequest the request
     * @return Response from chain
     */
    @NotNull
    private TransactionResponse sendTransaction(TransactionRequest transactionRequest)
            throws TransactionSendTransactionError {
        try {
            return this.rpcProvider.sendTransaction(transactionRequest);
        } catch (SendTransactionRpcError sendTransactionRpcError) {
            throw new TransactionSendTransactionError(
                    ErrorConstants.TRANSACTION_PROCESSOR_RPC_SEND_TRANSACTION,
                    sendTransactionRpcError);
        }
    }

    /**
     * Serialize current transaction
     *
     * @return serialized transaction in Hex
     * @throws TransactionCreateSignatureRequestError thrown if there are any exceptions while serializing transaction:
     *      <br>
     *          - Exception while cloning transaction for runtime handling. Method used {@link Utils#clone(Serializable)}
     *      <br>
     *      Thrown as parent error class for:
     *      <br>
     *          - {@link TransactionCreateSignatureRequestRpcError}, which is thrown if any RPC call
     *          ({@link IRPCProvider#getInfo()}) results in an exception
     *      <br>
     *          - {@link TransactionCreateSignatureRequestAbiError}, which is thrown if any error
     *          occurs while calling {@link IABIProvider#getAbi(String, AMAXName)} to get the ABI
     *          needed to serialize an action
     *      <br>
     *          - {@link TransactionCreateSignatureRequestSerializationError}, which is thrown if
     *          an exception occurs while calling
     *          {@link ISerializationProvider#serialize(AbiSerializationObject)} to serialize each action
     *          or calling {@link ISerializationProvider#serializeTransaction(String)} to serialize the whole transaction.
     */
    @NotNull
    private String serializeTransaction() throws TransactionCreateSignatureRequestError {
        Transaction clonedTransaction;
        try {
            clonedTransaction = this.getDeepClone();
        } catch (IOException e) {
            throw new TransactionCreateSignatureRequestError(
                    ErrorConstants.TRANSACTION_PROCESSOR_PREPARE_CLONE_ERROR, e);
        } catch (ClassNotFoundException e) {
            throw new TransactionCreateSignatureRequestError(
                    ErrorConstants.TRANSACTION_PROCESSOR_PREPARE_CLONE_CLASS_NOT_FOUND, e);
        }

        if (clonedTransaction == null) {
            throw new TransactionCreateSignatureRequestError(
                    ErrorConstants.TRANSACTION_PROCESSOR_PREPARE_CLONE_ERROR);
        }

        /* Check for chain id
         Call getInfo() if chainId is NULL or Empty
         */
        if (this.chainId == null || this.chainId.isEmpty()) {
            try {
                ChainInfo chainInfo = this.rpcProvider.getInfo();
                this.chainId = chainInfo.getChainId();
            } catch (GetInfoRpcError getInfoRpcError) {
                throw new TransactionCreateSignatureRequestRpcError(
                        ErrorConstants.TRANSACTION_PROCESSOR_RPC_GET_INFO, getInfoRpcError);
            }
        }

        // Serialize each action of Transaction's actions
        for (Action action : clonedTransaction.getActions()) {
            AbiSerializationObject actionAbiSerializationObject = this.serializeAction(action, this.chainId, this.abiProvider);
            // !!! Set serialization result to data field of the action
            action.setData(actionAbiSerializationObject.getHex());
        }

        // Serialize each action of Transaction's context free actions if they exist.
        if (!clonedTransaction.getContextFreeActions().isEmpty()) {
            for (Action contextFreeAction : clonedTransaction.getContextFreeActions()) {
                AbiSerializationObject actionAbiSerializationObject = this.serializeContextFreeAction(contextFreeAction, this.chainId, this.abiProvider);
                // !!! Set serialization result to data field of the contextFreeAction
                contextFreeAction.setData(actionAbiSerializationObject.getHex());
            }
        }

        // Apply serialized actions to current transaction to be used on getRequiredKeys
        // From now, the current transaction keep serialized actions
        this.transaction = clonedTransaction;

        // Serialize the whole transaction
        String _serializedTransaction;
        try {
            String clonedTransactionToJSON = Utils.getGson(DateFormatter.BACKEND_DATE_PATTERN).toJson(clonedTransaction);
            _serializedTransaction = this.serializationProvider.serializeTransaction(clonedTransactionToJSON);
            if (_serializedTransaction == null || _serializedTransaction.isEmpty()) {
                throw new TransactionCreateSignatureRequestSerializationError(
                        ErrorConstants.TRANSACTION_PROCESSOR_SERIALIZE_TRANSACTION_WORKED_BUT_EMPTY_RESULT);
            }

        } catch (SerializeTransactionError serializeTransactionError) {
            throw new TransactionCreateSignatureRequestSerializationError(
                    ErrorConstants.TRANSACTION_PROCESSOR_SERIALIZE_TRANSACTION_ERROR,
                    serializeTransactionError);
        }

        return _serializedTransaction;
    }

    /**
     * Serializing an action's JSON data to Hex format by using {@link IABIProvider} and {@link ISerializationProvider}
     *
     * @param action - input action to serialize.
     * @param chainId - the chain id.
     * @param abiProvider - an instance of ABI provider.
     * @return A serialized object from {@link ISerializationProvider} which contains the hex format of the action's JSON data.
     * @throws TransactionCreateSignatureRequestError thrown if there are any exceptions while serializing transaction:
     *      <br>
     *          - {@link TransactionCreateSignatureRequestAbiError}, which is thrown if any error
     *          occurs while calling {@link IABIProvider#getAbi(String, AMAXName)} to get the ABI
     *          needed to serialize an action
     *      <br>
     *          - {@link TransactionCreateSignatureRequestSerializationError}, which is thrown if
     *          an exception occurs while calling
     *          {@link ISerializationProvider#serialize(AbiSerializationObject)} to serialize each action
     *          or calling {@link ISerializationProvider#serializeTransaction(String)} to serialize the whole transaction.
     */
    @NotNull
    private AbiSerializationObject serializeAction(Action action, String chainId, IABIProvider abiProvider)
            throws TransactionCreateSignatureRequestError {
        AbiSerializationObject actionAbiSerializationObject = setupAbiEosSerializationObject(action, chainId, abiProvider);

        try {
            this.serializationProvider.serialize(actionAbiSerializationObject);
        } catch (SerializeError serializeError) {
            throw new TransactionCreateSignatureRequestSerializationError(
                    String.format(ErrorConstants.TRANSACTION_PROCESSOR_SERIALIZE_ACTION_ERROR,
                            action.getAccount()), serializeError);
        }

        return actionAbiSerializationObject;
    }

    /**
     * Serializing an action's JSON data to Hex format by using {@link IABIProvider} and {@link ISerializationProvider}
     *
     * @param action - input contextFreeAction to serialize.
     * @param chainId - the chain id.
     * @param abiProvider - an instance of ABI provider.
     * @return A serialized object from {@link ISerializationProvider} which contains the hex format of the action's JSON data.
     * @throws TransactionCreateSignatureRequestError thrown if there are any exceptions while serializing transaction:
     *      <br>
     *          - {@link TransactionCreateSignatureRequestAbiError}, which is thrown if any error
     *          occurs while calling {@link IABIProvider#getAbi(String, AMAXName)} to get the ABI
     *          needed to serialize an action
     */
    @NotNull
    private AbiSerializationObject serializeContextFreeAction(Action action, String chainId, IABIProvider abiProvider)
            throws TransactionCreateSignatureRequestError {
        AbiSerializationObject actionAbiSerializationObject = setupAbiEosSerializationObject(action, chainId, abiProvider);

        try {
            if (action.hasData()) {
                this.serializationProvider.serialize(actionAbiSerializationObject);
            }
        } catch (SerializeError serializeError) {
            throw new TransactionCreateSignatureRequestSerializationError(
                    String.format(ErrorConstants.TRANSACTION_PROCESSOR_SERIALIZE_ACTION_ERROR,
                            action.getAccount()), serializeError);
        }

        return actionAbiSerializationObject;
    }

    /**
     * Sets up AbiEosSerializationObject for use in serializing actions' data
     *
     * @param action - input action/contextFreeAction to setup.
     * @param chainId - the chain id.
     * @param abiProvider - an instance of ABI provider.
     * @return A new AbiEosSerializationObject that will be used in serializing actions' data.
     * @throws TransactionCreateSignatureRequestError thrown if there are any exceptions while serializing transaction:
     *      <br>
     *          - {@link TransactionCreateSignatureRequestAbiError}, which is thrown if any error
     *          occurs while calling {@link IABIProvider#getAbi(String, AMAXName)} to get the ABI
     *          needed to serialize an action
     */
    @NotNull
    private AbiSerializationObject setupAbiEosSerializationObject(Action action, String chainId, IABIProvider abiProvider)
            throws TransactionCreateSignatureRequestAbiError {
        String actionAbiJSON;
        try {
            actionAbiJSON = abiProvider
                    .getAbi(chainId, new AMAXName(action.getAccount()));
        } catch (GetAbiError getAbiError) {
            throw new TransactionCreateSignatureRequestAbiError(
                    String.format(ErrorConstants.TRANSACTION_PROCESSOR_GET_ABI_ERROR,
                            action.getAccount()), getAbiError);
        }

        AbiSerializationObject actionAbiSerializationObject = new AbiSerializationObject(
                action.getAccount(), action.getName(),
                null, actionAbiJSON);
        actionAbiSerializationObject.setHex("");

        // !!! At this step, the data field of the action is still in JSON format.
        actionAbiSerializationObject.setJson(action.getData());

        return actionAbiSerializationObject;
    }

    /**
     * Getting deep clone of the transaction
     */
    private Transaction getDeepClone() throws IOException, ClassNotFoundException {
        if (this.transaction == null) {
            return null;
        }

        return Utils.clone(this.transaction);
    }

    /**
     * Called when prepare() is finished
     *
     * @param preparingTransaction - prepared transaction
     */
    private void finishPreparing(Transaction preparingTransaction, ContextFreeData preparingContextFreeData) {
        this.transaction = preparingTransaction;
        this.contextFreeData = preparingContextFreeData;
        // Clear serialized transaction if it was serialized.
        if (!Strings.isNullOrEmpty(this.serializedTransaction)) {
            this.serializedTransaction = "";
        }
    }

    //endregion

    //region getters/setters

    /**
     * Gets transaction instance which holds all data relating to EOS Transaction
     * <p>
     * This object holds the non-serialized version of Transaction
     * @return the current transaction
     */
    @Nullable
    public Transaction getTransaction() {
        return transaction;
    }

    /**
     * Gets Transaction instance that holds the original transaction reference after signature provider
     * returns the signing result.
     *
     * Check getSignature() flow in "Complete Workflow" document for more details.
     * @return the original transaction
     */
    @Nullable
    public Transaction getOriginalTransaction() {
        return originalTransaction;
    }

    /**
     * List of signatures that will be available after they are returned from the signature
     * provider.
     * <p>
     * Check getSignature() flow in "Complete Workflow" document for more details.
     * @return list of EOS format signature.
     */
    @NotNull
    public List<String> getSignatures() {
        return signatures;
    }

    /**
     * Gets serialized version of Transaction.  This is stored to later determine whether the
     * signature provider modified the transaction.
     * <p>
     *     If the transaction has been modified, this object needs to be cleared or updated.
     * <p>
     *     Check getSignature() flow in "Complete Workflow" document for more details.
     * @return the serialized transaction.
     */
    @Nullable
    public String getSerializedTransaction() {
        return serializedTransaction;
    }

    /**
     * Gets configuration for Transaction which offers ability to set:
     * <p>
     * - The expiration period for the transaction in seconds
     * <p>
     * - How many blocks behind
     * @return the configuration for transaction
     */
    @NotNull
    public TransactionConfig getTransactionConfig() {
        return transactionConfig;
    }

    /**
     * Sets configuration for Transaction which offers ability to set:
     * <p>
     * - The expiration period for the transaction in seconds
     * <p>
     * - How many blocks behind
     * @param transactionConfig the input configuration for transaction
     */
    public void setTransactionConfig(@NotNull TransactionConfig transactionConfig) {
        this.transactionConfig = transactionConfig;
    }

    /**
     * Sets chain id value. If the value has not been set yet, the getInfo() RPC call will be used
     * to get it.
     * @param chainId - input chain id
     */
    public void setChainId(@Nullable String chainId) {
        this.chainId = chainId;
    }

    /**
     * Set value for available keys list.
     * <p>
     * List of available keys which most likely come from signature provider.
     * <p>
     * If this list is set, TransactionProcessor won't ask for available keys from the signature
     * provider and will use this list.
     * <p>
     * Check createSignatureRequest() flow in "Complete Workflow" document for more details.
     *
     * @param availableKeys the input available keys
     */
    public void setAvailableKeys(@NotNull List<String> availableKeys) {
        this.availableKeys = availableKeys;
    }

    /**
     * Set value for required keys list
     * <p>
     * List of required keys to sign the transaction.
     * <p>
     * If this list is set, TransactionProcessor won't make RPC call for getRequiredKeys().
     * <p>
     * Check createSignatureRequest() flow in "Complete Workflow" document for more details.
     *
     * @param requiredKeys the input required keys
     */
    public void setRequiredKeys(@NotNull List<String> requiredKeys) {
        this.requiredKeys = requiredKeys;
    }

    /**
     * Should the signature provider be able to modify the transaction?
     * @return Whether to allow transaction to be modified by Signature Provider.
     */
    public boolean isTransactionModificationAllowed() {
        return isTransactionModificationAllowed;
    }

    /**
     * True: The transaction could be modified and updated to current transaction by Signature
     * provider.
     * <p>
     * False: No modification. {@link TransactionGetSignatureNotAllowModifyTransactionError} will be
     * thrown if transaction is modified.
     * @param isTransactionModificationAllowed Whether allow transaction to be modified by Signature Provider.
     */
    public void setIsTransactionModificationAllowed(boolean isTransactionModificationAllowed) {
        this.isTransactionModificationAllowed = isTransactionModificationAllowed;
    }

    /**
     * Gets context free data instance.
     * @return the context free data instance.
     */
    public ContextFreeData getContextFreeData() {
        return this.contextFreeData;
    }

    /**
     * Return the current transaction serialized in packed V0 format.
     * @return Hex string format for the serialized transaction.
     * @throws SerializePackedTransactionError Thrown if error occurs during the serialization of the packed transaction by the provider.
     */
    public String getPackedTransactionV0()
            throws SerializePackedTransactionError {
        String json = Utils.getGson(DateFormatter.BACKEND_DATE_PATTERN).toJson(this.toJSON());
        String packedTransactionV0 = this.serializationProvider.serializePackedTransaction(json);

        return PACKED_TRANSACTION_V0_PREFIX + packedTransactionV0;
    }
    //endregion
}
