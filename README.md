<div  align="center">

<img src="./assets/dsa-streak-tracker-banner.gif" alt="DSA Streak Tracker animated banner" width="100%"/>
<hr>

# DSA Streak Tracker

**A focused, local-first companion for building a consistent Data Structures & Algorithms practice habit.** 

Track your daily practice, protect your streak, follow a fixed DSA roadmap, and see your progress grow over time.

<p>
  <img src="https://img.shields.io/badge/React-TypeScript-61DAFB?style=for-the-badge&logo=react&logoColor=white" alt="React"/>
  <img src="https://img.shields.io/badge/Tailwind-CSS-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white" alt="Tailwind CSS"/>
  <img src="https://img.shields.io/badge/Recharts-Data%20Viz-8884D8?style=for-the-badge" alt="Recharts"/>
  <img src="https://img.shields.io/badge/lucide-react-Icons-F97316?style=for-the-badge" alt="Lucide React"/>
  <img src="https://img.shields.io/badge/Storage-localStorage-22C55E?style=for-the-badge" alt="localStorage"/>
</p>

</div>

<hr>


##  <p>   ✨ Overview</p>

**DSA Streak Tracker** turns DSA practice into a simple daily habit.

The app is designed around a **fixed 25-topic learning order**, daily practice logs, streak tracking, topic progress, and visual analytics. It is **local-first**: there is no backend, and your tracker data is persisted in the browser with `localStorage`.

The supplied screen recording demonstrates the mobile-first experience across the **Roadmap, Daily Log, Stats & Settings, and Dashboard** views.

---

## 🎥 App Demo

### Watch the recorded walkthrough




https://github.com/user-attachments/assets/ad840920-1270-40fb-8cf8-b503a88d0d33



<video src="./assets/dsa-streak-tracker-demo.mp4" controls width="360">
  Your browser does not support embedded video.
  <a href="./assets/dsa-streak-tracker-demo.mp4">Watch the DSA Streak Tracker demo</a>.
</video>


##  Core Features

###  Streak Tracking
- Current streak and longest streak
- Today: **Done / Pending**
- Warning when today's practice is still pending
- Optional monthly **Streak Freeze**
- Milestone celebrations for **7, 30, 50, 100, and 365 days**
- Local-timezone-aware date handling
- Date-only `YYYY-MM-DD` storage to reduce timezone bugs

### 🗺️ Fixed DSA Roadmap

The learning path contains **25 topics in a fixed order**:

| # | Topic |
|---:|---|
| 01 | Time & Space Complexity |
| 02 | Arrays |
| 03 | Strings |
| 04 | Hashing |
| 05 | Two Pointers |
| 06 | Sliding Window |
| 07 | Prefix Sum |
| 08 | Binary Search |
| 09 | Sorting Algorithms |
| 10 | Recursion |
| 11 | Backtracking |
| 12 | Linked List |
| 13 | Stack |
| 14 | Queue & Deque |
| 15 | Monotonic Stack / Queue |
| 16 | Heap / Priority Queue |
| 17 | Trees |
| 18 | Binary Search Tree |
| 19 | Graphs |
| 20 | Advanced Graphs |
| 21 | Trie |
| 22 | Greedy |
| 23 | Dynamic Programming |
| 24 | Bit Manipulation |
| 25 | Intervals |

Each topic can contain:
- Subtopics
- Practice problems
- Difficulty
- Platform
- Optional problem link
- Solved state
- Topic progress
- Manual completion state

---

## 📝 Daily Practice Log

Log each practice session with:

- 📅 Date
- 🧩 Topic
- ✅ Problems solved
- ⏱️ Time spent
- 🟢 Easy / 🟡 Medium / 🔴 Hard mix
- 📝 What you learned / where you got stuck

A day counts as completed when the configured daily requirement is reached.

Daily entries are merged by date so repeated logging does not create duplicate days.

---

## 📊 Dashboard & Analytics

The app turns your practice history into useful visual feedback:

- 🔥 Current streak
- 🏆 Longest streak
- 🎯 Today's focus
- 📈 Weekly problem activity
- 🗓️ GitHub-style practice heatmap
- 🧭 Roadmap completion
- 📚 Problems by topic
- ⚡ Difficulty breakdown
- ⏱️ Total practice time
- 📅 Daily average
- 🥇 Best practice day

The supplied recording shows the analytics screen with total solved, total practice time, daily average, best record, difficulty breakdown, and topic-level progress.

---
<hr>

## ⚙️ Settings & Data

Personalize the tracker with:

- Daily problem goal
- Minimum minutes required per day
- Reminder time
- Dark / light theme
- Streak Freeze
- JSON data export
- JSON data import
- Full-data reset with confirmation
- Load demo data for quickly previewing the UI

---

## 🧠 Optional Productivity Features

The project specification also supports extending the tracker with:

- 🤖 **Daily Plan** — Gemini-powered problem suggestions for the current topic
- 🔁 **Spaced Revision** — resurface problems after 3, 7, and 30 days
- 📋 **Bulk Problem Import** — paste one problem title per line
- 📱 **PWA installation** — add the tracker to a phone home screen

These features can be enabled/extended independently of the local-first tracking system.

---

## 🏗️ Tech Stack

| Technology | Purpose |
|---|---|
| **React + TypeScript** | UI and application logic |
| **Tailwind CSS** | Responsive styling |
| **Recharts** | Charts and analytics |
| **Lucide React** | Interface icons |
| **localStorage** | Persistent local data |
| **PWA APIs** | Installable app experience |
| **Gemini API** | Optional Daily Plan feature |

---

## 📁 Suggested Project Structure

```text
dsa-streak-tracker/
├── app/
│   ├── components/
│   │   ├── Navbar
│   │   ├── StreakCard
│   │   ├── Heatmap
│   │   ├── TopicCard
│   │   ├── ProblemList
│   │   ├── LogForm
│   │   ├── StatsCharts
│   │   └── SettingsPanel
│   ├── utils/
│   │   └── streak.ts
│   └── ...
├── assets/
│   ├── dsa-streak-tracker-banner.gif
│   ├── demo-poster.jpg
│   └── dsa-streak-tracker-demo.mp4
├── .env.example
├── build.gradle.kts
├── gradle.properties
├── metadata.json
├── settings.gradle.kts
└── README.md
```

---

## 💾 Data Model

The tracker is designed around local data such as:

```ts
topics: [
  {
    id,
    order,
    name,
    status,
    subtopics: [],
    problems: [
      {
        id,
        title,
        difficulty,
        platform,
        link,
        solved,
        solvedDate
      }
    ]
  }
]

logs: [
  {
    date: "YYYY-MM-DD",
    topicId,
    problemsSolved,
    minutes,
    notes
  }
]

settings: {
  dailyGoal,
  minMinutes,
  reminderTime,
  theme,
  freezesPerMonth
}

streakFreezesUsed: []
```

---

## 🧪 Streak Logic Test Cases

The streak calculation should be verified against these important cases:

- ✅ Logged today
- ✅ Logged yesterday but not today
- ✅ Missed two consecutive days
- ✅ A streak freeze used to bridge a missed day
- ✅ Longest streak remains independent of the current streak
- ✅ Midnight rollover does not incorrectly break a streak
- ✅ Today's pending state does not immediately destroy an active streak

---

## 📱 Mobile-First UI

The recorded build uses a compact mobile layout with:

- Bottom navigation
- Dark theme
- Rounded cards
- Topic progress indicators
- Expandable roadmap cards
- Touch-friendly controls
- Smooth page transitions
- Responsive dashboard and analytics

On larger screens, the navigation can transition to a desktop sidebar layout.

---

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/dsa-streak-tracker.git
cd dsa-streak-tracker
```

### 2. Install dependencies

```bash
npm install
```

### 3. Start the development server

```bash
npm run dev
```

### 4. Open the local app

Use the local URL shown by the development server.

---

## 🔐 API Key Safety

If the optional Gemini feature is enabled, **never commit a real API key to GitHub**.

Use environment variables / the platform's secret-management system and keep real secrets out of:

```text
.env
.env.local
gradle.properties
source code
```

The committed `.env.example` should contain placeholders only.

---

## 🗺️ Roadmap

- [x] 25-topic DSA roadmap
- [x] Daily practice logging
- [x] Streak tracking
- [x] Topic and problem progress
- [x] Analytics dashboard
- [x] Local persistence
- [x] JSON import/export
- [x] Responsive mobile UI
- [ ] Gemini Daily Plan
- [ ] Spaced revision engine
- [ ] Bulk problem import
- [ ] PWA polish and offline enhancements

---

## 🎯 Project Goal

Every problem solved is a small step toward stronger problem-solving skills.

**Track it. Solve it. Learn from it. Repeat.**

---

<div align="center">

### Built for consistent DSA practice 💻🔥

**DSA Streak Tracker**

</div>
