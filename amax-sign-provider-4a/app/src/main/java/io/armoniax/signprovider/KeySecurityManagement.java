package io.armoniax.signprovider;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Calendar;

import javax.crypto.Cipher;

public class KeySecurityManagement {
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String AMAX_PRIVATE_KEY_ALIAS = "amax_private_key_alias";


    private boolean hasKey(String keyAlias) {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            return keyStore.containsAlias(keyAlias);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void generateKey(String keyAlias) {
        AlgorithmParameterSpec spec = null;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", ANDROID_KEYSTORE);
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            end.add(Calendar.YEAR, 999);
            // Android6.0（API23）
            spec = new KeyGenParameterSpec.Builder(
                    keyAlias, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setDigests(KeyProperties.DIGEST_SHA512)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                    .setCertificateNotBefore(start.getTime())
                    .setCertificateNotAfter(end.getTime())
                    .build();
            keyPairGenerator.initialize(spec);
            keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] encrypt(String keyAlias, byte[] data) {
        if (!hasKey(keyAlias)) {
            generateKey(keyAlias);
        }
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            KeyStore.Entry entry = keyStore.getEntry(keyAlias, null);
            if (entry instanceof KeyStore.PrivateKeyEntry) {
                PublicKey publicKey = ((KeyStore.PrivateKeyEntry) entry).getCertificate().getPublicKey();
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.ENCRYPT_MODE, publicKey);
                return cipher.doFinal(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] decrypt(String keyAlias, byte[] data) {
        try {
            if(hasKey(keyAlias)){
                KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
                keyStore.load(null);
                KeyStore.Entry entry = keyStore.getEntry(keyAlias, null);
                if (entry instanceof KeyStore.PrivateKeyEntry) {
                    PrivateKey privateKey = ((KeyStore.PrivateKeyEntry) entry).getPrivateKey();
                    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                    cipher.init(Cipher.DECRYPT_MODE, privateKey);
                    return cipher.doFinal(data);
                }
            }else {
                throw new Exception("The passed in keyAlias does not exist!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getRawKey(String key) {
        byte[] contentByte = decrypt(AMAX_PRIVATE_KEY_ALIAS, Base64.decode(key, Base64.NO_WRAP));
        return Base64.encodeToString(contentByte, Base64.NO_WRAP);
    }

    public String getSafeKey(String key) {
        byte[] contentByte = encrypt(AMAX_PRIVATE_KEY_ALIAS, key.getBytes());
        return Base64.encodeToString(contentByte, Base64.NO_WRAP);
    }


}
