#!/usr/bin/env bash

hikingappthing() {
  local repo_dir="${HIKING_APP_REPO:-$HOME/IdeaProjects/hikingappthing}"
  local jar="$repo_dir/target/hikingappthing-0.1.0-SNAPSHOT.jar"

  if [[ ! -f "$jar" ]]; then
    echo "Jar not found, building first..."
    (cd "$repo_dir" && mvn -q -DskipTests package) || return 1
  fi

  (cd "$repo_dir" && java -jar "$jar" "$@")
}
