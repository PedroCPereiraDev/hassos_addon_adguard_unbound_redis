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

## Config Files

All configuration is done by editing files directly — there are no add-on
options in the Home Assistant UI. Config files are stored in an isolated,
add-on-specific directory accessible via Samba (`addon_configs` share),
File Editor, or SSH. See the [documentation](DOCS.md) for full details.
