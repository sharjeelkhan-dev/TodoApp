# Smart Todo App 🚀

An advanced, intelligent task management application built with **Kotlin** and **Jetpack Compose**, featuring powerful **Generative AI** integration.

## ✨ Key Features

- **🤖 AI-Powered Commands:** Manage your tasks using natural language. Just type *"Add a task to buy groceries at 5 PM"* or *"Mark all my shopping tasks as done"*, and let Gemini 2.0 handle the rest.
- **🧠 Smart Prioritization:** An AI-driven engine that analyzes task deadlines, user-defined priorities, and descriptions to calculate a "Smart Score" (0-100), automatically highlighting what's most important.
- **🔄 Seamless Cloud Sync:** Offline-first architecture using **Room Database** with real-time cloud synchronization via **Firebase Firestore**.
- **📅 Smart Reminders:** Intelligent notification system powered by **WorkManager** to ensure you never miss a deadline.
- **🔒 Secure & Robust:** Integrated with **Firebase App Check** (Play Integrity) and **Firebase Authentication** for enterprise-grade security.
- **🌗 Dark Mode Support:** Beautiful, accessible UI that respects system theme settings with custom Material 3 components.

## 🛠 Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material 3)
- **Architecture:** Clean Architecture + MVVM + SOLID Principles
- **Dependency Injection:** Hilt
- **AI Engine:** Google Gemini 2.0 Flash (via Firebase SDK)
- **Database:** Room (Local), Firestore (Cloud)
- **Background Tasks:** WorkManager
- **Networking:** Retrofit & OkHttp
- **Reactive Programming:** Kotlin Coroutines & Flow

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug (or newer)
- A Google Gemini API Key
- Firebase Project setup

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/To-Do-List-App.git
   ```
2. Open the project in Android Studio.
3. Add your `GEMINI_API_KEY` to `app/build.gradle.kts` (or via Environment Variables).
4. Sync Gradle and run the app!

## 📸 Screenshots
*(Add your screenshots here)*

---
Developed with ❤️ by [Your Name]
