<p align="center">
  <img src="https://github.com/user-attachments/assets/2f3ad704-8a47-4694-8182-177774e2484f" alt="Screenshot 2026-05-20 225332" width="48%" />
  <img src="https://github.com/user-attachments/assets/fc7b825a-4f60-4479-b4a8-e7c0e7b26002" alt="Screenshot 2026-05-20 225549" width="48%" />
</p>
# Book Reading Tracker

A personal application designed to track reading progress for local PDF books. This project fills the gap for a reader-focused tracker that integrates directly with local files.

## Features
- **Local PDF Tracking**: Specifically designed to manage and track progress of PDF files stored on your device.
- **Multi-platform Support**: Built using Kotlin Multiplatform, targeting:
  - **Android**
  - **Desktop** (JVM)
  - **Web** (Compose HTML/Canvas)
- **Reading Progress**: Keep track of where you left off in each of your books.

## Tech Stack
- **Kotlin Multiplatform (KMP)**: Shared logic across all platforms.
- **Compose Multiplatform**: Shared UI for Android, Desktop, and Web.
- **Gradle**: Build system with Version Catalogs.

## Project Structure
- `:shared`: Core logic and UI shared between platforms.
- `:androidApp`: Android-specific implementation.
- `:desktopApp`: Desktop-specific implementation.
- `:webApp`: Web-specific implementation.
