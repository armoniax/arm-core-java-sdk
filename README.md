# arm-core-java-sdk
Armonia RealMe Account Core Java SDK


## Android

Add the required dependencies to the Android project:

```
    implementation 'org.bouncycastle:bcprov-jdk15on:1.61'
    implementation 'org.bouncycastle:bcpkix-jdk15on:1.61'
    implementation 'com.squareup.retrofit2:retrofit:2.5.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.5.0'
    implementation 'com.squareup.okhttp3:okhttp:3.12.1'
    implementation 'com.squareup.okhttp3:logging-interceptor:3.12.1'
    implementation 'org.bitcoinj:bitcoinj-core:0.15.2'
    implementation 'org.slf4j:slf4j-jdk14:1.7.25'
```

Then, you need at least two AMAX dependent libraries:
`amax4j.jar`、`amax-serialization-provider-4a@release.aar`

If you use `AmaxAndroidKeyStoreSignProvider` to manage private keys, then you also need `amax-sign-provider-4a@release.aar`


###  Simple Example

```java
    try {
        // Generate public and private keys
        KeyPair keyPair = AmaxTool.createKey();

        AmaxOption option = AmaxOption.builder()
                .setUrl("https://test-chain.ambt.art/")
                .setSerializationProvider(new AbiSerializationProviderImpl())
                .build();
        AmaxClient client = AmaxClientFactory.getAmaxClient(option);

        // import private key
        client.importKey(keyPair.getPriKey());

        // create new account
        client.createAccount(NewAccountOption.builder()
                        .setTransfer(true)
                        .setCreator("merchantxpro")
                        .setNewAccount("bruceying123")
                        .setBuyRamBytes(1024*8)
                        .setStakeCpuQuantity("10.00000000 AMAX")
                        .setStakeNetQuantity("10.00000000 AMAX")
                        .setOwnerPubKey(keyPair.getPubKey())
                        .setActivePubKey(keyPair.getPubKey())
                .build());

        client.transfer("merchantxpro","bruceying123","0.60000000 AMAX","this is test!");

        client.buyRam("bruceying123","bruceying123",8196);

        client.stakeCpuAndNet("bruceying123","bruceying123","10.0000 EOS","10.0000 EOS",true);
    } catch (Exception e) {
        e.printStackTrace();
    }
```

The above management of private keys in memory is insecure and not recommended. You can use keystore to manage keys:

```java
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
```

In this way, we don't need to explicitly import the private key, but it is handled by `AmaxAndroidKeyStoreSignProvider`.