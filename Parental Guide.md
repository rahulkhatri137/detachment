# Detachment Parental Control & Remote Enforcement Guide

Detachment provides remote parental control powered by a lightweight Cloudflare Worker backend with KV storage and a responsive web dashboard. This allows parents to monitor screen time, enforce daily app limits, tag essential or distracting apps, control app launch security, prevent uninstallation, and manage focus schedules from any web browser on a phone, tablet, or computer.

---

## 1. Quick Setup & Device Pairing

### Step 1: Deploy or Open the Cloudflare Worker
1. Deploy the worker script from `backend/worker.js` to your Cloudflare account (see `backend/README.md` for 2-minute setup).
2. Open your deployed worker URL (e.g. `https://detachment-parental.<your-subdomain>.workers.dev`) in any browser.

### Step 2: Pair the Child Phone
1. Open **Detachment** on the child's Android phone.
2. On the **Usage** (Dashboard) screen, look at the top-right header and tap the **Parental Control** button (Shield icon with "STANDBY" or "LIVE GUARD").
3. In the dialog, verify or enter your Cloudflare Worker URL.
4. Tap **Register & Generate Code** (or enter a custom 6-character code under Option B).
5. The child phone will display a unique 6-character code (for example: `A8K9X2`).
6. Once registered, the phone automatically enters **Managed Mode** (Child Protection Active).

### Step 3: Connect on Parent Dashboard
1. On your parent phone or laptop browser, open the Cloudflare Worker URL.
2. Select your device from the saved devices list or tap **Add Device** and enter the 6-character code.
3. Tap **Connect & Save Device**.
4. The dashboard will load the child device name, pairing code, battery status, and list of installed apps.

---

## 2. Enforcing App Limits & Managing Tags

Under the **App Limits & Tags** tab in the web dashboard, you can configure individual rules for every installed application:

### A. Daily Time Limits
- Enter maximum allowed screen time in minutes (e.g. `30` min for TikTok, `45` min for YouTube).
- Setting `0` minutes allows unlimited daily usage, unless an active Focus Schedule or Manual Lock is applied.
- When daily usage reaches the limit, Detachment blocks the app and displays a mindful block screen.

### B. Essential Apps (Immunity Override)
- Click the **Essential** tag button for critical applications like **Phone**, **WhatsApp**, **Google Classroom**, **Maps**, or **Calculator**.
- **Important**: Essential apps are prioritized and strictly immune to daily limits, friction delay shields, manual locks, and focus schedules. They can never be accidentally blocked.

### C. Distracting Apps
- Click the **Distracting** tag for social media, games, and streaming apps.
- Distracting apps are automatically subject to friction delays and focus schedules (e.g. Study or School hours).

### D. Friction Delay Shield
- Click the **Shield** tag to activate friction delay for an app.
- When opened, Detachment displays an animated mindfulness breathing pause (e.g. 15 seconds) before the app unlocks. This breaks impulsive, unconscious opening habits.

### E. Manual Lock
- Click the **Locked** tag to immediately lock any application down regardless of time limits.
- The app will remain locked until you toggle it off on the parent dashboard.

---

## 3. Creating & Managing Focus Schedules

Under the **Focus Schedules** tab in the web dashboard:

1. **Automated Blocking Windows**:
   - Create recurring focus blocks such as **School Hours** (e.g. 8:30 AM – 3:00 PM, Mon–Fri) or **Bedtime Rest** (e.g. 9:30 PM – 7:00 AM, Every day).
2. **Blocked Target**:
   - Choose whether the schedule blocks **Distracting Apps** only or **All Non-Essential Apps**.
3. **Toggle or Delete**:
   - Click **Enabled/Disabled** to quickly activate or deactivate schedules without deleting them.
   - Click **Delete** to remove a rule.
4. **Add New Schedule**:
   - Click **+ Add Focus Schedule** to set custom start/end times and active days.

---

## 4. Remote Master PIN Control & Child Lockout

Once paired, the device Master PIN is **strictly controlled by the parent** from the web dashboard:

1. **Child Lockout**:
   - The child cannot change or reset the PIN on their phone. All PIN-change options inside the mobile app are disabled and hidden.
   - When newly paired, the child phone opens with the default PIN (`1234`) or whichever PIN is set on the worker.
2. **Changing the PIN on the Worker**:
   - In the web dashboard, click the **🔑 Change Master PIN** button in the header or under the **Global Rules** tab.
   - Enter a new 4-to-6 digit PIN, confirm it, and click **Save & Push PIN**.
   - The server updates the PIN in KV storage.
3. **Automatic Device Adaptation**:
   - The child phone pulls the new PIN on app launch or during the next 15-minute background sync.
   - The new PIN is immediately enforced across all security screens (emergency unlocks, unpairing, and app launch authentication).

---

## 5. Uninstall & Anti-Tamper Protection (Device Administrator)

To prevent children from bypassing restrictions by uninstalling or force-stopping Detachment, the app incorporates a multi-layer defense system:

1. **Android Device Administrator**:
   - When paired, tap **Activate Device Administrator** in the Parental Control dialog or Dashboard screen.
   - Once activated, the Android operating system **physically disables the "Uninstall" button** for Detachment in system Settings and package installers.
2. **Accessibility-Level Tamper Interception**:
   - If the child attempts to open Detachment's App Info in system Settings, access Device Admin apps, or disable Accessibility while Device Admin is active, Detachment immediately redirects to the Home screen and prompts for the **Master PIN**.
3. **Authorized Parent Bypass**:
   - If the parent needs to change phone settings or update apps, entering the Master PIN grants a temporary grace period so Settings can be accessed legitimately.
4. **Remote Shield Toggle**:
   - Parents can remotely monitor and toggle **Uninstall Shield** from the web dashboard.

---

## 6. Initial Permission Setup & Frictionless Onboarding

Detachment requires four core Android system permissions to enforce limits, display mindful overlays, and prevent unauthorized removal:

1. **Usage Access Permission**: Allows calculating accurate foreground screen time.
2. **Accessibility Service**: Detects launched apps, enforces limits/schedules, and guards against settings tampering.
3. **Display Over Other Apps (Overlay)**: Displays mindful friction delay screens, blackout timers, and Master PIN lock overlays over other apps.
4. **Device Administrator**: Protects the app from unauthorized removal by disabling the Android OS uninstall button.

### Smooth Permission Granting on First Launch
To ensure a smooth setup experience, **Detachment's anti-tamper Settings block remains dormant until Device Administrator is officially activated**. This allows parents and users to navigate system Settings freely during initial installation to enable Usage Access, Accessibility, Overlay, and Device Administrator permissions without experiencing any screen blocks.

Once Device Administrator is activated and Parental Control or Uninstall Shield is turned on, anti-tamper protection engages automatically.

### The app will try hard to protect against changes to granted permissions, access to disable accessibility or uninstall screens. To uninstall first disable parental. The anti-tamper protection auto turns off if parental controls are not connected. 
Note: For precaution the app includes a secure **Destruction Mode** hidden behind the Master PIN in App Limits screen to cleanly deactivate all protections and allow app uninstallation. 

---

## 7. Remote App Launch Authentication Control

Just like changing the Master PIN, parents can remotely enable or disable PIN protection for launching the Detachment app itself:

1. **Remote Toggle**:
   - On the web dashboard, click the **🛡️ App Auth** button in the top bar, or navigate to **Global Rules** and toggle **App Launch Authentication**.
   - Clicking the button instantly updates the setting on the server and pushes to KV storage.
2. **Child Protection**:
   - The child cannot toggle or disable App Authentication locally on the phone while paired. The setting is completely locked.
3. **Enforcement**:
   - When enabled, opening Detachment on the child phone requires entering the Master PIN before accessing any dashboard or settings.
   - When disabled, Detachment launches directly into the dashboard without prompting for the PIN.

---

## 8. Pushing Changes to the Child Device

1. After making any adjustments on the web dashboard, tap the green **Save & Push** button.
2. A confirmation banner will indicate changes have been saved to the server.
3. The child device receives changes through three mechanisms:
   - **Launch Sync**: Every time Detachment is opened on the child device, it immediately pulls latest rules, PIN, and security updates.
   - **Background Sync**: An automatic background worker (`WorkManager`) runs every 15 minutes to pull updates and send battery/status telemetry.
   - **Manual Sync**: You can also tap **Sync Now** inside the phone's Parental Control dialog to immediately refresh rules and upload installed apps.

---

## 9. Tamper Resistance & Managed Mode

When parental control is paired:

- **Uninstall Blocked**: The app is protected as a Device Administrator; Android OS prevents uninstallation.
- **PIN Modification Blocked**: The child cannot change the Master PIN locally. The mobile app displays *"PIN change is locked by Parental Control. Must be changed via parent dashboard."*
- **App Authentication Locked**: The child cannot toggle App Launch Authentication locally on the phone. It is managed exclusively by the parent via the web dashboard.
- **Local Edit Controls Locked**: On the child device, the time limit adjustment dialog, schedule creation buttons, schedule toggle switches, and PIN settings are locked and hidden.
- **Top Status Banner**: App Limits and Schedules screens display a banner: *"Managed by Parental Control. Rules are enforced remotely."*
- **Unpairing Protection**: Tapping **Unpair** on the child device requires entering the Master PIN. A child cannot unpair the device without the parent's secret PIN.
- **Remote Unpair**: Parents can also remotely disconnect or unpair a device from the Cloudflare Worker API.
- **Destruction Mode**: The app can be uninstalled cleanly at any time by entering the Master PIN inside App Limits → Security and confirming Destruction Mode.

---

## 10. Troubleshooting

- **Child device not syncing immediately**: Ensure the child phone has an active internet connection (Wi-Fi or Mobile Data). Open the Detachment app or tap **Sync Now** to force an instant refresh.
- **Apps not displaying on dashboard**: Ensure the child phone has completed initial pairing or tap **Sync Now** in the Parental Control dialog. The dashboard automatically displays and merges all installed apps sent by the child device.
- **Permissions check**: Ensure **Usage Access**, **Accessibility Service**, **Display Over Other Apps**, and **Device Administrator** permissions show green status indicators on the child phone's Dashboard.
- **Adding new devices**: Open the web dashboard, tap **+ Add New Device Code**, and enter the 6-character code generated by the child phone. Both devices will appear in the saved device list for fast switching.
