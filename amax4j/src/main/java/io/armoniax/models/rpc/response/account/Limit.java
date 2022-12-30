
package io.armoniax.models.rpc.response.account;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
@SuppressWarnings("unused")
public class Limit {

    @Expose
    private Long available;
    @SerializedName("current_used")
    private Long currentUsed;
    @SerializedName("last_usage_update_time")
    private String lastUsageUpdateTime;
    @Expose
    private Long max;
    @Expose
    private Long used;

}
