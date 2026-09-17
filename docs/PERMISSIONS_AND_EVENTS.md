# Permissions and protected events

## Member roles

Current roles:

```text
MEMBER
BUILDER
TRUSTED
MANAGER
BLOCKED
```

A role decides the normal access level. The `Claim.Member` object can also store an `EnumSet<MemberAction>` with actions that are denied specifically for that player.

That makes it possible to have two members with the same role but different restrictions.

## Member actions

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

The listener resolves these high-level actions from different Bukkit events. This keeps the claim model independent from individual Bukkit event classes.

## Claim rules / flags

A rule stores two values:

```text
state   ALLOW / DENY style state
group   who the rule targets
```

The default config includes rules for things such as:

```text
PVP
TNT
creeper explosions
other explosions
ghast fireballs
Ender Dragon block damage
fire spread
Enderman grief
Ravager grief
mob spawning
villager/trading access
mob interaction
redstone
hoppers
liquids
fire damage
teleport
```

Subregions have their own rule map, so `ruleAllowsAt(...)` can use a more specific rule for a point inside the claim.

## Events checked by `ProtectionListener`

The current listener covers these groups:

### Movement / claim entry

```text
PlayerMoveEvent
PlayerQuitEvent
WorldLoadEvent
```

Movement is used to track the active claim and show enter/leave messages without repeating them every movement tick.

### Blocks and liquids

```text
BlockPlaceEvent
BlockBreakEvent
PlayerBucketEmptyEvent
PlayerBucketFillEvent
BlockIgniteEvent
BlockSpreadEvent
BlockBurnEvent
BlockFadeEvent
BlockPistonExtendEvent
BlockPistonRetractEvent
EntityChangeBlockEvent
```

### Containers and interactions

```text
PlayerInteractEvent
InventoryOpenEvent
PlayerInteractEntityEvent
PlayerInteractAtEntityEvent
PlayerArmorStandManipulateEvent
```

### Entities / combat / vehicles

```text
EntityDamageByEntityEvent
CreatureSpawnEvent
HangingPlaceEvent
HangingBreakByEntityEvent
VehicleEnterEvent
VehicleDamageEvent
ProjectileHitEvent
PlayerFishEvent
```

### Potions

```text
PotionSplashEvent
AreaEffectCloudApplyEvent
```

Harmful effects are treated differently from harmless potion effects so the claim does not need to block every potion interaction equally.

### Explosions

```text
EntityExplodeEvent
BlockExplodeEvent
```

The protection code can filter affected blocks rather than only cancelling the whole explosion globally.

### Items / miscellaneous

```text
PlayerDropItemEvent
EntityPickupItemEvent
EntityDamageEvent
```

The exact decision path depends on the event, claim rule and member action.

## Bypass

Administrative bypass is handled in the main plugin and permission layer rather than duplicated in every event method.

Relevant permissions include:

```text
patriaprotect.admin
patriaprotect.bypass
patriaprotect.unlimited
```
