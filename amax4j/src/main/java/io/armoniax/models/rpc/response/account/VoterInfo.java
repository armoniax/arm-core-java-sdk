
package io.armoniax.models.rpc.response.account;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
@SuppressWarnings("unused")
public class VoterInfo {

    @Expose
    private Long flags1;
    @SerializedName("is_proxy")
    private Long isProxy;
    @SerializedName("last_vote_weight")
    private String lastVoteWeight;
    @Expose
    private String owner;
    @Expose
    private List<Object> producers;
    @SerializedName("proxied_vote_weight")
    private String proxiedVoteWeight;
    @Expose
    private String proxy;
    @Expose
    private Long reserved2;
    @Expose
    private String reserved3;
    @Expose
    private Long staked;
}
