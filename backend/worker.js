const CONFIG_DEVICE_CODES = ["DEMO99"];

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    const path = url.pathname;
    const method = request.method;

    const corsHeaders = {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type, Authorization",
    };

    if (method === "OPTIONS") {
      return new Response(null, { headers: corsHeaders });
    }

    const kv = env && env.DETACHMENT_KV ? env.DETACHMENT_KV : globalInMemoryStore;

    if (path === "/" || path === "/dashboard") {
      return new Response(renderDashboardHtml(CONFIG_DEVICE_CODES), {
        headers: { "Content-Type": "text/html;charset=UTF-8", ...corsHeaders }
      });
    }

    if (path === "/api/device/register" && method === "POST") {
      try {
        const body = await request.json();
        const code = (body.code || generateCode()).toUpperCase().trim();
        const incomingApps = Array.isArray(body.apps) ? body.apps : [];
        const deviceData = {
          code: code,
          deviceName: body.deviceName || "Child Device",
          registeredAt: Date.now(),
          lastSyncAt: Date.now(),
          battery: body.battery !== undefined ? body.battery : 100,
          totalScreenTimeMinutes: body.totalScreenTimeMinutes || 0,
          installedApps: incomingApps
        };

        let existingConfig = await kv.get(`config:${code}`, { type: "json" });
        if (!existingConfig) {
          existingConfig = {
            version: 1,
            exportDate: Date.now(),
            appLimits: incomingApps.map(a => ({
              packageName: a.packageName,
              appName: a.appName,
              category: a.category || "General",
              dailyLimitMinutes: a.dailyLimitMinutes || 0,
              isDistracting: a.isDistracting || false,
              isEssential: a.isEssential || false,
              isShieldActive: a.isShieldActive || false,
              isLockedManually: a.isLockedManually || false,
              isHardMode: false
            })),
            schedules: [
              {
                title: "School Hours",
                type: "SCHOOL",
                startHour: 8,
                startMinute: 30,
                endHour: 15,
                endMinute: 30,
                activeDays: "MON,TUE,WED,THU,FRI",
                isEnabled: true,
                blockedTarget: "DISTRACTING"
              },
              {
                title: "Bedtime Rest",
                type: "BEDTIME",
                startHour: 21,
                startMinute: 30,
                endHour: 7,
                endMinute: 0,
                activeDays: "MON,TUE,WED,THU,FRI,SAT,SUN",
                isEnabled: true,
                blockedTarget: "ALL_NON_ESSENTIAL"
              }
            ],
            settings: {
              key_delay_seconds: "15",
              key_unlock_minutes: "15",
              key_delay_for_distracting_apps: "true",
              master_pin: "1234",
              app_auth_enabled: "false",
              uninstall_protection_enabled: "true"
            }
          };
          await kv.put(`config:${code}`, JSON.stringify(existingConfig));
        } else {
          if (!existingConfig.appLimits) existingConfig.appLimits = [];
          if (!existingConfig.settings) existingConfig.settings = {};
          if (existingConfig.settings.app_auth_enabled === undefined) {
            existingConfig.settings.app_auth_enabled = "false";
          }
          if (existingConfig.settings.uninstall_protection_enabled === undefined) {
            existingConfig.settings.uninstall_protection_enabled = "true";
          }
          if (incomingApps.length > 0) {
            const existingMap = new Map(existingConfig.appLimits.map(a => [a.packageName, a]));
            let updated = false;
            for (const a of incomingApps) {
              if (!existingMap.has(a.packageName)) {
                existingConfig.appLimits.push({
                  packageName: a.packageName,
                  appName: a.appName,
                  category: a.category || "General",
                  dailyLimitMinutes: a.dailyLimitMinutes || 0,
                  isDistracting: a.isDistracting || false,
                  isEssential: a.isEssential || false,
                  isShieldActive: a.isShieldActive || false,
                  isLockedManually: a.isLockedManually || false,
                  isHardMode: false
                });
                updated = true;
              }
            }
            if (updated) {
              await kv.put(`config:${code}`, JSON.stringify(existingConfig));
            }
          }
        }

        await kv.put(`device:${code}`, JSON.stringify(deviceData));

        return new Response(JSON.stringify({
          success: true,
          code: code,
          message: "Device registered successfully"
        }), {
          headers: { "Content-Type": "application/json", ...corsHeaders }
        });
      } catch (err) {
        return new Response(JSON.stringify({ success: false, error: err.message }), {
          status: 400,
          headers: { "Content-Type": "application/json", ...corsHeaders }
        });
      }
    }

    const deviceMatch = path.match(/^\/api\/device\/([A-Za-z0-9_-]+)(\/.*)?$/);
    if (deviceMatch) {
      const code = deviceMatch[1].toUpperCase();
      const subpath = deviceMatch[2] || "";

      if (subpath === "/config" && method === "GET") {
        let config = await kv.get(`config:${code}`, { type: "json" });
        const device = await kv.get(`device:${code}`, { type: "json" });

        if (!config && !device) {
          return new Response(JSON.stringify({ success: false, error: "Device code not found" }), {
            status: 404,
            headers: { "Content-Type": "application/json", ...corsHeaders }
          });
        }

        if (!config) {
          config = {
            version: 1,
            exportDate: Date.now(),
            appLimits: [],
            schedules: [],
            settings: {
              key_delay_seconds: "15",
              key_unlock_minutes: "15",
              key_delay_for_distracting_apps: "true",
              master_pin: "1234",
              app_auth_enabled: "false",
              uninstall_protection_enabled: "true"
            }
          };
        }
        if (!config.settings) config.settings = {};
        if (config.settings.app_auth_enabled === undefined) config.settings.app_auth_enabled = "false";
        if (config.settings.uninstall_protection_enabled === undefined) config.settings.uninstall_protection_enabled = "true";
        if (config.settings.master_pin === undefined) config.settings.master_pin = "1234";

        if (!config.appLimits) config.appLimits = [];
        if (device && device.installedApps && Array.isArray(device.installedApps)) {
          const existingMap = new Map(config.appLimits.map(a => [a.packageName, a]));
          let modified = false;
          for (const app of device.installedApps) {
            if (!existingMap.has(app.packageName)) {
              config.appLimits.push({
                packageName: app.packageName,
                appName: app.appName,
                category: app.category || "General",
                dailyLimitMinutes: app.dailyLimitMinutes || 0,
                isDistracting: app.isDistracting || false,
                isEssential: app.isEssential || false,
                isShieldActive: app.isShieldActive || false,
                isLockedManually: app.isLockedManually || false,
                isHardMode: false
              });
              modified = true;
            }
          }
          if (modified) {
            await kv.put(`config:${code}`, JSON.stringify(config));
          }
        }

        return new Response(JSON.stringify({
          success: true,
          code: code,
          device: device || { code: code, deviceName: "Child Device", lastSyncAt: Date.now() },
          config: config
        }), {
          headers: { "Content-Type": "application/json", ...corsHeaders }
        });
      }

      if (subpath === "/config" && method === "POST") {
        try {
          const body = await request.json();
          const configToSave = body.config || body;
          configToSave.exportDate = Date.now();
          await kv.put(`config:${code}`, JSON.stringify(configToSave));

          return new Response(JSON.stringify({
            success: true,
            code: code,
            updatedAt: configToSave.exportDate,
            message: "Config saved. Child device will pull changes on next sync."
          }), {
            headers: { "Content-Type": "application/json", ...corsHeaders }
          });
        } catch (err) {
          return new Response(JSON.stringify({ success: false, error: err.message }), {
            status: 400,
            headers: { "Content-Type": "application/json", ...corsHeaders }
          });
        }
      }

      if (subpath === "/pin" && method === "POST") {
        try {
          const body = await request.json();
          const newPin = (body.pin || "").trim();
          if (!newPin || newPin.length < 4 || newPin.length > 6) {
            return new Response(JSON.stringify({ success: false, error: "PIN must be 4 to 6 digits" }), {
              status: 400,
              headers: { "Content-Type": "application/json", ...corsHeaders }
            });
          }
          let config = await kv.get(`config:${code}`, { type: "json" }) || {};
          if (!config.settings) config.settings = {};
          config.settings.master_pin = newPin;
          config.exportDate = Date.now();
          await kv.put(`config:${code}`, JSON.stringify(config));

          return new Response(JSON.stringify({
            success: true,
            code: code,
            pin: newPin,
            message: "Master PIN updated successfully. Child device will enforce on next sync."
          }), {
            headers: { "Content-Type": "application/json", ...corsHeaders }
          });
        } catch (err) {
          return new Response(JSON.stringify({ success: false, error: err.message }), {
            status: 400,
            headers: { "Content-Type": "application/json", ...corsHeaders }
          });
        }
      }

      if (subpath === "/app-auth" && method === "POST") {
        try {
          const body = await request.json();
          const isEnabled = body.enabled === true || body.enabled === "true";
          let config = await kv.get(`config:${code}`, { type: "json" }) || {};
          if (!config.settings) config.settings = {};
          config.settings.app_auth_enabled = isEnabled ? "true" : "false";
          config.exportDate = Date.now();
          await kv.put(`config:${code}`, JSON.stringify(config));

          return new Response(JSON.stringify({
            success: true,
            code: code,
            appAuthEnabled: isEnabled,
            message: "App launch authentication " + (isEnabled ? "enabled" : "disabled") + " successfully. Child device will enforce on next sync."
          }), {
            headers: { "Content-Type": "application/json", ...corsHeaders }
          });
        } catch (err) {
          return new Response(JSON.stringify({ success: false, error: err.message }), {
            status: 400,
            headers: { "Content-Type": "application/json", ...corsHeaders }
          });
        }
      }

      if (subpath === "/uninstall-protection" && method === "POST") {
        try {
          const body = await request.json();
          const isEnabled = body.enabled === true || body.enabled === "true";
          let config = await kv.get(`config:${code}`, { type: "json" }) || {};
          if (!config.settings) config.settings = {};
          config.settings.uninstall_protection_enabled = isEnabled ? "true" : "false";
          config.exportDate = Date.now();
          await kv.put(`config:${code}`, JSON.stringify(config));

          return new Response(JSON.stringify({
            success: true,
            code: code,
            uninstallProtectionEnabled: isEnabled,
            message: "Uninstall protection " + (isEnabled ? "enabled" : "disabled") + " successfully. Child device will enforce on next sync."
          }), {
            headers: { "Content-Type": "application/json", ...corsHeaders }
          });
        } catch (err) {
          return new Response(JSON.stringify({ success: false, error: err.message }), {
            status: 400,
            headers: { "Content-Type": "application/json", ...corsHeaders }
          });
        }
      }

      if (subpath === "/sync" && method === "POST") {
        try {
          const body = await request.json();
          let device = await kv.get(`device:${code}`, { type: "json" }) || { code: code };
          device.lastSyncAt = Date.now();
          if (body.deviceName) device.deviceName = body.deviceName;
          if (body.battery !== undefined) device.battery = body.battery;
          if (body.totalScreenTimeMinutes !== undefined) device.totalScreenTimeMinutes = body.totalScreenTimeMinutes;
          if (body.apps && Array.isArray(body.apps)) device.installedApps = body.apps;

          await kv.put(`device:${code}`, JSON.stringify(device));

          let config = await kv.get(`config:${code}`, { type: "json" });
          if (!config) {
            config = {
              version: 1,
              exportDate: Date.now(),
              appLimits: [],
              schedules: [],
              settings: {
                key_delay_seconds: "15",
                key_unlock_minutes: "15",
                key_delay_for_distracting_apps: "true",
                master_pin: "1234",
                app_auth_enabled: "false",
                uninstall_protection_enabled: "true"
              }
            };
          }
          if (!config.appLimits) config.appLimits = [];
          if (!config.settings) config.settings = {};
          if (config.settings.uninstall_protection_enabled === undefined) {
            config.settings.uninstall_protection_enabled = "true";
          }
          if (body.apps && Array.isArray(body.apps)) {
            const existingMap = new Map(config.appLimits.map(a => [a.packageName, a]));
            let updated = false;
            for (const app of body.apps) {
              if (!existingMap.has(app.packageName)) {
                config.appLimits.push({
                  packageName: app.packageName,
                  appName: app.appName,
                  category: app.category || "General",
                  dailyLimitMinutes: app.dailyLimitMinutes || 0,
                  isDistracting: app.isDistracting || false,
                  isEssential: app.isEssential || false,
                  isShieldActive: app.isShieldActive || false,
                  isLockedManually: app.isLockedManually || false,
                  isHardMode: false
                });
                updated = true;
              }
            }
            if (updated) {
              await kv.put(`config:${code}`, JSON.stringify(config));
            }
          }

          return new Response(JSON.stringify({
            success: true,
            code: code,
            config: config,
            lastSyncAt: device.lastSyncAt
          }), {
            headers: { "Content-Type": "application/json", ...corsHeaders }
          });
        } catch (err) {
          return new Response(JSON.stringify({ success: false, error: err.message }), {
            status: 400,
            headers: { "Content-Type": "application/json", ...corsHeaders }
          });
        }
      }

      if (subpath === "/unpair" && method === "POST") {
        await kv.delete(`device:${code}`);
        await kv.delete(`config:${code}`);
        return new Response(JSON.stringify({ success: true, message: "Unpaired successfully" }), {
          headers: { "Content-Type": "application/json", ...corsHeaders }
        });
      }
    }

    return new Response(JSON.stringify({ error: "Endpoint not found" }), {
      status: 404,
      headers: { "Content-Type": "application/json", ...corsHeaders }
    });
  }
};

const memoryStore = new Map();
const globalInMemoryStore = {
  async get(key, options) {
    const val = memoryStore.get(key);
    if (!val) return null;
    if (options && options.type === "json") {
      try { return JSON.parse(val); } catch (e) { return null; }
    }
    return val;
  },
  async put(key, value) {
    memoryStore.set(key, typeof value === "string" ? value : JSON.stringify(value));
  },
  async delete(key) {
    memoryStore.delete(key);
  }
};

function generateCode() {
  const chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  let result = "";
  for (let i = 0; i < 6; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}

function renderDashboardHtml(defaultCodes) {
  const codesJson = JSON.stringify(defaultCodes || []);
  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Detachment Parental Dashboard</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@500;700&display=swap" rel="stylesheet">
  <style>
    :root {
      --bg: #0A0814;
      --card-bg: rgba(26, 21, 48, 0.7);
      --card-border: rgba(167, 139, 250, 0.15);
      --purple-primary: #7C3AED;
      --purple-light: #A78BFA;
      --cyan: #06B6D4;
      --emerald: #10B981;
      --amber: #F59E0B;
      --rose: #F43F5E;
      --text: #F3F4F6;
      --text-muted: #9CA3AF;
    }
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body {
      background: var(--bg);
      color: var(--text);
      font-family: 'Plus Jakarta Sans', sans-serif;
      min-height: 100vh;
      padding: 20px;
    }
    .container {
      max-width: 1080px;
      margin: 0 auto;
    }
    header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 20px;
      background: var(--card-bg);
      border: 1px solid var(--card-border);
      border-radius: 18px;
      margin-bottom: 24px;
      backdrop-filter: blur(16px);
      -webkit-backdrop-filter: blur(16px);
      flex-wrap: wrap;
      gap: 12px;
    }
    .brand {
      display: flex;
      align-items: center;
      gap: 12px;
    }
    .brand-logo {
      width: 40px;
      height: 40px;
      border-radius: 12px;
      background: linear-gradient(135deg, var(--purple-primary), var(--cyan));
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 800;
      font-size: 20px;
    }
    h1 { font-size: 20px; font-weight: 700; }
    .badge {
      display: inline-flex;
      align-items: center;
      padding: 4px 10px;
      border-radius: 999px;
      font-size: 11px;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      background: rgba(16, 185, 129, 0.15);
      color: var(--emerald);
      border: 1px solid rgba(16, 185, 129, 0.3);
    }
    .card {
      background: var(--card-bg);
      border: 1px solid var(--card-border);
      border-radius: 18px;
      padding: 20px;
      margin-bottom: 20px;
      backdrop-filter: blur(16px);
      -webkit-backdrop-filter: blur(16px);
    }
    .devices-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
      gap: 12px;
      margin-top: 14px;
    }
    .device-chip {
      background: rgba(15, 12, 29, 0.7);
      border: 1px solid rgba(167, 139, 250, 0.2);
      border-radius: 14px;
      padding: 12px 14px;
      cursor: pointer;
      transition: all 0.2s ease;
      display: flex;
      flex-direction: column;
      gap: 4px;
    }
    .device-chip:hover {
      border-color: var(--purple-light);
      transform: translateY(-2px);
    }
    .device-chip.active-chip {
      border-color: var(--emerald);
      background: rgba(16, 185, 129, 0.08);
    }
    .device-chip-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .device-chip-code {
      font-family: 'JetBrains Mono', monospace;
      font-weight: 700;
      font-size: 15px;
      color: var(--purple-light);
    }
    .device-chip.active-chip .device-chip-code {
      color: var(--emerald);
    }
    .device-chip-title {
      font-size: 13px;
      color: var(--text);
      font-weight: 600;
    }
    .device-chip-actions {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 4px;
    }
    .remove-code-btn {
      background: transparent;
      border: none;
      color: var(--rose);
      font-size: 11px;
      cursor: pointer;
      padding: 2px 4px;
    }
    .pairing-box {
      display: flex;
      gap: 10px;
      margin-top: 12px;
      flex-wrap: wrap;
    }
    .code-input {
      background: rgba(15, 12, 29, 0.8);
      border: 1px solid var(--card-border);
      color: var(--text);
      padding: 12px 16px;
      border-radius: 12px;
      font-family: 'JetBrains Mono', monospace;
      font-size: 16px;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 2px;
      width: 150px;
      text-align: center;
    }
    button {
      background: linear-gradient(135deg, var(--purple-primary), #6D28D9);
      color: white;
      border: none;
      padding: 12px 20px;
      border-radius: 12px;
      font-weight: 700;
      cursor: pointer;
      font-size: 14px;
      display: inline-flex;
      align-items: center;
      gap: 8px;
      transition: all 0.2s ease;
    }
    button:hover { opacity: 0.95; transform: scale(1.02); }
    button.secondary {
      background: rgba(255, 255, 255, 0.05);
      border: 1px solid var(--card-border);
      color: var(--text);
    }
    button.emerald {
      background: linear-gradient(135deg, #059669, #10B981);
    }
    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
      gap: 12px;
      margin-bottom: 20px;
    }
    .stat-card {
      background: rgba(15, 12, 29, 0.7);
      border: 1px solid var(--card-border);
      border-radius: 14px;
      padding: 14px;
    }
    .stat-label { font-size: 11px; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.5px; }
    .stat-val { font-size: 18px; font-weight: 800; margin-top: 4px; color: var(--text); word-break: break-all; }
    .tabs {
      display: flex;
      gap: 8px;
      border-bottom: 1px solid var(--card-border);
      padding-bottom: 12px;
      margin-bottom: 16px;
      overflow-x: auto;
    }
    .tab {
      padding: 8px 16px;
      border-radius: 10px;
      cursor: pointer;
      font-size: 13px;
      font-weight: 600;
      color: var(--text-muted);
      white-space: nowrap;
    }
    .tab.active {
      background: rgba(167, 139, 250, 0.15);
      color: var(--purple-light);
    }
    .app-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 12px 14px;
      background: rgba(15, 12, 29, 0.5);
      border: 1px solid rgba(167, 139, 250, 0.1);
      border-radius: 12px;
      margin-bottom: 8px;
      flex-wrap: wrap;
      gap: 10px;
    }
    .app-info {
      display: flex;
      align-items: center;
      gap: 12px;
      min-width: 220px;
    }
    .app-icon {
      width: 36px;
      height: 36px;
      border-radius: 10px;
      background: rgba(167, 139, 250, 0.2);
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 700;
      color: var(--purple-light);
    }
    .app-name { font-weight: 700; font-size: 14px; }
    .app-pkg { font-size: 11px; color: var(--text-muted); font-family: monospace; }
    .controls-row {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;
    }
    .tag-btn {
      padding: 6px 12px;
      border-radius: 8px;
      font-size: 11px;
      font-weight: 700;
      border: 1px solid var(--card-border);
      background: rgba(255, 255, 255, 0.04);
      color: var(--text-muted);
      cursor: pointer;
    }
    .tag-btn.active-essential { background: rgba(16, 185, 129, 0.2); border-color: var(--emerald); color: var(--emerald); }
    .tag-btn.active-distracting { background: rgba(245, 158, 11, 0.2); border-color: var(--amber); color: var(--amber); }
    .tag-btn.active-shield { background: rgba(6, 182, 212, 0.2); border-color: var(--cyan); color: var(--cyan); }
    .tag-btn.active-locked { background: rgba(244, 63, 94, 0.2); border-color: var(--rose); color: var(--rose); }
    .limit-input-group {
      display: flex;
      align-items: center;
      gap: 6px;
      background: rgba(15, 12, 29, 0.8);
      border: 1px solid var(--card-border);
      border-radius: 8px;
      padding: 4px 8px;
    }
    .limit-input-group input {
      width: 48px;
      background: transparent;
      border: none;
      color: var(--text);
      font-weight: 700;
      text-align: center;
      font-size: 13px;
    }
    input[type="number"], input[type="text"] {
      background: rgba(15, 12, 29, 0.8);
      border: 1px solid var(--card-border);
      color: var(--text);
      padding: 10px 14px;
      border-radius: 10px;
      font-size: 14px;
    }
    .guide-card {
      background: rgba(15, 12, 29, 0.55);
      border: 1px solid rgba(167, 139, 250, 0.15);
      border-radius: 14px;
      padding: 16px;
      margin-bottom: 14px;
    }
    .guide-card h3 {
      font-size: 16px;
      font-weight: 700;
      color: var(--purple-light);
      margin-bottom: 8px;
      display: flex;
      align-items: center;
      gap: 8px;
    }
    .guide-card p, .guide-card li {
      font-size: 13.5px;
      line-height: 1.6;
      color: #D1D5DB;
    }
    .guide-card ul {
      margin-left: 20px;
      margin-top: 6px;
    }
    .guide-card li {
      margin-bottom: 4px;
    }
    .modal-overlay {
      position: fixed; inset: 0; background: rgba(5, 3, 14, 0.75);
      backdrop-filter: blur(10px); -webkit-backdrop-filter: blur(10px);
      z-index: 2000; display: flex; align-items: center; justify-content: center; padding: 16px;
    }
    .toast {
      position: fixed; bottom: 24px; right: 24px; background: var(--emerald); color: black;
      font-weight: 700; padding: 12px 20px; border-radius: 12px; box-shadow: 0 10px 25px rgba(0,0,0,0.4);
      display: none; animation: slideUp 0.3s forwards; z-index: 2500;
    }
    @keyframes slideUp { from { transform: translateY(20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
  </style>
</head>
<body>
  <div class="container">
    <header>
      <div class="brand">
        <div class="brand-logo">D</div>
        <div>
          <h1>Detachment Parental</h1>
          <p style="font-size: 13px; color: var(--text-muted);">Remote Screen Time & App Enforcer</p>
        </div>
      </div>
      <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap;">
        <button class="secondary" id="btnChangePin" style="display: none; padding: 8px 12px; font-size: 12px; border-color: var(--purple-light); color: var(--purple-light);" onclick="openChangePinModal()">🔑 Master PIN</button>
        <button class="secondary" id="btnToggleAppAuth" style="display: none; padding: 8px 12px; font-size: 12px; border-color: var(--cyan); color: var(--cyan);" onclick="toggleAppAuthRemote()">🛡️ App Auth: <span id="appAuthTopBadge">Off</span></button>
        <button class="secondary" id="btnToggleUninstallShield" style="display: none; padding: 8px 12px; font-size: 12px; border-color: var(--emerald); color: var(--emerald);" onclick="toggleUninstallProtectionRemote()">🔒 Uninstall Shield: <span id="uninstallTopBadge">Active</span></button>
        <button class="secondary" id="btnSwitchDevice" style="display: none; padding: 8px 12px; font-size: 12px;" onclick="showDeviceSelector()">Switch Device</button>
        <div class="badge" id="connStatus">Ready</div>
      </div>
    </header>

    <div class="card" id="deviceSelectorCard">
      <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px;">
        <div>
          <h2 style="font-size: 18px;">Choose Paired Device</h2>
          <p style="font-size: 13px; color: var(--text-muted); margin-top: 2px;">
            Select a saved child device or add a new 6-character code.
          </p>
        </div>
        <button class="secondary" onclick="toggleAddDeviceForm()">+ Add New Device Code</button>
      </div>

      <div class="devices-grid" id="savedDevicesList"></div>

      <div id="addDeviceForm" style="display: none; margin-top: 16px; padding-top: 16px; border-top: 1px solid var(--card-border);">
        <h3 style="font-size: 14px; margin-bottom: 8px; color: var(--purple-light);">Enter Device Pairing Code</h3>
        <div class="pairing-box">
          <input type="text" id="deviceCode" class="code-input" placeholder="CODE" maxlength="6" />
          <button onclick="handleConnectInputCode()">Connect & Save Device</button>
          <button class="secondary" onclick="generateDemoDevice()">Load Demo Device</button>
        </div>
        <p style="font-size: 12px; color: var(--text-muted); margin-top: 10px;">
          Find this code inside Detachment on child device &gt; Top Bar &gt; Parental Control.
        </p>
      </div>
    </div>

    <div id="deviceDashboard" style="display: none;">
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-label">Device Name</div>
          <div class="stat-val" id="statDeviceName">Pixel 8</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Pairing Code</div>
          <div class="stat-val" id="statCode" style="color: var(--purple-light); font-family: monospace;">---</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Master PIN</div>
          <div class="stat-val" id="statMasterPin" style="color: var(--emerald); font-family: monospace;">1234</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">App Launch Auth</div>
          <div class="stat-val" id="statAppAuth" style="color: var(--cyan); font-size: 16px;">Disabled</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Uninstall Shield</div>
          <div class="stat-val" id="statUninstallShield" style="color: var(--emerald); font-size: 16px;">Active</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Enforced Apps</div>
          <div class="stat-val" id="statTotalApps" style="color: var(--cyan);">0</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Last App Sync</div>
          <div class="stat-val" id="statLastSync" style="font-size: 16px; color: #D1D5DB;">Never</div>
        </div>
      </div>

      <div class="card">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; flex-wrap: wrap; gap: 12px;">
          <div class="tabs" style="margin-bottom: 0; border-bottom: none;">
            <div class="tab active" onclick="switchTab('apps')">App Limits & Tags</div>
            <div class="tab" onclick="switchTab('schedules')">Focus Schedules</div>
            <div class="tab" onclick="switchTab('settings')">Global Rules</div>
            <div class="tab" onclick="switchTab('help')">Help & Guide</div>
          </div>
          <div style="display: flex; gap: 8px; align-items: center; flex-wrap: wrap;">
            <button class="secondary" style="border-color: var(--purple-light); color: var(--purple-light);" onclick="openChangePinModal()">🔑 Change PIN</button>
            <button class="emerald" onclick="saveConfigToServer()">Save & Push</button>
          </div>
        </div>

        <div id="tabApps">
          <p style="font-size: 13px; color: var(--text-muted); margin-bottom: 14px;">
            Set daily minutes limits. Tag apps as <b>Essential</b> (immune to delays/blocks), <b>Distracting</b>, or apply <b>Friction Shield</b>.
          </p>
          <div id="appsListContainer"></div>
        </div>

        <div id="tabSchedules" style="display: none;">
          <p style="font-size: 13px; color: var(--text-muted); margin-bottom: 14px;">
            Enforce automated blocking windows (e.g. School hours, Bedtime).
          </p>
          <div id="schedulesListContainer"></div>
          <button class="secondary" style="margin-top: 12px;" onclick="addSchedule()">+ Add Focus Schedule</button>
        </div>

        <div id="tabSettings" style="display: none;">
          <div style="display: flex; flex-direction: column; gap: 16px; max-width: 540px;">
            <div style="background: rgba(15, 12, 29, 0.6); border: 1px solid rgba(167, 139, 250, 0.2); border-radius: 14px; padding: 16px;">
              <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                <label style="font-size: 14px; font-weight: 700; color: var(--purple-light);">Device Master PIN</label>
                <span class="badge" style="background: rgba(244, 63, 94, 0.15); color: var(--rose); border-color: rgba(244, 63, 94, 0.3);">Locked on Phone</span>
              </div>
              <p style="font-size: 12px; color: var(--text-muted); margin-bottom: 12px; line-height: 1.5;">
                Child cannot change or view this PIN in the app. Phone opens by default with <b>1234</b> until changed here.
              </p>
              <div style="display: flex; gap: 10px; align-items: center;">
                <input type="text" id="settingMasterPin" value="1234" maxlength="6" style="flex: 1;" />
                <button type="button" class="secondary" style="border-color: var(--purple-light); color: var(--purple-light); white-space: nowrap;" onclick="quickUpdatePinFromInput()">Update PIN</button>
              </div>
            </div>

            <div style="background: rgba(15, 12, 29, 0.6); border: 1px solid rgba(16, 185, 129, 0.25); border-radius: 14px; padding: 16px;">
              <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                <label style="font-size: 14px; font-weight: 700; color: var(--emerald);">Uninstall & Tamper Shield</label>
                <span class="badge" id="uninstallSettingBadge" style="background: rgba(16, 185, 129, 0.15); color: var(--emerald); border-color: rgba(16, 185, 129, 0.3);">Active</span>
              </div>
              <p style="font-size: 12px; color: var(--text-muted); margin-bottom: 12px; line-height: 1.5;">
                Enforces Android Device Administrator to block OS uninstallation and restricts access to Settings, App Details, and Device Admin with Master PIN verification.
              </p>
              <div style="display: flex; gap: 10px; align-items: center; justify-content: space-between;">
                <span style="font-size: 13px; font-weight: 600;" id="uninstallSettingStatusText">Anti-Uninstall Active</span>
                <button type="button" id="btnToggleUninstallInSettings" class="secondary" style="border-color: var(--emerald); color: var(--emerald);" onclick="toggleUninstallProtectionRemote()">Disable Shield</button>
              </div>
            </div>

            <div style="background: rgba(15, 12, 29, 0.6); border: 1px solid rgba(6, 182, 212, 0.25); border-radius: 14px; padding: 16px;">
              <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                <label style="font-size: 14px; font-weight: 700; color: var(--cyan);">App Launch Authentication</label>
                <span class="badge" id="appAuthSettingBadge" style="background: rgba(6, 182, 212, 0.15); color: var(--cyan); border-color: rgba(6, 182, 212, 0.3);">Disabled</span>
              </div>
              <p style="font-size: 12px; color: var(--text-muted); margin-bottom: 12px; line-height: 1.5;">
                When enabled, child phone requires Master PIN to open Detachment. Child cannot toggle this in the app when paired.
              </p>
              <div style="display: flex; gap: 10px; align-items: center; justify-content: space-between;">
                <span style="font-size: 13px; font-weight: 600;" id="appAuthSettingStatusText">Require PIN on App Open</span>
                <button type="button" id="btnToggleAppAuthInSettings" class="secondary" style="border-color: var(--cyan); color: var(--cyan);" onclick="toggleAppAuthRemote()">Enable App Auth</button>
              </div>
            </div>

            <div>
              <label style="font-size: 13px; color: var(--text-muted); display: block; margin-bottom: 6px;">Friction Delay Seconds</label>
              <input type="number" id="settingDelaySec" value="15" min="5" max="120" style="width: 100%;" />
            </div>
            <div>
              <label style="font-size: 13px; color: var(--text-muted); display: block; margin-bottom: 6px;">Temporary Unlock Minutes</label>
              <input type="number" id="settingUnlockMin" value="15" min="5" max="60" style="width: 100%;" />
            </div>
          </div>
        </div>

        <div id="tabHelp" style="display: none;">
          <div class="guide-card">
            <h3>1. Setup & Pairing</h3>
            <p>On the child phone, open Detachment and tap the <b>Parental Control (Shield)</b> icon in the top header. Verify the worker URL, tap <b>Register & Generate Code</b>, and note down the 6-character code. Enter it here on the dashboard to pair.</p>
          </div>

          <div class="guide-card">
            <h3>2. Master PIN Protection (Child Cannot Change)</h3>
            <p>Once paired, the PIN lock <b>cannot be changed on the child phone</b>. It opens by the default PIN (<code>1234</code>) until you change it remotely here on the dashboard.</p>
            <p style="margin-top: 6px;">To change the PIN, use the <b>🔑 Change Master PIN</b> button above. The child phone automatically pulls and enforces the new PIN on next sync.</p>
          </div>

          <div class="guide-card">
            <h3>3. Uninstall & Tamper Protection (Device Administrator)</h3>
            <p>Detachment uses Android <b>Device Administrator</b> privileges combined with accessibility-level tamper detection to physically prevent uninstallation and workarounds on the child device:</p>
            <ul>
              <li>The Android OS disables and greys out the <b>Uninstall</b> button in system Settings.</li>
              <li>Opening App Details, Accessibility, or Device Admin settings on the child phone is intercepted and guarded by the <b>Master PIN</b>.</li>
              <li>Only the parent can enter the Master PIN to authorize temporary settings adjustments or unpairing.</li>
            </ul>
          </div>

          <div class="guide-card">
            <h3>4. App Launch Authentication (Remote Control)</h3>
            <p>Remotely toggle <b>App Launch Authentication</b> using the button above or under Global Rules. When enabled, opening Detachment on the child phone immediately requires the Master PIN. The child cannot disable this setting while paired.</p>
          </div>

          <div class="guide-card">
            <h3>5. App Limits & Essential Override</h3>
            <ul>
              <li><b>Daily Limits:</b> Enter the maximum minutes per day allowed. 0 minutes means unlimited.</li>
              <li><b>Essential Apps:</b> Tag critical apps (Phone, WhatsApp, Maps, Calculator). Essential apps are strictly immune to limits, delay shields, and schedule blocks.</li>
              <li><b>Distracting Apps:</b> Tag apps to be restricted during active focus schedules.</li>
              <li><b>Friction Shield:</b> Enforces a mindful countdown (e.g. 15s) before the app opens to stop impulse scrolling.</li>
              <li><b>Manual Lock:</b> Instantly shuts down the app until you unlock it remotely.</li>
            </ul>
          </div>

          <div class="guide-card">
            <h3>6. Focus Schedules</h3>
            <p>Create automated blocking periods (e.g. School Hours or Bedtime). Choose days and whether they block <b>Distracting</b> apps or <b>All Non-Essential</b> apps.</p>
          </div>

          <div class="guide-card">
            <h3>7. Sync Frequency & Push</h3>
            <p>Tap <b>Save & Push to Device</b> after making changes. The child phone automatically pulls changes upon launching Detachment and every 15 minutes in the background via WorkManager. Tap <b>Sync Now</b> in the phone dialog to refresh instantly.</p>
          </div>

          <div class="guide-card">
            <h3>8. Child Device Lock Protection</h3>
            <p>Once paired, the child phone enters <b>Managed Mode</b>. Limit sliders, schedule rules, and PIN changes are locked locally on the phone. Unpairing on the child device is guarded by the Master PIN.</p>
          </div>
        </div>
      </div>
    </div>
  </div>

  <div id="changePinModal" class="modal-overlay" style="display: none;">
    <div class="card" style="max-width: 420px; width: 100%; border-color: var(--purple-light); margin-bottom: 0;">
      <h3 style="font-size: 18px; margin-bottom: 6px; color: var(--text);">Change Device Master PIN</h3>
      <p style="font-size: 12.5px; color: var(--text-muted); margin-bottom: 16px; line-height: 1.5;">
        Child cannot alter this PIN on the device. Enter a new 4 to 6 digit secret passcode to enforce remotely.
      </p>
      <div style="display: flex; flex-direction: column; gap: 12px;">
        <div>
          <label style="font-size: 12px; color: var(--text-muted); display: block; margin-bottom: 4px;">New 4-6 Digit PIN</label>
          <input type="password" id="modalNewPin" maxlength="6" placeholder="••••" class="code-input" style="width: 100%; text-align: left; letter-spacing: 4px;" />
        </div>
        <div>
          <label style="font-size: 12px; color: var(--text-muted); display: block; margin-bottom: 4px;">Confirm New PIN</label>
          <input type="password" id="modalConfirmPin" maxlength="6" placeholder="••••" class="code-input" style="width: 100%; text-align: left; letter-spacing: 4px;" />
        </div>
        <div style="display: flex; gap: 10px; margin-top: 10px;">
          <button class="emerald" style="flex: 1;" onclick="submitNewPinModal()">Save & Push PIN</button>
          <button class="secondary" onclick="closeChangePinModal()">Cancel</button>
        </div>
      </div>
    </div>
  </div>

  <div id="toast" class="toast">Saved and pushed to child device!</div>

  <script>
    const SERVER_CODES = ${codesJson};
    let currentCode = "";
    let currentConfig = null;
    let currentDevice = null;
    let savedCodes = [];

    function setSafeText(id, text) {
      const el = document.getElementById(id);
      if (el) el.innerText = text;
    }

    function setSafeValue(id, val) {
      const el = document.getElementById(id);
      if (el) el.value = val;
    }

    function setSafeDisplay(id, val) {
      const el = document.getElementById(id);
      if (el) el.style.display = val;
    }

    function setSafeColor(id, val) {
      const el = document.getElementById(id);
      if (el) el.style.color = val;
    }

    function initSavedCodes() {
      try {
        const stored = localStorage.getItem("detachment_saved_codes");
        if (stored) {
          savedCodes = JSON.parse(stored);
        } else {
          savedCodes = SERVER_CODES.slice();
          saveCodesToStorage();
        }
      } catch (e) {
        savedCodes = SERVER_CODES.slice();
      }
      if (!Array.isArray(savedCodes)) savedCodes = [];
      SERVER_CODES.forEach(c => {
        if (!savedCodes.includes(c)) savedCodes.push(c);
      });
      renderSavedDevices();

      const lastActive = localStorage.getItem("detachment_last_active_code") || savedCodes[0];
      if (lastActive) {
        loadDeviceByCode(lastActive);
      }
    }

    function saveCodesToStorage() {
      try {
        localStorage.setItem("detachment_saved_codes", JSON.stringify(savedCodes));
      } catch (e) {}
    }

    function renderSavedDevices() {
      const list = document.getElementById("savedDevicesList");
      if (!list) return;
      list.innerHTML = "";
      if (savedCodes.length === 0) {
        list.innerHTML = "<p style='color: var(--text-muted); font-size: 13px; grid-column: 1/-1;'>No devices saved. Tap + Add New Device Code to connect.</p>";
        return;
      }

      savedCodes.forEach(code => {
        const chip = document.createElement("div");
        chip.className = "device-chip" + (code === currentCode ? " active-chip" : "");
        chip.onclick = () => loadDeviceByCode(code);
        chip.innerHTML = \`
          <div class="device-chip-header">
            <span class="device-chip-code">\${code}</span>
            <span class="badge" style="font-size: 9px; padding: 2px 6px;">Saved</span>
          </div>
          <div class="device-chip-title" id="chip_name_\${code}">Child Device</div>
          <div class="device-chip-actions">
            <span style="font-size: 11px; color: var(--text-muted);">Tap to load</span>
            <button class="remove-code-btn" onclick="removeDeviceCode(event, '\${code}')">Remove</button>
          </div>
        \`;
        list.appendChild(chip);
      });
    }

    function removeDeviceCode(ev, code) {
      ev.stopPropagation();
      savedCodes = savedCodes.filter(c => c !== code);
      saveCodesToStorage();
      if (currentCode === code) {
        currentCode = "";
        setSafeDisplay("deviceDashboard", "none");
        setSafeDisplay("btnSwitchDevice", "none");
        setSafeDisplay("btnChangePin", "none");
        setSafeDisplay("btnToggleAppAuth", "none");
        setSafeDisplay("btnToggleUninstallShield", "none");
        setSafeDisplay("deviceSelectorCard", "block");
      }
      renderSavedDevices();
    }

    function toggleAddDeviceForm() {
      const f = document.getElementById("addDeviceForm");
      if (!f) return;
      f.style.display = f.style.display === "none" ? "block" : "none";
      if (f.style.display === "block") {
        const inp = document.getElementById("deviceCode");
        if (inp) inp.focus();
      }
    }

    function showDeviceSelector() {
      setSafeDisplay("deviceSelectorCard", "block");
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    function openChangePinModal() {
      if (!currentCode) return;
      setSafeValue("modalNewPin", "");
      setSafeValue("modalConfirmPin", "");
      setSafeDisplay("changePinModal", "flex");
      const inp = document.getElementById("modalNewPin");
      if (inp) inp.focus();
    }

    function closeChangePinModal() {
      setSafeDisplay("changePinModal", "none");
    }

    async function submitNewPinModal() {
      const newPin = (document.getElementById("modalNewPin")?.value || "").trim();
      const confirmPin = (document.getElementById("modalConfirmPin")?.value || "").trim();
      if (!newPin || newPin.length < 4 || newPin.length > 6) {
        alert("PIN must be 4 to 6 digits");
        return;
      }
      if (newPin !== confirmPin) {
        alert("PINs do not match");
        return;
      }
      await pushPinToServer(newPin);
      closeChangePinModal();
    }

    async function quickUpdatePinFromInput() {
      const pin = (document.getElementById("settingMasterPin")?.value || "").trim();
      if (!pin || pin.length < 4 || pin.length > 6) {
        alert("PIN must be 4 to 6 digits");
        return;
      }
      await pushPinToServer(pin);
    }

    async function pushPinToServer(pin) {
      if (!currentCode) return;
      try {
        const res = await fetch("/api/device/" + currentCode + "/pin", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ pin: pin })
        });
        const data = await res.json();
        if (data.success) {
          if (!currentConfig.settings) currentConfig.settings = {};
          currentConfig.settings.master_pin = pin;
          setSafeValue("settingMasterPin", pin);
          setSafeText("statMasterPin", pin);
          showToast("Master PIN updated! Enforced on child phone.");
        } else {
          alert("Error: " + data.error);
        }
      } catch (err) {
        alert("Failed to update PIN: " + err.message);
      }
    }

    function updateAppAuthUI(isAuth) {
      setSafeText("statAppAuth", isAuth ? "Enabled (PIN)" : "Disabled");
      setSafeColor("statAppAuth", isAuth ? "var(--cyan)" : "var(--text-muted)");
      setSafeText("appAuthTopBadge", isAuth ? "Enabled" : "Disabled");
      setSafeText("appAuthSettingBadge", isAuth ? "Enabled" : "Disabled");
      setSafeText("btnToggleAppAuthInSettings", isAuth ? "Disable App Auth" : "Enable App Auth");
    }

    function updateUninstallUI(isShield) {
      setSafeText("statUninstallShield", isShield ? "Active" : "Disabled");
      setSafeColor("statUninstallShield", isShield ? "var(--emerald)" : "var(--rose)");
      setSafeText("uninstallTopBadge", isShield ? "Active" : "Disabled");
      setSafeText("uninstallSettingBadge", isShield ? "Active" : "Disabled");
      setSafeText("btnToggleUninstallInSettings", isShield ? "Disable Shield" : "Enable Shield");
    }

    async function toggleAppAuthRemote() {
      if (!currentCode || !currentConfig) return;
      const isAuth = (currentConfig.settings && currentConfig.settings.app_auth_enabled === "true");
      const nextState = !isAuth;
      try {
        const res = await fetch("/api/device/" + currentCode + "/app-auth", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ enabled: nextState })
        });
        const data = await res.json();
        if (data.success) {
          if (!currentConfig.settings) currentConfig.settings = {};
          currentConfig.settings.app_auth_enabled = nextState ? "true" : "false";
          updateAppAuthUI(nextState);
          showToast("App Launch Authentication " + (nextState ? "Enabled" : "Disabled") + "! Enforced on phone.");
        } else {
          alert("Error: " + data.error);
        }
      } catch (err) {
        alert("Failed to update App Authentication: " + err.message);
      }
    }

    async function toggleUninstallProtectionRemote() {
      if (!currentCode || !currentConfig) return;
      const isShield = (currentConfig.settings && currentConfig.settings.uninstall_protection_enabled !== "false");
      const nextState = !isShield;
      try {
        const res = await fetch("/api/device/" + currentCode + "/uninstall-protection", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ enabled: nextState })
        });
        const data = await res.json();
        if (data.success) {
          if (!currentConfig.settings) currentConfig.settings = {};
          currentConfig.settings.uninstall_protection_enabled = nextState ? "true" : "false";
          updateUninstallUI(nextState);
          showToast("Uninstall Protection " + (nextState ? "Enabled" : "Disabled") + "! Enforced on phone.");
        } else {
          alert("Error: " + data.error);
        }
      } catch (err) {
        alert("Failed to update Uninstall Protection: " + err.message);
      }
    }

    function handleConnectInputCode() {
      const inp = document.getElementById("deviceCode");
      const code = (inp?.value || "").trim().toUpperCase();
      if (!code) {
        alert("Please enter a 6-character code");
        return;
      }
      if (!savedCodes.includes(code)) {
        savedCodes.push(code);
        saveCodesToStorage();
        renderSavedDevices();
      }
      loadDeviceByCode(code);
    }

    async function loadDeviceByCode(code) {
      currentCode = code;
      localStorage.setItem("detachment_last_active_code", code);
      renderSavedDevices();
      setSafeText("connStatus", "Connecting...");
      setSafeColor("connStatus", "var(--amber)");

      try {
        const res = await fetch("/api/device/" + code + "/config");
        const data = await res.json();
        if (!data.success) {
          alert("Error loading " + code + ": " + (data.error || "Device not found"));
          setSafeText("connStatus", "Error");
          return;
        }

        currentConfig = data.config || {};
        currentDevice = data.device || {};

        setSafeDisplay("deviceDashboard", "block");
        setSafeDisplay("btnSwitchDevice", "inline-flex");
        setSafeDisplay("btnChangePin", "inline-flex");
        setSafeDisplay("btnToggleAppAuth", "inline-flex");
        setSafeDisplay("btnToggleUninstallShield", "inline-flex");
        setSafeText("statDeviceName", currentDevice.deviceName || "Child Device");
        setSafeText("statCode", code);
        setSafeText("statMasterPin", (currentConfig.settings && currentConfig.settings.master_pin) ? currentConfig.settings.master_pin : "1234");
        setSafeText("statLastSync", currentDevice.lastSyncAt ? new Date(currentDevice.lastSyncAt).toLocaleTimeString() : "Never");
        setSafeText("connStatus", "Connected");
        setSafeColor("connStatus", "var(--emerald)");

        const isAuth = (currentConfig.settings && currentConfig.settings.app_auth_enabled === "true");
        updateAppAuthUI(isAuth);

        const isUninstall = (currentConfig.settings && currentConfig.settings.uninstall_protection_enabled !== "false");
        updateUninstallUI(isUninstall);

        const nameLabel = document.getElementById("chip_name_" + code);
        if (nameLabel && currentDevice.deviceName) {
          nameLabel.innerText = currentDevice.deviceName;
        }

        renderApps();
        renderSchedules();
        renderSettings();
      } catch (err) {
        alert("Connection failed: " + err.message);
        setSafeText("connStatus", "Failed");
      }
    }

    async function generateDemoDevice() {
      const demoCode = "DEMO99";
      if (!savedCodes.includes(demoCode)) {
        savedCodes.push(demoCode);
        saveCodesToStorage();
        renderSavedDevices();
      }
      await fetch("/api/device/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          code: demoCode,
          deviceName: "Alex's Galaxy A54",
          apps: [
            { packageName: "com.instagram.android", appName: "Instagram", category: "Social", dailyLimitMinutes: 30, isDistracting: true },
            { packageName: "com.zhiliaoapp.musically", appName: "TikTok", category: "Social", dailyLimitMinutes: 20, isDistracting: true },
            { packageName: "com.google.android.youtube", appName: "YouTube", category: "Video", dailyLimitMinutes: 45, isDistracting: true },
            { packageName: "com.whatsapp", appName: "WhatsApp", category: "Communication", dailyLimitMinutes: 0, isEssential: true },
            { packageName: "com.duolingo", appName: "Duolingo", category: "Education", dailyLimitMinutes: 0, isEssential: true }
          ]
        })
      });
      loadDeviceByCode(demoCode);
    }

    function switchTab(tabId) {
      document.querySelectorAll(".tab").forEach(t => t.classList.remove("active"));
      if (event && event.target) event.target.classList.add("active");
      setSafeDisplay("tabApps", tabId === 'apps' ? 'block' : 'none');
      setSafeDisplay("tabSchedules", tabId === 'schedules' ? 'block' : 'none');
      setSafeDisplay("tabSettings", tabId === 'settings' ? 'block' : 'none');
      setSafeDisplay("tabHelp", tabId === 'help' ? 'block' : 'none');
    }

    function renderApps() {
      const container = document.getElementById("appsListContainer");
      if (!container) return;
      container.innerHTML = "";
      if (!currentConfig.appLimits) currentConfig.appLimits = [];
      if (currentDevice && currentDevice.installedApps && Array.isArray(currentDevice.installedApps)) {
        const existingMap = new Map(currentConfig.appLimits.map(a => [a.packageName, a]));
        for (const app of currentDevice.installedApps) {
          if (!existingMap.has(app.packageName)) {
            currentConfig.appLimits.push({
              packageName: app.packageName,
              appName: app.appName,
              category: app.category || "General",
              dailyLimitMinutes: app.dailyLimitMinutes || 0,
              isDistracting: app.isDistracting || false,
              isEssential: app.isEssential || false,
              isShieldActive: app.isShieldActive || false,
              isLockedManually: app.isLockedManually || false,
              isHardMode: false
            });
          }
        }
      }
      const apps = currentConfig.appLimits;
      setSafeText("statTotalApps", apps.length);

      if (apps.length === 0) {
        container.innerHTML = \`
          <div style="padding: 24px; text-align: center; color: var(--text-muted); background: rgba(15, 12, 29, 0.4); border: 1px dashed var(--card-border); border-radius: 14px;">
            <p style="font-size: 14px; margin-bottom: 8px; color: var(--text); font-weight: 600;">No installed apps received from child device yet.</p>
            <p style="font-size: 12.5px; line-height: 1.5; color: var(--text-muted);">Open <b>Detachment</b> on child phone &gt; tap <b>Parental Control</b> &gt; tap <b>Sync Now</b> to immediately send installed apps.</p>
          </div>
        \`;
        return;
      }

      apps.forEach((app, idx) => {
        const row = document.createElement("div");
        row.className = "app-row";
        const initial = (app.appName && app.appName.length > 0) ? app.appName.charAt(0) : "A";
        row.innerHTML = \`
          <div class="app-info">
            <div class="app-icon">\${initial}</div>
            <div>
              <div class="app-name">\${app.appName || app.packageName}</div>
              <div class="app-pkg">\${app.packageName}</div>
            </div>
          </div>
          <div class="controls-row">
            <div class="limit-input-group">
              <span style="font-size: 12px; color: var(--text-muted);">Limit:</span>
              <input type="number" min="0" max="600" value="\${app.dailyLimitMinutes || 0}" onchange="updateAppLimit(\${idx}, this.value)" />
              <span style="font-size: 12px; color: var(--text-muted);">min</span>
            </div>
            <button class="tag-btn \${app.isEssential ? 'active-essential' : ''}" onclick="toggleTag(\${idx}, 'isEssential')">Essential</button>
            <button class="tag-btn \${app.isDistracting ? 'active-distracting' : ''}" onclick="toggleTag(\${idx}, 'isDistracting')">Distracting</button>
            <button class="tag-btn \${app.isShieldActive ? 'active-shield' : ''}" onclick="toggleTag(\${idx}, 'isShieldActive')">Shield</button>
            <button class="tag-btn \${app.isLockedManually ? 'active-locked' : ''}" onclick="toggleTag(\${idx}, 'isLockedManually')">\${app.isLockedManually ? 'Locked' : 'Unlocked'}</button>
          </div>
        \`;
        container.appendChild(row);
      });
    }

    function updateAppLimit(idx, val) {
      if (currentConfig && currentConfig.appLimits && currentConfig.appLimits[idx]) {
        currentConfig.appLimits[idx].dailyLimitMinutes = parseInt(val) || 0;
      }
    }

    function toggleTag(idx, tag) {
      if (!currentConfig || !currentConfig.appLimits || !currentConfig.appLimits[idx]) return;
      const app = currentConfig.appLimits[idx];
      if (tag === 'isEssential') {
        app.isEssential = !app.isEssential;
        if (app.isEssential) {
          app.isDistracting = false;
          app.isShieldActive = false;
          app.isLockedManually = false;
        }
      } else {
        if (app.isEssential) return;
        app[tag] = !app[tag];
      }
      renderApps();
    }

    function renderSchedules() {
      const container = document.getElementById("schedulesListContainer");
      if (!container) return;
      container.innerHTML = "";
      const schedules = currentConfig.schedules || [];
      schedules.forEach((sch, idx) => {
        const row = document.createElement("div");
        row.className = "app-row";
        const start = String(sch.startHour).padStart(2, '0') + ":" + String(sch.startMinute).padStart(2, '0');
        const end = String(sch.endHour).padStart(2, '0') + ":" + String(sch.endMinute).padStart(2, '0');
        row.innerHTML = \`
          <div>
            <div style="font-weight: 700; font-size: 16px;">\${sch.title}</div>
            <div style="font-size: 13px; color: var(--text-muted); font-family: monospace;">\${start} - \${end} (\${sch.activeDays})</div>
          </div>
          <div class="controls-row">
            <button class="tag-btn \${sch.isEnabled ? 'active-essential' : ''}" onclick="toggleSchedule(\${idx})">\${sch.isEnabled ? 'Enabled' : 'Disabled'}</button>
            <button class="tag-btn active-locked" onclick="deleteSchedule(\${idx})">Delete</button>
          </div>
        \`;
        container.appendChild(row);
      });
    }

    function toggleSchedule(idx) {
      if (currentConfig && currentConfig.schedules && currentConfig.schedules[idx]) {
        currentConfig.schedules[idx].isEnabled = !currentConfig.schedules[idx].isEnabled;
        renderSchedules();
      }
    }

    function deleteSchedule(idx) {
      if (currentConfig && currentConfig.schedules) {
        currentConfig.schedules.splice(idx, 1);
        renderSchedules();
      }
    }

    function addSchedule() {
      const title = prompt("Schedule Title:", "Study Focus");
      if (!title) return;
      if (!currentConfig.schedules) currentConfig.schedules = [];
      currentConfig.schedules.push({
        title: title,
        type: "CUSTOM",
        startHour: 17,
        startMinute: 0,
        endHour: 19,
        endMinute: 0,
        activeDays: "MON,TUE,WED,THU,FRI",
        isEnabled: true,
        blockedTarget: "DISTRACTING"
      });
      renderSchedules();
    }

    function renderSettings() {
      const s = currentConfig.settings || {};
      setSafeValue("settingDelaySec", s.key_delay_seconds || "15");
      setSafeValue("settingUnlockMin", s.key_unlock_minutes || "15");
      const pin = s.master_pin || "1234";
      setSafeValue("settingMasterPin", pin);
      setSafeText("statMasterPin", pin);
      const isAuth = s.app_auth_enabled === "true";
      updateAppAuthUI(isAuth);
      const isUninstall = s.uninstall_protection_enabled !== "false";
      updateUninstallUI(isUninstall);
    }

    async function saveConfigToServer() {
      if (!currentConfig || !currentCode) return;
      if (!currentConfig.settings) currentConfig.settings = {};
      const delayInp = document.getElementById("settingDelaySec");
      const unlockInp = document.getElementById("settingUnlockMin");
      const pinInp = document.getElementById("settingMasterPin");
      if (delayInp) currentConfig.settings.key_delay_seconds = delayInp.value;
      if (unlockInp) currentConfig.settings.key_unlock_minutes = unlockInp.value;
      if (pinInp) currentConfig.settings.master_pin = pinInp.value;

      try {
        const res = await fetch("/api/device/" + currentCode + "/config", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ config: currentConfig })
        });
        const data = await res.json();
        if (data.success) {
          showToast("Configuration saved and pushed!");
        } else {
          alert("Error saving: " + data.error);
        }
      } catch (err) {
        alert("Save failed: " + err.message);
      }
    }

    function showToast(msg) {
      const t = document.getElementById("toast");
      if (!t) return;
      if (msg) t.innerText = msg;
      t.style.display = "block";
      setTimeout(() => { t.style.display = "none"; }, 3500);
    }

    window.onload = initSavedCodes;
  </script>
</body>
</html>`;
}
