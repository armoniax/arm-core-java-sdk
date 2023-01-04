
package io.armoniax.models.rpc.response.account;

import java.util.List;

import com.google.gson.annotations.Expose;
import lombok.Data;

@Data
@SuppressWarnings("unused")
public class RequiredAuth {

    @Expose
    private List<Account> accounts;
    @Expose
    private List<Key> keys;
    @Expose
    private Long threshold;
    @Expose
    private List<Object> waits;
}
