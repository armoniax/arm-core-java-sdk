package io.armoniax.client;

import io.armoniax.enums.AmaxSignKind;
import io.armoniax.provider.ISerializationProvider;
import io.armoniax.provider.ISignatureProvider;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class AmaxOption {

    private AmaxOption(
            String baseUrl,AmaxSignKind signKind,
            ISerializationProvider serialProvider,ISignatureProvider signProvider,boolean enableDebug){
        this.baseUrl = baseUrl;
        this.signKind = signKind;
        this.serialProvider = serialProvider;
        this.signProvider = signProvider;
        this.enableDebug = enableDebug;
    }

    private String baseUrl;
    private boolean enableDebug;
    private AmaxSignKind signKind;
    private  ISerializationProvider serialProvider;
    private ISignatureProvider signProvider;

    public static AmaxOption.Builder builder() {
        return new AmaxOption.Builder();
    }

    public static class Builder {
        @NotNull
        private String baseUrl;

        private AmaxSignKind signKind;
        @NotNull
        private  ISerializationProvider serialProvider;

        private ISignatureProvider signProvider;
        private boolean enableDebug;

        public AmaxOption.Builder setUrl(String url) {
            baseUrl = url;
            return this;
        }

        public AmaxOption.Builder setAmaxSignKind(AmaxSignKind kind) {
            signKind = kind;
            return this;
        }

        public AmaxOption.Builder setSerializationProvider(ISerializationProvider serialProvider) {
            this.serialProvider = serialProvider;
            return this;
        }

        public AmaxOption.Builder setSignatureProvider(ISignatureProvider signProvider) {
            this.signProvider = signProvider;
            return this;
        }

        public AmaxOption.Builder setEnableDebug(boolean b){
            enableDebug = b;
            return this;
        }

        public AmaxOption build() {
            if (signKind == null){
                signKind = AmaxSignKind.SOFT;
            }
            return new AmaxOption(baseUrl,signKind,serialProvider,signProvider,enableDebug);
        }
    }
}
