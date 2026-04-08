# Loading Protection

Short-term spawn protection for the moments when the world is visible, but not fully ready yet.

[中文说明](./README.zh-CN.md)

[Overview](#overview) • [Support](#support) • [Triggers](#triggers) • [Installation](#installation) • [Configuration](#configuration)

> [!IMPORTANT]
> This mod targets **Minecraft 26.1+**.

## Overview

Loading Protection gives players a temporary safety window after joining a world, respawning, or changing dimensions. The point is simple: cover the awkward gap where chunks, entities, or networking may still be settling in, but damage is already possible.

While protection is active, the mod can:

- prevent immediate damage right after a join, respawn, or dimension change
- clear hostile mob targeting from protected players
- stop protected players from dealing damage until protection ends
- end protection early once the player actually moves
- show countdown notices in chat, the action bar, or both

## Support

| Item      | Status    |
|-----------|-----------|
| Minecraft | 26.1+     |
| Fabric    | Supported |
| NeoForge  | Supported |

## Good fit for

Loading Protection is especially useful when:

- players join a server and nearby mobs are already active while the client is still loading
- respawn stutter turns one death into two
- dimension travel finishes before the destination area is fully stable
- a survival or adventure server wants safer login, respawn, or teleport flow

## Triggers

Protection can start after:

- joining a world or server
- respawning
- changing dimensions

By default, protection starts on **join**. Respawn and dimension-change protection can be enabled if needed.

## What happens during protection

With the default rules, protected players:

- do not take damage
- are dropped as targets by hostile mobs
- cannot deal damage to players or mobs
- receive countdown messages while protection is active

If "End On Movement" is enabled, protection ends as soon as the player actually changes position. Looking around does not count.

## Configuration

Config file:

```text
config/loadingprotection-common.toml
```

Main options:

- `Protection Duration`: protection time in seconds (1-300)
- `Trigger On Join`: enable protection when joining a world or server
- `Trigger On Respawn`: enable protection after respawn
- `Trigger On Dimension Change`: enable protection after cross-dimension travel
- `End On Movement`: end protection once the player actually moves
- `Protect Mount`: also protect the vehicle currently being ridden
- `Prevent Mob Targeting`: keep hostile mobs from staying locked onto the player
- `Prevent Outgoing Damage`: stop protected players from attacking
- `Show Messages`: show start, countdown, and end notices
- `Message Mode`: show notices in chat, the action bar, or both

## Default behavior

Out of the box, the mod uses a fairly conservative setup:

- protection lasts **60 seconds**
- protection starts when a player **joins**
- moving ends protection early
- hostile mob targeting is cleared
- outgoing damage is blocked
- messages appear in both chat and the action bar
