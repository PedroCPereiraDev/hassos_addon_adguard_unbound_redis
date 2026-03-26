# AdGuard Home with Unbound & Redis

All-in-one DNS solution combining AdGuard Home ad blocking, Unbound recursive
DNS resolver with prefetching, and Redis in-memory caching — built for speed,
privacy, and performance.

Based on the work by [imTHAI](https://github.com/imTHAI) —
[adguardhome-unbound-redis](https://github.com/imTHAI/adguardhome-unbound-redis).
Thanks to imTHAI for creating and maintaining the upstream project.

## Features

- **AdGuard Home** — Network-wide ad and tracker blocking with web UI
- **Unbound** — Validating, recursive DNS resolver with DNSSEC
- **Redis** — High-performance DNS cache backend via Unix socket
- **Config sync** — Config folder compatible with the standalone Docker container
  for multi-server high-availability setups
- **Auto-updates** — Automatically tracks upstream releases with smoke-tested builds
- **No UI options needed** — All configuration via files, just like the original project

## Quick Start

1. **Install and start** the add-on from the Home Assistant Add-on Store.
2. **Open the web UI** — click "Open Web UI" above, or go to
   `http://<your-ha-ip>:3000`.
3. **Log in** with the default credentials: **admin** / **admin**.
4. **Change the default password** — the password is stored as a bcrypt hash in
   `AdGuardHome/AdGuardHome.yaml`. Generate a new hash with:
   ```
   docker run --rm httpd:2-alpine htpasswd -nbB admin YOUR_NEW_PASSWORD
   ```
   Then replace the `password` value in the `users` section and restart the add-on.
   See the [documentation][docs] for detailed instructions.
5. **Set your router/DHCP** to use your Home Assistant IP as the DNS server so
   all network clients benefit from ad blocking.

## Default Credentials

| Field    | Value   |
|----------|---------|
| Username | `admin` |
| Password | `admin` |

> **Important:** Change the default password immediately after first login.

## Configuration

All configuration is done by editing files directly — there are no add-on
options in the Home Assistant UI. This keeps the config folder identical to the
standalone Docker container, enabling multi-server sync.

Config files are stored in an isolated, add-on-specific directory accessible via:

| Method | Location |
|---|---|
| **File Editor add-on** | `/addon_configs/*_adguard-unbound-redis/` |
| **Samba add-on** | `addon_configs` share → `*_adguard-unbound-redis` folder |
| **SSH / Terminal** | `/addon_configs/*_adguard-unbound-redis/` |
| **AdGuard Home web UI** | Most DNS/filtering settings at `http://<your-ha-ip>:3000` |

See the [documentation][docs] for full details on config files, recursive DNS,
custom blocklists, DNS-over-TLS, performance tuning, and more.

[docs]: https://github.com/PedroCPereiraDev/hassos_addon_adguard_unbound_redis/blob/main/adguard-unbound-redis/DOCS.md
