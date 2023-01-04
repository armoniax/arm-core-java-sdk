package io.armoniax.client;

import io.armoniax.models.rpc.Action;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Setter
@Getter
public class Trx {
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
    private int expiresSeconds;
    private int blocksBehind;
    private boolean useLastIrreversible;

    private List<Action> actions;

    Trx(int expiresSeconds, int blocksBehind,
        boolean useLastIrreversible,List<Action> actions) {
        this.expiresSeconds = expiresSeconds;
        this.blocksBehind = blocksBehind;
        this.useLastIrreversible = useLastIrreversible;
        this.actions = actions;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int expiresSeconds = DEFAULT_EXPIRES_SECONDS;
        private int blocksBehind = DEFAULT_BLOCKS_BEHIND;
        private boolean useLastIrreversible = DEFAULT_USE_LAST_IRREVERSIBLE;
        private List<Action> actions;

        public Builder setExpiresSeconds(int val) {
            expiresSeconds = val;
            return this;
        }

        public Builder setBlocksBehind(int val) {
            blocksBehind = val;
            return this;
        }

        public Builder setUseLastIrreversible(boolean b) {
            useLastIrreversible = b;
            return this;
        }

        public Builder setActions(List<Action> actions) {
            this.actions = actions;
            return this;
        }

        public Trx build() {
            return new Trx(
                    expiresSeconds, blocksBehind,
                    useLastIrreversible, actions);
        }
    }
}


