# README

Using vertx to stream files from disk to an http request "without" loading all to memory. This allows to run this project in memory constrained environments.

Disclaimer: There are other ways to do this, but I wanted to experiment with the Pump mechanism.


### Compile

```mvn clean pakage```

### Run

```java -jar target/download-1.0-SNAPSHOT-fat.jar pathToFile```
