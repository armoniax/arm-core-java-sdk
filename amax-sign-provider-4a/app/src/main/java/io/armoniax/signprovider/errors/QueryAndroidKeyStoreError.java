package io.armoniax.signprovider.errors;

import io.armoniax.error.signatureProvider.GetAvailableKeysError;

public class QueryAndroidKeyStoreError extends GetAvailableKeysError {
    public QueryAndroidKeyStoreError(String message){
        super(message);
    }

    public QueryAndroidKeyStoreError(String message,Exception e){
        super(message,e);
    }
}
