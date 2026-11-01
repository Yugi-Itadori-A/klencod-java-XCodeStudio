# XCode Studio implementation roadmap

## Foundation included
- Android Java app shell
- Theme selector
- Project directory and ZIP import
- Basic text file editor
- Sample GitHub Actions workflow

## Planned modules (not implemented in this starter)
- XIO Editor: tabs, syntax highlighting, completion, LSP, formatter, refactoring
- XIO Projects: templates, Git operations, compare, recovery, snapshots, backup, export ZIP
- XIO Build Engine: build queue, cache, matrix builds, tests, APK/AAB analysis, artifact handling
- XIO GitHub: secure OAuth/GitHub App, repository browser, multiple accounts, Actions logs and artifacts
- XIO AI: OpenAI, Gemini, Claude, OpenRouter, Groq, Mistral, DeepSeek and compatible endpoints
- XIO Security: dependency and secret scanning, permission analysis, license checks, signing and privacy reports
- XIO Extensions: signed plugins, permissions, SDK and marketplace
- XIO Developer Tools: terminal, debugger, API tester, database explorer and documentation generator
- XIO Localization: full translations for Arabic, English, French, German, Spanish, Portuguese, Russian, Turkish, Simplified Chinese, Japanese, Korean, Hindi, Urdu, Persian, Indonesian and Bengali

## Security principles
- Least-privilege GitHub permissions.
- Never put API tokens or signing keys in source control.
- Use Android Keystore-backed encrypted storage for credentials.
- Ask before sending source code to an AI provider.
- Treat build logs and scanner results as untrusted input.
- Security scans report findings; they do not certify an app as perfectly safe.
- No Google Play publishing workflow is included.
