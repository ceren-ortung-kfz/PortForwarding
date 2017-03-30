# JavaPortForwarding
This script forwards a number of configured local ports to local or remote ports to remote.

## Build
```sh
./gradlew build
```

## Run
```sh
cd JavaPortForwarding
java -cp ./build/classes/main/ portforwarding.PortForwarding 10040 127.0.0.1:10041
```

## Run with multiple targets
```sh
cd JavaPortForwarding
java -cp ./build/classes/main/ portforwarding.PortForwarding 10040 127.0.0.1:10041 127.0.0.1:10042
```
