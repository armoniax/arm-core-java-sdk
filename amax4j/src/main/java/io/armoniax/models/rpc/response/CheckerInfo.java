
package io.armoniax.models.rpc.response;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class CheckerInfo {
    @Expose
    private Boolean more;
    @SerializedName("next_key")
    private String nextKey;
    @Expose
    private List<String> rows;
}
