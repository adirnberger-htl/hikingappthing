# Copilot instructions for this repository

## Project status and source of truth
- Product intent lives in `docs/me.md` and should be treated as the primary feature source.
- Existing implementation is a Java CLI app (Maven project) under `src/main/java`.

## Build, test, and lint commands
- Build: `mvn package`
- Test suite: `mvn test`
- Single test class: `mvn -Dtest=AsciiMapRendererTest test`
- Run app: `java -jar target/hikingappthing-0.1.0-SNAPSHOT.jar`
- Run peaks command: `java -jar target/hikingappthing-0.1.0-SNAPSHOT.jar peaks`

## High-level architecture
- `Main` orchestrates command routing:
  - no args: fetch activities, cycle to the next one each run, print map + curated stats
  - `peaks`: print recent peaks from hike activities only
- `config` package loads and validates JSON config (`hikingappthing.config.json`, or `HIKING_APP_CONFIG` override).
- `state` package persists app state in JSON (`.hikingappthing-state.json`) for next-activity cycle index.
- `data` package reads local Komoot-exported GPX files (`KomootExportActivityProvider`) and maps them into domain objects.
- `render` package owns ASCII route rendering with ANSI color output.
- `domain` package defines neutral models (`Activity`, `Peak`, `RoutePoint`, `ActivityType`) used across provider/renderer/CLI.

## Key repository conventions
- Keep dependencies minimal: standard library first, Jackson for JSON, JUnit for tests.
- Keep behavior config-driven through `hikingappthing.config.json`; avoid hardcoding export paths or GPX assumptions.
- GPX import behavior should read from `komootExport.exportsDir` and support non-recursive/recursive file loading per config.
- Cycling behavior is persistent across runs; changes to selection flow must update state semantics accordingly.
- `peaks` output should stay hike-only unless `docs/me.md` changes.
