# Technical Documentation - Resource Pack Loader 

## Architecture Overview

This mod is built using the Fabric modding framework for Minecraft 1.21.1 and follows a modular architecture.

### Project Structure

```
src/main/java/com/anomuxx/resourcepackloader/
├── ResourcePackLoader.java           # Main mod entry point
├── ModMenuIntegration.java          # ModMenu API integration
├── config/
│   ├── ModConfig.java               # Configuration management
│   └── ServerProfile.java           # Server profile data structure
├── event/
│   └── ServerConnectionHandler.java # Server join/leave event handlers
├── manager/
│   └── PackManager.java             # Resource pack manipulation logic
└── gui/
    ├── ConfigScreen.java            # Main configuration GUI
    ├── ServerListWidget.java        # Server list widget
    └── PackListWidget.java          # Pack list widget
```

## Key Components

### 1. ResourcePackLoader (Main Entry)

**File**: `ResourcePackLoader.java`

The main mod initializer that:
- Loads the configuration on startup
- Registers event handlers
- Sets up the keybinding (Right Shift)
- Provides static access to config instance

**Key Methods**:
- `onInitializeClient()` - Mod initialization
- `openConfigScreen()` - Opens the GUI
- `saveConfig()` / `reloadConfig()` - Config persistence

### 2. Configuration System

**Files**: `ModConfig.java`, `ServerProfile.java`

**ModConfig.java**:
- Manages JSON-based configuration
- Stores default profile and server-specific profiles
- Handles loading/saving to disk
- Location: `.minecraft/config/resourcepack-loader.json`

**ServerProfile.java**:
- Data structure for pack enable/disable lists
- Methods to add/remove packs from lists
- Validation and state management

**JSON Structure**:
```json
{
  "defaultProfile": {
    "enable": ["pack1.zip"],
    "disable": ["pack2.zip"]
  },
  "servers": {
    "server.address.com": {
      "enable": ["pvp_pack.zip"],
      "disable": ["vanilla_tweaks.zip"]
    }
  }
}
```

### 3. Pack Manager

**File**: `PackManager.java`

Core logic for managing resource packs:

**Key Features**:
- Lightweight reload (F3+A style) using `client.reloadResources()`
- Cooldown mechanism (2 seconds) to prevent reload loops
- Incremental pack changes (only toggle what changed)
- Pack validation and existence checking

**Key Methods**:
- `applyProfile(ServerProfile)` - Apply a profile's pack configuration
- `getAllAvailablePacks()` - Get all installed packs
- `getEnabledPacks()` - Get currently enabled packs
- `triggerInGameReload()` - Trigger F3+A style reload

**Technical Details**:
```java
// Get current enabled packs
List<String> currentEnabled = packManager.getEnabledNames();

// Modify the list
newEnabled.add("pack_to_enable.zip");
newEnabled.remove("pack_to_disable.zip");

// Apply changes
packManager.setEnabledProfiles(newEnabled);

// Trigger reload (F3+A style)
client.reloadResources();
```

### 4. Server Event Handler

**File**: `ServerConnectionHandler.java`

Listens for server connection events using Fabric API:

**Events Used**:
- `ClientPlayConnectionEvents.JOIN` - Player joins server
- `ClientPlayConnectionEvents.DISCONNECT` - Player leaves server

**Behavior**:
- **On Join**: 
  - Extract server address from `client.getCurrentServerEntry()`
  - Look up profile in config
  - Apply profile with 1 second delay (ensures client fully loaded)
  
- **On Disconnect**:
  - Apply default profile if configured
  - Otherwise keep current state

**Technical Details**:
```java
// Get server address
ServerInfo serverInfo = client.getCurrentServerEntry();
String serverAddress = serverInfo.address;

// Apply profile asynchronously with delay
new Thread(() -> {
    Thread.sleep(1000); // Wait for client to be ready
    client.execute(() -> PackManager.applyProfile(profile));
}).start();
```

### 5. GUI System

**Files**: `ConfigScreen.java`, `ServerListWidget.java`, `PackListWidget.java`

**ConfigScreen.java**:
- Main configuration screen
- Two-panel layout: servers on left, packs on right
- Add/remove server functionality
- Pack state cycling (Neutral → Enable → Disable → Neutral)

**UI Layout**:
```
┌─────────────────────────────────────────────────┐
│     Resource Pack Loader Configuration          │
├─────────────────────────────────────────────────┤
│ [Server Address Field] [Add Server]             │
├──────────────────┬──────────────────────────────┤
│ Server Profiles  │ Resource Packs               │
│                  │                               │
│ Default Profile  │ ○ vanilla_tweaks.zip         │
│ localhost        │ ✓ pvp_pack.zip               │
│ example.net      │ ✗ shader_pack.zip            │
│                  │                               │
│ [Remove]         │                               │
├──────────────────┴──────────────────────────────┤
│         [Save & Close]  [Cancel]                │
└─────────────────────────────────────────────────┘
```

**Pack States**:
- **○** (Neutral/Gray) - Pack not configured for this server
- **✓** (Enable/Green) - Pack will be enabled on this server
- **✗** (Disable/Red) - Pack will be disabled on this server

**ServerListWidget.java**:
- Custom EntryListWidget for displaying servers
- Click to select server
- Highlights selected server

**PackListWidget.java**:
- Custom EntryListWidget for displaying packs
- Click to cycle pack state
- Color-coded state indicators

### 6. ModMenu Integration

**File**: `ModMenuIntegration.java`

Simple ModMenu API integration:
```java
@Override
public ConfigScreenFactory<?> getModConfigScreenFactory() {
    return ConfigScreen::new;
}
```

This adds a "Config" button in ModMenu for easy access.

## Important Implementation Details

### F3+A Style Reload (NOT Red Screen)

The mod uses `MinecraftClient.reloadResources()` which triggers an in-game reload without the Mojang loading screen:

```java
private static void triggerInGameReload(MinecraftClient client) {
    CompletableFuture.runAsync(() -> {
        client.reloadResources();
    }, client);
}
```

This is the same method triggered by pressing F3+A in-game.

### Cooldown Protection

Prevents rapid reload loops:
```java
private static final long RELOAD_COOLDOWN_MS = 2000;
private static long lastReloadTime = 0;

// Check before reload
if (currentTime - lastReloadTime < RELOAD_COOLDOWN_MS) {
    return false; // Skip reload
}
```

### Thread Safety

Profile application uses proper thread synchronization:
```java
new Thread(() -> {
    Thread.sleep(1000); // Background delay
    client.execute(() -> {
        // Execute on client thread
        PackManager.applyProfile(profile);
    });
}).start();
```

### Server Address Normalization

Ensures consistent matching:
```java
private String normalizeServerAddress(String address) {
    return address.toLowerCase().trim();
}
```

## Building & Development

### Building the Mod

```bash
# Clean build
./gradlew clean build

# Development run
./gradlew runClient
```

### Testing

1. Run the client with `./gradlew runClient`
2. Configure profiles in the GUI
3. Join a test server (or localhost)
4. Verify packs are toggled correctly
5. Check logs for debug information

### Debugging

Enable debug logging by checking the game logs:
- Location: `.minecraft/logs/latest.log`
- Prefix: `[ResourcePackLoader]`

Example log output:
```
[INFO] Initializing Resource Pack Loader
[INFO] Loaded configuration with 2 server profile(s)
[INFO] Joined server: localhost
[INFO] Enabling pack: my_pack.zip
[INFO] Triggering in-game resource reload (F3+A style)
[INFO] Applied profile successfully
```

## Safety Features

1. **No Auto-Enable**: Only configured servers trigger pack changes
2. **Cooldown**: 2-second reload protection
3. **Validation**: Pack existence checked before enabling
4. **Error Handling**: Comprehensive try-catch blocks
5. **Thread Safety**: Proper synchronization for async operations

## Compatibility

- **Minecraft**: 1.21.1 (should work with 1.21.10 and 1.21.11)
- **Fabric Loader**: 0.16.0+
- **Fabric API**: Required
- **Java**: 21
- **ModMenu**: Optional dependency

## Future Improvements

Potential enhancements:
- Regex server matching
- Pack groups/presets
- Import/export profiles
- Visual pack preview
- Hotkey to manually apply profiles
- Per-world singleplayer profiles
