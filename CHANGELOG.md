# Changelog

All notable changes to Tribal Trouble: Resurrected are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project uses the versioning described in [docs/releasing.md](docs/releasing.md).
Entries are written in English only and are not translated.
Write entries for players first and put engine, build and server work last.
The game shows this file under Patch notes in Options and opens it once after
a new major or minor version. GitHub release notes are taken from the Unreleased section when a
release is cut.

## [Unreleased]

## [2.1.96] - 2026-10-15

Everything since the original Oddlabs source was released on 2014-09-01. Builds before 2.1 were prereleases and are not listed separately.

### Added

- Multiplayer servers are back online. No registration, serial key or demo mode.
- Steam release with Steam login, campaign achievements, multiplayer stats and rich presence.
- Up to 12 players per game, with 12 team colors that follow lobby slots.
- Spectate live games from the game list: follow a player's camera, cursor, selection, orders and building placement, cycle players or use a free camera, and chat with other spectators.
- Ships for both tribes: build them on the shoreline, crew them, fight other ships and land on other islands. The chieftain can board to speed the ship up.
- Archipelago map type where everyone starts at sea, with an AI that attacks by ship and lands invasions.
- Advanced game settings: max units, starting units, max buildings and a boats toggle.
- Game presets that save and load your favorite setups, with a roster tab in the create game screen.
- Word-based map codes.
- Lobby countdown before the game starts and closable player slots.
- Enormous islands.
- Units standing in a new building site are moved aside instead of blocking placement.
- Water reflections, specular highlights, Gerstner waves, transparent shallows and a low-detail water option.
- Normal mapping and specular on rocks and iron, higher resolution textures, anisotropic filtering and anti-aliasing.
- Optional team color overlay on units and buildings, with configurable team colors.
- Full key remapping with categories, conflict detection and swap, a filter box, import and export. Tooltips show the bound key.
- Camera options: Alt or Option plus arrow keys, invert horizontal and vertical, and a recording camera with HUD toggle, cinematic mode, auto-orbit and auto-pan.
- Full keyboard navigation of menus and campaign dialogs.
- UI scaling, high contrast mode, a color vision mode and an option to confine the cursor to the game window.
- On-screen compass.
- OpenAL effects, forest reverb and a headphone mode using HRTF, with a classic audio toggle.
- Brazilian Portuguese translation. Translations are maintained in a shared sheet by community translators.
- Contributors tab in the credits, GPL notice in the About tab, and Discord and GitHub buttons on the main menu.
- Patch notes window in Options that shows this changelog and opens once after each new major or minor version.
- Banned word filter on player names and chat, with Discord moderation commands.
- Discord bot that bridges lobby chat, posts game embeds and answers /matches, /profile and /streaks.
- Engine: LWJGL 3, JOML, OpenGL 4.1 core profile with shaders, uniform buffers and hardware instancing.
- Build: GitHub Actions builds for Windows, Linux AppImage and both Mac architectures, with Steam, itch.io, GitHub Releases and website publishing.
- Build: Keep a Changelog file bundled into the game and used for GitHub release notes.
- Server: ported database layer with migrations, Steam Web API stats, web spectator streaming.
- Server: simulation version reported at login so mismatched clients are kept apart in the game list.
- Server: OpenSkill skill rating is computed and stored after every game. The leaderboard stays hidden until it is ready for players.

### Changed

- The renderer was rebuilt on modern OpenGL. Terrain cracks are gone and large scenes render faster. Lighting stays close to the original look by default.
- Fullscreen is now borderless windowed, so it works with OBS and screenshot tools and never changes the desktop resolution.
- Windowed mode and high-DPI displays are supported properly.
- The game keeps running when unfocused or minimized.
- Game speed is remembered and used as the default for single player and multiplayer.
- Original fonts are kept, with Inter as a fallback for characters they lack.
- Oddlabs references removed from in-game text.
- Engine: Java 26, Gradle 9 with a standard source layout, declared dependencies instead of bundled jars, and assets in their own module.
- Engine: STB image and Vorbis loading, Basis Universal texture compression to standard DDS, Cleaner instead of finalizers.
- Engine: sources and properties converted to UTF-8 with LF line endings; Spotless formatting enforced in CI; ErrorProne and NullAway in the build.
- Engine: game and log directories follow platform conventions, with per-run event logs for bug reports.
- Build: the release version is `v<major>.<minor>.<patch>-<api>.<sim>` and an api-guard check forces an API bump when network code changes.

### Removed

- Registration, serial keys, trial mode, the getdown updater and the old installers.
- Applet loader, javasvn, PBuffer rendering, software cursors and the StrictMath wrapper classes.

### Fixed

- German umlaut in the localization files.

### Security

- Deserialization is filtered and oversized network events are rejected on both client and server.
- The server guards against malformed client data.
- Steam auth tickets are validated through the Steam Web API.

[Unreleased]: https://github.com/Tribal-Trouble/tribaltrouble/compare/v2.1.96-103.1009...main
[2.1.96]: https://github.com/Tribal-Trouble/tribaltrouble/releases/tag/v2.1.96-103.1009
