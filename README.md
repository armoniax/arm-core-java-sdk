Table of Content
- [arm-core-java-sdk](#arm-core-java-sdk)
  - [Configuration](#configuration)
    - [Android](#android)
    - [Local Machine](#local-machine)
      - [Windows](#windows)
      - [Ubuntu](#ubuntu)
      - [MacOS](#macos)
      - [Configure with gradle](#configure-with-gradle)
  - [Usage](#usage)
    - [Android](#android-1)
    - [Local Machine](#local-machine-1)
    - [Other](#other)
    - [Exception handling](#exception-handling)

# arm-core-java-sdk
Armonia RealMe Account Core Java SDK


## Configuration
### Android
Configure warehouse address：
```
maven {
    url "https://s01.oss.sonatype.org/content/repositories/snapshots/"
}
```

Add the required dependencies to the Android project:

```
implementation 'io.github.armoniax:amax4j:1.0.2-SNAPSHOT'
implementation 'io.github.armoniax:amax-sign-provider-4a:1.0.0-SNAPSHOT'
implementation 'io.github.armoniax:amax-serialization-provider-4a:1.0.0-SNAPSHOT'
```

You can view the [sample project](https://github.com/arcticfox88/arm-core-java-sdk/tree/main/AmaxExample).


### Local Machine

Since the serialization library is implemented in C++, you need to compile the serialization library [amax-serialization-provider](https://github.com/arcticfox88/arm-core-java-sdk/tree/main/amax-serialization-provider) when using this SDK on your local machine.

The Android environment has already provided a cross-compilation package, so you don't need to worry about compilation issues. However, local compilation is also not complicated, you just need to follow the steps below:

#### Windows
1. Install the C++ compiler .
   The MinGW compiler is required here, you can open [this page](https://sourceforge.net/projects/mingw-w64/files/) and select the `x86_64-win32-sjlj` or `x86_64-win32-seh` version to download.
2. Extract the downloaded package and add the `bin` directory to the system environment variable Path
3. Reopen a new cmd command line and execute `gcc -v`, which correctly outputs the compiler version information
4. Clone the entire project and `cd amax-serialization-provider`, then run `. /gradlew assemble`, and after successful compilation, generate the `amax-serialization-provider` jar package under `build/libs`.

#### Ubuntu
Execute the following command to install the gcc compiler:

```shell
sudo apt-get update
sudo apt-get install build-essential
```

Next, execute the above step 4 to compile the jar package.

#### MacOS
```shell
brew install gcc
```

Basically the same steps as above.


#### Configure with gradle

Configure warehouse address：
```
maven {
    url "https://s01.oss.sonatype.org/content/repositories/snapshots/"
}
```

Non-Android environments, no need to configure packages with `-4a` suffix,just configure the `amax-serialization-provider` jar package we compiled earlier:

```
implementation 'io.github.armoniax:amax4j:1.0.2-SNAPSHOT'
implementation files('libs/amax-serialization-provider.jar')
```


```java
AmaxOption option = AmaxOption.builder()
        .setUrl("https://test-chain.ambt.art/")
        .setSerializationProvider(new AbiSerializationProviderImpl())
        .setAmaxSignKind(AmaxSignKind.SOFT)
        .setSignatureProvider(new SoftSignatureProviderImpl())
        .build();
```

##  Usage

### Android

You can refer to the [example project](https://github.com/arcticfox88/arm-core-java-sdk/tree/main/AmaxExample).


Here is an example for Android:
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


### Local Machine

In non-Android environments, the SDK is initialized slightly differently, as shown below:

```java
try {
    // Generate public and private keys
    KeyPair keyPair = AmaxTool.createKey();
    System.out.println("private:"+keyPair.getPriKey());
    System.out.println("public:"+keyPair.getPubKey());

    AmaxOption option = AmaxOption.builder()
            .setUrl("https://test-chain.ambt.art")
            .setAmaxSignKind(AmaxSignKind.SOFT)
            .setSignatureProvider(new SoftSignatureProviderImpl())
            .setSerializationProvider(new AbiSerializationProviderImpl())
            .build();
    AmaxClient client = AmaxClientFactory.getAmaxClient(option);

    // import private key
    client.importKey("xxxxxxxxxxxxxx");
    client.transfer("abchftmvb2ic","dcbam3tlgyjli","500.01000000 AMAX","this is test!");
} catch (Exception e) {
    e.printStackTrace();
}
```



### Other

You can also use the utility classes provided with the SDK to generate keys and verify signatures:

```java
// Creating Keys
KeyPair keyPair = AmaxTool.createKey();
String priKey = keyPair.getPriKey();
String pubKey = keyPair.getPubKey();
String message = "123456";

// Signature data
String signature = AmaxTool.sign(priKey,message);
System.out.println("priKey:"+priKey);
System.out.println("pubKey:"+pubKey);
System.out.println("signature:"+signature);

// Verify Signature
boolean r = AmaxTool.verifySignature(pubKey,message,signature);
System.out.println("verify:"+r);
```

### Exception handling
SDK exceptions are chained, but we usually pay more attention to the exceptions returned by the RPC interface. We can traverse the chained exceptions in the following way:

```java
try {
    // import private key
    client.importKey("xxxxxxxxxxxxxx");
    client.transfer("abchftmvb2ic","dcbam3tlgyjli","500.01000000 AMAX","this is test!");
} catch (Exception e) {
    RpcCallError error = Utils.findCause(e,RpcCallError.class);
    if(error != null){
        RpcError rpcError = error.getRpcResponseError().getError();
        System.out.println(rpcError.getName());
        System.out.println(rpcError.getWhat());
        for (Detail detail:rpcError.getDetails()){
            System.out.println(detail.getMessage());
            System.out.println(detail.getFile());
            System.out.println(detail.getMethod());
            System.out.println(detail.getFile());
        }
    }
}
```
