# Code structure

## `PatriaProtect`

Main plugin class. It owns the services and controls startup/shutdown.

Services held by the main class:

```text
Settings
GuardBridge
IntegrationBridge
ClaimService
Gui
Borders
PatriaProtectAPI
AuditStore
MariaDbSync
ClanService
BedrockForms
```

This keeps the Bukkit entry point small and lets the actual claim logic live in dedicated classes.

## `Claim`

Main claim data object.

Stored fields include:

```text
id
world UUID + world name
owner UUID + last owner name
core x/y/z
Bounds
type
core material
created timestamp
custom name
clan id
members
rules
subregions
```

Permission-related methods include:

```java
canView(UUID player)
isMemberOrOwner(UUID player)
allowed(UUID player, MemberAction action)
denied(UUID player, MemberAction action)
ruleAllows(String flag, UUID player, boolean defaultValue)
ruleAllowsAt(String flag, UUID player, boolean defaultValue, int x, int y, int z)
```

`ruleAllowsAt` is what lets subregion rules participate in the normal claim permission flow.

## `Bounds`

Immutable 3D bounds record.

```java
contains(int x, int y, int z)
intersects(Bounds other)
width()
depth()
```

It is used when creating claims, finding overlaps and defining subregions.

## `ClaimIndex`

Fast in-memory lookup layer.

```text
Map<String, Claim> claims
Map<ChunkKey, Set<String>> chunks
Map<BlockKey, String> cores
```

Main operations:

```java
put(Claim claim)
remove(String id)
at(UUID world, int x, int y, int z)
core(UUID world, int x, int y, int z)
intersecting(UUID world, Bounds bounds)
```

This avoids scanning every claim for normal gameplay events.

## `ClaimStore`

Persistence layer. Each claim can be written separately, which makes it easier to update one protection without rewriting all of them.

```java
load()
save(Claim claim)
delete(Claim claim)
restoreBackup(String id)
raw(String id)
writeRaw(String id, String yaml)
```

The raw methods are also useful to the optional database sync layer.

## `ClaimService`

This is the main business-logic layer.

Responsibilities:

```text
load/reload claims
restore WorldGuard regions on world load
validate stored claims
create claims
calculate player limits
upgrade claims
rollback changes
add/remove members
change roles
create subregions
delete claims
```

Changes pass through this layer so persistence, indexes, WorldGuard and sync can stay consistent.

The update path uses a consumer-style mutation pattern conceptually like:

```java
claimService.update(player, claim, "change-name", copy -> {
    copy.name = newName;
});
```

The real implementation additionally handles validation, storage, indexing, audit/sync and rollback safety.

## `ProtectionListener`

Contains the Bukkit event enforcement. It has small helpers that resolve the claim and check a `MemberAction`, then the event methods reuse that logic.

The listener handles much more than block place/break. See [PERMISSIONS_AND_EVENTS.md](PERMISSIONS_AND_EVENTS.md) for the event groups.

## `Gui`

Inventory GUI controller.

Main pages/methods:

```java
list(player, page)
main(player, claimId)
upgrades(player, claimId)
members(player, claimId, page)
addPlayers(player, claimId, page)
member(player, claimId, memberUuid)
flags(player, claimId, page)
flag(player, claimId, flagId)
```

Click/drag events are validated by this class so players cannot move GUI items around as normal inventory items.

## `BedrockForms`

Optional Floodgate/Bedrock UI bridge.

```java
reload()
isBedrock(Player player)
showClaim(Player player, Claim claim)
```

If the Bedrock API is not available, normal Java behavior can still work because this integration is optional.

## `Borders`

Shows temporary particle borders to one player.

It keeps a view map by UUID and updates the rendered border on a scheduled task. Views automatically stop instead of creating permanent armor stands/entities around every protection.

## `FlagSpec`

Defines the known configurable flags with:

```text
id
label
icon
globalOnly
```

The actual state/group is stored in `Claim.Rule`.

## `IntegrationBridge`

Keeps optional integrations isolated from the claim code. The current economy integration resolves Vault dynamically and exposes:

```java
charge(Player player, double amount)
refund(Player player, double amount)
status()
```

## `GuardBridge`

WorldGuard-specific code.

```java
registerFlags()
checkSpace(...)
createOrRestore(Claim claim)
sync(Claim claim)
remove(Claim claim)
allowed(Player player, Location location, String flag)
emergencyLock()
```

## `MariaDbSync`

Optional shared-server synchronization.

```java
start()
stop()
push(Claim claim)
remove(String claimId)
poll()
```

Database work is separated from the local `ClaimStore`, so the plugin can still run normally with database sync disabled.

## Clans

`Clan`, `ClanStore` and `ClanService` make up the clan part.

A clan stores its owner, members and officers. `ClanService` handles create/add/remove operations and can bind a claim to a clan.
