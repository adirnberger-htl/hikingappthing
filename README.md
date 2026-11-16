# Hiking App thing

Java CLI app that reads Komoot-exported GPX files, renders an ASCII route map in terminal with orange coloring, prints sport-specific stats, and supports a `peaks` command for hike peaks.

## Setup

1. Copy config template:
   - `cp hikingappthing.config.example.json hikingappthing.config.json`
2. Export your tours/activities from Komoot as `.gpx` files.
3. Put the GPX files in `komoot-exports/` (or set `komootExport.exportsDir` in config).

## Build, test, run

- Build jar: `mvn package`
- Run tests: `mvn test`
- Run one test class: `mvn -Dtest=AsciiMapRendererTest test`
- Run GPX provider test class: `mvn -Dtest=KomootExportActivityProviderTest test`
- Run app once: `java -jar target/hikingappthing-0.1.0-SNAPSHOT.jar`
- Peaks command once: `java -jar target/hikingappthing-0.1.0-SNAPSHOT.jar peaks`

## Shell function integration

- Install Fish command automatically: `bash scripts/install-fish-command.sh`
- Bash: `source scripts/hikingappthing.bash`
- Fish: `source scripts/hikingappthing.fish`

Then run:

- `hikingappthing`
- `hikingappthing peaks`
