// Public API usage example only. This is not the PatriaProtect source code.

import bo.patriacraft.protect.Claim;
import bo.patriacraft.protect.PatriaProtect;
import bo.patriacraft.protect.PatriaProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class PatriaProtectApiExample {
    public static boolean canPlayerBuild(Player player, Location location) {
        PatriaProtect plugin = (PatriaProtect) Bukkit.getPluginManager()
                .getPlugin("PatriaProtect");

        if (plugin == null) return true;

        PatriaProtectAPI api = plugin.api();
        Claim claim = api.claimAt(location);

        if (claim == null) return true;
        return api.canBuild(player, location);
    }
}
