package io.armoniax.models.rpc.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import io.armoniax.models.rpc.request.RequiredKeysRequest;
import io.armoniax.models.signature.TransactionSignatureRequest;

/**
 * The response of getRequiredKeys() RPC call {@link io.armoniax.provider.IRPCProvider#getRequiredKeys(RequiredKeysRequest)}
 */
public class RequiredKeysResponse {

    /**
     * The required public EOSIO keys to sign the transaction. It gets assigned to {@link
     * TransactionSignatureRequest#setSigningPublicKeys(List)},
     * which is passed to a Signature Provider to sign a transaction.
     */
    @SerializedName("required_keys")
    private List<String> requiredKeys;

    /**
     * Gets the required public EOSIO keys to sign the transaction. It gets assigned to {@link
     * TransactionSignatureRequest#setSigningPublicKeys(List)},
     * which is passed to a Signature Provider to sign a transaction.
     * @return The required public EOSIO keys.
     */
    public List<String> getRequiredKeys() {
        return requiredKeys;
    }
}
