![Java Logo](img/java-logo.png)
# EOSIO SDK for Java: ABIEOS Serialization Provider

[![Software License](https://img.shields.io/badge/license-MIT-lightgrey.svg)](./LICENSE)
![Language Java](https://img.shields.io/badge/Language-C%2B%2B%2FJava-yellow.svg)
![](https://img.shields.io/badge/Deployment%20Target-JVM-blue.svg)

ABIEOS Serialization Provider is a pluggable serialization provider for EOSIO SDK for Java.

Serialization providers are responsible for ABI-driven transaction and action serialization and deserialization between JSON and binary data representations. This particular serialization provider wraps ABIEOS, a C/C++ library that facilitates this conversion.

*All product and company names are trademarksâ„˘ or registeredÂ® trademarks of their respective holders. Use of them does not imply any affiliation with or endorsement by them.*

## Contents

- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Direct Usage](#direct-usage)
- [Releases](#releases)
- [Want to Help?](#want-to-help)
- [License & Legal](#license)

## Prerequisites

* Eclipse 4.5+, Intellij 2019+, or other Java IDE
* Host C++ compiler that is supported by the Gradle cpp-library plugin (GCC, Clang, LLVM, Visual Studio C++) that conforms to C++ standard 17
* Gradle 4.10.1+
* Gradle Plugin 3.3.0+
* Java SE 8+
* Docker (if you wish to leverage one of the Docker-based build containers to cross compile for a server environment.) Docker Desktop 2.4.0+ is a good way to install this on a developer machine. Full instructions for building with Docker can be found in the [CONTRIBUTING](CONTRIBUTING.md) file.

This project is compatible with server-side Java but requires native libraries built on the target machine. Therefore, any project depending on Java Serialization Provider with [EOSIO SDK for Java](https://github.com/EOSIO/eosio-java) **must use a version built for the target server host and be a server-side Java project**.

If you need support for ABIEOS serialization on Android, please see the [Android ABIEOS Serialization Provider](https://github.com/EOSIO/eosio-java-android-abieos-serialization-provider) project.

Other serialization providers can be created to support other target platforms. If your project requires alternate platform support, or if you'd like to create a serialization provider and have questions, please reach out to us by [logging an issue](/../../issues/new).

## Installation

ABIEOS Serialization Provider is intended to be used in conjunction with [EOSIO SDK for Java](https://github.com/EOSIO/eosio-java) as a provider plugin.

To use ABIEOS Serialization Provider with EOSIO SDK for Java in your app, add the following modules to your `build.gradle`:

```groovy
implementation 'one.block:eosiojava:1.0.0'
implementation 'one.block:eosio-java-abieos-serialization-provider:1.0.0'
```

The `build.gradle` files for the project currently include configurations for publishing the project to Artifactory. These should be removed if you are not planning to use Artifactory or you will encounter build errors. To do so, make the changes marked by comments throughout the files.

Then refresh your gradle project.

Now ABIEOS Serialization Provider is ready for use within EOSIO SDK for Java according to the [EOSIO SDK for Java Basic Usage instructions](https://github.com/EOSIO/eosio-java/tree/master#basic-usage).

## Direct Usage

If you wish to use ABIEOS Serialization Provider directly, its public methods can be called like this:

```java
try {
    AbiEos abieos = new AbiEosSerializationProviderImpl()
} catch (SerializationProviderError serializationProviderError) {
    serializationProviderError.printStackTrace();
}

String hex = "1686755CA99DE8E73E1200" // some binary data
String json = "{"name": "John"}" // some JSON

try {
    String jsonToBinaryTransaction = abieos.serializeTransaction(json)
} catch (SerializeTransactionError err) {
    err.printStackTrace();
}

try {
    String binaryToJsonTransaction = abieos.deserializeTransaction(hex)
} catch (DeserializeTransactionError err) {
    err.printStackTrace();
}
```

You should explicitly destroy the provider's context (_i.e._, in a `finally` block), or you could run into a crash when multithreading, per this call:

```
abieos.destroyContext();
```

## Releases

- 11/05/20: Version 1.0.0 Release with EOSIO 3.0 functionality. Docker builds for Alpline and Ubuntu Linux.
- 10/15/20: Version 0.1.3 Initial release as a Java library project. This version is feature compatible with the 0.1.3 version of the [Android ABIEOS Serialization Provider](https://github.com/EOSIO/eosio-java-android-abieos-serialization-provider) project.

## Want to help?

Interested in contributing? That's awesome! Here are some [Contribution Guidelines](./CONTRIBUTING.md) and the [Code of Conduct](./CONTRIBUTING.md#conduct).

We're always looking for ways to improve this library. Check out our [#enhancement Issues](/../../issues?q=is%3Aissue+is%3Aopen+label%3Aenhancement) for ways you can pitch in.

## License

[MIT](./LICENSE)

## Important

See LICENSE for copyright and license terms.  Block.one makes its contribution on a voluntary basis as a member of the EOSIO community and is not responsible for ensuring the overall performance of the software or any related applications.  We make no representation, warranty, guarantee or undertaking in respect of the software or any related documentation, whether expressed or implied, including but not limited to the warranties of merchantability, fitness for a particular purpose and noninfringement. In no event shall we be liable for any claim, damages or other liability, whether in an action of contract, tort or otherwise, arising from, out of or in connection with the software or documentation or the use or other dealings in the software or documentation. Any test results or performance figures are indicative and will not reflect performance under all conditions.  Any reference to any third party or third-party product, service or other resource is not an endorsement or recommendation by Block.one.  We are not responsible, and disclaim any and all responsibility and liability, for your use of or reliance on any of these resources. Third-party resources may be updated, changed or terminated at any time, so the information here may be out of date or inaccurate.  Any person using or offering this software in connection with providing software, goods or services to third parties shall advise such third parties of these license terms, disclaimers and exclusions of liability.  Block.one, EOSIO, EOSIO Labs, EOS, the heptahedron and associated logos are trademarks of Block.one.

Wallets and related components are complex software that require the highest levels of security.  If incorrectly built or used, they may compromise usersâ€™ private keys and digital assets. Wallet applications and related components should undergo thorough security evaluations before being used.  Only experienced developers should work with this software.
