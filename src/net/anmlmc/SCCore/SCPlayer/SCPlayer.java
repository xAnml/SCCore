package net.anmlmc.SCCore.SCPlayer;

import net.anmlmc.SCCore.Main;
import net.anmlmc.SCCore.Ranks.RankManager;
import net.anmlmc.SCCore.Stats.Stat;
import net.anmlmc.SCCore.Stats.StatsManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Created by kishanpatel on 12/6/15.
 */

public class SCPlayer {

    private Main instance;
    private UUID uuid;
    private SCPlayerManager scPlayerManager;
    private RankManager rankManager;
    private StatsManager statsManager;
    private Map<UUID, BukkitRunnable> duelRequests;
    private boolean combatTagged;
    private BukkitTask combatTask;

    public SCPlayer(Main instance, UUID uuid) {
        this.instance = instance;
        this.uuid = uuid;
        scPlayerManager = instance.getSCPlayerManager();
        rankManager = instance.getRankManager();
        statsManager = instance.getStatsManager();
        duelRequests = new HashMap<>();
        combatTagged = false;
    }

    public boolean isShoutCooldowned() {
        return scPlayerManager.getShoutCooldowns().contains(uuid);
    }

    public void shoutCooldown() {
        if (!isShoutCooldowned()) {
            scPlayerManager.getShoutCooldowns().add(uuid);

            instance.getServer().getScheduler().scheduleSyncDelayedTask(instance, new Runnable() {
                public void run() {
                    if (isShoutCooldowned())
                        scPlayerManager.getShoutCooldowns().remove(uuid);
                }
            }, 300L);
        }
    }

    public String getTag() {
        return rankManager.getRank(uuid).getTag().replace("%s", Bukkit.getOfflinePlayer(uuid).getName());
    }

    public List<String> getHoverText() {
        return Arrays.asList(
                "§bStats:",
                "   §aKills: §f" + statsManager.getIntegerStat(uuid, Stat.KILLS),
                "   §aDeaths: §f" + statsManager.getIntegerStat(uuid, Stat.DEATHS),
                "   §aK/D: §f" + +statsManager.getKD(uuid),
                "§bInfo:",
                "   §aFaction: §f", // ((getFaction().isNone() || getFaction() == null) ? "Wilderness" : getFaction().getName();
                "   §aPower: §f",
                "§bDuels:",
                "   §aWins: §f" + statsManager.getIntegerStat(uuid, Stat.WINS),
                "   §aLosses: §f" + statsManager.getIntegerStat(uuid, Stat.LOSSES),
                "   §aW/L: §f" + statsManager.getWL(uuid));
    }


    public boolean isLockpicking() {
        return scPlayerManager.getLockpicking().containsKey(uuid);
    }

    public boolean lockpickAttempt() {

        int random = (int) (Math.random() * 100) + 1;
        return random <= rankManager.getRank(uuid).getLockpickChance();
    }

    public Map<UUID, BukkitRunnable> getDuelRequests() {
        return duelRequests;
    }

    public void addDuelRequest(UUID target) {

        BukkitRunnable request = new BukkitRunnable() {
            @Override
            public void run() {
                duelRequests.remove(target);
            }
        };

        duelRequests.put(target, request);
        request.runTaskLater(instance, 6000L);
    }

    public void removeDuelRequest(UUID target) {
        if (!duelRequests.containsKey(target))
            return;

        BukkitRunnable task = duelRequests.get(target);
        if (task != null)
            task.cancel();

        duelRequests.remove(target);
    }

    public boolean isCombatTagged() {
        return combatTagged;
    }

    public void combatTag() {
        if (Bukkit.getPlayer(uuid) == null)
            return;

        Player player = Bukkit.getPlayer(uuid);

        if (combatTask != null)
            combatTask.cancel();

        if (!combatTagged) {
            combatTagged = true;
            player.sendMessage("§eYou are now in combat.");
        }

        combatTask = new BukkitRunnable() {

            @Override
            public void run() {
                removeCombatTag();
            }

        }.runTaskLater(instance, 160L);

    }

    public void removeCombatTag() {
        combatTagged = false;

        if (combatTask != null && Bukkit.getPlayer(uuid) != null) {
            combatTask.cancel();
            combatTask = null;
            Bukkit.getPlayer(uuid).sendMessage("§eYou have left combat.");
        }
    }

}

