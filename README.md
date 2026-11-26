# Court IQ

Court IQ is an Android basketball-training companion for combo-guard development. It includes guided workouts, a shot-tracking lab, player-skill profiles, confidence missions, parent coaching notes, and an optional Firebase AI coach.

## Build and test

Use JDK 17 and Android SDK Platform 36.1 or newer. From the repository root:

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`. Debug builds generate an ignored local signing key when needed. Release signing requires a separately provisioned upload keystore and the `KEYSTORE_PATH`, `STORE_PASSWORD`, and `KEY_PASSWORD` environment variables.

## Storage modes

Without Firebase app configuration, Court IQ starts in on-device mode. Player settings, workouts, missions, observations, schedule updates, and Shot Lab statistics are saved locally; training and navigation do not require an account. Coach IQ's generative features require Firebase AI Logic.

To enable account-backed storage and Google sign-in, place the Firebase Android configuration at `app/google-services.json` for application ID `com.aistudio.courtiq.kxwpmz`. Enable Google Authentication, configure the Firestore database whose ID is in `app/src/main/res/values/firebase_applet_config.xml`, and enable Firebase AI Logic if you want the AI coach. The configuration file is ignored by Git. App Check debug tokens must be registered in the Firebase console for development requests; configure a production App Check provider before releasing.

Local and account-backed profiles are separate. Signing in does not automatically merge or upload the existing on-device profile.
