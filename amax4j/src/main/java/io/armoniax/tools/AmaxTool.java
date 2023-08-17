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
     * @param priKey 私钥
     * @param data 数据
     * @return
     */
    public static String sign(String priKey,String data){
        return  EccTool.sign(priKey,data);
    }

    /**
     * 校验签名
     * @param publicKey
     * @param message
     * @param signature
     * @return
     */
    public static boolean verifySignature(String publicKey, String message, String signature){
        return  EccTool.verifySignature(publicKey,message,signature);
    }
}
