package net.anmlmc.SCCore.Duels;

import net.anmlmc.SCCore.Main;
import org.bukkit.Location;

/**
 * Created by Anml on 1/3/16.
 */
public class ArenaManager {

    private Main instance;
    private Arena arena;

    public ArenaManager(Main instance) {
        this.instance = instance;
        arena = new Arena(instance);
    }

    public Arena getArena() {
        return arena;
    }

    public boolean insideBorders(Location location) {

        Location primaryCorner = arena.getLocation(ArenaLocationType.PrimaryCorner);
        Location secondaryCorner = arena.getLocation(ArenaLocationType.SecondaryCorner);

        if (primaryCorner == null || secondaryCorner == null)
            return false;

        int x1 = Math.min(primaryCorner.getBlockX(), secondaryCorner.getBlockX());
        int y1 = Math.min(primaryCorner.getBlockY(), secondaryCorner.getBlockY());
        int z1 = Math.min(primaryCorner.getBlockZ(), secondaryCorner.getBlockZ());
        int x2 = Math.max(primaryCorner.getBlockX(), secondaryCorner.getBlockX());
        int y2 = Math.max(primaryCorner.getBlockY(), secondaryCorner.getBlockY());
        int z2 = Math.max(primaryCorner.getBlockZ(), secondaryCorner.getBlockZ());
        Location primary = new Location(primaryCorner.getWorld(), x1, y1, z1);
        Location secondary = new Location(primaryCorner.getWorld(), x2, y2, z2);

        return location.getBlockX() >= primary.getBlockX() && location.getBlockX() <= secondary.getBlockX()
                && location.getBlockY() >= primary.getBlockY() && location.getBlockY() <= secondary.getBlockY()
                && location.getBlockZ() >= primary.getBlockZ() && location.getBlockZ() <= secondary.getBlockZ();
    }
}
