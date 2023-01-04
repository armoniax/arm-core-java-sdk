package io.armoniax.signprovider.errors;

import io.armoniax.error.signatureProvider.SignatureProviderError;

public class AndroidKeyStoreSigningError extends SignatureProviderError {
    public AndroidKeyStoreSigningError(Exception ex) {
        super(ex);
    }
}
