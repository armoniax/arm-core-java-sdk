package io.armoniax.signprovider;

import static io.armoniax.signprovider.errors.ErrorString.CONVERT_EC_TO_EOS_INVALID_INPUT_KEY;
import static io.armoniax.signprovider.errors.ErrorString.DELETE_KEY_KEYSTORE_GENERIC_ERROR;
import static io.armoniax.signprovider.errors.ErrorString.GENERATE_KEY_ECGEN_MUST_USE_SECP256R1;
import static io.armoniax.signprovider.errors.ErrorString.GENERATE_KEY_KEYGENSPEC_MUST_USE_EC;
import static io.armoniax.signprovider.errors.ErrorString.GENERATE_KEY_MUST_HAS_PURPOSE_SIGN;
import static io.armoniax.signprovider.errors.ErrorString.QUERY_ANDROID_KEYSTORE_GENERIC_ERROR;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Pair;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import io.armoniax.error.utilities.EOSFormatterError;
import io.armoniax.signprovider.errors.AndroidKeyStoreDeleteError;
import io.armoniax.signprovider.errors.AndroidKeyStoreSigningError;
import io.armoniax.signprovider.errors.InvalidKeyGenParameter;
import io.armoniax.signprovider.errors.PublicKeyConversionError;
import io.armoniax.signprovider.errors.QueryAndroidKeyStoreError;
import io.armoniax.tools.EOSFormatter;

public class AndroidKeyStoreUtility {
    private static final int ANDROID_PUBLIC_KEY_OID_ID = 0;
    private static final int EC_PUBLICKEY_OID_INDEX = 0;
    private static final int SECP256R1_OID_INDEX = 1;
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String ANDROID_ECDSA_SIGNATURE_ALGORITHM = "SHA256withECDSA";
    private static final String SECP256R1_CURVE_NAME = "secp256r1";
    private static final String PEM_OBJECT_TYPE_PUBLIC_KEY = "PUBLIC KEY";


    /**
     * Generate a new key inside Android KeyStore by the given [keyGenParameterSpec] and return the new key in EOS format
     *
     * The given [keyGenParameterSpec] is the parameter specification to generate a new key. This specification
     * must include the following information if the key to be generated needs to be EOS Mainnet compliant:
     *
     * - [KeyGenParameterSpec] must include [KeyProperties.PURPOSE_SIGN]
     * - [KeyGenParameterSpec.getAlgorithmParameterSpec] must be of type [ECGenParameterSpec]
     * - [KeyGenParameterSpec.getAlgorithmParameterSpec]'s curve name must be [SECP256R1_CURVE_NAME]
     */
    public static String generateAndroidKeyStoreKey(KeyGenParameterSpec keyGenParameterSpec) throws InvalidKeyGenParameter, NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException, PublicKeyConversionError, EOSFormatterError {
        // Parameter Spec must include PURPOSE_SIGN
        if ((KeyProperties.PURPOSE_SIGN & keyGenParameterSpec.getPurposes()) != KeyProperties.PURPOSE_SIGN) {
            throw new InvalidKeyGenParameter(GENERATE_KEY_MUST_HAS_PURPOSE_SIGN);
        }

        // Parameter Spec's algorithm spec must be of type ECGenParameterSpec
        if (!(keyGenParameterSpec.getAlgorithmParameterSpec() instanceof ECGenParameterSpec)) {
            throw new InvalidKeyGenParameter(GENERATE_KEY_KEYGENSPEC_MUST_USE_EC);
        }

        // The curve of Parameter Spec's algorithm must be SECP256R1
        if (!((ECGenParameterSpec) (keyGenParameterSpec.getAlgorithmParameterSpec())).getName().equals(SECP256R1_CURVE_NAME)) {
            throw new InvalidKeyGenParameter(GENERATE_KEY_ECGEN_MUST_USE_SECP256R1);
        }

        KeyPairGenerator kpg  =
                KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, ANDROID_KEYSTORE);

        kpg.initialize(keyGenParameterSpec);

        KeyPair newKeyPair  = kpg.generateKeyPair();
        return convertAndroidKeyStorePublicKeyToEOSFormat((ECPublicKey)newKeyPair.getPublic());
    }


    /**
     * Generate a new key inside AndroidKeyStore by the given [alias] and return the new key in EOS format
     *
     * The given [alias] is the identity of the key. The new key will be generated with the Default [KeyGenParameterSpec] from the [generateDefaultKeyGenParameterSpecBuilder]
     */
    public static String generateAndroidKeyStoreKey(String alias) throws InvalidAlgorithmParameterException, InvalidKeyGenParameter, NoSuchAlgorithmException, NoSuchProviderException, IOException, PublicKeyConversionError, EOSFormatterError {
        // Create a default KeyGenParameterSpec
        KeyGenParameterSpec keyGenParameterSpec = generateDefaultKeyGenParameterSpecBuilder(alias).build();

        return generateAndroidKeyStoreKey(keyGenParameterSpec);
    }

    /**
     * Convert an ECPublic Key (SECP256R1) that resides in the Android KeyStore to EOS format
     * @param androidECPublicKey ECPublicKey - the ECPublic Key (SECP256R1) to convert
     * @return String - EOS format of the provided key
     */
    private static String convertAndroidKeyStorePublicKeyToEOSFormat(ECPublicKey androidECPublicKey) throws IOException, PublicKeyConversionError, EOSFormatterError {
        // Read the key information using the supported ASN.1 standard.
        ASN1InputStream bIn  = new ASN1InputStream(new ByteArrayInputStream(androidECPublicKey.getEncoded()));
        ASN1Sequence asn1Sequence = (ASN1Sequence) (bIn.readObject()).toASN1Primitive();

        // Verify if the key is ECPublicKey and SECP256R1
        ASN1Encodable[] publicKeyOID =
                ((ASN1Sequence)(asn1Sequence.getObjectAt(ANDROID_PUBLIC_KEY_OID_ID))).toArray();

        if (!X9ObjectIdentifiers.id_ecPublicKey.getId().equals(publicKeyOID[EC_PUBLICKEY_OID_INDEX].toString())
                || !X9ObjectIdentifiers.prime256v1.getId().equals(publicKeyOID[SECP256R1_OID_INDEX].toString())
        ) {
            throw new PublicKeyConversionError(CONVERT_EC_TO_EOS_INVALID_INPUT_KEY);
        }

        StringWriter stringWriter = new StringWriter();
        PemWriter pemWriter = new PemWriter(stringWriter);
        PemObject pemObject = new PemObject(PEM_OBJECT_TYPE_PUBLIC_KEY, asn1Sequence.getEncoded());
        pemWriter.writeObject(pemObject);
        pemWriter.flush();

        String pemFormattedPublicKey  = stringWriter.toString();

        return EOSFormatter.convertPEMFormattedPublicKeyToEOSFormat(pemFormattedPublicKey, false);
    }

    /**
     * Get all (SECP256R1) curve keys in EOS format from Android KeyStore
     * @param password KeyStore.ProtectionParameter? - the password to load all the keys
     * @param loadStoreParameter KeyStore.LoadStoreParameter? - the KeyStore Parameter to load the KeyStore instance
     *
     * @return List<String> - List of SECP256R1 keys inside Android KeyStore (EOS Format)
     */
    public static List<Pair<String, String>> getAllAndroidKeyStoreKeysInEOSFormat(
            KeyStore.ProtectionParameter password,
            KeyStore.LoadStoreParameter loadStoreParameter
    ) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException, UnrecoverableEntryException, InvalidKeySpecException, EOSFormatterError, PublicKeyConversionError {
        List<Pair<String, String>> aliasKeyPair = new ArrayList();
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(loadStoreParameter);
        List<String> aliases = Collections.list(keyStore.aliases());

        for (String alias : aliases) {
            KeyStore.Entry keyEntry = keyStore.getEntry(alias, password);
            if (keyEntry instanceof KeyStore.PrivateKeyEntry) {
                ECPublicKey ecPublicKey = (ECPublicKey) KeyFactory.getInstance(((KeyStore.PrivateKeyEntry) keyEntry).getCertificate().getPublicKey().getAlgorithm())
                        .generatePublic(new X509EncodedKeySpec(((KeyStore.PrivateKeyEntry) keyEntry).getCertificate().getPublicKey().getEncoded()));

                aliasKeyPair.add(
                        new Pair(alias,convertAndroidKeyStorePublicKeyToEOSFormat(ecPublicKey))
                );
            }
        }

        return aliasKeyPair;
    }

    /**
     * Get all (SECP256R1) keys in EOS format from Android KeyStore
     * @param alias String - the key's identity
     * @param password KeyStore.ProtectionParameter? - the password to load all the keys
     * @param loadStoreParameter KeyStore.LoadStoreParameter? - the KeyStore Parameter to load the KeyStore instance
     * @return String - the SECP256R1 key in the Android KeyStore
     */
    public static String getAndroidKeyStoreKeyInEOSFormat(
            String alias ,
            KeyStore.ProtectionParameter password,
            KeyStore.LoadStoreParameter loadStoreParameter
    ) throws QueryAndroidKeyStoreError {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(loadStoreParameter);

            KeyStore.PrivateKeyEntry keyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, password);
            ECPublicKey ecPublicKey = (ECPublicKey) KeyFactory.getInstance(keyEntry.getCertificate().getPublicKey().getAlgorithm()).generatePublic(
                    new X509EncodedKeySpec(keyEntry.getCertificate().getPublicKey().getEncoded())
            );

            return convertAndroidKeyStorePublicKeyToEOSFormat(ecPublicKey);
        } catch (Exception ex) {
            throw new QueryAndroidKeyStoreError(QUERY_ANDROID_KEYSTORE_GENERIC_ERROR, ex);
        }
    }

    /**
     * Sign data with a key in the KeyStore.
     *
     * @param data ByteArray - data to be signed
     * @param alias String - identity of the key to be used for signing
     * @param password KeyStore.ProtectionParameter - password of the key
     * @return Binary version of the signature
     * @throws AndroidKeyStoreSigningError
     */
    public static byte[] sign(byte[] data, String alias,
                       KeyStore.ProtectionParameter password,
                       KeyStore.LoadStoreParameter loadStoreParameter
    ) throws AndroidKeyStoreSigningError {
        try {
            KeyStore ks  = KeyStore.getInstance(ANDROID_KEYSTORE);
            ks.load(loadStoreParameter);

            KeyStore.PrivateKeyEntry key = (KeyStore.PrivateKeyEntry) ks.getEntry(alias, password);

            Signature sign = Signature.getInstance(ANDROID_ECDSA_SIGNATURE_ALGORITHM);
            sign.initSign(key.getPrivateKey());
            sign.update(data);
            return sign.sign();
        } catch (Exception ex) {
            throw new AndroidKeyStoreSigningError(ex);
        }
    }

    /**
     * Delete a key inside Android KeyStore by its alias
     *
     * @param keyAliasToDelete String - the alias of the key to delete
     * @param loadStoreParameter KeyStore.LoadStoreParameter? - the KeyStore Parameter to load the KeyStore instance
     * @throws AndroidKeyStoreDeleteError
     */
    public static Boolean deleteKeyByAlias(String keyAliasToDelete , KeyStore.LoadStoreParameter loadStoreParameter) throws AndroidKeyStoreDeleteError {
        try {
            KeyStore ks  = KeyStore.getInstance(ANDROID_KEYSTORE);
            ks.load(loadStoreParameter);

            ks.deleteEntry(keyAliasToDelete);

            // If the key still exists, return false. Otherwise, return true
            return !ks.containsAlias(keyAliasToDelete);
        } catch (Exception ex) {
            throw new AndroidKeyStoreDeleteError(DELETE_KEY_KEYSTORE_GENERIC_ERROR, ex);
        }
    }

    /**
     * Delete all keys in the Android KeyStore
     *
     * @param loadStoreParameter KeyStore.LoadStoreParameter? - the KeyStore Parameter to load the KeyStore instance
     */
    public static void deleteAllKeys(KeyStore.LoadStoreParameter loadStoreParameter) throws AndroidKeyStoreDeleteError {
        try {
            KeyStore ks = KeyStore.getInstance(ANDROID_KEYSTORE);
            ks.load(loadStoreParameter);

            List<String> aliases = Collections.list(ks.aliases());
            for (String it : aliases) {
                ks.deleteEntry(it);
            }
        } catch (Exception ex) {
            throw new AndroidKeyStoreDeleteError(DELETE_KEY_KEYSTORE_GENERIC_ERROR, ex);
        }
    }

    /**
     * Generate a default [KeyGenParameterSpec.Builder] with
     *
     * [KeyProperties.DIGEST_SHA256] as its digest
     *
     * [ECGenParameterSpec] as its algorithm parameter spec
     *
     * [SECP256R1_CURVE_NAME] as its EC curve
     *
     * @return KeyGenParameterSpec
     */
    private static KeyGenParameterSpec.Builder generateDefaultKeyGenParameterSpecBuilder(String alias) {
        return new KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_SIGN)
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setAlgorithmParameterSpec(new ECGenParameterSpec(SECP256R1_CURVE_NAME));
    }
}
