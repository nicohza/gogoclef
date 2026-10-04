# TenorClef

TenorClef is an autonomous Minecraft client bot derived from AltoClef. It combines
high-level objective tasks, survival and inventory behavior with the Ostinato
pathfinding engine. Its current focus is reliable autonomous play, speedrun-oriented
tasks, and a modern Fabric target.

TenorClef is not affiliated with AltoClef, Marvion, or MiranCZ. Those projects are
the upstream history of this fork.

> [!WARNING]
> The Minecraft 26.3 port is experimental. Build and startup checks do not establish
> that every autonomous task works in-game; start with a single-player test world.

## Supported versions

TenorClef always runs on [Ostinato](https://github.com/vexrypt-rgb/Ostinato), so it is only built
for the Minecraft versions Ostinato is built for:

| Minecraft | Status | Ostinato | Notes |
| --- | --- | --- | --- |
| 26.3 | Experimental | branch `26.3` (`libs/baritone-unoptimized-fabric-ostinato-26.3.jar`) | Java 25; opt-in via `-Pwith26` |
| 1.21.4 | Primary | `main` (`libs/baritone-unoptimized-fabric-1.21.4.jar`) | Anarchy target; vanilla recipe-book crafting disabled (1.21.2+ servers do not sync recipes) |
| 1.16.1 | Legacy | branch `1.16.1` (`libs/baritone-unoptimized-fabric-1.16.1.jar`) | Legacy pairing |
| 1.21.11 | Experimental | branch `1.21.11` (built from source) | Not a release target |

The other versions under `versions/` (1.21.1 down to 1.16.5) are only steps in the source
preprocessor chain; they are not compiled or released. The complete, version-matched setup is in
[the Ostinato wiring guide](docs/OSTINATO_WIRING.md).

## Minecraft 26.3 (experimental)

The 26.3 module is opt-in: it is only included in the Gradle build with `-Pwith26`, so it cannot
break the other targets. It uses Mojang names (Minecraft 26.x is unobfuscated) and Java 25.

### Requirements

- Minecraft 26.3
- Fabric Loader 0.19.5
- Fabric API `0.161.0+26.3`
- Ostinato jar for 26.3, included in `libs/baritone-unoptimized-fabric-ostinato-26.3.jar`
  (built from the Ostinato branch `26.3`; see the [wiring guide](docs/OSTINATO_WIRING.md))
- Java 25 to run the game

### Install (26.3)

Put these jars in your `mods` folder:

1. `fabric-api-0.161.0+26.3.jar`
2. `altoclef-26.3-<version>.jar` (the full jar, **not** the `-slim` one)
3. `baritone-unoptimized-fabric-ostinato-26.3.jar`

4. `tungsten-fabric-ALPHA-1.6.0-26.3.jar` for Tungsten travel

Build the matching Tungsten jar from `vendor/tungsten-26.3` (see its README), then
run `#set movementBackend tungsten` in chat. Ostinato handles mining and building
and can recover travel when Tungsten cannot finish a route. For an Ostinato-only
setup, omit Tungsten and select `#set movementBackend baritone`.
Do not install duplicate TenorClef, Baritone or Tungsten jars.
The regular jar leaves experimental kinematic travel off on 26.3;
`-Dtenorclef.kinematic=true` explicitly enables it.

### Kinematic release (26.3)

The separate **`altoclef-26.3-0.22.2-kinematic.jar`** enables experimental kinematic
travel and selects the Baritone movement backend automatically at startup. No
launcher flag or manual settings file is needed. Install it **instead of** the
regular AltoClef/TenorClef jar; both have the same mod ID.

Use the `mods/` contents of `tenorclef-mc26.3-0.22.2-kinematic.zip`:

- `altoclef-26.3-0.22.2-kinematic.jar`
- `baritone-unoptimized-fabric-ostinato-26.3.jar`
- `fabric-api-0.161.0+26.3.jar`

Requires Minecraft 26.3, Fabric Loader 0.19.5, and Java 25. Remove duplicate
AltoClef/TenorClef and Baritone jars. Tungsten is optional and is not used for this
preset. An explicit `-Dtenorclef.kinematic=false` still disables kinematic travel.
In-game changes remain possible with `#set movementBackend baritone` and
`#set kinematicTravel true`; the release preset is reapplied on each launch.

Start a task with `@get blaze_rod 3`; stop it with `@stop`.
The 4 October 2026 test of the underlying controller was stopped by the user after
11m 28s at 0/3 rods while approaching a discovered blaze spawner. Execution of the
kinematic controller was observed, but three-rod completion is **not validated**.
The historical 1.16.1 benchmarks below do not establish performance on 26.3.

See [release notes and installation](docs/releases/26.3-kinematic.md) and
[reproducible variant packaging](tools/release/README.md). Downloadable JARs and ZIPs
belong in GitHub Release assets; they are not committed as source files.

### Build (26.3)

> [!IMPORTANT]
> Run Gradle itself on **JDK 21** (set `JAVA_HOME` to a JDK 21 install, and run `gradlew.bat --stop`
> first if a daemon on another JDK is still running). Gradle on JDK 25 fails in
> `:1.21.4:preprocessCode` with the message `25.0.4.1`. The Java 25 toolchain is used
> to compile, test, and launch 26.3. Install both JDKs; Gradle does not download them.

On Windows, set `JAVA_HOME` to JDK 21 and `JAVA_HOME_25` to JDK 25, then run:

```bat
gradlew.bat :26.3:build -Pwith26 -Porg.gradle.java.installations.fromEnv=JAVA_HOME_25
```

On Linux/macOS, with the same environment variables:

```sh
./gradlew :26.3:build -Pwith26 -Porg.gradle.java.installations.fromEnv=JAVA_HOME_25
```

The installable mod is `versions/26.3/build/libs/altoclef-26.3-<version>.jar`.
Do not install the `-slim` or `-sources` jars. To launch a development client, replace
`:26.3:build` with `:26.3:runClient` in the same command.

### Runtime compatibility (26.3)

The local-player yaw hook uses `getViewYRot(float)`. Pitch already inherits the
vanilla implementation on 26.3, so it needs no local-player injection.
Inventory crafting falls back to manual slot placement when the modern recipe-book
map is unavailable, preventing stalls while crafting planks and other ingredients.
When carrying at least ten obsidian, Nether travel builds with those blocks and
keeps the same construction task through interruptions. Otherwise it uses buckets.

The second-bucket recovery timer counts travel and completed mining as progress,
so it does not cancel iron collection just because no ingot has been smelted yet.
On exit, TenorClef stops Ostinato's background workers and saves visited world
caches so Minecraft's shutdown watchdog does not report a crash.

## Install

1. Download the TenorClef Fabric jar for your exact Minecraft version from this
   repository's [Releases](https://github.com/nicohza/gogoclef/releases).
2. Place it in the instance's `mods` directory with Fabric Loader and Fabric API.
3. Install the matching Ostinato jar when the release notes require it. Do not add a
   second Baritone jar unless the release notes explicitly say to do so.
4. Start a single-player test world first. Include the game version, TenorClef and
   Ostinato versions, mod list, and `latest.log` when reporting a problem.

Each release lists the matching Ostinato jar in its notes (see [CHANGELOG.md](CHANGELOG.md)). If the Releases page is empty,
build from source using the instructions below rather than downloading an upstream
AltoClef jar.

## Build from source

TenorClef uses Java 21 for the current modern modules. On Windows run:

```bat
gradlew.bat :1.21.4:build
```

On macOS or Linux run:

```sh
./gradlew :1.21.4:build
```

For a version that depends on a local Ostinato build, follow the wiring guide first.
The initial Gradle configuration can take a while because Minecraft is remapped.

## Movement backends

Every build uses Ostinato, the AltoClef-compatible Baritone fork. On the modern targets,
Tungsten is an optional travel backend; mining, building, and inventory operations use
Ostinato's Baritone processes. Ostinato also carries the encrypted `#swarm` link for
multi-bot groups, including coordinated region builds (`#swarm build`); see Ostinato's
`docs/REGION_BUILD.md`.

When using an Ostinato-enabled pairing, its `movementBackend` setting selects
`baritone`, `tungsten`, or `auto`; `auto` falls back to Baritone when Tungsten is not
installed. The 1.16.1 pairing also has an experimental physics-driven `kinematicTravel`
controller, plus `pitfallAvoidance`. See [Ostinato's README](https://github.com/vexrypt-rgb/Ostinato) for
backend details.

## Benchmarking movement

The in-game `@pathbench` command measures the pathfinder and the movement layer:

- `@pathbench search [-|setting=a,b] [reps]` times path searches, optionally sweeping a setting.
- `@pathbench travel [baritone|tungsten|kinematic] [reps]` runs end-to-end trials over a fixed
  set of goals and records reached/stalled and ticks per goal.

Results are written as CSV to `versions/<mc>/run/pathbench/` (not committed). The table below was
recorded in earlier sessions; its Baritone and physics source CSVs were not retained, so treat it as
historical, not reproducible from the repo. A local kinematic run at 09:34 on 2026-09-28 reached only 11/38; every miss
never started moving (`firstMoveTicks=-1`), which points to the mover not starting, not to pathing. It did not reproduce:
a fresh run on the same day, 1 rep, reached 16/16 (avg 415 ticks, first move 8.5 ticks).
The kinematic row comes from a 3-rep run at 20:35 the same day (`pathbench_travel_kinematic_20260928_203533.csv`,
first move after 8.9 ticks on average, 0 runs that never moved).
The Baritone row comes from a 3-rep run at 20:56 the same day (`pathbench_travel_baritone_20260928_205606.csv`;
the one miss was goal 15 rep 0, which stalled 44 blocks away). The physics row is still historical. Earlier 1.16.1 travel runs (16 goals; baritone and kinematic x 3 reps, physics x 1; kinematic from a later run):

| Mover | Goals reached | Avg ticks (reached goals) |
| --- | --- | --- |
| Baritone | 47/48 | 368 |
| Kinematic (experimental) | 46/48 | 383 |
| Physics search (experimental, `physicsTravel`) | 15/16 | 401 |

Newer kinematic runs (2026-09-29, 16 goals x 3 reps, pinned origin, every trial fed to full hunger; one run each):

| Mover | Conditions | Goals reached | Avg ticks |
| --- | --- | --- | --- |
| Kinematic (experimental) | Mobs on | 48/48 | 233 |
| Kinematic (experimental) | Peaceful | 48/48 | 230 |

These replace the kinematic row above. Its lower score and higher tick count came mostly from hunger carrying over between trials. Baritone has not been re-run fed, so the two movers can't be compared yet. Details and logs are in [docs/BENCHMARKS.md](docs/BENCHMARKS.md).

The averages only cover goals each mover reached. The bench origin moves between runs, so
compare runs taken together. Single-rep runs are noisy; re-run with 3 reps before drawing conclusions.

## Project guides

- [Development / CI (Phase 1)](docs/DEVELOPMENT.md)
- [Ostinato wiring](docs/OSTINATO_WIRING.md)
- [Usage](usage.md)
- [Development](develop.md)
- [Planned work](TODO.md)

## Reporting issues

Please use the [TenorClef issue tracker](https://github.com/vexrypt-rgb/TenorClef/issues).
Describe the goal, Minecraft version, TenorClef and Ostinato versions, installed mods,
and attach a relevant log or reproduction steps.

## License and notices

TenorClef is licensed under the [MIT License](LICENSE). Releases which bundle or
depend on Ostinato must preserve Ostinato's LGPL-3.0 notices and provide a way to
obtain its corresponding source. See [LICENSING.md](LICENSING.md).