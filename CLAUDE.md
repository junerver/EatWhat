# EatWhat Development Guidelines

Last updated: 2025-12-29

> **⚠️ IMPORTANT**: This file is maintained for backwards compatibility.
>
> **All AI development tools should use [`AGENTS.md`](AGENTS.md) as the single source of truth.**
>
> The unified rule set at [`AGENTS.md`](AGENTS.md) ensures consistency across all AI tools:
>
> - Kilocode (`.kilocode/rules.md`)
> - Claude Code (`CLAUDE.md`)
> - Other AI development tools

---

## 📋 Complete Project Rules

**For all development guidelines, architecture principles, code patterns, and best practices, please read:**

### → [`AGENTS.md`](AGENTS.md) ←

This includes:

- 🎯 Architecture Principles
- 🛠️ Technology Stack
- 📁 Project Structure
- 💻 Code Style & Conventions
- 🎨 UI Development with ComposeHooks
- 🗄️ Database Guidelines & Patterns
- 🔧 Build & Development Commands
- 🧪 Testing Guidelines
- 🚫 Anti-Patterns to Avoid
- 📝 Documentation Standards

---

## Quick Reference

### Common Commands

```bash
# Build & Install
./gradlew assembleDebug
./gradlew installDebug

# Testing
./gradlew test
./gradlew connectedAndroidTest

# Code Quality
./gradlew ktlintFormat
./gradlew ktlintCheck
```

### Technology Stack Summary

- **Language**: Kotlin 2.1.0
- **UI**: Jetpack Compose with ComposeHooks 2.2.1 (hooks2)
- **Database**: Room 2.6.1
- **Navigation**: Navigation Compose 2.7.6
- **Design**: Material Design 3

---

## Recent Changes

- 2025-12-29: Updated to Kotlin 2.1.0, Compose BOM 2024.06.00, ComposeHooks 2.2.1 (hooks2 package)
- 003-settings-sync-export: Added Kotlin 1.9.21 + Jetpack Compose, ComposeHooks 3.0.0, Room 2.6.1, dav4jvm 2.2.1, kotlinx.serialization 1.6.2
- 002-dark-mode-adapt: Added Kotlin 1.9.21 + Jetpack Compose (Material 3), ComposeHooks
- 2025-12-15: **Created unified `AGENTS.md` as single source of truth for all AI tools**

---

## Documentation

- [`AGENTS.md`](AGENTS.md) - Complete project rules (main reference)
- [`docs/RULES-SYNC.md`](docs/RULES-SYNC.md) - Rules synchronization guide
- [`.kilocode/README.md`](.kilocode/README.md) - Kilocode integration info
- [`specs/`](specs/) - Feature specifications and tasks

---

**Remember**: All project rules are in [`AGENTS.md`](AGENTS.md). This file is for quick reference only.

<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->
