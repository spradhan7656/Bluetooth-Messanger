# Bluetooth Messenger

A simple Android application built with Kotlin that allows users to send and receive messages over Bluetooth. This project demonstrates core concepts of Bluetooth communication, device discovery, secure RFCOMM connections, and a clean UI for messaging.

---

## 🚀 Features

* Discover nearby Bluetooth devices
* Connect to paired devices
* Send and receive real-time messages
* Simple and clean chat interface
* Uses Kotlin and Android Jetpack components

---

## 📁 Project Structure

```
Bluetooth-Messenger/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/ (or kotlin/) – main app source code
│   │   │   ├── res/ – layouts, drawables, UI resources
│   ├── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
```

---

## 🛠️ Requirements

* Android Studio Flamingo or later
* Android device with Bluetooth capability
* Minimum SDK: *depends on project config*
* Two physical devices required for chat testing

---

## 📦 Installation

1. Clone this repository:

   ```bash
   git clone https://github.com/spradhan7656/Bluetooth-Messanger.git
   ```
2. Open the project in **Android Studio**.
3. Let Gradle sync automatically.
4. Run the app on two Android devices.

---

## 🔒 Permissions

The app requires the following Bluetooth permissions depending on Android version:

* `BLUETOOTH`
* `BLUETOOTH_ADMIN`
* `BLUETOOTH_CONNECT`
* `BLUETOOTH_SCAN`
* `ACCESS_FINE_LOCATION` (older Android versions)

---

## 📡 How It Works

1. Device A hosts a Bluetooth server socket.
2. Device B discovers and connects to Device A.
3. Both devices exchange messages via input/output streams.
4. Messages are displayed in the app's chat interface.

---

## 🤝 Contributing

Pull requests are welcome! Please follow standard Kotlin and Android architecture patterns.

---

## 📜 License

This project currently has **no license**. If you'd like, I can help you add one (MIT, Apache, GPL, etc.).

---

If you'd like to expand this README with screenshots, architecture diagrams, or setup tutorials, just tell me!
