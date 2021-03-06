package net.anmlmc.SCCore.Duels;

import net.anmlmc.SCCore.Main;
import net.anmlmc.SCCore.SCPlayer.SCPlayer;
import net.anmlmc.SCCore.SCPlayer.SCPlayerManager;
import net.anmlmc.SCCore.Stats.Stat;
import net.anmlmc.SCCore.Stats.StatsManager;
import net.anmlmc.SCCore.Utils.Fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anml on 12/31/15.
 */
public class Arena {

    private Main instance;
    private FileConfiguration config;
    private SCPlayerManager scPlayerManager;
    private StatsManager statsManager;
    private Player primaryPlayer, secondaryPlayer;
    private Location primaryPlayerLocation, secondaryPlayerLocation;
    private boolean running;
    private BukkitTask task;


    public Arena(Main instance) {
        this.instance = instance;
        config = instance.getConfig();
        scPlayerManager = instance.getSCPlayerManager();
        statsManager = instance.getStatsManager();
        running = false;
    }

    public Location getLocation(ArenaLocationType type) {
        String path = "Duels.Arena." + type.name();
        if (!config.contains(path)) {
            return null;
        } else {
            String[] loc = config.getString(path).split(",");
            try {
                World w = Bukkit.getWorld(loc[0]);
                Double x = Double.parseDouble(loc[1]);
                Double y = Double.parseDouble(loc[2]);
                Double z = Double.parseDouble(loc[3]);
                float yaw = Float.parseFloat(loc[4]);
                float pitch = Float.parseFloat(loc[5]);
                Location location = new Location(w, x, y, z, yaw, pitch);
                return location;
            } catch (Exception e) {
                return null;
            }
        }
    }

    public boolean allValidLocations() {
        for (ArenaLocationType type : ArenaLocationType.values()) {
            if (!isValidLocation(type)) return false;
        }
        return true;
    }

    public boolean isValidLocation(ArenaLocationType type) {
        return getLocation(type) != null;
    }

    public World getWorld() {
        for (ArenaLocationType type : ArenaLocationType.values()) {
            if (getLocation(type) != null) return getLocation(type).getWorld();
        }
        return null;
    }

    public void setLocation(ArenaLocationType type, Location loc) {
        String path = "Duels.Arena." + type.name();

        if (loc == null) {
            config.set(path, null);
            instance.saveConfig();
            return;
        }

        String location = loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
        config.set(path, location);
        instance.saveConfig();
    }

    public boolean isRunning() {
        return running;
    }

    public void startMatch(Player primaryPlayer, Player secondaryPlayer) {

        this.primaryPlayer = primaryPlayer;
        primaryPlayerLocation = primaryPlayer.getLocation();
        this.secondaryPlayer = secondaryPlayer;
        secondaryPlayerLocation = secondaryPlayer.getLocation();

        if (!primaryPlayer.teleport(getLocation(ArenaLocationType.PrimarySpawn), PlayerTeleportEvent.TeleportCause.PLUGIN)) {
            this.forceEnd();
            return;
        }
        if (!secondaryPlayer.teleport(getLocation(ArenaLocationType.SecondarySpawn), PlayerTeleportEvent.TeleportCause.PLUGIN)) {
            this.forceEnd();
            return;
        }

        SCPlayer primary = scPlayerManager.getSCPlayer(primaryPlayer.getUniqueId());
        SCPlayer secondary = scPlayerManager.getSCPlayer(secondaryPlayer.getUniqueId());

        running = true;

        FancyMessage message = new FancyMessage(primary.getTag()).tooltip(primary.getHoverText()).then(" §6and ")
                .then(secondary.getTag()).tooltip(secondary.getHoverText()).then(" §6are now dueling! '/spectate' to " +
                        "spectate the battle!");
        scPlayerManager.broadcast(message);

        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                forceEnd();
            }
        }.runTaskLater(instance, 6000L);
    }

    protected void endMatch(final Player loser) {
        if (task != null) {
            task.cancel();
            task = null;
        }

        SCPlayer scLoser = scPlayerManager.getSCPlayer(loser.getUniqueId());
        Player winner = loser.equals(primaryPlayer) ? secondaryPlayer : primaryPlayer;
        SCPlayer scWinner = scPlayerManager.getSCPlayer(winner.getUniqueId());

        statsManager.setIntegerStat(winner.getUniqueId(), Stat.WINS, statsManager.getIntegerStat(winner.getUniqueId(), Stat.WINS) + 1);
        statsManager.setIntegerStat(loser.getUniqueId(), Stat.KILLS, statsManager.getIntegerStat(loser.getUniqueId(), Stat.LOSSES) + 1);

        FancyMessage message = new FancyMessage(scWinner.getTag()).tooltip(scWinner.getHoverText()).then(" §6has " +
                "beaten ").then(scLoser.getTag()).tooltip(scLoser.getHoverText()).then(" §6in the duel arena!");
        scPlayerManager.broadcast(message);
        scWinner.removeCombatTag();
        scLoser.removeCombatTag();
        winner.sendMessage("§aYou have 10 seconds to collect the dropped items.");

        new BukkitRunnable() {
            @Override
            public void run() {

                Location location = winner.getName().equalsIgnoreCase(primaryPlayer.getName()) ? primaryPlayerLocation : secondaryPlayerLocation;
                winner.teleport(location);

                reset();
            }
        }.runTaskLater(instance, 200L);
    }

    public void forceEnd() {
        if (task != null) {
            task.cancel();
            task = null;
        }

        SCPlayer primary = scPlayerManager.getSCPlayer(primaryPlayer.getUniqueId());
        SCPlayer secondary = scPlayerManager.getSCPlayer(secondaryPlayer.getUniqueId());

        FancyMessage message = new FancyMessage("§6The duel between ").then(primary.getTag()).tooltip(primary
                .getHoverText()).then(" and ").then(secondary.getTag()).tooltip(secondary.getHoverText()).then(
                "§6ended in a draw!");

        primary.removeCombatTag();
        secondary.removeCombatTag();
        primaryPlayer.teleport(primaryPlayerLocation);
        secondaryPlayer.teleport(secondaryPlayerLocation);

        reset();
    }

    public List<Player> getArenaPlayers() {
        List<Player> players = new ArrayList<>();
        if (primaryPlayer != null)
            players.add(primaryPlayer);
        if (secondaryPlayer != null)
            players.add(secondaryPlayer);

        return players;
    }

    public void reset() {
        primaryPlayer = null;
        secondaryPlayer = null;
        primaryPlayerLocation = null;
        secondaryPlayerLocation = null;

        running = false;
    }


}
