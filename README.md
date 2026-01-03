# Resource Pack Loader 

A Minecraft Fabric **CLIENT-SIDE** mod for automatically enabling/disabling resource packs based on the server you join.

## Features

✅ **Server-Based Profiles** - Configure different resource pack setups for each server  
✅ **Automatic Switching** - Packs are automatically enabled/disabled when joining servers  
✅ **No Red Screen** - Uses F3+A style reload, not the Mojang reload screen  
✅ **Client-Side Only** - No server-side mod required  
✅ **Easy Configuration** - GUI interface accessible via ModMenu or keybind (Right Shift)  
✅ **Safe & Stable** - Includes cooldown protection and error handling  

## Requirements

- **Minecraft**: 1.21.10
- **Fabric Loader**: 0.16.0 or higher
- **Fabric API**: Required
- **ModMenu**: Optional (but recommended for easy config access)

## Installation

1. Download and install [Fabric Loader](https://fabricmc.net/use/)
2. Download [Fabric API](https://modrinth.com/mod/fabric-api)
3. Download this mod
4. Place both mods in your `.minecraft/mods` folder
5. Launch Minecraft with the Fabric profile

## Usage

### Opening the Configuration GUI

You can open the configuration screen in two ways:

1. **Via ModMenu** (if installed): Click "Mods" → "Resource Pack Loader" → "Config"
2. **Via Keybind**: Press **Right Shift** (default key)

### Configuring Server Profiles

1. Open the configuration GUI
2. Enter a server address in the text field and click "Add Server"
3. Select the server from the list
4. Click on resource packs to cycle through states:
   - **○** (Gray) - No change (neutral)
   - **✓** (Green) - Enable on this server
   - **✗** (Red) - Disable on this server
5. Click "Save & Close" to apply your changes

### Default Profile

The "Default Profile" entry allows you to configure which packs should be enabled/disabled when you disconnect from a server or are in singleplayer.

## How It Works

- **On Server Join**: The mod detects the server address and applies the configured profile
- **On Server Leave**: Optionally applies the default profile (if configured)
- **Resource Reload**: Uses Minecraft's internal reload mechanism (same as F3+A) for seamless switching
- **Cooldown**: 2-second cooldown prevents rapid reload loops
- **Safety**: Unknown servers are ignored unless explicitly configured

## Configuration File

The configuration is stored in `.minecraft/config/resourcepack-loader.json`:

```json
{
  "defaultProfile": {
    "enable": [],
    "disable": []
  },
  "servers": {
    "localhost": {
      "enable": ["my_pack.zip"],
      "disable": ["other_pack.zip"]
    },
    "example.net": {
      "enable": ["pvp_ui.zip"],
      "disable": []
    }
  }
}
```

## Building from Source

```bash
git clone https://github.com/Anom-uxx/Resourcepack-loader-.git
cd Resourcepack-loader-
./gradlew build
```

The compiled JAR will be in `build/libs/`

## Important Notes

⚠️ **This is NOT a cheat mod** - It only manages local resource packs  
⚠️ **Client-side only** - No server communication or packet manipulation  
⚠️ **No auto-enabling** - Packs are only changed for explicitly configured servers  

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Credits

- Developed by Anom-uxx
- Built with [Fabric](https://fabricmc.net/)
- ModMenu integration support

## Support

If you encounter any issues, please report them on the [GitHub Issues](https://github.com/Anom-uxx/Resourcepack-loader-/issues) page.

## Version 
1.21.11 Not Supportet (in Work)