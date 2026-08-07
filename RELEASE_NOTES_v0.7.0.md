# TrueHub v0.7.0-Alpha — August Feature Drop 🚀

Hey r/TrueNAS and r/selfhosted! Just shipped a massive update to TrueHub (the Android TrueNAS management app).

## What's New

### 🔍 App-Wide Search
Tap the search icon from any tab and search across your entire TrueNAS instance:
- **Installed apps** by name, title, description, categories, keywords
- **Marketplace catalog** — queries live from TrueNAS, shows real app icons
- **Storage pools, disks, shares** — find by name, path, model, serial
- **Containers & VMs** — search by name, status, specs
- **30+ screens & subsections** — type "network", "users", "update" and jump directly there
- **Quick actions grid** when empty — shortcuts to Instance Settings, API Keys, Marketplace, etc
- **Recent searches** with per-entry delete and clear-all

### 🔐 MFA / TOTP Two-Factor Authentication
TrueHub now supports TrueNAS accounts with 2FA enabled:
- Full TOTP flow: enter credentials → OTP page appears automatically
- Cold-boot auto-login for TOTP users — credentials validated, OTP page shown directly
- Session recovery with automatic re-authentication
- Enter-to-submit on password/API key fields

### ⚡ Performance
- Homepage is significantly smoother — removed the heavy animated gauges that caused jank
- Reduced API calls on startup by 60%
- Static backgrounds on login/setup screens (no more infinite animation recomposition)

### 🎨 UI Polish
- Pill chips on homepage: Update → Performance → Instance Settings → System Info (horizontally scrollable)
- App icons render in search results
- Zero flash transition from login to main
- OTP page with full Material 3 styling and wavy background

## Download
[GitHub Releases](https://github.com/Imnotndesh/TrueHub/releases/tag/v0.7.0-Alpha)

---

Feedback and bug reports welcome! What would you like to see next?
