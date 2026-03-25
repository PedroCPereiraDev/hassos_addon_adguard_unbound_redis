# AdGuard Home with Unbound & Redis — Home Assistant Add-on Repository

[![Add repository to Home Assistant](https://my.home-assistant.io/badges/supervisor_add_addon_repository.svg)](https://my.home-assistant.io/redirect/supervisor_add_addon_repository/?repository_url=https%3A%2F%2Fgithub.com%2FPedroCPereiraDev%2Fhassos_addon_adguard_unbound_redis)

## About

This repository contains a Home Assistant add-on that wraps the
[imTHAI/adguardhome-unbound-redis](https://github.com/imTHAI/adguardhome-unbound-redis)
Docker project into a native Home Assistant add-on.

It combines three services in a single add-on for a complete DNS solution:

- **[AdGuard Home](https://github.com/AdguardTeam/AdGuardHome)** — Network-wide ad
  and tracker blocking with a web-based management UI.
- **[Unbound](https://unbound.docs.nlnetlabs.nl/)** — A validating, recursive, and
  caching DNS resolver compiled with `hiredis` support for Redis-backed caching.
- **[Redis](https://redis.io/)** — In-memory data store used as a high-performance
  DNS cache backend for Unbound via Unix socket.

## Installation

1. Click the **"Add repository"** button above, or manually add this repository URL
   in Home Assistant under **Settings → Add-ons → Add-on Store → ⋮ → Repositories**:

   ```
   https://github.com/PedroCPereiraDev/hassos_addon_adguard_unbound_redis
   ```

2. Find **"AdGuard Home with Unbound & Redis"** in the add-on store and click
   **Install**.

3. Start the add-on. On first run, default configuration files are created
   automatically.

4. Open the web UI (via the sidebar or `http://<your-ha-ip>:3000`) and log in
   with the default credentials (`admin` / `admin`).

5. **Change the default password** by editing `AdGuardHome/AdGuardHome.yaml` in
   your config folder — the password is stored as a bcrypt hash. See the
   [add-on documentation](adguard-unbound-redis/DOCS.md#changing-the-password)
   for step-by-step instructions.

6. Set your router/DHCP to use your Home Assistant IP as the DNS server.

## Configuration

All configuration is done through config files — the same files used by the
standalone Docker container. This means you can **sync your config folder between
Home Assistant OS and an Unraid/Docker server** for a high-availability DNS setup.

See the [add-on documentation](adguard-unbound-redis/DOCS.md) for full details.

## Automatic Updates

This add-on **automatically tracks the upstream Docker image**. A daily CI check
detects new upstream releases and builds a new add-on version — with smoke tests
to block broken updates. No manual maintenance needed.

## Support

- [Open an issue](https://github.com/PedroCPereiraDev/hassos_addon_adguard_unbound_redis/issues)
- [Upstream project](https://github.com/imTHAI/adguardhome-unbound-redis)

## Credits

This add-on is built upon the excellent work by
**[imTHAI](https://github.com/imTHAI)** and the
[adguardhome-unbound-redis](https://github.com/imTHAI/adguardhome-unbound-redis)
Docker project. A huge thanks to imTHAI for creating and maintaining the
upstream project that makes this all-in-one DNS stack possible.

## License

MIT License — see [LICENSE](LICENSE) for details.
