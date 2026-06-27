# PhoneGuard

An Android app that blocks adult/NSFW content by displaying a warning screen when detected in your browser.

## Features
- Scans browser content in real-time
- Full-screen warning overlay + voice alert when blocked keywords detected
- 30-day clean streak tracker
- Auto-start on phone boot
- Lightweight — only active when a browser is open
- English, classic UI design

## Installation

1. Download and unzip the project
2. Push to GitHub (see Termux steps below)
3. GitHub automatically builds APK
4. Download APK and install on phone

## Termux Setup

```
cd ~/storage/downloads
unzip PhoneGuard.zip
cd PhoneGuard
git init
git add .
git config --global user.email "you@example.com"
git config --global user.name "YourName"
git commit -m "initial commit"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/PhoneGuard.git
git push -u origin main
```

Use GitHub Personal Access Token (with repo + workflow scope) as password.

## First Time Setup

After installing:
1. Open PhoneGuard
2. Tap "Open Accessibility Settings" → enable PhoneGuard
3. Tap "Grant Overlay Permission" → allow
4. Edit keywords if needed, tap "Save Keywords"
5. Tap "Test Now" to preview warning screen

## How It Works

- Service monitors browser content continuously
- When a blocked keyword is detected → full-screen warning appears
- Warning only disappears after phone restart
- Streak resets on restart (incentive to stay clean)
- Background service starts automatically on boot
