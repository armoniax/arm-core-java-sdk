package com.example.amaxexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.util.UUID;

import io.armoniax.abiserializationprovider.AbiSerializationProviderImpl;
import io.armoniax.client.AmaxClient;
import io.armoniax.client.AmaxClientFactory;
import io.armoniax.client.AmaxOption;
import io.armoniax.client.NewAccountOption;
import io.armoniax.enums.AmaxSignKind;
import io.armoniax.signprovider.AmaxAndroidKeyStoreSignProvider;
import io.armoniax.signprovider.AndroidKeyStoreUtility;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    void test(){
        try {
            // Generate public and private keys
            String pubKey = AndroidKeyStoreUtility.generateAndroidKeyStoreKey(UUID.randomUUID().toString());

            AmaxOption option = AmaxOption.builder()
                    .setUrl("https://test-chain.ambt.art/")
                    .setSerializationProvider(new AbiSerializationProviderImpl())
                    .setAmaxSignKind(AmaxSignKind.KEYSTORE)
                    .setSignatureProvider(AmaxAndroidKeyStoreSignProvider.builder().build())
                    .build();
            AmaxClient client = AmaxClientFactory.getAmaxClient(option);

            // create new account
            client.createAccount(NewAccountOption.builder()
                    .setTransfer(true)
                    .setCreator("merchantxpro")
                    .setNewAccount("bruceying123")
                    .setBuyRamBytes(1024*8)
                    .setStakeCpuQuantity("10.00000000 AMAX")
                    .setStakeNetQuantity("10.00000000 AMAX")
                    .setOwnerPubKey(pubKey)
                    .setActivePubKey(pubKey)
                    .build());

            client.transfer("merchantxpro","bruceying123","0.60000000 AMAX","this is test!");

            client.buyRam("bruceying123","bruceying123",8196);

            client.stakeCpuAndNet("bruceying123","bruceying123","10.0000 EOS","10.0000 EOS",true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}