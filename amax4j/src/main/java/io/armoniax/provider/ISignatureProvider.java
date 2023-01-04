package io.armoniax.provider;

import java.util.List;

import io.armoniax.error.signatureProvider.GetAvailableKeysError;
import io.armoniax.error.signatureProvider.SignTransactionError;
import io.armoniax.models.signature.TransactionSignatureRequest;
import io.armoniax.models.signature.TransactionSignatureResponse;
import org.jetbrains.annotations.NotNull;

/**
 * The interface of Signature provider.
 */
public interface ISignatureProvider {

    /**
     * Sign a transaction in Signature Provider <br> Check signTransaction flow() in "complete
     * workflow" for more detail
     *
     * @param transactionSignatureRequest the request
     * @return the response
     * @throws SignTransactionError thrown if there are any exceptions during the signing process.
     */
    @NotNull
    TransactionSignatureResponse signTransaction(
            @NotNull TransactionSignatureRequest transactionSignatureRequest)
            throws SignTransactionError;

    /**
     * Gets available keys from signature provider <br> Check createSignatureRequest() flow in
     * "complete workflow" for more detail of how the method is used
     *
     * @return the available keys of signature provider in EOS format
     * @throws GetAvailableKeysError thrown if there are any exceptions during the get available keys process.
     */
    @NotNull
    List<String> getAvailableKeys() throws GetAvailableKeysError;
}
