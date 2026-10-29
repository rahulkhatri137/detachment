# Detachment Parental Control Backend

Lightweight Cloudflare Worker with KV storage and an embedded web dashboard for remote screen time enforcement, app limit controls, and focus schedule management.

## Quick Start (Cloudflare Dashboard - 2 minutes)

1. Log into your [Cloudflare Dashboard](https://dash.cloudflare.com/) and go to **Workers & Pages**.
2. Click **Create Application** > **Create Worker**. Name it `detachment-parental`.
3. In the worker editor, replace the default code with the contents of `worker.js`.
4. Go to **Workers & Pages** > **KV** and create a KV namespace called `DETACHMENT_KV`.
5. In your Worker's **Settings** > **Variables** > **KV Namespace Bindings**, bind `DETACHMENT_KV` to the namespace created in step 4.
6. Deploy the Worker. Your parental dashboard is now live at `https://detachment-parental.<your-subdomain>.workers.dev`!

## Quick Start (Wrangler CLI)

```bash
cd backend
npm install -g wrangler
wrangler kv namespace create DETACHMENT_KV
# Update wrangler.toml with the returned KV id
wrangler deploy
```

## How It Works

1. **Child App Pairing**: Open Detachment on child device > Dashboard > Tap the Parental Control (Shield) button in the header. Enter your Cloudflare Worker URL and tap **Register & Generate Code**.
2. **Parent Dashboard**: Open the Cloudflare Worker URL in any web browser on your phone or laptop. Enter the 6-character code to connect.
3. **Remote Control**:
   - Set daily minute limits for apps.
   - Tag apps as **Essential** (immune to delay shields or locks), **Distracting**, or apply **Friction Shields**.
   - Create or edit **Focus Schedules** (Study, Bedtime, Custom).
   - Change global delay seconds, temporary grace periods, or master PIN.
   - Tap **Save & Push to Device**.
4. **Child Enforcing**: The child device immediately pulls updates on app launch and runs a background sync every 15 minutes. While paired, local editing on the child device is completely locked and guarded.

## Detailed Guide

For the complete guide on app setup, limits enforcement, essential apps, and focus schedules, see **[Parental Guide.md](../Parental%20Guide.md)** or open the **Help & Guide** tab directly inside the Worker Web Dashboard.
