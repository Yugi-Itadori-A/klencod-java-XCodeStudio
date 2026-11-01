# XCode Studio

Android IDE starter project written in Java.

## Included in this starter
- Native Android Java application scaffold.
- Black (`#000000`), Comfort Dark, and White themes.
- Language selector listing the 16 planned UI languages; this starter currently translates the primary home/settings flow into Arabic and English only.
- Local project workspace.
- Create and delete local projects.
- ZIP import through Android's Storage Access Framework.
- Basic text-file preview/edit/save.
- GitHub Actions sample workflow for building a debug APK.
- Design placeholders and implementation notes for GitHub, AI providers, build reporting, and security verification.

## Requirements
- Android Studio with Android SDK 35.
- JDK 17.
- Android Gradle Plugin 8.7.3 / Gradle 8.9 or a compatible Gradle installation.

## Build
Open this folder in Android Studio and sync Gradle, then run the `app` configuration.

For command line:
```bash
gradle assembleDebug
```

The sample GitHub Actions workflow is at `.github/workflows/android-build.yml`.

## Important limitations
This is an initial source scaffold, not the complete IDE specification. GitHub OAuth/GitHub App authentication, Git operations, remote builds, AI API calls, secure API-key storage, full multilingual translations, code intelligence/LSP, debugger, terminal, APK/AAB signing, and security-provider integrations are not implemented yet.

The ZIP importer uses Android's document picker and rejects archive path traversal. For production use, add archive size/file-count limits and more robust error handling.

GitHub Actions availability, quotas, and billing depend on GitHub's current plan and repository eligibility. Public repositories do not mean literally unlimited compute, storage, concurrency, or artifact retention.

No application is uploaded to Google Play by this project.
