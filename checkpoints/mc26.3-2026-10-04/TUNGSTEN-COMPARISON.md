# Tungsten comparison for the Minecraft 26.3 build

Source inspection: 4 October 2026 (UTC). Upstream heads and latest published releases were checked through public Git remotes and the GitHub releases API. Upstream 1.21.11 and 26.2 code was inspected, not built or run. Gameplay observations below are from our 26.3 build only.

## Which versions are being compared?

| Version | Exact revision | Minecraft target | Status |
|---|---|---|---|
| KaptainWutax original | `e0e474242d4a1e8fcf8f83a41f327a22d4c81a59` | 1.19.2 | Original main, last commit April 2023; no latest published release returned |
| Hackerokuz latest published release | `34746a5947ce6ceb9e6897b6ec65ecda3720eaff`, tag `Tungsten-BETA-1.0.0` | 1.21.11 | Published 27 March 2026 |
| Hackerokuz newer migration branch | `d0f3f504faff99c8d53dfb0d4faf59f67e9d70d7`, `migration-to-26.2` | 26.2 | Development checkpoint dated 3 October 2026; not the latest published release |
| 3ndetz compatibility fork | `5cb12ad65c0e045aa5a02d017c98df64eeda6d40`, `altoclef-compat` | 1.21 | TenorClef's pinned base; also the fork's current default branch head when checked |
| Our vendored port | 3ndetz pin above + source changes in this delivery | 26.3 | Adapted and exercised with TenorClef/Ostinato; not a port of the newer Hackerokuz beta or migration branch |

Sources: [original revision](https://github.com/KaptainWutax/Tungsten/tree/e0e474242d4a1e8fcf8f83a41f327a22d4c81a59), [latest beta release](https://github.com/Hackerokuz/Tungsten/releases/tag/Tungsten-BETA-1.0.0), [26.2 checkpoint](https://github.com/Hackerokuz/Tungsten/tree/d0f3f504faff99c8d53dfb0d4faf59f67e9d70d7), [compatibility base](https://github.com/3ndetz/Tungsten/tree/5cb12ad65c0e045aa5a02d017c98df64eeda6d40).

## Functional differences

| Area | Latest Hackerokuz beta (1.21.11) | Hackerokuz 26.2 branch | Our 26.3 port |
|---|---|---|---|
| Physics simulation | Custom `Agent` model | New `AgentEntity extends Player`, driven by `AgentSimulator` | Custom `Agent` inherited from 3ndetz, with 26.3 API adaptations |
| Movement repertoire | Walking/running, sprint/long/corner/neo jumps, ladders, swimming/diving, entering/exiting water | Corresponding move generators remain, adapted to new simulator | Corresponding move generators retained from compatibility fork |
| Route search | Coarse block route plus physics search and partial paths | Same overall structure; separate node-cost helper and 550 ms primary search window | Compatibility fork's search with configurable limits and follow-specific search behavior |
| Following moving entities | No compatibility fork's `FollowEntityTask`/`FollowPlayerTask`/`TrailTracker` | These APIs also absent | Follow/trail integration retained; TenorClef uses entity following during combat |
| Chat commands | Click, goto, stop, settings registered | Registration of all four commands is commented out; source files remain | Click, goto, stop, settings, followPlayer registered |
| Other controls | Keyboard/click goal controls | P pause, G run, J block search, H goal and click goal code present | Compatibility controls adapted to 26.3 SDL input |
| Rendering | Older rendering implementation | Custom render pipelines and staged vertex buffers | 26.3 native Gizmos |
| Server fake player | Server entry point uses Fake Player API with summon/stop/come commands | Common initializer is empty; beta's server bot entry point is absent | Client-only mod, no server fake-player feature |
| Crafting, mining, portal building, blaze collection | Not complete survival-task automation | Not complete survival-task automation | Supplied by TenorClef/Ostinato, with Tungsten used for supported movement |

The 2023 original is a smaller physics/path execution prototype. The later forks add much of the command, movement and integration machinery discussed above; calling all these versions “the original” hides material differences.

## What the 26.2 rewrite offers—and its limits

The new simulator seeds a simulated player from the real player's position, velocity, attribute base values, pitch and sprint/swim state. It can copy simulation state, advance individual or multiple ticks and return status snapshots. Using Minecraft's player implementation could reduce duplicated physics logic, but this is an architectural benefit inferred from the source, not a measured accuracy improvement.

It also provides a world-free kinematic mode that skips collisions and medium checks. That simulator option is separate from Ostinato's `kinematicTravel` setting, which we disabled for the 26.3 survival test.

The new executor re-simulates the remaining path from the current player state and logs/renders predicted differences. In the inspected checkpoint, the line that would stop execution on those differences is commented out. Therefore, it should not be described as verified automatic drift recovery. Re-simulating the remaining path each tick could also cost CPU; no performance benchmark was run.

The cost helper heavily penalizes lava and adds a damage-related cost. Those costs do not guarantee lava avoidance or survival. The simulator documents world-thread ownership requirements, while the pathfinder still starts worker threads. Thread safety needs review and testing before importing it.

Source references: [simulator](https://github.com/Hackerokuz/Tungsten/blob/d0f3f504faff99c8d53dfb0d4faf59f67e9d70d7/src/client/java/kaptainwutax/tungsten/client/sim/AgentSimulator.java), [executor](https://github.com/Hackerokuz/Tungsten/blob/d0f3f504faff99c8d53dfb0d4faf59f67e9d70d7/src/client/java/kaptainwutax/tungsten/client/path/PathExecutor.java), [costs](https://github.com/Hackerokuz/Tungsten/blob/d0f3f504faff99c8d53dfb0d4faf59f67e9d70d7/src/client/java/kaptainwutax/tungsten/client/helpers/NodeCostCalculator.java), [disabled command registration](https://github.com/Hackerokuz/Tungsten/blob/d0f3f504faff99c8d53dfb0d4faf59f67e9d70d7/src/client/java/kaptainwutax/tungsten/client/TungstenCommands.java).

## What would importing 26.2 require?

1. Port its Minecraft/Fabric APIs, rendering and GLFW input to 26.3.
2. Update the Ostinato bridge for renamed `kaptainwutax.tungsten.client.*` packages and `find(Level, Vec3, LocalPlayer)` signature.
3. Restore/adapt the compatibility fork's follow and trail behavior.
4. Restore commands, review worker-thread world access, cancellation, executor drift handling and shutdown.
5. Re-test crafting/travel ownership and repeat the same equipped survival scenario on 26.3.

It is not a drop-in jar replacement. No 26.2 implementation has been silently substituted into this delivery.

## Differences introduced in our 26.3 work

- Official 26.3 names/APIs, Java 25, SDL keys, native rendering and updated mixins.
- Respect server corrections; stop path execution before applying corrections.
- Damaging falls rejected by default; corner-jump predecessor null guard.
- Client-thread entity collision snapshots for worker physics queries, after reproducing a concurrent modification crash.
- Removed the unplanned forward-leap shortcut from entity following; pursuit now uses planned paths/trailing. Fixed trail mode oscillation when a nearby target is on another floor, with three regression tests.
- TenorClef caches Ostinato's stateful movement engine, preventing per-tick follow cancel/restart loops. Fixed-position travel goes through Ostinato's goal process so a failed Tungsten route can fall back instead of restarting indefinitely.
- Entity approaches fall back to Ostinato after a progress failure or a 15-second attempt, allowing mining/placing through obstructions.
- Search/executor shutdown and client-thread debug output fixes.

## Does newer Tungsten mean better combat or fortress exploration?

There is no measured basis yet to rank these versions. Tungsten supplies movement; TenorClef still selects combat targets, attacks, shields, heals and manages survival tasks. Neither inspected newer upstream version supplies TenorClef's complete combat/task system.

Tungsten can navigate toward known fortress positions. Discovering an unknown fortress requires exploration policy and world/chunk handling; our integration currently delegates that exploration to Ostinato, then can use Tungsten for a discovered target. The 26.2 simulator rewrite does not itself add that missing integration.

Our earlier 26.3 run built its own portal, reached a fortress and collected two rods, then died in lava. That was a failed three-rod test, not proof of reliable completion or superiority. Consult the delivery's VALIDATION.txt for the final test outcome after subsequent fixes.
