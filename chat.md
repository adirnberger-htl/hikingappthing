# Questions from planning

1. Data source decision: should the app use Terra API (as noted in `docs/me.md`) for Komoot activity/route access, or do you want a different source/integration approach?

do what geminie suggested if possible.

2. Activity scope: for "last hikes / runs / bikes", how many 
recent activities should be fetched and shown by default?

one at a time and if you reach the end just loop back around, like a for loop every time i run the command it cycles through.

3. Output mode: should the app print one selected activity per run, or a list plus an interactive selection before rendering the ASCII map?

one at a time

4. `peaks` behavior: should `peaks` show only named summits from hike activities, or include runs/bikes that pass high points as well?

nah only hikes

5. Stats per sport: which exact stats do you want for each type (hike, run, bike)?

look what komoot tracks

6. Config file format: which format should be used (`.properties`, JSON, YAML, or TOML)?

json

7. Config location: should config live in project directory, home directory (for command usage), or both with precedence rules?

project

8. Terminal color behavior: should orange route rendering be optional/fallback when terminal does not support truecolor?

I want it always coloured, do orange as default.

9. Transparency/background tuning: do you want user-configurable palette values in config, or fixed defaults tuned for dark purple backgrounds?

do defaults that work well with my background but make all colours configurable

10. Fish/Bash integration target: do you want a shell alias, shell function, or installed executable command (same command name in both shells)?

shell function

11. Desktop app wording: should this remain strictly a CLI tool, or do you also want a GUI desktop wrapper later?

CLI only

12. Authentication flow: should auth/token setup happen via first-run CLI prompts, config file fields, or environment variables?

when running the command the first time a browser opens with the login stuff or whatever needs to open.

## New questions after re-reading docs/me.md

13. `docs/me.md` now points to Komoot **Standard Web Embedding** (iframe) which does not provide raw GPX/data points. The current app requirements still need terminal ASCII maps and sport stats. Which data source should provide raw route + stats now?
14. If you want to stay strictly with embedding only, should we change output from terminal ASCII to an embedded web-view map instead, or keep terminal output as mandatory?