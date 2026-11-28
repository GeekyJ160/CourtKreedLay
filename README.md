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

## GitHub Actions verification

The workflow at `.github/workflows/android.yml` verifies clean-checkout debug
keystore generation and reuse, Android unit tests, lint, and debug APK assembly.
It runs without Firebase credentials. Gradle and build caches are disabled so the
initial verification does not depend on restored outputs.

### Setup and running

1. Add the workflow and these instructions to PR #1's branch,
   `feat/courtiq-runnable-local-first`, which contains the corrected keystore task.
2. If your connected token cannot write workflows, use GitHub's
   **Add file → Create new file** on that branch. Alternatively, use Git credentials
   with repository write access and workflow permission: classic PAT `workflow`
   scope, or fine-grained PAT **Contents: write** and **Workflows: write**.
3. Under **Settings → Actions → General**, enable Actions and allow the actions
   referenced in the workflow. No Firebase secrets, `google-services.json`,
   release keystore, or environment secrets are required.
4. A new commit on the open PR triggers the workflow. Open
   **PR #1 → Checks → Android verification** to follow it. After the workflow
   reaches `main`, manual runs are available through
   **Actions → Court IQ Android → Run workflow**.
5. Download `court-iq-debug-…` from the run's **Artifacts** section and unzip it
   to obtain `app-debug.apk`. Reports are in `court-iq-reports-…`.
   Artifacts are retained for 14 days.
6. After the first successful run, optionally require **Android verification**
   in the `main` branch protection rule.

Tests, lint, and assembly run independently; failures still fail the job. An
assembled APK can be published even if tests or lint fail, so check the run's
result before using it.

Each run generates a fresh debug signing key. Installing an APK over an earlier
build signed with a different key requires uninstalling the earlier app, which
removes its local data. These APKs are for testing.

### Verification baseline

At commit `f37ce03646ab335a6e2dcaa23716cf6f333d93c1`, source inspection confirmed
that both signing and generation use the project-root `debug.keystore`.
The repository's keytool arguments generated a readable temporary JKS using
Java 21. Local Gradle verification was blocked downloading Gradle 9.3.1 because
the verification environment could not reach the distribution server; it also
had no Android SDK. Unit tests, lint, APK assembly, and artifact upload were
therefore not verified in that environment. Check the Actions run for actual CI
results; this baseline is not a claim that CI passed.
