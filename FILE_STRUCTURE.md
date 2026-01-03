# Complete File Structure

```
Resourcepack-loader-/
│
├── .git/                                    # Git repository data
├── .gitignore                               # Git ignore rules
│
├── README.md                                # Main user documentation
├── LICENSE                                  # MIT License
├── CHANGELOG.md                             # Version history
├── FAQ.md                                   # Frequently asked questions
├── QUICK_START.md                           # Quick start guide
├── DEVELOPER_GUIDE.md                       # Developer documentation
├── TECHNICAL_DOCS.md                        # Technical implementation details
├── PROJECT_SUMMARY.md                       # Project overview
├── config-example.json                      # Example configuration file
│
├── build.gradle                             # Gradle build script
├── settings.gradle                          # Gradle settings
├── gradle.properties                        # Project properties
├── gradlew                                  # Gradle wrapper (Unix)
├── gradlew.bat                              # Gradle wrapper (Windows)
│
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar               # Gradle wrapper JAR
│       └── gradle-wrapper.properties        # Wrapper configuration
│
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── anomuxx/
        │           └── resourcepackloader/
        │               │
        │               ├── ResourcePackLoader.java          # Main mod class
        │               ├── ModMenuIntegration.java          # ModMenu API
        │               │
        │               ├── config/
        │               │   ├── ModConfig.java               # Config manager
        │               │   └── ServerProfile.java           # Profile data
        │               │
        │               ├── event/
        │               │   └── ServerConnectionHandler.java # Event handler
        │               │
        │               ├── manager/
        │               │   └── PackManager.java             # Pack logic
        │               │
        │               └── gui/
        │                   ├── ConfigScreen.java            # Main GUI
        │                   ├── ServerListWidget.java        # Server list
        │                   └── PackListWidget.java          # Pack list
        │
        └── resources/
            ├── fabric.mod.json                              # Mod metadata
            │
            └── assets/
                └── resourcepack-loader/
                    └── lang/
                        ├── en_us.json                       # English
                        └── de_de.json                       # German
```

## File Count Summary

### Documentation Files (9)
- README.md
- LICENSE
- CHANGELOG.md
- FAQ.md
- QUICK_START.md
- DEVELOPER_GUIDE.md
- TECHNICAL_DOCS.md
- PROJECT_SUMMARY.md
- config-example.json

### Source Files (9)
- ResourcePackLoader.java
- ModMenuIntegration.java
- ModConfig.java
- ServerProfile.java
- ServerConnectionHandler.java
- PackManager.java
- ConfigScreen.java
- ServerListWidget.java
- PackListWidget.java

### Build Files (6)
- build.gradle
- settings.gradle
- gradle.properties
- gradlew
- gradlew.bat
- gradle/wrapper/gradle-wrapper.properties

### Resource Files (3)
- fabric.mod.json
- lang/en_us.json
- lang/de_de.json

### Configuration Files (1)
- .gitignore

---

## Total Files: 28

**Lines of Code**: ~1,500+ Java  
**Documentation**: ~3,000+ lines  
**Total Project Size**: ~5,000+ lines

---

## Key Directories

### `/src/main/java/`
All Java source code organized by package:
- `config/` - Configuration management
- `event/` - Event handling
- `manager/` - Core logic
- `gui/` - User interface

### `/src/main/resources/`
Mod resources:
- `fabric.mod.json` - Mod metadata
- `assets/` - Translations, icons, etc.

### `/gradle/`
Build system configuration

### Root Directory
Documentation and configuration files

---

## Generated Directories (Not in Git)

These are created during build:

```
build/                    # Compiled classes and JARs
├── classes/             # Compiled .class files
├── libs/                # Built mod JAR
├── resources/           # Processed resources
└── tmp/                 # Temporary build files

.gradle/                 # Gradle cache
run/                     # Test client files
```

---

## Runtime Files (Created by mod)

```
.minecraft/
└── config/
    └── resourcepack-loader.json    # User configuration
```

---

## Development Files

When imported into IDEs:

```
.idea/           # IntelliJ IDEA
.vscode/         # Visual Studio Code
.settings/       # Eclipse
.classpath       # Eclipse
.project         # Eclipse
*.iml            # IntelliJ IDEA
```

These are ignored by `.gitignore`.
