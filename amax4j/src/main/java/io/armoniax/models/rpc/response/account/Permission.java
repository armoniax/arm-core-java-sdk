
package io.armoniax.models.rpc.response.account;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
@SuppressWarnings("unused")
public class Permission {

    @Expose
    private String parent;
    @SerializedName("perm_name")
    private String permName;
    @SerializedName("required_auth")
    private RequiredAuth requiredAuth;
}
