package io.armoniax.client;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class NewAccountOption {
    private String creator;
    private String newAccount;
    private String ownerPubKey;
    private String activePubKey;
    private int buyRamBytes;
    String stakeNetQuantity;
    String stakeCpuQuantity;
    boolean transfer;

    public NewAccountOption(String creator, String newAccount,
                            String ownerPubKey, String activePubKey,
                            int buyRamBytes, String stakeNetQuantity,
                            String stakeCpuQuantity, boolean transfer) {
        this.creator = creator;
        this.newAccount = newAccount;
        this.ownerPubKey = ownerPubKey;
        this.activePubKey = activePubKey;
        this.buyRamBytes = buyRamBytes;
        this.stakeNetQuantity = stakeNetQuantity;
        this.stakeCpuQuantity = stakeCpuQuantity;
        this.transfer = transfer;
    }

    public static Builder builder() {
        return new Builder();
    }


    public static class Builder {
        @NotNull
        private String creator;
        @NotNull
        private String newAccount;
        @NotNull
        private String ownerPubKey;
        @NotNull
        private String activePubKey;
        private int buyRamBytes = 8192;
        @NotNull
        String stakeNetQuantity;
        @NotNull
        String stakeCpuQuantity;
        boolean transfer = false;
        public Builder setCreator(String creator) {
            this.creator = creator;
            return this;
        }

        public Builder setNewAccount(String newAccount) {
            this.newAccount = newAccount;
            return this;
        }

        public Builder setOwnerPubKey(String ownerPubKey) {
            this.ownerPubKey = ownerPubKey;
            return this;
        }

        public Builder setActivePubKey(String activePubKey) {
            this.activePubKey = activePubKey;
            return this;
        }

        public Builder setBuyRamBytes(int buyRamBytes) {
            this.buyRamBytes = buyRamBytes;
            return this;
        }

        public Builder setStakeNetQuantity(String stakeNetQuantity) {
            this.stakeNetQuantity = stakeNetQuantity;
            return this;
        }

        public Builder setStakeCpuQuantity(String stakeCpuQuantity) {
            this.stakeCpuQuantity = stakeCpuQuantity;
            return this;
        }

        public Builder setTransfer(boolean transfer) {
            this.transfer = transfer;
            return this;
        }

        public NewAccountOption build(){
            return new NewAccountOption(
                    creator,newAccount,ownerPubKey,
                    activePubKey,buyRamBytes,
                    stakeNetQuantity,stakeCpuQuantity,transfer);
        }
    }
}
