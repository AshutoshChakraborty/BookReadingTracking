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
