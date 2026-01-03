# Developer Guide

## Setup Development Environment

### Prerequisites
- JDK 21
- Git
- IDE (IntelliJ IDEA recommended)

### Clone & Setup
```bash
git clone https://github.com/Anom-uxx/Resourcepack-loader-.git
cd Resourcepack-loader-
./gradlew genSources
```

### Import to IDE

**IntelliJ IDEA**:
1. Open `build.gradle` as a project
2. Wait for Gradle to sync
3. Run configuration will be auto-generated

**Eclipse**:
```bash
./gradlew eclipse
```

**VS Code**:
1. Install "Extension Pack for Java"
2. Open folder
3. Gradle tasks will be detected

## Running the Mod

### Test Client
```bash
./gradlew runClient
```

### Build JAR
```bash
./gradlew build
# Output: build/libs/resourcepack-loader-1.0.0.jar
```

## Project Structure

```
src/main/
├── java/com/anomuxx/resourcepackloader/
│   ├── ResourcePackLoader.java          # Main entry point
│   ├── ModMenuIntegration.java          # ModMenu support
│   ├── config/                          # Configuration classes
│   ├── event/                           # Event handlers
│   ├── manager/                         # Pack management
│   └── gui/                             # GUI screens
└── resources/
    ├── fabric.mod.json                  # Mod metadata
    └── assets/resourcepack-loader/
        └── lang/                        # Translations
```

## Key Classes

### ResourcePackLoader
- Main mod initializer
- Implements `ClientModInitializer`
- Registers keybindings and events

### ModConfig
- Manages JSON configuration
- Loads/saves profiles
- Thread-safe operations

### PackManager
- Core pack manipulation logic
- F3+A style reload
- Cooldown protection

### ServerConnectionHandler
- Fabric event listeners
- Server join/leave detection
- Profile application

### ConfigScreen
- Main GUI screen
- Extends Minecraft's `Screen`
- Custom widgets for lists

## Common Tasks

### Adding a New Feature

1. Create new class in appropriate package
2. Register in `ResourcePackLoader.onInitializeClient()`
3. Add configuration fields to `ModConfig` if needed
4. Update GUI if user-facing

### Adding Translations

Edit language files:
- `src/main/resources/assets/resourcepack-loader/lang/en_us.json`
- `src/main/resources/assets/resourcepack-loader/lang/de_de.json`

### Testing Changes

```bash
# Run test client
./gradlew runClient

# Check logs
tail -f run/logs/latest.log | grep ResourcePackLoader
```

## Debugging

### Enable Debug Logs

Modify logger level in code or use a logging config.

### Common Issues

**Issue**: GUI not rendering correctly
- Check `ConfigScreen.init()` dimensions
- Verify widget positions

**Issue**: Packs not switching
- Check `PackManager.applyProfile()` logs
- Verify pack names match exactly
- Check cooldown timer

**Issue**: Server not detected
- Verify `getCurrentServerEntry()` returns valid data
- Check address normalization

## Code Style

- Use 4 spaces for indentation
- Follow Java naming conventions
- Add JavaDoc comments for public methods
- Keep methods focused and small

## Testing Checklist

Before submitting:
- [ ] Build succeeds without errors
- [ ] Test client launches
- [ ] GUI opens and functions
- [ ] Packs switch on server join
- [ ] Config saves/loads correctly
- [ ] No errors in logs
- [ ] Code is documented

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## Resources

- [Fabric Wiki](https://fabricmc.net/wiki/)
- [Yarn Mappings](https://github.com/FabricMC/yarn)
- [Fabric API Docs](https://maven.fabricmc.net/docs/fabric-api-0.107.0+1.21.1/)
- [ModMenu API](https://github.com/TerraformersMC/ModMenu)

## License

MIT License - See [LICENSE](LICENSE) file
