# Package the Minecraft 26.3 kinematic variant

This packages an alternative **full TenorClef jar**, not an additional mod.
It preserves the validated base build's compiled classes and embedded dependencies
and adds `KinematicRelease` as the first Fabric main entrypoint. It changes only
mod metadata and adds the initializer class, its source, and base checksum.
The exact base and Ostinato SHA256 values are enforced by the packaging script.

Prerequisites: Python 3.9+, JDK 25 (`javac`), the validated
`altoclef-26.3-0.22.2.jar`, the repository's Ostinato 26.3 jar, and Fabric Loader
0.19.5's jar (compile dependency only; do not put the loader jar in `mods/`).
Use the full jar from the validated Gradle build. The default checksum still targets
the original checkpoint; updates must explicitly supply their recorded checksum.

```sh
python3 tools/release/build_kinematic.py \
  --base /path/to/altoclef-26.3-0.22.2.jar \
  --ostinato libs/baritone-unoptimized-fabric-ostinato-26.3.jar \
  --fabric-loader /path/to/fabric-loader-0.19.5.jar \
  --base-sha256 b5278e9243b49e4ade5f4b97aa3b9e134daf6effb06545ed5f1910a12ec417f2 \
  --version 26.3-0.22.2-kinematic.2 \
  --output /path/to/release/mods
```

Use `--javac /path/to/jdk25/bin/javac` if needed. The output contains the kinematic
TenorClef and unchanged Ostinato jars. Add `fabric-api-0.161.0+26.3.jar`, include
`docs/releases/26.3-kinematic.md` as installation/release notes, and create a
SHA256 manifest and distribution ZIP. Fixed jar timestamps and sorted entries
make output repeatable with the same compiler and inputs.

The regular Gradle build remains documented in the root README. The
packaging script rejects binaries that differ from the supplied base checksum or
the pinned Ostinato checksum. When upgrading, verify the base build against its
validation record and revalidate startup.
A startup check must verify actual Baritone settings after TenorClef initializes,
with no kinematic JVM flag and a previously persisted Tungsten backend; also
check the explicit `-Dtenorclef.kinematic=false` override.

Upload binaries as GitHub Release assets. Existing workflows do not build this
variant automatically; the previously deferred workflow patch does not add it.
