package io.armoniax.models.rpc.request;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

/**
 * The request class of getRawAbi() RPC call {@link io.armoniax.provider.IRPCProvider#getRawAbi(RawAbiRequest)}
 */
public class RawAbiRequest {

    /**
     * Instantiates a new GetRawAbiRequest.
     *
     * @param accountName the String representation of EOSIO name type
     */
    public RawAbiRequest(@NotNull String accountName) {
        this.accountName = accountName;
    }

    /**
     * The string representation of EOSIO name type
     */
    @SerializedName("account_name")
    @NotNull
    private String accountName;

    /**
     * Gets the string representation of EOSIO name type
     *
     * @return the string representation of EOSIO name type
     */
    @NotNull
    public String getAccountName() {
        return accountName;
    }

    /**
     * Sets the string representation of EOSIO name type
     *
     * @param accountName the string representation of EOSIO name type
     */
    public void setAccountName(@NotNull String accountName) {
        this.accountName = accountName;
    }
}
