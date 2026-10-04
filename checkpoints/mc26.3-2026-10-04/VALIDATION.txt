# Minecraft 26.3 checkpoint — 2026-10-04

This is a development checkpoint. The equipped `@get blaze_rod 3` survival
scenario has **not completed**. Do not treat this checkpoint as a verified
full speedrun or a completed `@gamer` test.

## Build checks

- TenorClef 26.3: build and sources artifact passed; 150 tests passed.
- Vendored Tungsten 26.3: build and sources artifact passed; three trail
  regression tests passed.
- Runtime jar metadata, Java 25 bytecode, mixin targets, bundled dependencies,
  licenses and compiled-class equivalence checked.
- GitHub Actions YAML and collection of all three mod artifacts checked
  locally. Remote Actions execution is pending delivery.

Gradle runs on Java 21; the Minecraft 26.3 compiler/client use Java 25.
The synthetic singleplayer test uses Fabric Loader 0.19.5 and Fabric API
0.161.0+26.3. No user save was modified.

## Observed gameplay

- `@get stone_pickaxe 1` completed gathering and crafting in 56.283 seconds
  after the crafting fixes. This does not establish full `@gamer` completion.
- Controlled Overworld water and nearby-hostile portal approaches passed.
- Equipped survival runs built a portal and discovered Nether fortresses
  without supplied portal/fortress coordinates or blaze rods.
- Tungsten search, path execution and entity following were observed.
- Fixed repeated follow restarts, a corner-jump null predecessor, concurrent
  entity collision queries, different-floor trail oscillation and missing
  static-route fallback. Removed an unplanned forward leap.
- Static-route fallback was reproduced and verified at a stalled fortress
  approach: Ostinato mined/traveled beyond the blocked location.
- A worn iron pickaxe was excluded by preservation while still satisfying the
  mining requirement. The 26.3 fix allows the only suitable tool to be used;
  the same-world retry collected the missing iron and crafted a furnace and
  table, then began smelting. Diamond replacement preference compiles, but
  replacement after an actual tool break remains to be observed.
- Clean client shutdown and cache saves were observed.

## Outstanding survival validation

The initial loadout was gold helmet, remaining iron armor, shield, iron
pickaxe/sword/axe, 128 cobblestone, 64 golden carrots, 64 diamonds, 14 obsidian
and flint-and-steel, at a random Overworld location. The target was reduced
from six rods to three. Later continuations enabled `keepInventory` at the
user's request.

An earlier build collected two rods, then died in lava. Subsequent retries
exposed additional route and pursuit problems; those are not three-rod
passes. The latest saved continuation is in the Overworld after a death with
inventory retained. It has cleared the iron-mining stall and is making a
bucket to enter the Nether again. Three-rod completion and live coverage of
the bounded entity-follow fallback remain outstanding.

The separate Hackerokuz `migration-to-26.2` port is authorized to begin only
after this 26.3 delivery works and its GitHub upload is confirmed. It has not
started in this checkpoint.
