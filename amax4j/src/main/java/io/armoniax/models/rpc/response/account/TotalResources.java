
package io.armoniax.models.rpc.response.account;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
@SuppressWarnings("unused")
public class TotalResources {
    @SerializedName("cpu_weight")
    private String cpuWeight;
    @SerializedName("net_weight")
    private String netWeight;
    @Expose
    private String owner;
    @SerializedName("ram_bytes")
    private Long ramBytes;
}
