# Vocabulous - English Learning App

This Android application helps users learn English vocabulary, grammar, and pronunciation through interactive exercises and quizzes. Built with Jetpack Compose, MVVM architecture, and Firebase integration.

## Features

- User authentication with Firebase Auth (email/password and Google Sign-In)
- Vocabulary learning with categorized word lists
- Grammar lessons and exercises
- Pronunciation practice
- Progress tracking and statistics
- Cloud data synchronization with Firestore
- Offline support with Room database

## Setup Instructions

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 11 or higher
- Firebase account

### Firebase Setup
1. Create a new Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Add an Android app to your Firebase project
   - Use package name: `com.aregyan.compose`
   - Download the `google-services.json` file
3. Place the `google-services.json` file in the app directory
   - Note: This file contains API keys and should not be committed to version control
   - A template file `google-services.json.template` is provided for reference
4. Enable Authentication methods in Firebase Console:
   - Email/Password
   - Google Sign-In
5. Create Firestore Database in Firebase Console
6. Deploy Firestore security rules (available in the `firestore.rules` file)

### Building the App
1. Clone the repository
2. Add your `google-services.json` file to the app directory
3. Sync the project with Gradle files
4. Build and run the application

## Architecture

The app follows MVVM (Model-View-ViewModel) architecture and Repository pattern:

- **Model**: Data classes and Room entities
- **View**: Jetpack Compose UI components
- **ViewModel**: Manages UI-related data and business logic
- **Repository**: Abstracts data sources (Firestore and Room)

## The app has the following base packages:

- **data**: Models, Room database, and DAOs
- **di**: Hilt dependency injection modules
- **repository**: Repository implementations for data access
- **ui**: Compose UI components and ViewModels
- **util**: Utility classes and extensions

## Technologies Used

- **Jetpack Compose**: Modern UI toolkit for Android
- **Coroutines & Flow**: Asynchronous programming
- **Hilt**: Dependency injection
- **Room**: Local database
- **Firebase Auth**: User authentication
- **Firestore**: Cloud database
- **Navigation Compose**: In-app navigation
- **Coil**: Image loading
- **Material Design 3**: UI components and theming

## Library reference resources:

- MVVM Architecture: https://developer.android.com/jetpack/guide
- Hilt: https://developer.android.com/training/dependency-injection/hilt-android
- Coroutines: https://developer.android.com/kotlin/coroutines
- Firebase: https://firebase.google.com/docs/android/setup
- Firestore: https://firebase.google.com/docs/firestore
- Room: https://developer.android.com/training/data-storage/room
- Jetpack Compose: https://developer.android.com/jetpack/compose
- Navigation Compose: https://developer.android.com/jetpack/compose/navigation