# 🧘‍♂️ Detachment — Digital Mindfulness & Screen Time Control

**Detachment** is a digital mindfulness, habit interruption, and screen time management Android application built using modern **Kotlin**, **Jetpack Compose (Material 3)**, and **Room Database**. 

Detachment empowers users to reclaim their focus and attention through intentional friction, intelligent app time limits, Pomodoro blackout sessions, automated focus schedules, and Distraction Shield to delay app launches.

---

## 🧠 Consciousness Score Experience
<details>
<br>
  
**Fibonacci Phyllotaxis Rosette Structure**: Replaced the line hexagon with a 260-node golden-angle spiral lattice that radiates outward in intertwined clockwise and counter-clockwise curved spokes visualizing real-time intentionality across:
- Distraction Resistance (delay screen completions)
- Session Intentionality (deep sessions vs. quick bounces)
- Unplugged Gap (longest phone-free block)
- Limit Discipline (adherence to daily app limits)
- Pomodoro Focus (completed blackout focus minutes)
- Unlock Mindfulness (intentional unlocks vs. habitual checks)

**In-Depth Mindful Telemetry**:
- Unlocks Distribution: Interactive visual breakdown separating intentional vs. habitual unlocks.
- Time Leak Analysis: Metrics for potentially unnecessary usage and quick-bounce (<60s) sessions.
- Restorative Gaps: Precision tracking of the longest continuous phone-free time and longest continuous screen streak.

**Habit Loop Detector**:
- Scans for repetitive open-and-close loops of distracting apps within tight time windows.
- Displays loop severity, average bounce duration, and direct actions to apply mindful friction delays.

**You vs You Comparative Engine**:
- Real-time comparison comparing Today vs. Yesterday with percentage changes and trend indicators.
- Compares consciousness score, screen time, total unlocks, habitual pickups, phone-free blocks, and mindless sessions.
</details>

---
## 🌟 Key Features

### ⏳ Per-App Screentime Limits & Emergency Unlock:
* Configure individual daily usage limits in minutes or hours for any installed app.
* When an app exceeds its limit (or is manually locked), an App Lock Screen is presented.
* Master Passcode to unlock the app for breaks. When period expires, the app automatically relocks. Users can also manually relock the app early at any point.
* Notch Pill: A compact, top-notch heads-up pill features an illuminated blue capsule contour to aware about app screentime. Reminders every 15 min interval.

---

### 🛡️ Distraction Shield (Habit Loop Interrupter): 
* Designate apps as "Distracting".
* Every attempt to launch a distracting app triggers a full-screen customised delay time period mindful intercept with live second countdown to build positive willpower habits.

---

### 🍅 Pomodoro Blackout Mode
* Pitch-Blackout canvas featuring a glowing countdown timer, session activities (Deep Work, Study, Reading).
* Essential Apps Whitelist: Users can select up to a strict maximum of 10 essential apps (e.g. Phone, Messages, Maps, Calendar, Notes) to remain accessible from the blackout dock while all other phone usage is completely blocked.

---

### 📅 Automated Focus Schedules
* Recurring Time Windows: Set automated focus blocks tailored for Work, Sleep, Morning Focus, or Study routines.
* Automatic Enforcement: The background accessibility engine detects active schedule time slots and enforces blackout restrictions seamlessly.
* Configure customizable ranges, active days of the week, and blocking policies ("Lock Distracting Apps Only" vs "Lock All Non-Essential Apps").

---

### ⚙️ Permissions Required
For full device-level blocking functionality:
1. **Usage Access Permission** (`android.permission.PACKAGE_USAGE_STATS`) — To calculate accurate foreground screen time.
2. **Accessibility Service** (`android.permission.BIND_ACCESSIBILITY_SERVICE`) — To detect launched foreground apps and enforce blocking rules.
3. **Display Over Other Apps** (`android.permission.SYSTEM_ALERT_WINDOW`) — To display the mindful friction and blackout lock overlays over other applications.

---

## 📲 Screenshots 
<div>
<img src="screenshots/01.png" width="30%" />
<img src="screenshots/02.png" width="30%" />
<img src="screenshots/03.png" width="30%" />
<img src="screenshots/04.png" width="30%" />
<img src="screenshots/05.png" width="30%" />
<img src="screenshots/06.png" width="30%" />
<img src="screenshots/07.png" width="30%" />
<img src="screenshots/08.png" width="30%" />
<img src="screenshots/09.png" width="30%" />
<img src="screenshots/10.png" width="30%" />
<img src="screenshots/11.png" width="30%" />
<img src="screenshots/12.png" width="30%" />
</div>

---

## 🤖 AI Assistance Disclosure

This application has been developed with the assistance of advanced Google Artificial Intelligence models for debugging and some initial code structure and Conciousness Score quantification and visualization.
