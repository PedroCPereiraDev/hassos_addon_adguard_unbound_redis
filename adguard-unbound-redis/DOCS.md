# Home Assistant Add-on: AdGuard Home with Unbound & Redis

All-in-one DNS solution combining AdGuard Home network-wide ad blocking,
Unbound recursive DNS resolver with prefetching, and Redis in-memory caching —
built for speed, privacy, and performance.

This add-on wraps the
[imTHAI/adguardhome-unbound-redis](https://github.com/imTHAI/adguardhome-unbound-redis)
Docker project into a native Home Assistant add-on. The configuration folder is
**fully compatible** with the standalone Docker container, enabling multi-server
sync for high-availability DNS setups.

## How it works

```
Client DNS query
    ↓ (port 53)
AdGuard Home — filters ads, trackers, malware domains
    ↓ (upstream: 127.0.0.1:5335)
Unbound — recursive/forwarding DNS resolver with DNSSEC validation
    ↓ (Unix socket: /tmp/redis.sock)
Redis — in-memory DNS cache for near-instant repeat lookups
    ↓
Upstream DNS (Cloudflare DoT by default) or Root Servers (recursive mode)
```

## Installation

1. Add the repository to Home Assistant:

   [![Add repository](https://my.home-assistant.io/badges/supervisor_add_addon_repository.svg)](https://my.home-assistant.io/redirect/supervisor_add_addon_repository/?repository_url=https%3A%2F%2Fgithub.com%2FPedroCPereiraDev%2Fhassos_addon_adguard_unbound_redis)

   Or manually: **Settings → Add-ons → Add-on Store → ⋮ → Repositories** and
   paste:
   ```
   https://github.com/PedroCPereiraDev/hassos_addon_adguard_unbound_redis
   ```

2. Install **"AdGuard Home with Unbound & Redis"** from the store.

3. Start the add-on. First startup takes a moment to initialize default configs.

4. Open the web UI by clicking **"Open Web UI"** on the add-on page, or browse
   to `http://<your-ha-ip>:3000`. Log in with the default credentials.

5. **Change the default password immediately** (see below).

6. Set your router/DHCP server to use your Home Assistant IP as the DNS server
   so that all network clients benefit from ad blocking.

## Default Credentials

| Field    | Value   |
|----------|---------|
| Username | `admin` |
| Password | `admin` |

## Changing the Password

The password **cannot be changed through the AdGuard Home web UI** — there is
no option for it in the settings. It is stored as a bcrypt hash in
`AdGuardHome.yaml` and must be changed by editing the config file directly.

### Step 1 — Generate a bcrypt hash

Use one of these methods to hash your new password:

- **Using Docker** (easiest, works anywhere):
  ```bash
  docker run --rm httpd:2-alpine htpasswd -nbB admin YOUR_NEW_PASSWORD
  ```

- **Linux** (install `htpasswd` from Apache utilities):
  ```bash
  # Debian/Ubuntu
  sudo apt-get install apache2-utils
  # Fedora
  sudo dnf install httpd-tools

  htpasswd -nbB admin YOUR_NEW_PASSWORD
  ```

- **macOS** (htpasswd is pre-installed with Xcode tools):
  ```bash
  htpasswd -nbB admin YOUR_NEW_PASSWORD
  ```

All of the above will output something like:
```
admin:$2y$05$O5xIZ1z8k...(long hash)...
```

Copy everything **after** `admin:` — that is your password hash.

### Step 2 — Edit the config file

Open `AdGuardHome/AdGuardHome.yaml` in your config folder and find the `users`
section:

```yaml
users:
  - name: admin
    password: $2y$05$...OLD_HASH...
```

Replace the `password` value with the hash you generated:

```yaml
users:
  - name: admin
    password: $2y$05$...YOUR_NEW_HASH...
```

### Step 3 — Restart the add-on

Restart the add-on for the new password to take effect. You can now log in with
`admin` / `YOUR_NEW_PASSWORD`.

> **Tip:** You can also change the username by editing the `name` field.

## HAOS CoreDNS Coexistence

Home Assistant OS runs its own DNS server (CoreDNS) on `127.0.0.1:53` for
internal container name resolution. This add-on **automatically configures
AdGuard Home** to bind only to the host's real network interface IPs, avoiding
any conflict with CoreDNS.

**What this means:**

- On every startup, the add-on writes the host's real interface IPs to
  `dns.bind_hosts` in `AdGuardHome.yaml`. This is the **only** config value the
  add-on modifies automatically.
- CoreDNS continues to handle internal HA container DNS on `127.0.0.1:53`.
- AdGuard Home handles your network's DNS on the host's real IP(s).
- You don't need to do anything — this is handled transparently.

## Configuration

**All configuration is done by editing files directly** — there are no add-on
options in the Home Assistant UI. This design ensures the config folder is
identical to the standalone Docker container, enabling cross-server sync.

### Accessing Config Files

Your config files are stored in an **isolated, add-on-specific directory** — they
are completely separate from your main Home Assistant configuration
(`configuration.yaml`, automations, etc.) and cannot collide with other add-ons.

The host-side path is:
```
/mnt/data/supervisor/addon_configs/<repo-hash>_adguard-unbound-redis/
```

You can access them using any of these methods:

| Method | Path / Location |
|---|---|
| **File Editor add-on** | Disable "Enforce basepath", then browse to `/addon_configs/<hash>_adguard-unbound-redis/` |
| **Samba add-on** | Open the `addon_configs` share → `<hash>_adguard-unbound-redis` folder |
| **SSH / Terminal add-on** | `cd /addon_configs/` then into the `*_adguard-unbound-redis` folder |
| **VS Code Server add-on** | Browse to `/addon_configs/<hash>_adguard-unbound-redis/` in the file tree |
| **AdGuard Home web UI** | Most DNS and filtering settings can also be changed at `http://<your-ha-ip>:3000` |

> **Tip:** The `<hash>` prefix is auto-generated by Home Assistant from the
> repository URL. You don't need to know it — just look for the folder ending
> in `_adguard-unbound-redis` inside `addon_configs`.

### Config Folder Structure

After the first run, your config folder contains:

```
config/
├── AdGuardHome/
│   └── AdGuardHome.yaml          ← Main AdGuard Home configuration
├── unbound/
│   ├── unbound.conf              ← Main Unbound configuration
│   └── unbound.conf.d/
│       ├── cache.conf            ← Redis cachedb backend config
│       ├── dnssec.conf           ← DNSSEC validation & hardening
│       └── forward-queries.conf  ← Upstream DNS forwarders (Cloudflare DoT)
├── redis/
│   └── redis.conf                ← Redis server configuration
├── userfilters/                  ← Place custom blocklist files here
└── data/                         ← AdGuard Home working data (auto-created)
```

This is the **exact same structure** created by the
[standalone Docker container](https://github.com/imTHAI/adguardhome-unbound-redis).

> **Note:** These default configs are extracted directly from the upstream Docker
> image at build time, so they are always in sync with the upstream project.
> If upstream adds new config files in a future release, they will automatically
> appear here on the next add-on update.

## Multi-Server Sync (High Availability)

This add-on is designed to allow **syncing the config folder between multiple
servers** (e.g., Home Assistant OS and an Unraid machine) for a high-availability
DNS setup at home.

### How to Sync

1. Set up the same container on your second server using the
   [upstream Docker image](https://github.com/imTHAI/adguardhome-unbound-redis).
2. Use a sync tool (rsync, Syncthing, etc.) to keep the `/config` folders in
   sync between both machines.
3. After syncing, restart the add-on on the HAOS side for changes to take effect.

### Important: `dns.bind_hosts` Warning

The `dns.bind_hosts` field in `AdGuardHome.yaml` is **machine-specific**. On
HAOS, this add-on automatically overwrites it on every startup with the host's
real interface IPs (for CoreDNS coexistence — see above).

**When syncing configs:**

- **HAOS → Unraid**: The Unraid container uses `0.0.0.0` by default (binds to all
  interfaces). If you sync from HAOS, the specific IPs in `bind_hosts` will still
  work fine on Unraid, but you may want to reset it to `0.0.0.0` for simplicity.
- **Unraid → HAOS**: The `0.0.0.0` value will be synced over, but the HAOS add-on
  will **auto-correct** it to the real interface IPs on the next restart. Just
  restart the add-on after syncing.

**Bottom line:** Sync freely in either direction. The HAOS add-on self-corrects
`bind_hosts` on every restart. All other config values sync perfectly.

## Recursive DNS Mode

By default, Unbound forwards DNS queries to **Cloudflare DNS over TLS** (DoT).
To enable **full recursive resolution** (Unbound queries root DNS servers
directly for maximum privacy):

1. Navigate to your config folder (see [Accessing Config Files](#accessing-config-files)).
2. **Delete** the file `unbound/unbound.conf.d/forward-queries.conf`.
3. **Restart** the add-on.

Unbound will now resolve DNS queries by querying the root servers directly,
without relying on any third-party DNS provider.

> **Note:** Initial DNS lookups will be slower in recursive mode because Unbound
> must query root servers and build its cache from scratch. After the cache
> warms up, performance is comparable to forwarding mode.

To switch back to forwarding mode, delete your entire `unbound/` folder and
restart the add-on — the default configs (including `forward-queries.conf`)
will be recreated automatically.

## DNS Upstream Providers

To change the upstream DNS forwarder (default is Cloudflare DoT):

1. Edit `unbound/unbound.conf.d/forward-queries.conf` in your config folder.
2. Comment out the current Cloudflare lines and uncomment one of the
   pre-configured alternatives:
   - **Cloudflare** (default): `1.1.1.1@853`, `1.0.0.1@853`
   - **AdGuard DNS**: `94.140.14.14@853`, `94.140.14.15@853`
   - **Quad9**: `9.9.9.9@853`, `149.112.112.112@853`
   - **NextDNS**: `45.90.28.21@853`, `45.90.30.21@853`
   - **DNS4all**: `194.0.5.3@853`
   - **Control D**: `76.76.2.0@853`
   - **Yandex DNS**: `77.88.8.8@853`
3. Restart the add-on.

## Custom Blocklist Filters

You can add your own DNS blocklist files:

1. Place your blocklist files (`.txt` format) in the `userfilters/` folder.
2. Ensure `AdGuardHome.yaml` contains the following (it does by default):
   ```yaml
   safe_fs_patterns:
     - /config/userfilters/*
   ```
3. In the AdGuard Home web UI, go to **Filters → DNS blocklists → Add blocklist
   → Add a custom list** and enter the file path, e.g.:
   ```
   /config/userfilters/myblocklist.txt
   ```

## DNS-over-TLS / DNS-over-HTTPS

AdGuard Home supports DNS-over-TLS (DoT) and DNS-over-HTTPS (DoH) for encrypted
DNS serving to clients. To enable:

1. Place your SSL certificates in Home Assistant's `/ssl/` directory.
2. Edit `AdGuardHome.yaml` and configure the `tls` section:
   ```yaml
   tls:
     enabled: true
     server_name: dns.yourdomain.com
     port_https: 443
     port_dns_over_tls: 853
     certificate_path: /ssl/fullchain.pem
     private_key_path: /ssl/privkey.pem
   ```
3. Restart the add-on.

## Performance Tuning

This container comes pre-optimized with settings from the upstream project:

- **Unbound threads**: 4 threads with matching slab counts to reduce lock
  contention.
- **Redis memory**: 128MB with `allkeys-lru` eviction and lazy-freeing for
  non-blocking deletions.
- **Unix socket**: Unbound communicates with Redis via `/tmp/redis.sock`,
  bypassing TCP overhead.
- **Prefetching**: Unbound proactively renews cache entries before they expire.
- **Cache sizes**: 256MB RRset cache, 128MB message cache, 4MB negative cache.

> **Tip**: Average DNS response times of 30–40ms are normal. This balances
> near-instant cache hits (<1ms) with the initial recursive lookups needed to
> populate the cache.

To tune these values, edit the respective config files:
- `unbound/unbound.conf` — threads, cache sizes, prefetch settings
- `redis/redis.conf` — memory limit, eviction policy
- `unbound/unbound.conf.d/cache.conf` — Redis cachedb backend settings

## Blocklists Enabled by Default

- [AdGuard DNS Filter](https://github.com/AdguardTeam/AdguardSDNSFilter)
- [HaGeZi's Threat Intelligence Feeds](https://github.com/hagezi/dns-blocklists)
- [HaGeZi's Multi Pro Blocklist](https://github.com/hagezi/dns-blocklists)

## Troubleshooting

### Add-on won't start / port 53 conflict

If you see errors about port 53 being in use:
- This add-on automatically binds to real interface IPs (not `0.0.0.0`) to avoid
  conflicting with HAOS CoreDNS. If you still see conflicts, check if another
  add-on (like the standalone AdGuard Home add-on) is also using port 53.
- Only one DNS add-on should be active at a time.

### DNS not resolving

1. Check the add-on logs in Home Assistant for errors.
2. Verify Unbound is running: look for "Starting Unbound DNS resolver" in logs.
3. Verify Redis is running: look for "Starting Redis server" in logs.
4. Try a direct query: `nslookup google.com <your-ha-ip>`

### Network clients can't use the DNS

1. Ensure your router/DHCP server points clients to your Home Assistant IP as
   the DNS server.
2. Verify the add-on is running and port 53 is accessible from your network.

### Viewing Logs

- **Home Assistant UI**: Settings → Add-ons → AdGuard Home with Unbound & Redis → Log
- **AdGuard Home UI**: Query Log section shows DNS query history

### After upgrading

The add-on preserves all your config files across updates. New default config
files (added in future versions) will be copied automatically if they don't
already exist — your existing configs are never overwritten.

## Automatic Updates

This add-on **automatically tracks the upstream Docker image**
(`imthai/adguardhome-unbound-redis`). A daily check runs to detect when a new
version is published upstream. When a change is detected:

1. A new version is tagged automatically in this repository.
2. The CI pipeline builds the add-on with the latest upstream binaries and
   configs.
3. Smoke tests verify the image structure and binaries before publishing.
4. If tests pass, the new version is pushed to the container registry.

This means you get upstream updates (new AdGuard Home versions, Unbound patches,
config improvements) **without any manual intervention**. If an upstream change
breaks something, the smoke tests block the release and no broken update reaches
your system.

## Quick Reference — Common Tasks

| Task | How |
|------|-----|
| Change password | Edit `AdGuardHome/AdGuardHome.yaml` → `users[].password` (see [Changing the Password](#changing-the-password)) |
| Switch to recursive DNS | Delete `unbound/unbound.conf.d/forward-queries.conf`, restart |
| Change upstream DNS provider | Edit `unbound/unbound.conf.d/forward-queries.conf`, restart |
| Add custom blocklists | Place files in `userfilters/`, add path in AdGuard Home web UI |
| Enable DNS-over-TLS | Edit `AdGuardHome/AdGuardHome.yaml` → `tls` section, restart |
| View DNS query logs | AdGuard Home web UI → Query Log |
| Tune cache/performance | Edit `unbound/unbound.conf`, `redis/redis.conf`, restart |
| Reset all configs to defaults | Delete the config subfolder (e.g. `unbound/`), restart |

## Credits & Upstream Project

This add-on is built upon the excellent work by
**[imTHAI](https://github.com/imTHAI)**. The entire DNS stack architecture,
default configurations, and container design originate from the
[adguardhome-unbound-redis](https://github.com/imTHAI/adguardhome-unbound-redis)
Docker project.

A huge thanks to imTHAI for creating and maintaining this project — without
their work, this Home Assistant add-on would not exist.

Please visit the [upstream project](https://github.com/imTHAI/adguardhome-unbound-redis)
for detailed technical documentation about the DNS stack and its components.
