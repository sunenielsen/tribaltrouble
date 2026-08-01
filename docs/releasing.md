# Releasing

Tribal Trouble ships through Steam, itch.io, GitHub Releases, and the tribaltrouble.org website. CI handles every platform; only one step is manual: flipping the Steam build branch on Valve's partner dashboard, which Valve doesn't expose to CI.

The release pipeline runs in two halves:

1. **Push to `release`** publishes the build to every platform's **prerelease** location (Steam `prerelease` branch, itch `*-prerelease` channels, GitHub Release marked `--prerelease`, website `/prerelease/` subdirectory).
2. **Dispatching the `Promote Release to Stable` workflow** with a tag promotes that exact build to every platform's **stable** location.

Splitting the two lets you sit on a prerelease for as long as you want (a day, a week) and validate it with internal players before flipping anything visible to all users.

## What gets published where

| Destination | After push to `release` | After `Promote Release to Stable` |
|---|---|---|
| **Steam** | Build uploaded to `prerelease` branch | _Manual via partner dashboard_ (moves it to `default`) |
| **itch.io** | `windows-prerelease`, `linux-prerelease`, `osx-arm64-prerelease`, `osx-x86-prerelease` channels | `windows`, `linux`, `osx-arm64`, `osx-x86` channels |
| **GitHub Releases** | New release tagged `v<VER>`, marked `--prerelease` | `--prerelease=false`, `--latest` set |
| **tribaltrouble.org** | `/prerelease/{windows,linux,mac-arm64,mac-x86}.zip` | `/{windows,linux,mac-arm64,mac-x86}.zip` (the homepage URLs) |

## Cutting a release

Everything below assumes you're working from `main` with the changes ready to ship.

### 1. Bump version metadata if needed

The release tag is `v<MAJOR>.<MINOR>.<PATCH>-<API>` (see [Versioning](#versioning)).

- **New minor or major release** (e.g., `2.0` → `2.1`): edit `version = "..."` in the root `build.gradle.kts`. PATCH resets to `0` automatically.
- **API/protocol change**: bump `API_VERSION` in `common/src/main/java/com/oddlabs/util/Compatibility.java`. The `api-guard` CI job will fail your build if you changed a wire-protocol interface without bumping this. If the interface change is wire compatible (e.g., an addition old clients never call) and doesn't warrant a bump, a maintainer can instead record that decision with an empty ack commit:

  ```bash
  git commit --allow-empty -m "<why the change is compatible> [api-compat-ack]"
  ```

  The guard anchors on the newer of the last `API_VERSION` bump and the last `[api-compat-ack]` commit, so the ack clears the check for that change and everything before it. Any interface edit pushed after the ack fails the check again and needs a fresh review. A regular merge carries the ack into `main`'s history; a squash merge drops it, so in that case push the ack commit directly to `main` after merging.
- **Patch release**: no edit needed. PATCH is auto-computed from commit count since the last bump.

### 2. Merge `main` → `release` and push

```bash
git checkout release
git merge --ff-only main
git push origin release
```

This kicks off the `Build & Release` workflow. The flow:

1. **Build wave**: `format-check`, `api-guard`, `version`, then all 6 platform builds (Linux AppImage, Windows app-image, Mac arm64 dmg/app, Mac x86 dmg/app, server bundle). The Mac `-app` variants are only built on `release` because they're only consumed by Steam.
2. **Approval gate**: the four prerelease publisher jobs (`steam-prerelease`, `itch-prerelease`, `github-prerelease`, `website-prerelease`) all declare `environment: release` and wait on the same reviewer prompt. GitHub batches them so one click approves all four.
3. **Prerelease publishes** (parallel after approval): Steam, itch (`*-prerelease` channels), GitHub Release (`--prerelease`), website (`/prerelease/`).

If anything fails, fix it, push again. Re-pushing the same commit just builds and publishes the same version again (the `github-release` job has a "tag already exists" guard so it doesn't fail on the second pass).

### 3. Test the prerelease

The prerelease destinations are user-visible to anyone who knows where to look (e.g., players using the Steam `prerelease` branch, or visiting `tribaltrouble.org/prerelease/`), but they aren't promoted to the default download path until step 4.

Test on the prerelease as long as you want. When you're confident, move on.

### 4. Promote to stable

Open the GitHub Actions tab → **Promote Release to Stable** → **Run workflow**. Enter the tag (e.g. `v2.0.14-103`). You can copy the tag from the GitHub Releases page or the workflow run that produced it.

The workflow:

1. Verifies the tag exists (errors clearly if you typo'd).
2. Sits at `environment: release` for one approval click.
3. Downloads the tag's GitHub Release assets. These are the byte-identical artifacts from when the tag was originally cut.
4. Pushes them to itch stable channels, rsyncs them to the website root, and runs `gh release edit <tag> --prerelease=false --latest`.
5. Writes a summary reminding you to do the Steam manual step.

### 5. Promote on Steam

CI can push to Steam but **cannot** move a build to the `default` branch (Valve restriction). Do this yourself:

- **Main game**: <https://partner.steamgames.com/apps/builds/3945720>
- **Demo**: <https://partner.steamgames.com/apps/builds/3945722>

Find the build matching the tag you promoted, change its branch from `prerelease` to `default`, and save. Players on the default branch will get the update on next launch.

## Rolling back

To re-deploy an older release (e.g., the just-promoted one breaks something):

1. Run **Promote Release to Stable** again with the previous good tag (e.g. `v2.0.13-103`).
2. On the Steam partner dashboard, move the older build back to `default`.

The promote workflow always pulls from GitHub Release assets, so any past release tag is a valid rollback target. No rebuild needed; rollback takes ~2 minutes plus the manual Steam click.

## Versioning

The canonical release identifier is **`v<MAJOR>.<MINOR>.<PATCH>-<API>`** (e.g., `v2.0.3-102`) and shows up everywhere: git tags, GitHub Release titles, artifact names, Steam build descriptions, the in-game About screen, and the matchmaker server logs.

It's built from three sources:

- **MAJOR.MINOR**: `version = "2.0"` in the root `build.gradle.kts`. Bump manually for a minor or major release.
- **PATCH**: auto-computed as the number of commits since the commit that introduced the current `version = "<MAJOR>.<MINOR>"` line in `build.gradle.kts`. Every commit on top of the bump advances it by one.
- **API**: `public static final int API_VERSION = 102;` in `common/src/main/java/com/oddlabs/util/Compatibility.java`. Bump only when the wire protocol between client and server changes (mismatched clients get rejected at login).

Both Gradle and CI compute PATCH from the same git history using identical logic (`git log -G'version = .<BASE>.' -- build.gradle.kts | head -1` as the anchor, then `git rev-list --count <anchor>..HEAD`), so the value the game and servers log (from the generated `BuildInfo.java` under `common/build/generated/sources/buildinfo/`) always matches the git tag and Steam build description of the build it came from.

When you bump `MAJOR.MINOR` (say `2.0` → `2.1`), that bump commit becomes the new anchor. PATCH is `0` at the bump (yielding `v2.1.0-102`), `1` after the next commit (`v2.1.1-102`), and so on.

## Required repository configuration

Set under **Settings → Secrets and variables → Actions**.

### Variables

| Name | Used by | Value |
|---|---|---|
| `STEAM_USER` | `steam-release` | Steam build account username |
| `STEAM_APP_ID` | `steam-release`, promote summary | Main game Steam app ID (`3945720`) |
| `STEAM_DEMO_APP_ID` | `steam-release`, promote summary | Demo Steam app ID (find under Steamworks → App admin) |
| `ITCH_PROJECT` | `itch-release`, promote | itch project slug (e.g., `rcubdev/tribal-trouble-resurrected`) |
| `DEPLOY_HOST_PROD` | `website-prerelease`, promote | `tribaltrouble.org` |
| `DEPLOY_USER_PROD` | `website-prerelease`, promote | SSH user on the prod box (typically `deploy`) |

### Secrets

| Name | Used by | Value |
|---|---|---|
| `STEAM_CONFIG_VDF` | `steam-release` | Contents of your `config.vdf` (lets the action authenticate without 2FA) |
| `ITCH_BUTLER_API_KEY` | `itch-release`, promote | API key from <https://itch.io/user/settings/api-keys> |
| `DEPLOY_SSH_KEY_PROD` | `website-prerelease`, promote | Private SSH key authorized on the prod box for `DEPLOY_USER_PROD` |

### `release` environment

Create under **Settings → Environments → release** with required reviewers set. This gates the four prerelease publisher jobs in `gradle.yml` (one click batches them) and the `promote` job in `promote-release.yml`. All `secrets.*` and `vars.*` listed above live under this environment.

## Security notes

- `game-ci/steam-deploy` is pinned to a commit SHA, not the `v3` tag, so a compromised upstream release can't silently swap action code under our `STEAM_CONFIG_VDF`. To bump: `gh api repos/game-ci/steam-deploy/git/refs/tags/v3 --jq '.object.sha'`, update both pin sites in `gradle.yml`, and leave the tag in a `# v3` trailing comment.
- First-party `actions/*` (checkout, setup-java, download-artifact, etc.) are conventionally trusted at major-version tags and aren't pinned.
- Branch protection should restrict who can push to `release`. The `prerelease-gate` approval is defense-in-depth, not the primary access control.

## Where to find things

- **Workflows**: `.github/workflows/gradle.yml`, `.github/workflows/promote-release.yml`
- **Build version logic**: `build.gradle.kts` (root, computes BuildInfo) + `version` job in `gradle.yml`
- **API guard**: tracked wire-protocol interfaces are listed in the `api-guard` job's `INTERFACE_FILES` array; compatible changes can be acknowledged without a bump via `[api-compat-ack]` commits (see [Cutting a release](#1-bump-version-metadata-if-needed))
- **Website homepage** (download links pointing at the stable URLs): upstream `Tribal-Trouble/tribaltrouble` → branch `new_main` → `website/index.html`
