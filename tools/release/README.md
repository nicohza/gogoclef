# Package the Minecraft 26.3 kinematic variant

This packages an alternative **full TenorClef jar**, not an additional mod.
It preserves the recovered checkpoint's compiled classes and embedded dependencies
and adds `KinematicRelease` as the first Fabric main entrypoint. It changes only
mod metadata and adds the initializer class, its source, and base checksum.
The exact base and Ostinato SHA256 values are enforced by the packaging script.

Prerequisites: Python 3.9+, JDK 25 (`javac`), the recovered
`altoclef-26.3-0.22.2.jar`, the repository's Ostinato 26.3 jar, and Fabric Loader
0.19.5's jar (compile dependency only; do not put the loader jar in `mods/`).
Extract the original TenorClef jar from the previous checkpoint release archive.

```sh
python3 tools/release/build_kinematic.py \
  --base /path/to/altoclef-26.3-0.22.2.jar \
  --ostinato libs/baritone-unoptimized-fabric-ostinato-26.3.jar \
  --fabric-loader /path/to/fabric-loader-0.19.5.jar \
  --output /path/to/release/mods
```

Use `--javac /path/to/jdk25/bin/javac` if needed. The output contains the kinematic
TenorClef and unchanged Ostinato jars. Add `fabric-api-0.161.0+26.3.jar`, include
`docs/releases/26.3-kinematic.md` as installation/release notes, and create a
SHA256 manifest and distribution ZIP. Fixed jar timestamps and sorted entries
make output repeatable with the same compiler and inputs.

The regular Gradle build remains documented in the root README. This pinned
checkpoint packaging script deliberately rejects other base binaries. When
upgrading TenorClef or Ostinato, review and update the pins and revalidate startup.
A startup check must verify actual Baritone settings after TenorClef initializes,
with no kinematic JVM flag and a previously persisted Tungsten backend; also
check the explicit `-Dtenorclef.kinematic=false` override.

Upload binaries as GitHub Release assets. Existing workflows do not build this
variant automatically; the previously deferred workflow patch does not add it.
