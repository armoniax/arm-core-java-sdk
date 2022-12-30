package io.armoniax.models.rpc;

import io.armoniax.provider.IRPCProvider;
import io.armoniax.models.rpc.request.BlockInfoRequest;
import io.armoniax.models.rpc.response.ChainInfo;
import lombok.Data;

/**
 * A configuration class that allows the developer to change certain defaults associated with a
 * Transaction.
 */
@Data
public class TransactionConfig {

    /**
     * Default blocks behind to use if blocksbehind is not set or instance of this class is not
     * used
     */
    private static final int DEFAULT_BLOCKS_BEHIND = 3;

    /**
     * Default expires seconds to use if expires seconds is not set or instance of this class is not
     * used
     */
    private static final int DEFAULT_EXPIRES_SECONDS = 5 * 60;

    /**
     * Default useLastIrreversible to use last irreversible block rather than blocksBehind when
     * calculating TAPOS
     */
    private static final boolean DEFAULT_USE_LAST_IRREVERSIBLE = true;

    /**
     * The Expires seconds.
     * <br>
     * Append this value to {@link ChainInfo#getHeadBlockTime()} in second then assign it to
     * {@link Transaction#setExpiration(String)}
     */
    private int expiresSeconds = DEFAULT_EXPIRES_SECONDS;

    /**
     * The amount blocks behind from head block.
     * <br>
     * It is an argument to calculate head block number to call {@link
     * io.armoniax.provider.IRPCProvider#getBlockInfo(BlockInfoRequest)}
     */
    private int blocksBehind = DEFAULT_BLOCKS_BEHIND;

    /**
     * Use the last irreversible block when calculating TAPOS rather than blocks behind.
     * <br>
     * Mutually exclusive with {@link TransactionConfig#setBlocksBehind(int)}.  If set,
     * {@link TransactionConfig#getBlocksBehind()} will be ignored and TAPOS will be calculated by fetching the last
     * irreversible block with {@link IRPCProvider#getInfo()} and the expiration for the transaction
     * will be set {@link TransactionConfig#setExpiresSeconds(int)} after that block's time.
     */
    private boolean useLastIrreversible = DEFAULT_USE_LAST_IRREVERSIBLE;
}
