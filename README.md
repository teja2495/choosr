# Choosr - Random Picker

Choosr was built for people who hate decision fatigue. Create lists, add items, and tap the shuffle icon to let the app pick one for you. It's fast, clean, and focused on one job: helping you decide without overthinking.

[![Get it on Google Play](https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png)](https://play.google.com/store/apps/details?id=com.tk.choosr)

## Features

- 🎲 **Smart Random Selection** - Intelligently avoids repeating the same result in a session
- 📋 **Flexible Views** - Switch between list view and block view
- 🌙 **Dark Mode** - Comes with dark mode out of the box
- 🎨 **Customizable Themes** - Set theme colors for individual lists
- 💾 **Data Management** - Import and export your entire data
- 🔒 **Privacy First** - All data stays on your device
- 📱 **Open Source** - The code is available on GitHub

## Technical Details

### Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 36 (Android 15)
- **Build System**: Gradle with Kotlin DSL

### Architecture

The app follows a clean architecture pattern with clear separation of concerns:

- **Data Layer**: `PreferencesListRepository` handles all data persistence using Android's SharedPreferences
- **ViewModel Layer**: `ListsViewModel` manages UI state and business logic using StateFlow for reactive updates
- **UI Layer**: Jetpack Compose screens organized by feature (home, edit, shuffle, settings)
- **Navigation**: Navigation Compose for type-safe navigation between screens

### Key Libraries

- **Jetpack Compose**: Modern declarative UI framework
- **Material 3**: Material Design 3 components and theming
- **Navigation Compose**: Type-safe navigation between screens
- **Lifecycle ViewModel**: ViewModel integration with Compose
- **Gson**: JSON serialization for data export/import
- **Kotlin Coroutines**: Asynchronous operations and state management

### Data Storage

- **Local Storage**: All data is stored locally using `SharedPreferences`
- **Serialization**: JSON format using Gson for export/import functionality
- **Data Models**: Immutable data classes with proper sanitization and validation
- **No Cloud Sync**: All data remains on-device for maximum privacy

### Smart Shuffle Algorithm

The app implements an intelligent shuffle system that avoids repeating results:

- **Queue-based Approach**: When "avoid previous results" is enabled, all items are shuffled once into a queue
- **Efficient Memory**: Uses `ArrayDeque` for O(1) operations
- **Session Management**: Each list maintains its own shuffle session that resets when items are modified
- **Fallback**: Falls back to simple random selection when the feature is disabled

### Project Structure

```
app/src/main/java/com/tk/choosr/
├── data/              # Data models and repository
├── navigation/        # Navigation setup and routes
├── ui/                # Compose UI screens and components
│   ├── components/    # Reusable UI components
│   ├── edit/          # Edit list screen
│   ├── home/          # Home screen
│   ├── settings/      # Settings screen
│   ├── shuffle/       # Shuffle-related screens
│   └── theme/         # Theme configuration
├── util/              # Utility classes (BackupManager, ShuffleSessionManager)
└── viewmodel/         # ViewModels for state management
```

### UI Features

- **Edge-to-Edge Design**: Modern Android edge-to-edge UI implementation
- **Material 3 Theming**: Dynamic color theming with custom list colors
- **Dark Mode**: Built-in dark mode support
- **Smooth Animations**: Compose animations for transitions and interactions

## Privacy

Choosr is committed to your privacy. All data is stored locally on your device and is never transmitted or shared with third parties. For more details, see the [Privacy Policy](PRIVACY_POLICY.md).

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contact

- **Developer**: Teja Karlapudi
- **Email**: tejakarlapudi.apps@gmail.com
- **Support Email**: tejakarlapudi.apps@gmail.com


