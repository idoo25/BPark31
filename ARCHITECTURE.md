# BPark System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                       BPark System Launcher                    │
│                    (BParkLauncher.java)                        │
│                                                                 │
│  ┌─────────────────┐    ┌─────────────────┐                   │
│  │  Client Portal  │    │ Kiosk Terminal  │                   │
│  │    Button       │    │     Button      │                   │
│  └─────────────────┘    └─────────────────┘                   │
└─────────────────────────────────────────────────────────────────┘
              │                        │
              ▼                        ▼
┌─────────────────────────┐  ┌─────────────────────────┐
│    BParkClientApp       │  │    BParkKioskApp        │
│                         │  │                         │
│ • Login Interface       │  │ • Parking Operations    │
│ • User Portals:         │  │ • ID/RF Card Login      │
│   - Subscriber Portal   │  │ • Reservation Activation│
│   - Employee Portal     │  │ • Car Retrieval         │
│   - Manager Portal      │  │                         │
│                         │  │                         │
│ extends BParkBaseApp    │  │ extends BParkBaseApp    │
└─────────────────────────┘  └─────────────────────────┘
              │                        │
              └────────┬───────────────┘
                       ▼
        ┌─────────────────────────────────────┐
        │           BParkBaseApp              │
        │        (Abstract Base Class)       │
        │                                     │
        │ • Server Connection Management      │
        │ • Message Sending/Receiving         │
        │ • Client Communication Handling     │
        │ • Connection Error Management       │
        │ • Common JavaFX Application Logic   │
        └─────────────────────────────────────┘
                       │
                       ▼
        ┌─────────────────────────────────────┐
        │         Server Communication        │
        │                                     │
        │ • MySQL Database (Users, Parking)   │
        │ • OCSF Client-Server Framework      │
        │ • Message Serialization/Handling    │
        └─────────────────────────────────────┘
```

## Key Benefits:

1. **Single Entry Point**: Users start with one launcher application
2. **Code Reuse**: Common networking code shared between applications
3. **Maintainability**: Changes to connection logic only needed in one place
4. **Separation of Concerns**: Each app focuses on its specific functionality
5. **Extensibility**: Easy to add new application modes to the launcher

## File Structure:
```
src/client/
├── BParkLauncher.java      (Main entry point)
├── LauncherController.java (Launcher UI controller)
├── BParkBaseApp.java       (Common base class)
├── BParkClientApp.java     (Client portal - refactored)
├── BParkKioskApp.java      (Kiosk terminal - refactored)
└── ClientMessageHandler.java (Message handling)

resources/client/
├── Launcher.fxml           (Launcher UI layout)
├── Login.fxml              (Client login)
├── KioskMain.fxml          (Kiosk main)
└── ... (other FXML files)

resources/css/
└── BParkStyle.css          (Updated with launcher styling)
```