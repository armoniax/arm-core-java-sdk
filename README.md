# arm-core-java-sdk
Armonia RealMe Account Core Java SDK


## Android
Configure warehouse address：
```
        maven {
            url "https://s01.oss.sonatype.org/content/repositories/snapshots/"
        }
```

Add the required dependencies to the Android project:

```
    implementation 'io.github.armoniax:amax4j:1.0.0-SNAPSHOT'
    implementation 'io.github.armoniax:amax-sign-provider-4a:1.0.0-SNAPSHOT'
    implementation 'io.github.armoniax:amax-serialization-provider-4a:1.0.0-SNAPSHOT'
```

You can view the [sample project](https://github.com/arcticfox88/arm-core-java-sdk/tree/main/AmaxExample).


###  Usage

You can refer to the [example project](https://github.com/arcticfox88/arm-core-java-sdk/tree/main/AmaxExample).

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

        client.stakeCpuAndNet("bruceying123","bruceying123","10.0000 AMAX","10.0000 AMAX",true);
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