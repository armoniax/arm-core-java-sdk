package io.armoniax.signprovider.errors;

public class ErrorString {
    public static final String CONVERT_EC_TO_EOS_INVALID_INPUT_KEY = "Input key is invalid! It must be an EC Public key in a SECP256R1 curve!";
    public static final String SIGN_TRANSACTION_PREPARE_FOR_SIGNING_GENERIC_ERROR = "Something went wrong on preparing transaction for signing! serialized transaction content = [%s]";
    public static final String SIGN_TRANSACTION_UNABLE_TO_FIND_KEY_TO_SIGN = "The requested key for signing is not available in the Android KeyStore.";
    public static final String SIGN_TRANSACTION_RAW_SIGNATURE_IS_NULL = "Signature from Android KeyStore is NULL!";
    public static final String SIGN_TRANS_EMPTY_CHAIN_ID = "Chain id cannot be empty!";
    public static final String QUERY_ANDROID_KEYSTORE_GENERIC_ERROR = "Something went wrong while querying key(s) in Android KeyStore!";
    public static final String DELETE_KEY_KEYSTORE_GENERIC_ERROR = "Something went wrong while deleting key(s) in AndroidKeyStore!";
    public static final String GENERATE_KEY_KEYGENSPEC_MUST_USE_EC = "KeyGenParameterSpec must use ECGenParameterSpec for its algorithm!";
    public static final String GENERATE_KEY_ECGEN_MUST_USE_SECP256R1 = "ECGenParameterSpec must use a SECP256R1 curve!";
    public static final String GENERATE_KEY_MUST_HAS_PURPOSE_SIGN = "KeyGenParameterSpec must include KeyProperties.PURPOSE_SIGN!";
}
