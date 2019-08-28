# Library for BattlEye Rcon server.

[![Build Status](https://travis-ci.org/mlobb/java-be-rcon.svg?branch=master)](https://travis-ci.org/mlobb/java-be-rcon)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=mlobb_java-be-rcon&metric=alert_status)](https://sonarcloud.io/dashboard?id=mlobb_java-be-rcon)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=mlobb_java-be-rcon&metric=bugs)](https://sonarcloud.io/dashboard?id=mlobb_java-be-rcon)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=mlobb_java-be-rcon&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=mlobb_java-be-rcon)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=mlobb_java-be-rcon&metric=coverage)](https://sonarcloud.io/dashboard?id=mlobb_java-be-rcon)

## Requirements
* Java-11 or later

## Informations
* This Library provide implementation for RCON protocol
* Definition: http://www.battleye.com/downloads/BERConProtocol.txt

## Features
* Connection is handle automatically
* You don't need to reconnect every due once connection will be established it will keep sending HeartBeats to Rcon Server.
* Send messages to Rcon Server
* Configure your own Rcon Server Messages reader through ResponseHandler.class

## Examples
#### Create Instance and Connection
```java
InetSocketAddress hostAddress = new InetSocketAddress("127.0.0.1", 8896);                
BELoginCredential loginCredential = new BELoginCredential(hostAddress, "password");
battleEyeRConClient.connect(loginCredential);
```

#### Custom Output Reader
```java
battleEyeRConClient.addResponseHandler(response -> log.info("Rcon Response: {}", response));
```

#### Send Message
```java
battleEyeRConClient.sendCommand(DayzBECommandType.Say, -1, "Message to all players !");
```

## Copyright & License
BattlEye Rcon Library (c) Matej Lobb. All Rights Reserved.

Permission to modify and redistribute is granted under the terms of the 
[Apache License 2.0] [license-url]

  [license-url]: https://github.com/DependencyTrack/dependency-track/blob/master/LICENSE.txt
