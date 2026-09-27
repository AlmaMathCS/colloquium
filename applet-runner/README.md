# Java Applet Runner

Browsers dropped applet support, and JDK 11 removed the `appletviewer` tool, so
this folder has a small replacement that runs classic `java.applet.Applet` code.

## Requirements

- JDK 8 to 25 (the Applet API is deprecated but still included; JDK 26 removes it)
- On a machine with no display (a server or cloud container): `xvfb-run`

## Usage

```bash
./run-applet.sh examples/HelloWorld.java
```

The script compiles the applet into `build/`, then:

- **with a display**, it opens the applet in a window, like `appletviewer` did;
- **without a display**, it runs the applet on a virtual screen (Xvfb) and saves
  `HelloWorld.png`.

Options (put them after the `.java` file):

| Option | Meaning |
| --- | --- |
| `--width N` / `--height N` | Applet size (default 400×300) |
| `--param NAME=VALUE` | Value returned by `getParameter("NAME")` (repeatable) |
| `--screenshot FILE` | Render to a PNG and exit, even with a display |
| `--delay MS` | Wait before the screenshot (default 500 ms), for animated applets |

Example with parameters:

```bash
./run-applet.sh examples/Greeting.java --width 320 --height 100 --param name=Alma
```

## How it works

`AppletRunner.java` does what a browser used to do: it loads the class, puts it
in a `Frame`, gives it an `AppletStub`/`AppletContext` (for `getParameter`,
`getCodeBase`, `showStatus`, and so on), then calls `init()` and `start()`.
Closing the window calls `stop()` and `destroy()`.
