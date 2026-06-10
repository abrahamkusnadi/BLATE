# 💖 BLATE - Modern Dating Application

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Java](https://img.shields.io/badge/Language-Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Firebase](https://img.shields.io/badge/Database-Firestore-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)

BLATE is a sleek, intuitive, and interactive Android dating application designed to help users discover, connect, and chat with potential matches around them.

> **Project Context & Author's Note:**
> Originally initiated as a university group project, I (**Ignatius Abraham Aristio Kusnadi**) took the initiative to fork and heavily refactor this repository. Just like my previous Laravel project, I felt the initial group collaboration did not reach its maximum potential or meet standard industry practices. I have since independently re-engineered the data flow, polished the UI/UX, squashed persistent UI lifecycle bugs, and optimized the Firebase integration to transform it into a proper, production-ready application for my developer portfolio.

---

## ✨ Key Features

* **🔍 Discover (Swipe & Match):** Browse through potential matches with a dynamic, Tinder-like interface. Features real-time Firestore fetching and custom empty states when users run out of nearby profiles.
* **💬 Interactive Chat System:** A fully functional, real-time chat interface using `RecyclerView`. Features dynamic chat bubbles, integrated profile pictures using `CircleImageView`, and seamless data passing between activities.
* **🕒 Activity History:** A dedicated tracking page where users can review profiles they have "Liked" or "Disliked", complete with color-coded statuses.
* **👤 Profile Management:** Users can register, set up, and update their personal details, hobbies, and preferences, safely stored locally via `SharedPreferences` and synced to the cloud.
* **💡 Help Dialogs:** Context-aware help popups integrated across all major screens to guide new users.

---

## 📸 Screenshots

*(Replace the image paths below with the actual files in your repository)*

| Discover (Match) | Empty State | Chat Detail |
| :---: | :---: | :---: |
| <img src="screenshots/screenshot_discover.png" width="250"> | <img src="screenshots/screenshot_empty_state.png" width="250"> | <img src="screenshots/screenshot_chat_detail.png" width="250"> |

| History Page | Chat Inbox | Profile Setup |
| :---: | :---: | :---: |
| <img src="screenshots/screenshot_history.png" width="250"> | <img src="screenshots/screenshot_chat_list.png" width="250"> | <img src="screenshots/screenshot_profile.png" width="250"> |

---

## 🛠️ Tech Stack & Libraries

* **Frontend:** Android SDK (Java), XML Layouts
* **Backend / Database:** Google Firebase (Cloud Firestore)
* **Architecture:** MVC Pattern
* **UI Components:** Material Design Components, ConstraintLayout
* **Third-Party Libraries:**
  * `de.hdodenhof:circleimageview:3.1.0` (For perfect circular avatar rendering)

---

## 🧑‍💻 My Contributions (Ignatius Abraham Aristio Kusnadi)

As the primary developer refining this project, my specific technical contributions and commits include:

* **Chat Infrastructure:** Engineered the entire Chat feature utilizing `RecyclerView`. Mapped complex Firestore nested data into dynamic chat interfaces (`item_message_sent`, `item_message_received`) and implemented real-time profile picture binding.
* **History Tracking System:** Built the History tracking module from scratch. Designed the logic to dynamically pull "Accepted" and "Rejected" user arrays from Firestore and display them with corresponding visual UI states.
* **Profile & Navigation:** Developed the `ProfileActivity` and established a robust, memory-leak-free Bottom Navigation system (`Intent.FLAG_ACTIVITY_CLEAR_TOP`) across the application.
* **UI/UX Overhaul & Bug Squashing:**
  * Designed and integrated the global Help Dialog system.
  * Fixed critical `DayNight` theme visual glitches that caused text to disappear on dark-mode physical devices.
  * Resolved severe UI overlapping and transparency bugs during activity transitions.
  * Implemented aesthetic "Empty States" for the Discover page to improve user experience.
* **Data & Session Management:** Refactored the login and registration logic, ensuring flawless ID passing and session persistence using `SharedPreferences`.

---

## 🚀 How to Run Locally

1. Clone this repository:readme_content = """
