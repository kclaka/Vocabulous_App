# Firebase Authentication Setup for English Learning App

This guide will help you set up Firebase Authentication for your Android English learning app.

## Prerequisites

1. A Google account
2. Android Studio installed

## Steps to Set Up Firebase Authentication

### 1. Create a Firebase Project

1. Go to the [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project" and follow the prompts to create a new project
3. Enter a project name (e.g., "English Learning App")
4. Choose whether to enable Google Analytics (recommended)
5. Click "Create project"

### 2. Register Your Android App with Firebase

1. In the Firebase console, click on the Android icon to add an Android app
2. Enter your app's package name: `com.aregyan.compose`
3. Enter a nickname for your app (optional)
4. Enter your app's SHA-1 signing certificate (required for Google Sign-In)
   - You can get this by running the following command in your project directory:
     ```
     ./gradlew signingReport
     ```
5. Click "Register app"

### 3. Download and Add the Configuration File

1. Download the `google-services.json` file
2. Place it in the app-level directory of your project (the `/app` folder)

### 4. Enable Authentication Methods in Firebase Console

1. In the Firebase console, go to "Authentication" in the left sidebar
2. Click on "Get started" or "Sign-in method"
3. Enable the authentication methods you want to use:
   - Email/Password: Toggle to enable
   - Google: Toggle to enable and configure with your Web Client ID

### 5. Update Your App's Resources

1. Open the `app/src/main/res/values/auth_strings.xml` file
2. Replace `YOUR_WEB_CLIENT_ID` with the actual Web Client ID from your Firebase project
   - You can find this in the Firebase console under Project settings > General > Your apps > Web apps

## Testing Authentication

After completing the setup, you can test the authentication flow in your app:

1. Run your app on an emulator or physical device
2. Try to sign up with email and password
3. Try to sign in with Google
4. Verify that user profiles are created in Firestore

## Troubleshooting

- If Google Sign-In fails, make sure your SHA-1 certificate is correctly added to the Firebase project
- If authentication fails, check the Logcat for detailed error messages
- Ensure that the `google-services.json` file is correctly placed in the app directory
- Verify that all dependencies are correctly added to your Gradle files

## Next Steps

After setting up authentication, you might want to:

1. Implement user profile management
2. Add progress tracking for the user's English learning journey
3. Implement data synchronization across devices
4. Add security rules to your Firestore database
