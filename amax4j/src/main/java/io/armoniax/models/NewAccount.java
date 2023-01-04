package io.armoniax.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

@NoArgsConstructor
@Data
public class NewAccount {

    @SerializedName("creator")
    @NotNull
    private String creator;
    @SerializedName("name")
    @NotNull
    private String name;
    @SerializedName("owner")
    @NotNull
    private OwnerOrActive owner;
    @SerializedName("active")
    @NotNull
    private OwnerOrActive active;

    @NoArgsConstructor
    @Data
    public static class OwnerOrActive {
        @SerializedName("threshold")
        private Integer threshold;
        @SerializedName("keys")
        private List<KeysDTO> keys;
        @SerializedName("accounts")
        private List<?> accounts;
        @SerializedName("waits")
        private List<?> waits;

        OwnerOrActive(Integer threshold, List<KeysDTO> keys, List<?> accounts, List<?> waits) {
            this.threshold = threshold;
            this.keys = keys;
            this.accounts = accounts;
            this.waits = waits;
        }

        @NoArgsConstructor
        @Data
        public static class KeysDTO {
            @SerializedName("key")
            private String key;
            @SerializedName("weight")
            private Integer weight;

            KeysDTO(String key, Integer weight) {
                this.key = key;
                this.weight = weight;
            }
        }
    }

    public static NewAccount getNewAccount(
            String creator, String accountName, String ownerPubKey, String activePubKey) {
        NewAccount account = new NewAccount();
        account.creator = creator;
        account.name = accountName;
        account.owner = new NewAccount.OwnerOrActive(
                1, Collections.singletonList(
                        new OwnerOrActive.KeysDTO(ownerPubKey, 1)),
                Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        account.active = new NewAccount.OwnerOrActive(
                1, Collections.singletonList(
                new OwnerOrActive.KeysDTO(activePubKey, 1)),
                Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        return account;
    }
}
