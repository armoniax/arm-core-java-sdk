package io.armoniax.signprovider.errors;

import io.armoniax.error.signatureProvider.SignatureProviderError;

public class PublicKeyConversionError extends SignatureProviderError {

    public PublicKeyConversionError(String message){
        super(message);
    }
}
