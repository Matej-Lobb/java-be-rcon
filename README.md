# Library for BattlEye Rcon server.

![Build Project](https://github.com/Matej-Lobb/java-be-rcon/workflows/Build%20Project/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=mlobb_java-be-rcon&metric=alert_status)](https://sonarcloud.io/dashboard?id=mlobb_java-be-rcon)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=mlobb_java-be-rcon&metric=bugs)](https://sonarcloud.io/dashboard?id=mlobb_java-be-rcon)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=mlobb_java-be-rcon&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=mlobb_java-be-rcon)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=mlobb_java-be-rcon&metric=coverage)](https://sonarcloud.io/dashboard?id=mlobb_java-be-rcon)
[![Release](https://img.shields.io/github/v/release/mlobb/java-be-rcon)](https://github.com/mlobb/java-be-rcon/releases)
[![CodeFactor](https://www.codefactor.io/repository/github/matej-lobb/java-be-rcon/badge)](https://www.codefactor.io/repository/github/matej-lobb/java-be-rcon)

## Dependency
``` xml
<dependency>
  <groupId>sk.mlobb</groupId>
  <artifactId>be-rcon</artifactId>
  <version>1.1.0</version>
</dependency>
```
## Requirements
* Java-14 or later

## Informations
* This Library provide implementation for BattlEye RCON protocol
* Definition: http://www.battleye.com/downloads/BERConProtocol.txt

## Features
* The connection is handled automatically.
    * You don't need to reconnect. 
    * Once connection will be established it will keep sending HeartBeats to Rcon Server.
* Send messages to Rcon Server
* Configure your own Rcon Server Messages reader through ResponseHandler.class
* Perform any action to Rcon Server

## Configuration Properties
```java
public class BERconConfiguration {
    private Long keepAliveTime;
    private Long connectionDelay;
    private Long timeoutTime;
}
```
#### Details
```
Keep Alive Time (KAT) 
    - Describing interval for KA messages to Rcon server.
    - In Miliseconds

Connection Delay (CD)
    - To prevent spamming of Rcon server you can define delay.
    - In Miliseconds

Timeout Time (TT)
    - Define a timeout for lost of connection and re-trying of connection.
    - In Miliseconds
```

## Examples
### Instantiation
#### Create new Instance with no logs
```java
    private BERconClient beRconClient(){
        BERconConfiguration beRconConfiguration = new BERconConfiguration();
        return new BERconClient(beRconConfiguration);
    }
```
#### With Custom Logging
```java
    private BERconClient beRconClient(){
        BERconConfiguration beRconConfiguration = new BERconConfiguration();
        return new BERconClient(beRconConfiguration, new BELogging());
    }

    static class BELogging implements LogWrapper {
        public void info(String msg) {
            log.info(msg);
        }
        public void info(String msg, Throwable t) {
            log.info(msg, t);
        }
        public void debug(String msg) {
            log.debug(msg);
        }
        public void debug(String msg, Throwable t) {
            log.debug(msg, t);
        }
        public void warn(String msg) {
            log.warn(msg);
        }
        public void warn(String msg, Throwable t) {
            log.warn(msg, t);
        }
    }
```
#### Create Connection
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

  [license-url]: https://github.com/DependencyTrack/dependency-track/blob/develop/LICENSE.txt
