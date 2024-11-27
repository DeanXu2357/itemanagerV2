# ItemManager V2

An Android application for personal asset management that allows users to quickly record and manage personal items. Features include barcode scanning for quick item addition and customizable item types and attributes.

[繁體中文版](README_zh.md)

## Features

- Item Management (Add, Edit, Delete, View)
- Barcode Scanning (supports barcode and QR code)
- Custom Item Types and Attributes
- Image Management (item cover, barcode image, multiple item images)
- Paginated Loading and Virtual Scrolling List

## Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Dagger Hilt
- **Local Database**: Room Database
- **Asynchronous Operations**: Kotlin Coroutines & Flow

## Project Structure

```
com.example.itemanagerv2/
├── data/                    # Data layer
│   ├── local/              # Local data related
│   │   ├── dao/           # Data Access Objects
│   │   ├── entity/        # Data entities
│   │   └── model/         # Data models
│   ├── manager/           # Business logic managers
│   └── repository/        # Data repositories
├── di/                     # Dependency injection
├── ui/                     # UI layer
│   ├── component/         # UI components
│   └── theme/            # Theme related
└── viewmodel/             # ViewModel layer
```

## Data Models

### Main Entities

1. **Item**: Basic item information
2. **ItemCategory**: Item categories
3. **CategoryAttribute**: Category attribute definitions
4. **ItemAttributeValue**: Item attribute values
5. **Image**: Image resources

## Development Environment Setup

1. Required Tools:
   - Android Studio Arctic Fox or newer
   - JDK 11 or higher
   - Android SDK 31 or higher

2. Clone the project:
   ```bash
   git clone [repository-url]
   ```

3. Open project in Android Studio

4. Sync Gradle files

5. Run the project
   - Select target device (emulator or physical device)
   - Click "Run" button

## Development Guide

### Adding New Features

1. Create necessary classes in appropriate packages
2. Follow MVVM architecture pattern
3. Use dependency injection for managing dependencies
4. Write unit tests

### Database Operations

- Use Room DAO for database operations
- Handle data logic in Repository layer
- Provide data to UI layer through ViewModel

### UI Development

- Create UI components using Jetpack Compose
- Follow Material Design guidelines
- Ensure adaptation to different screen sizes

## Planned Features

1. User Management System
2. Cloud Sync and Backup
3. Multi-platform Support
4. Advanced Statistics and Reporting
5. Custom Barcode Recognition Rules
6. Detailed Item Information Management
7. Settings Page
8. Enhanced Form Validation

## License

[License information to be added]
