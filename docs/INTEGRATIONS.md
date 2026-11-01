# External integrations

## GitHub
Use OAuth or a GitHub App with least-privilege permissions. Use the system browser or a secure Custom Tab for authentication. Do not collect the user's GitHub password inside the app.

## AI providers
Provider API keys must be stored using Android Keystore-backed encryption. Never write keys to project files, logs, Git history, or GitHub Actions output. Ask for explicit consent before sending source code to an AI provider.

## Security verification
Do not claim a universal Google security certificate. Integrate actual scanners/providers and show the provider, scan time, scanned artifact hash, findings, and limitations. Google Play Protect and Play Integrity are not general-purpose public APK certification endpoints.
