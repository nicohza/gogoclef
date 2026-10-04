# Minecraft 26.3 development checkpoint

The original `tenorclef-ostinato-mc26.3.zip` is available in the Coding Agent
task's Outputs. It includes TenorClef, Ostinato, Tungsten, Fabric API,
installation instructions, the source overlay, and per-file checksums.

The compiled ZIP is distributed separately because including it in Git caused
HTTP 413 during publication. To host it on GitHub, download it from the task's
Outputs and attach it as an asset when creating a GitHub Release. The source
changes and checkpoint documentation are committed in this repository.

This is the previous agent's 4 October 2026 checkpoint, recovered without
changes to the ZIP. The source overlay has been applied to this repository, except for the
GitHub Actions updates described below, on base commit `aae83b16e9abe0b2c5048d99221f54709ba84b38`.

- [Installation instructions](INSTALL.txt)
- [Original validation report](VALIDATION.txt)
- [Tungsten comparison](TUNGSTEN-COMPARISON.md)
- [Archive checksum](SHA256SUMS.txt)

The previous session reported 150 TenorClef tests and three Tungsten tests
passing. Recovery verified archive integrity, every embedded checksum, the
separately supplied jars, and source-file equality; builds and gameplay were
not rerun during recovery.

**The three-blaze-rod survival test is incomplete.** This checkpoint is not a
verified full speedrun. See the original validation report for remaining work.

After downloading the archive into this directory, verify it with:

```sh
sha256sum -c SHA256SUMS.txt
```

## GitHub Actions updates

The publishing app lacks GitHub Workflows permission. Active workflow files
therefore retain their base versions. [workflow-updates.patch](workflow-updates.patch)
preserves the exact intended CI and release updates. These updates include the
26.3 build job and its Tungsten build; they are not active until applied.

Apply the patch from the repository root and publish it using a GitHub account
or app authorized to update workflows:

```sh
git apply checkpoints/mc26.3-2026-10-04/workflow-updates.patch
git add .github/workflows/gradle.yml .github/workflows/release.yml
git commit -m "Update Minecraft 26.3 build and release workflows"
git push
```
