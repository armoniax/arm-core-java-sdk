
package io.armoniax.models.rpc.response.account;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
@SuppressWarnings("unused")
public class Account {

    @SerializedName("account_name")
    private String accountName;
    @SerializedName("core_liquid_balance")
    private String coreLiquidBalance;
    @SerializedName("cpu_limit")
    private Limit cpuLimit;
    @SerializedName("cpu_weight")
    private Long cpuWeight;
    @Expose
    private String created;
    @SerializedName("head_block_num")
    private Long headBlockNum;
    @SerializedName("head_block_time")
    private String headBlockTime;
    @SerializedName("last_code_update")
    private String lastCodeUpdate;
    @SerializedName("net_limit")
    private Limit netLimit;
    @SerializedName("net_weight")
    private Long netWeight;
    @Expose
    private List<Permission> permissions;
    @Expose
    private Boolean privileged;
    @SerializedName("ram_quota")
    private Long ramQuota;
    @SerializedName("ram_usage")
    private Long ramUsage;
    @SerializedName("refund_request")
    private Object refundRequest;
    @SerializedName("rex_info")
    private Object rexInfo;
    @SerializedName("self_delegated_bandwidth")
    private Bandwidth selfDelegatedBandwidth;
    @SerializedName("total_resources")
    private TotalResources totalResources;
    @SerializedName("voter_info")
    private VoterInfo voterInfo;
}
