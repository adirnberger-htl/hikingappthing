function hikingappthing
    set repo_dir "$HOME/IdeaProjects/hikingappthing"
    if test -n "$HIKING_APP_REPO"
        set repo_dir "$HIKING_APP_REPO"
    end

    set jar "$repo_dir/target/hikingappthing-0.1.0-SNAPSHOT.jar"

    if not test -f "$jar"
        echo "Jar not found, building first..."
        cd "$repo_dir"; or return 1
        set -l warning_regex 'sun\.misc\.Unsafe::objectFieldOffset|terminally deprecated method in sun\.misc\.Unsafe|Please consider reporting this to the maintainers of class com\.google\.common\.util\.concurrent\.AbstractFuture\$UnsafeAtomicHelper'
        mvn -q -DskipTests package 2>&1 | string match -v -r -- $warning_regex
        set -l status $pipestatus[1]
        if test $status -ne 0
            return $status
        end
    end

    cd "$repo_dir"; and java -jar "$jar" $argv
end
