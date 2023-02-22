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
    private AmaxClient mClient;
    private String mPubKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
//        createAccount();
//        transfer();
//        buyMemory();
//        stakeCpuAndNet();
    }

    private void init() {
        try {
            // Generate public and private keys
            mPubKey = AndroidKeyStoreUtility.generateAndroidKeyStoreKey(UUID.randomUUID().toString());
            AmaxOption option = AmaxOption.builder()
                    .setUrl("https://test-chain.ambt.art/")
                    .setSerializationProvider(new AbiSerializationProviderImpl())
                    .setAmaxSignKind(AmaxSignKind.KEYSTORE)
                    .setSignatureProvider(AmaxAndroidKeyStoreSignProvider.builder().build())
                    .build();
            mClient = AmaxClientFactory.getAmaxClient(option);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createAccount() {
        try {
            // create new account
            mClient.createAccount(NewAccountOption.builder()
                    .setTransfer(true)
                    .setCreator("merchantxpro")
                    .setNewAccount("bruceying123")
                    .setBuyRamBytes(1024 * 8)
                    .setStakeCpuQuantity("10.00000000 AMAX")
                    .setStakeNetQuantity("10.00000000 AMAX")
                    .setOwnerPubKey(mPubKey)
                    .setActivePubKey(mPubKey)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void transfer() {
        try {
            mClient.transfer("merchantxpro", "bruceying123", "0.60000000 AMAX", "this is test!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void buyMemory() {
        try {
            mClient.buyRam("bruceying123", "bruceying123", 8196);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stakeCpuAndNet() {
        try {
            mClient.stakeCpuAndNet("bruceying123", "bruceying123", "10.0000 AMAX", "10.0000 AMAX", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}