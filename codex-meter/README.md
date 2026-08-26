# Codex meter

Small Codex quota meter for Termux/tmux and ordinary shell status lines. It also emits Waybar JSON for Linux.

Default output:

```text
Codex 5h 73% · 7d 41%
```

It reads the existing Codex OAuth login from `~/.codex/auth.json`. It does not store or print the token.

## Android / Termux install

From this checkout:

```sh
sh codex-meter/install.sh
```

On Termux the installer writes `codex-meter` into `$PREFIX/bin`. The installed command is only a wrapper pointing back at this checkout, so after that a normal `git pull` updates the meter immediately; there is no copied Python file to go stale.

Run `codex login` first if needed.

For tmux:

```tmux
set -g status-interval 60
set -g status-right '#(codex-meter) | %H:%M'
```

## Other output

```sh
codex-meter --json
codex-meter --waybar
```

Window labels come from the server's actual limit-window duration instead of assuming that `primary` always means five hours or one week.

## Environment

- `CODEX_HOME` — alternate Codex directory.
- `CODEX_METER_BASE_URL` — alternate backend base URL.
- `CODEX_CHATGPT_BASE_URL` — older alias for the same override.
- `CODEX_METER_BIN_DIR` — installer destination override.
- `CODEX_METER_PYTHON` — Python executable override.

For ChatGPT backend URLs containing `/backend-api`, the meter uses the `/wham/usage` route. For Codex API-style base URLs it uses `/api/codex/usage`.
