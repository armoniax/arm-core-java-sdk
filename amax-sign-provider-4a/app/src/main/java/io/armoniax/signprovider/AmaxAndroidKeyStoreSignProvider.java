package io.armoniax.signprovider;

import static io.armoniax.signprovider.errors.ErrorString.SIGN_TRANSACTION_RAW_SIGNATURE_IS_NULL;
import static io.armoniax.signprovider.errors.ErrorString.SIGN_TRANSACTION_UNABLE_TO_FIND_KEY_TO_SIGN;

import android.util.Pair;

import org.bouncycastle.util.encoders.Hex;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import io.armoniax.error.signatureProvider.SignTransactionError;
import io.armoniax.error.utilities.EOSFormatterError;
import io.armoniax.signprovider.errors.ErrorString;
import io.armoniax.models.signature.TransactionSignatureRequest;
import io.armoniax.models.signature.TransactionSignatureResponse;
import io.armoniax.provider.ISignatureProvider;
import io.armoniax.tools.EOSFormatter;

public class AmaxAndroidKeyStoreSignProvider implements ISignatureProvider {
    private KeyStore.ProtectionParameter password;
    private KeyStore.LoadStoreParameter loadStoreParameter;

    @Override
    public TransactionSignatureResponse signTransaction(TransactionSignatureRequest request) throws SignTransactionError {
        if (request.getChainId() ==null && request.getChainId().isEmpty()) {
            throw new SignTransactionError(ErrorString.SIGN_TRANS_EMPTY_CHAIN_ID);
        }

        // Prepare message to be signed.
        // Getting serializedTransaction and preparing signable transaction
        String serializedTransaction  = request.getSerializedTransaction();

        // This is the un-hashed message which is used to recover public key
        byte[] message;

        try {
            message = Hex.decode(
                    EOSFormatter.prepareSerializedTransactionForSigning(
                            serializedTransaction,
                            request.getChainId()
                    ).toUpperCase()
            );
        } catch (EOSFormatterError eosFormatterError) {
            throw new SignTransactionError(
                    String.format(
                            ErrorString.SIGN_TRANSACTION_PREPARE_FOR_SIGNING_GENERIC_ERROR,
                            serializedTransaction
                    ), eosFormatterError
            );
        }

        List<Pair<String, String>> aliasKeyPairs = null;
        List<String> signatures = new ArrayList<>();
        try {
            aliasKeyPairs = AndroidKeyStoreUtility.getAllAndroidKeyStoreKeysInEOSFormat(
                    password, loadStoreParameter
            );

            List<String> signingPublicKeys  = request.getSigningPublicKeys();

            for (String signingPublicKey : signingPublicKeys) {
                String keyAlias = "";

                for (Pair<String, String> aliasKeyPair : aliasKeyPairs) {
                    if (signingPublicKey.equals(aliasKeyPair.second)) {
                        keyAlias = aliasKeyPair.first;
                        break;
                    }
                }

                if (keyAlias.isEmpty()) {
                    throw new SignTransactionError(SIGN_TRANSACTION_UNABLE_TO_FIND_KEY_TO_SIGN);
                }

                byte[] rawSignature =
                        AndroidKeyStoreUtility.sign(message, keyAlias,
                                password, loadStoreParameter
                        );
                if (rawSignature == null){
                    throw new SignTransactionError(SIGN_TRANSACTION_RAW_SIGNATURE_IS_NULL);
                }

                signatures.add(
                        EOSFormatter.convertDERSignatureToEOSFormat(
                                rawSignature,
                                message,
                                EOSFormatter.convertEOSPublicKeyToPEMFormat(signingPublicKey)
                        )
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new TransactionSignatureResponse(serializedTransaction, signatures, null);
    }

    @Override
    public List<String> getAvailableKeys(){
        List<Pair<String,String>> pairList = null;
        List<String> result = new ArrayList();
        try {
            pairList = AndroidKeyStoreUtility.getAllAndroidKeyStoreKeysInEOSFormat(
                    password, loadStoreParameter
            );

            if (!pairList.isEmpty()){
                for (Pair<String,String> p: pairList) {
                    result.add(p.second);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static AmaxAndroidKeyStoreSignProvider.Builder builder() {
        return new AmaxAndroidKeyStoreSignProvider.Builder();
    }

    /**
     * Builder class for Android KeyStore Signature Provider
     *
     * @property androidKeyStoreSignatureProvider AndroidKeyStoreSignatureProvider
     */
    public static class Builder {

        private AmaxAndroidKeyStoreSignProvider androidKeyStoreSignatureProvider =
                new AmaxAndroidKeyStoreSignProvider();

        /**
         * Set password protection for adding, using and removing key
         *
         * @param password KeyStore.ProtectionParameter
         * @return Builder
         */
        public Builder setPassword(KeyStore.ProtectionParameter password) {
            this.androidKeyStoreSignatureProvider.password = password;
            return this;
        }

        /**
         * Set Load KeyStore Parameter to load the KeyStore instance
         *
         * @param loadStoreParameter KeyStore.LoadStoreParameter
         * @return Builder
         */
        public Builder setLoadStoreParameter(KeyStore.LoadStoreParameter loadStoreParameter){
            this.androidKeyStoreSignatureProvider.loadStoreParameter = loadStoreParameter;
            return this;
        }

        /**
         * Build and return the Android KeyStore Signature Provider instance
         *
         * @return AndroidKeyStoreSignatureProvider
         */
        public AmaxAndroidKeyStoreSignProvider build() {
            return this.androidKeyStoreSignatureProvider;
        }
    }
}
