
package io.armoniax.models.rpc.response.account;

import com.google.gson.annotations.Expose;
import lombok.Data;

@Data
@SuppressWarnings("unused")
public class Key {

    @Expose
    private String key;
    @Expose
    private Long weight;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Long getWeight() {
        return weight;
    }

    public void setWeight(Long weight) {
        this.weight = weight;
    }

}
