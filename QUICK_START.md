# Quick Start Guide

## For Users

### Installation (3 Steps)

1. **Install Fabric**
   - Download [Fabric Installer](https://fabricmc.net/use/)
   - Run installer, select Minecraft 1.21.1
   - Click "Install"

2. **Download Required Mods**
   - [Fabric API](https://modrinth.com/mod/fabric-api) (Required)
   - [ModMenu](https://modrinth.com/mod/modmenu) (Optional)
   - Resource Pack Loader (this mod)

3. **Install Mods**
   - Put all `.jar` files in `.minecraft/mods/`
   - Launch Minecraft with Fabric profile

### First Time Setup

1. **Launch Minecraft**
2. **Open Config** (Right Shift or ModMenu)
3. **Add a Server**:
   - Enter server address (e.g., "hypixel.net")
   - Click "Add Server"
   - Select the server from the list
4. **Configure Packs**:
   - Click packs to cycle states:
     - ○ = Don't change
     - ✓ = Enable on this server
     - ✗ = Disable on this server
5. **Save** and join your server!

### Example Configuration

**Server**: `pvp.server.net`
- ✓ `pvp_pack.zip` (Enable)
- ✗ `vanilla_tweaks.zip` (Disable)

**Default Profile**:
- ✓ `vanilla_tweaks.zip` (Enable when leaving servers)

## For Developers

### Build in 2 Steps

```bash
# 1. Clone
git clone https://github.com/Anom-uxx/Resourcepack-loader-.git
cd Resourcepack-loader-

# 2. Build
./gradlew build
```

**Output**: `build/libs/resourcepack-loader-1.0.0.jar`

### Run Test Client

```bash
./gradlew runClient
```

### Project Structure

```
src/main/
├── java/com/anomuxx/resourcepackloader/
│   ├── ResourcePackLoader.java       # Start here
│   ├── config/                       # Configuration
│   ├── event/                        # Event handlers
│   ├── manager/                      # Pack management
│   └── gui/                          # UI screens
└── resources/
    ├── fabric.mod.json               # Mod metadata
    └── assets/                       # Resources
```

## Troubleshooting

### Packs Not Switching?
- ✅ Check server address is correct
- ✅ Verify pack names match exactly
- ✅ Wait 2 seconds (cooldown)
- ✅ Check logs: `.minecraft/logs/latest.log`

### GUI Not Opening?
- ✅ Press Right Shift
- ✅ Or use ModMenu
- ✅ Check Fabric API is installed

### Build Failed?
- ✅ Java 21 installed?
- ✅ Run `./gradlew clean build`
- ✅ Check internet connection (downloads deps)

## Quick Commands

```bash
# Build
./gradlew build

# Run test client
./gradlew runClient

# Clean build
./gradlew clean build

# Generate IDE files
./gradlew genSources

# Check for updates
./gradlew dependencies --refresh-dependencies
```

## Config File Location

**Manual editing**: `.minecraft/config/resourcepack-loader.json`

## Common Pack Names

To find your pack names:
1. Open Minecraft
2. Options → Resource Packs
3. Hover over a pack
4. The file name is usually visible

Or check: `.minecraft/resourcepacks/`

## Video Tutorial (Placeholder)

Coming soon! Check the GitHub repository.

## Links

- 📖 [Full README](README.md)
- 🔧 [Technical Docs](TECHNICAL_DOCS.md)
- 💻 [Developer Guide](DEVELOPER_GUIDE.md)
- ❓ [FAQ](FAQ.md)
- 🐛 [Report Issues](https://github.com/Anom-uxx/Resourcepack-loader-/issues)

---

**Need help?** Open an issue on GitHub!
