#!/usr/bin/env bash
# Compile and run a Java applet without a browser or the removed appletviewer.
#
# Usage: ./run-applet.sh path/to/MyApplet.java [AppletRunner options...]
#   e.g. ./run-applet.sh examples/HelloWorld.java
#        ./run-applet.sh examples/HelloWorld.java --width 300 --height 100 --param name=Alma
#        ./run-applet.sh examples/HelloWorld.java --screenshot hello.png
#
# With no display (e.g. a server or cloud container), it runs under Xvfb and
# saves a screenshot to <ClassName>.png instead of opening a window.
set -euo pipefail

if [ $# -lt 1 ]; then
  sed -n '2,10p' "$0"
  exit 2
fi

HERE="$(cd "$(dirname "$0")" && pwd)"
SRC="$1"; shift
CLASS="$(basename "$SRC" .java)"
BUILD="$HERE/build"
mkdir -p "$BUILD"

# The Applet API is deprecated (not yet removed) in JDK 9-25; silence the warnings.
javac -nowarn -Xlint:-removal -XDsuppressNotes -d "$BUILD" "$HERE/AppletRunner.java" 2>/dev/null
javac -nowarn -Xlint:-removal -XDsuppressNotes -cp "$BUILD" -d "$BUILD" "$SRC"

ARGS=(--classpath "$BUILD" "$@")
if [ -z "${DISPLAY:-}" ]; then
  case " $* " in *" --screenshot "*) ;; *) ARGS+=(--screenshot "$CLASS.png") ;; esac
  exec xvfb-run -a java -cp "$BUILD" AppletRunner "${ARGS[@]}" "$CLASS"
else
  exec java -cp "$BUILD" AppletRunner "${ARGS[@]}" "$CLASS"
fi
