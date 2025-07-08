# BPark Launcher Implementation

## Overview

The BPark system now includes a unified launcher that provides a clean interface for selecting between the Client Portal and Kiosk Terminal modes. This implementation consolidates redundant code and provides a better user experience.

## New Architecture

### 1. BParkLauncher.java
- **Main entry point** for the entire BPark system
- Provides a GUI launcher with mode selection
- Users can choose between Client Portal and Kiosk Terminal
- Uses FXML for UI layout and CSS for styling

### 2. BParkBaseApp.java
- **Common base class** for both client and kiosk applications
- Consolidates shared functionality:
  - Server connection management
  - Message sending/receiving
  - Client communication handling
  - Connection error handling
- Eliminates ~60 lines of duplicated code

### 3. Refactored Applications
- **BParkClientApp**: Now extends BParkBaseApp, focuses on client portal functionality
- **BParkKioskApp**: Now extends BParkBaseApp, focuses on kiosk terminal functionality

## Usage

### Running the System

1. **Using the Launcher (Recommended)**:
   ```bash
   java client.BParkLauncher
   ```
   - Opens the launcher GUI
   - Click "Client Portal" for login-based access (subscribers, employees, managers)
   - Click "Kiosk Terminal" for parking operations

2. **Direct Access**:
   ```bash
   # Client Portal directly
   java client.BParkClientApp
   
   # Kiosk Terminal directly  
   java client.BParkKioskApp
   ```

### Application Modes

- **Client Portal**: Login-based interface for:
  - Subscribers: Parking reservations, history, profile management
  - Employees: Parking assistance and management
  - Managers: System administration and reporting

- **Kiosk Terminal**: Public interface for:
  - Parking entry/exit operations
  - Reservation activation
  - Lost code retrieval

## Technical Improvements

### Code Consolidation
- Removed duplicate `BParkClient` inner classes
- Centralized server connection logic
- Unified message handling
- Shared connection error handling

### Better Separation of Concerns
- Launcher handles application selection
- Base class handles common networking
- Specific apps focus on their unique functionality

### Enhanced Maintainability
- Single point of truth for connection logic
- Easier to add new application modes
- Reduced code duplication

## Files Modified/Added

### New Files:
- `src/client/BParkLauncher.java` - Main launcher application
- `src/client/BParkBaseApp.java` - Common base class
- `src/client/LauncherController.java` - Launcher UI controller
- `resources/client/Launcher.fxml` - Launcher UI layout

### Modified Files:
- `src/client/BParkClientApp.java` - Refactored to extend base class
- `src/client/BParkKioskApp.java` - Refactored to extend base class  
- `resources/css/BParkStyle.css` - Added launcher styling

## Configuration

The system maintains the same configuration approach:
- Default server: `localhost:5555`
- Server IP can be changed in the login screen
- Connection settings are managed by the base class

## Benefits

1. **Unified Entry Point**: Single launcher for the entire system
2. **Reduced Duplication**: ~60 lines of duplicate code eliminated
3. **Better UX**: Clear mode selection interface
4. **Maintainability**: Centralized common functionality
5. **Extensibility**: Easy to add new application modes
6. **Consistency**: Shared styling and behavior patterns

## Future Enhancements

- Add configuration options in launcher
- Support for multiple server connections
- Enhanced error handling and recovery
- Launcher settings persistence