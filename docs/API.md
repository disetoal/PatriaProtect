# PatriaProtect API

The plugin exposes a small query API so another plugin does not have to know how claims are indexed or stored.

Class:

```text
bo.patriacraft.protect.PatriaProtectAPI
```

Methods:

```java
Claim claimAt(Location location)
boolean canBuild(Player player, Location location)
boolean canUse(Player player, Location location)
boolean isOwner(Player player, Location location)
UUID owner(Location location)
```

## Example

```java
PatriaProtect protect = (PatriaProtect) Bukkit.getPluginManager()
        .getPlugin("PatriaProtect");

if (protect == null) return;

PatriaProtectAPI api = protect.api();

if (!api.canBuild(player, block.getLocation())) {
    player.sendMessage("You cannot build here.");
    return;
}

Claim claim = api.claimAt(block.getLocation());
if (claim != null) {
    player.sendMessage("Claim: " + claim.id);
}
```

This is an API usage example only. The internal permission checks and claim lookup implementation are not published in this repo.
