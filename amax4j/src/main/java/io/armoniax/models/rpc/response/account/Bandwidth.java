
package io.armoniax.models.rpc.response.account;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
@SuppressWarnings("unused")
public class Bandwidth {
    @SerializedName("cpu_weight")
    private String cpuWeight;
    @Expose
    private String from;
    @SerializedName("net_weight")
    private String netWeight;
    @Expose
    private String to;
}
