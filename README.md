# world-downloader

A Minecraft world downloader that works as a proxy server between the client and the server to read & save chunk data. Download multiplayer worlds by connecting to them and walking around. Chunks can be sent back to the client to extend the render distance.

> **Note:** This is a modified fork of [mircokroon/minecraft-world-downloader](https://github.com/mircokroon/minecraft-world-downloader). See [Attribution & License](#attribution--license) below for details.
>
> This fork does not currently provide pre-built downloads — you'll need to [build it from source](#building-from-source).

### Basic usage

After [building the jar](#building-from-source), run it using the command-line. Enter the server address with the `-s` flag (or via the GUI address field, if using the GUI) and start.

Instead of connecting to the server itself, connect to `localhost` in Minecraft to start downloading the world.

If you run into any problems, check the [FAQ](https://github.com/mircokroon/minecraft-world-downloader/wiki/FAQ) page on the original project for common issues.

### Features

- Requires no client modifications and as such works with every game client, vanilla or not
- Automatically merge into previous downloads or existing worlds
- Save chests and other inventories by opening them
- Extend the client's render distance by sending chunks downloaded previously back to the client
- Overview map of chunks that have been saved

### Requirements

- Java 21 or higher
- Minecraft version 1.12.2+ // 1.13.2+ // 1.14.1+ // 1.15.2+ // 1.16.2+ // 1.17+ // 1.18+ // 1.19.3+ // 1.20+ // 1.21+ // 26.1+

### Command-line

Once you've [built the jar](#building-from-source), run it from the `target/` directory:

```
java -jar world-downloader.jar
```

Arguments can be specified to change the behaviour of the downloader. Running with `--help` shows all the available commands.

```
java -jar world-downloader.jar --help
```

The GUI can be disabled by including the `--no-gui` option, and specifying the server address:

```
java -jar world-downloader.jar --no-gui -s address.to.server.com
```

### Running on Linux

After building the jar, run it from the terminal:

```
java -jar world-downloader.jar -s address.to.server.com
```

When running headless Java, the GUI should be disabled by including the GUI option:

```
java -jar world-downloader.jar -s address.to.server.com --no-gui
```

Some Linux distributions may require `-Djdk.gtk.version=2` for the GUI to work:

```
java -Djdk.gtk.version=2 -jar world-downloader.jar
```

### Building from source

<details>
  <summary>Dependencies on Linux</summary>

  #### debian/ubuntu

  ```
  sudo apt-get install default-jdk maven
  ```

  #### arch/manjaro

  ```
  sudo pacman -S --needed jdk-openjdk maven
  ```
</details>

<details>
  <summary>Build project to executable jar file</summary>

  Building the project manually can be done using Maven:

  ```
  git clone https://github.com/Jeffery879/world-downloader
  cd world-downloader
  mvn package
  java -jar ./target/world-downloader.jar -s address.to.server.com
  ```
</details>

### Contact

For problems, bugs, or feature requests specific to this fork, please [open an issue](https://github.com/Jeffery879/world-downloader/issues) on this repository.

For questions about the original project's usage, see the [upstream issues page](https://github.com/mircokroon/minecraft-world-downloader/issues) or discussions.

## Attribution & License

This project is a modified version of [minecraft-world-downloader](https://github.com/mircokroon/minecraft-world-downloader) by **mircokroon**, used and modified under the terms of the GPL-3.0 license.

- **Original project:** [mircokroon/minecraft-world-downloader](https://github.com/mircokroon/minecraft-world-downloader)
- **Original license:** GNU General Public License v3.0 (GPL-3.0)
- **This fork's license:** GNU General Public License v3.0 (GPL-3.0) — see [LICENSE](LICENSE)
- **Modifications (2026):**
  - Added support for Minecraft 26.1+