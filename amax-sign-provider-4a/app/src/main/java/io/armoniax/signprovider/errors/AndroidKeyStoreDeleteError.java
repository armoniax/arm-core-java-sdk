package io.armoniax.signprovider.errors;

import io.armoniax.error.signatureProvider.SignatureProviderError;

public class AndroidKeyStoreDeleteError extends SignatureProviderError {

    public AndroidKeyStoreDeleteError(String message,Exception e){
        super(message,e);
    }
}
