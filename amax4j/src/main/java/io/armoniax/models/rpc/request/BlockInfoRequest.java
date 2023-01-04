package io.armoniax.models.rpc.request;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

/**
 * The request class for getBlockInfo() RPC call {@link io.armoniax.provider.IRPCProvider#getBlockInfo(BlockInfoRequest)}
 */
public class BlockInfoRequest {

    /**
     * Instantiates a new GetBlockInfoRequest.
     *
     * @param blockNum the block number
     */
    public BlockInfoRequest(@NotNull BigInteger blockNum) {
        this.blockNum = blockNum;
    }

    /**
     * Provide a block number
     */
    @SerializedName("block_num")
    @NotNull
    private BigInteger blockNum;

    /**
     * Gets block number.
     *
     * @return the block number
     */
    @NotNull
    public BigInteger getBlockNum() {
        return blockNum;
    }

    /**
     * Sets block number.
     *
     * @param blockNum the block number
     */
    public void setBlockNum(@NotNull BigInteger blockNum) {
        this.blockNum = blockNum;
    }
}
