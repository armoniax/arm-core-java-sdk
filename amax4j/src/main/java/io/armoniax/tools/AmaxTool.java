package io.armoniax.tools;

import io.armoniax.key.EccTool;
import io.armoniax.key.KeyPair;

public class AmaxTool {
    /**
     * 生成密钥
     * @return 返回密钥对 KeyPair
     */
    public static KeyPair createKey() {
        String pri = EccTool.createPrivateKey();
        String pub = EccTool.toPublicKey(pri);
        return new KeyPair(pri, pub);
    }

    /**
     * 对字符串数据签名
     * @param pubKey 公钥
     * @param data 数据
     * @return
     */
    public static String sign(String pubKey,String data){
        return  EccTool.sign(pubKey,data);
    }
}
