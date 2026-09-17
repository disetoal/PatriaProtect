# PatriaProtect

PatriaProtect is the custom land protection system I made for PatriaCraft. I originally needed something more specific than a basic claim plugin because I wanted different protection sizes, member roles, individual permissions, custom flags, clans, Java/Bedrock menus and the option to sync protections between servers.

The version documented here is `2.0.0` for Paper `1.21.11` / Java 21.

## Main features

- block-based land claims
- several protection sizes/types
- configurable cost and acquisition method per protection type
- owner/member permission system
- roles: member, builder, trusted, manager and blocked
- individual denied actions per member
- protection flags with target groups
- subregions with their own rules
- GUI management
- Bedrock form support
- particle border preview
- clan system and clan protection type
- YAML storage with backups/rollback support
- audit log
- chunk/block claim index for fast lookups
- WorldGuard bridge
- optional Vault economy integration
- optional MariaDB synchronization between servers
- small Java API for other plugins
- protection checks across a large set of Bukkit events

## Code structure

This project is split into multiple classes instead of putting the whole protection system in the main plugin class.

```text
bo.patriacraft.protect
├── PatriaProtect             plugin lifecycle / service access
├── PatriaProtectAPI          public API
├── Settings                  config parsing
├── Claim                     claim data + permission logic
├── Bounds                    3D bounds helper
├── Subregion                 sub-area + rules
├── ClaimService              create/update/delete business logic
├── ClaimStore                YAML persistence + backups
├── ClaimIndex                chunk/core indexes
├── ProtectionListener        Bukkit event enforcement
├── Commands                  /pp command handling
├── Gui                       inventory menus
├── BedrockForms              Bedrock form bridge
├── Borders                   particle borders
├── FlagSpec                  known protection flags
├── IntegrationBridge         Vault/other plugin status
├── GuardBridge               WorldGuard bridge
├── AuditStore                audit log
├── MariaDbSync               optional shared-server sync
├── Clan                      clan data
├── ClanStore                 clan persistence
├── ClanService               clan logic
└── MemberAction              member permission actions
```

The class-by-class explanation is in [docs/CODE_STRUCTURE.md](docs/CODE_STRUCTURE.md).

## Claim lookup

I did not want every block interaction to loop through every protection on the server. `ClaimIndex` keeps multiple indexes:

```text
claim id -> Claim
chunk -> set of claim ids
core block position -> claim id
```

A location lookup first narrows the candidates by chunk and then checks the claim bounds.

That is important because `ProtectionListener` runs on many events and claim checks need to stay cheap.

## Permission model

A claim has:

```text
owner
members
member roles
individual denied actions
global rules
subregion rules
```

Member actions currently include:

```text
BUILD
BREAK
PLACE
CONTAINERS
USE
TRADE
ANIMALS
MOBS
VILLAGERS
PVP
RIDE
SLEEP
IGNITE
DECORATE
```

Roles provide a base level of access, then specific actions can be denied for a member. Flags handle broader rules such as PVP, TNT, explosions, fire spread, hoppers, liquids and mob behavior.

Subregions can override rules inside part of a claim.

See [docs/PERMISSIONS_AND_EVENTS.md](docs/PERMISSIONS_AND_EVENTS.md).

## Storage

Claims are persisted through `ClaimStore`. The store can:

```text
load all claims
save one claim
delete one claim
read/write raw claim files
restore a backup
```

`ClaimService` is the layer that validates and changes claims. The rest of the plugin does not need to directly edit YAML files.

There is also an `AuditStore` that appends administrative/player claim actions to an audit log.

## Optional network sync

`MariaDbSync` can be enabled when more than one server shares the same database.

It can push updated claims, remove deleted ones and periodically poll for remote changes. A `server-id` is stored in config so the sync layer can tell which server is participating.

I kept it disabled by default because a single survival server does not need a database dependency just to use protections.

## GUI and Bedrock

Java players can manage claims through inventory GUIs. The GUI includes pages for claims, upgrades, members, per-member settings, flags and deletion confirmation.

For Bedrock, `BedrockForms` detects Floodgate players and can use a form-based path instead of forcing the normal Java inventory workflow everywhere.

## Protection types

The default config currently has examples such as:

```text
small        10x10
medium       20x20
large        50x50
premium      100x100 / 200x200
clan         200x200
```

Each type can have its own permission, material, price, member limit and acquisition method (`KIT`, `COINS`, `WEB`, `CLAN`, etc.).

## Commands

Main command:

```text
/patriaprotect
/pp
/proteccion
```

The command class also handles admin giving, buying, help and clan operations. Most normal claim management is available from the menus.

## Integrations

The build declares soft support for:

```text
Vault
Essentials
LuckPerms
PlaceholderAPI
TAB
DeluxeMenus
Citizens
Geyser-Spigot
Floodgate
ProtectionStones
```

WorldGuard has its own bridge class because protection regions/flags need more than a simple "is the plugin enabled" check.

## Public API

Other plugins can do basic protection queries without touching internal services directly:

```java
Claim claimAt(Location location)
boolean canBuild(Player player, Location location)
boolean canUse(Player player, Location location)
boolean isOwner(Player player, Location location)
UUID owner(Location location)
```

A safe usage example is in [examples/PatriaProtectApiExample.java](examples/PatriaProtectApiExample.java).

## About the source

The complete source code and jar are private because this is still used on my server. I do not want the public portfolio repo to also be a free copy of the project.

The repository documents the real package/class layout, main methods, data model and flow, and includes small API examples. That gives clients enough to see what I built without exposing the parts that make the plugin reusable as-is.
