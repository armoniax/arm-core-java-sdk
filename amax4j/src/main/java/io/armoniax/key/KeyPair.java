package io.armoniax.key;


import lombok.Setter;
import lombok.Getter;

@Setter
@Getter
public class KeyPair {
    private String pubKey;
    private String priKey;

    public KeyPair(String pri, String pub){
        priKey = pri;
        pubKey = pub;
    }
}
