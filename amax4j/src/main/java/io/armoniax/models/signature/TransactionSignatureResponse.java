package io.armoniax.models.signature;

import java.util.List;
import io.armoniax.error.signatureProvider.SignatureProviderError;
import io.armoniax.models.rpc.ContextFreeData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The response object returned from the SignatureProvider after the transaction has been signed.
 */
public class TransactionSignatureResponse {

    /**
     * The serialized (Hex) version of {@link io.armoniax.models.rpc.Transaction}.
     * <br>
     * It is the result of {@link io.armoniax.interfaces.ISerializationProvider#serializeTransaction(String)}
     * <br>
     * The transaction could have been modified by the signature provider.
     * <br>
     * If signature provider modifies the serialized transaction returned in the response {@link
     * TransactionSignatureResponse#getSerializedTransaction()} but {@link
     * TransactionSignatureRequest#isModifiable()} is false then {@link
     * io.armoniax.error.session.TransactionGetSignatureNotAllowModifyTransactionError} will
     * be thrown
     */
    @NotNull
    private String serializedTransaction;

    /**
     * The serialized (Hex) version of {@link io.armoniax.models.rpc.ContextFreeData}.
     * <br>
     * It is the result of {@link ContextFreeData#getSerialized()}
     */
    @NotNull
    private String serializedContextFreeData;

    /**
     * The signatures that are signed by private keys of {@link TransactionSignatureRequest#getSigningPublicKeys()}
     */
    @NotNull
    private List<String> signatures;

    /**
     * The error that occurred during signing.
     */
    @Nullable
    private SignatureProviderError error;

    public TransactionSignatureResponse(@NotNull String serializedTransaction, @NotNull String serializedContextFreeData,
                                        @NotNull List<String> signatures, @Nullable SignatureProviderError error) {
        this.serializedTransaction = serializedTransaction;
        this.serializedContextFreeData = serializedContextFreeData;
        this.signatures = signatures;
        this.error = error;
    }

    public TransactionSignatureResponse(@NotNull String serializedTransaction,
                                        @NotNull List<String> signatures, @Nullable SignatureProviderError error) {
        this(serializedTransaction, "", signatures, error);
    }

    /**
     * Gets the serialized transaction.
     *
     * @return the serialize transaction
     */
    @NotNull
    public String getSerializedTransaction() {
        return serializedTransaction;
    }

    /**
     * Gets signatures.
     *
     * @return the signatures
     */
    @NotNull
    public List<String> getSignatures() {
        return signatures;
    }

    /**
     * Gets error.
     *
     * @return the error
     */
    @Nullable
    public SignatureProviderError getError() {
        return error;
    }

    /**
     * Gets serialized context free data.
     *
     * @return the serialized context free data
     */
    @NotNull
    public String getSerializedContextFreeData() { return serializedContextFreeData; }
}
