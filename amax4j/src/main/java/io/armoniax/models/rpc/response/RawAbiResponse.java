package io.armoniax.models.rpc.response;

import com.google.gson.annotations.SerializedName;
import io.armoniax.models.AMAXName;
import io.armoniax.models.rpc.request.RawAbiRequest;

/**
 * The response of getRawAbi() RPC call {@link io.armoniax.provider.IRPCProvider#getRawAbi(RawAbiRequest)}
 */
public class RawAbiResponse {

    /**
     * The account name (contract name) found in {@link AMAXName}
     */
    @SerializedName("account_name")
    private String accountName;

    @SerializedName("code_hash")
    private String codeHash;

    @SerializedName("abi_hash")
    private String abiHash;

    /**
     * The ABI (raw ABI) of the account name (contract name)
     * <br>
     * This ABI is used to serialize a contract's action data.
     */
    @SerializedName("abi")
    private String abi;

    /**
     * Gets the account name (contract name) found in {@link AMAXName}
     *
     * @return the account name.
     */
    public String getAccountName() {
        return accountName;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public String getAbiHash() {
        return abiHash;
    }

    /**
     * Gets The ABI (raw ABI) of the account name (contract name).
     * <br>
     *      This ABI is used to serialize a contract's action data.
     *
     * @return the raw ABI
     */
    public String getAbi() {
        return abi;
    }
}
