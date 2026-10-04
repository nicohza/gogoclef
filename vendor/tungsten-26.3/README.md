# Tungsten for Minecraft 26.3

Source port of 3ndetz/Tungsten altoclef-compat at
5cb12ad65c0e045aa5a02d017c98df64eeda6d40 (TenorClef vendor pin).
GPL-3.0; see LICENSE. This directory uses official 26.3 names and Java 25.

Build with JDK 21 for Gradle and a JDK 25 toolchain:

```sh
JAVA_HOME_25=/path/to/jdk25 ./gradlew build sourcesJar \
  -Porg.gradle.java.installations.fromEnv=JAVA_HOME_25
```

Install `build/libs/tungsten-fabric-ALPHA-1.6.0-26.3.jar` alongside
TenorClef, Ostinato and Fabric API for 26.3. Do not install the sources jar.
Set `#set movementBackend tungsten` in game. Ostinato still handles mining
and construction; unsupported travel can fall back to Ostinato.

Damaging falls are rejected by default (`ignoreFallDamage=false`). Debug chat is
posted on the client thread, and search/executor services are stopped on exit.
