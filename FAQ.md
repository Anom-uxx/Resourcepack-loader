# Frequently Asked Questions (FAQ)

## General Questions

### What does this mod do?
This mod automatically enables or disables your local resource packs based on which Minecraft server you join. Think of it as "profiles" for resource packs per server.

### Is this a cheat?
**No.** This mod only manages your local resource packs. It does not give you any gameplay advantage, modify packets, or communicate with servers. It's purely a quality-of-life tool.

### Does this work on servers?
Yes! This is a **client-side only** mod. You don't need the server to have it installed. It works on any server, vanilla or modded.

### Do I need the mod on the server?
**No.** This is entirely client-side. The server doesn't know you're using it.

## Installation

### What do I need to install?
You need:
1. Fabric Loader
2. Fabric API
3. This mod
4. (Optional) ModMenu for easy config access

### Which Minecraft versions are supported?
Currently: **Minecraft 1.21.1** (should work with 1.21.10 and 1.21.11)

### Can I use this with Forge?
No, this is a Fabric mod only. It will not work with Forge.

### Can I use this with OptiFine?
OptiFine is not officially supported with Fabric. Consider using [Sodium](https://modrinth.com/mod/sodium) and [Iris](https://modrinth.com/mod/iris) instead.

## Usage

### How do I open the config screen?
Two ways:
1. **ModMenu**: Mods → Resource Pack Loader → Config
2. **Keybind**: Press **Right Shift** (default)

### How do I add a server?
1. Open the config screen
2. Type the server address in the text field
3. Click "Add Server"
4. Select the server and configure packs

### What do the symbols mean?
- **○** (Gray) - Pack is not configured (no change)
- **✓** (Green) - Pack will be **enabled** on this server
- **✗** (Red) - Pack will be **disabled** on this server

### How do I cycle pack states?
Click on a pack in the right panel to cycle through:
Neutral (○) → Enable (✓) → Disable (✗) → Neutral (○)

### What is the "Default Profile"?
The Default Profile is applied when you:
- Disconnect from a server
- Are in singleplayer

Leave it empty if you want to keep your current packs when disconnecting.

## Technical

### Why don't I see the red Mojang loading screen?
This mod uses Minecraft's internal resource reload (F3+A style) instead of the full reload. This makes switching much faster and smoother.

### How fast does the reload happen?
The reload triggers 1 second after joining a server. There's also a 2-second cooldown to prevent reload loops.

### Where is the config file stored?
`.minecraft/config/resourcepack-loader.json`

You can edit this file manually if needed.

### Can I share my config with friends?
Yes! Just share your `resourcepack-loader.json` file. Place it in their `.minecraft/config/` folder.

### Does this work in singleplayer?
The mod doesn't auto-switch in singleplayer, but you can manually apply profiles or use the Default Profile.

### Can I use wildcards for server addresses?
Not currently. Server addresses must match exactly. This might be added in a future version.

## Troubleshooting

### The packs aren't switching!
Check:
1. Is the server address correct? (case-insensitive)
2. Are the pack names exactly correct?
3. Check the game logs for errors: `.minecraft/logs/latest.log`
4. Is the cooldown active? Wait 2 seconds between switches

### I can't find my pack in the list!
Make sure the resource pack is:
1. Installed in `.minecraft/resourcepacks/`
2. Valid and loadable by Minecraft
3. Visible in the vanilla resource pack menu

### The GUI isn't opening!
Try:
1. Reinstalling the mod
2. Checking if ModMenu is installed (if using ModMenu method)
3. Pressing the keybind (Right Shift)
4. Check for conflicting mods

### The mod crashes on startup!
Check:
1. Fabric Loader version (needs 0.16.0+)
2. Fabric API is installed
3. Minecraft version is 1.21.1
4. Java version is 21
5. No conflicting mods

### How do I reset the config?
Delete `.minecraft/config/resourcepack-loader.json` and restart Minecraft. A new default config will be created.

### Can I change the keybind?
Currently no, it's hardcoded to Right Shift. Custom keybinds might be added in a future version. You can still access via ModMenu.

## Features

### Does it work with shader packs?
Shader packs are separate from resource packs and are not managed by this mod. This mod only handles resource packs.

### Can I create groups of servers?
Not currently. Each server needs its own profile. This might be added as a future feature.

### Can I export/import profiles?
You can manually copy the JSON config file. A built-in import/export feature might be added later.

### Does this support modded servers?
Yes! It works on any server, vanilla or modded. It only cares about the server address.

## Performance

### Does this affect game performance?
The reload itself uses the same mechanism as F3+A, so it has the same performance impact as pressing F3+A manually.

### Is there a cooldown?
Yes, there's a 2-second cooldown between reloads to prevent rapid switching and potential issues.

### Does it use a lot of memory?
No, the mod is very lightweight. It only stores configuration data and doesn't keep packs loaded in memory.

## Privacy & Safety

### Does this send data to servers?
**No.** This mod is entirely client-side and does not send any data to servers or external services.

### Can servers detect this mod?
Servers can see you have Fabric installed (if they check), but they cannot specifically detect this mod or what it does.

### Is this allowed on servers?
Since this is purely a client-side quality-of-life mod that doesn't provide any gameplay advantage, it should be fine on most servers. However, always check server-specific rules.

### Does this modify game files?
No, it only reads and reloads existing resource packs. It doesn't modify any game files.

## Support

### Where can I report bugs?
GitHub Issues: https://github.com/Anom-uxx/Resourcepack-loader-/issues

### How can I request a feature?
Open a GitHub issue with the "enhancement" tag.

### Is there a Discord?
Not currently. Use GitHub Issues for support.

### How can I contribute?
See [DEVELOPER_GUIDE.md](DEVELOPER_GUIDE.md) for development setup and contribution guidelines.

## Compatibility

### Does this work with other mods?
Generally yes. This mod doesn't modify core game mechanics, so it should be compatible with most other mods.

### Known incompatibilities?
None currently known. Report any issues on GitHub.

### Does this work with custom launchers?
Yes, as long as they support Fabric mods (e.g., MultiMC, Prism Launcher, ATLauncher).

## Miscellaneous

### Why "Resource Pack Loader"?
Because it loads (enables/disables) resource packs based on server profiles!

### Is this open source?
Yes! MIT License. See the GitHub repository.

### Can I use this in a modpack?
Yes! Just credit the original author and include a link to the GitHub repository.

### Will this be updated for newer Minecraft versions?
Updates depend on community interest and Minecraft API changes. Feel free to contribute!
