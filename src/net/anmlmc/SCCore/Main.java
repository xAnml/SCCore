package net.anmlmc.SCCore;

import com.earth2me.essentials.Essentials;
import net.anmlmc.SCCore.Chat.Commands.ShoutCommand;
import net.anmlmc.SCCore.Duels.ArenaManager;
import net.anmlmc.SCCore.Duels.Commands.ArenaCommand;
import net.anmlmc.SCCore.Duels.Commands.DuelCommand;
import net.anmlmc.SCCore.Duels.DuelListeners;
import net.anmlmc.SCCore.Lockpicks.LockpickListeners;
import net.anmlmc.SCCore.MySQL.MySQL;
import net.anmlmc.SCCore.Punishments.Commands.*;
import net.anmlmc.SCCore.Punishments.PunishmentListeners;
import net.anmlmc.SCCore.Punishments.PunishmentManager;
import net.anmlmc.SCCore.Ranks.Commands.PermsCommand;
import net.anmlmc.SCCore.Ranks.Commands.RankCommand;
import net.anmlmc.SCCore.Ranks.PermissionsManager;
import net.anmlmc.SCCore.Ranks.RankManager;
import net.anmlmc.SCCore.SCPlayer.SCPlayerManager;
import net.anmlmc.SCCore.Stats.StatListeners;
import net.anmlmc.SCCore.Stats.StatsManager;
import net.anmlmc.SCCore.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Anml on 12/26/15.
 */

public class Main extends JavaPlugin implements Listener {

    public static Main instance;
    private Essentials essentials;
    private SCPlayerManager scPlayerManager;
    private ArenaManager arenaManager;
    private RankManager rankManager;
    private StatsManager statsManager;
    private PermissionsManager permissionsManager;
    private Utils utils;
    private MySQL mySQL;
    private PunishmentManager punishmentManager;

    public static Main getInstance() {
        return instance;
    }

    public MySQL getMySQL() {
        return mySQL;
    }

    public RankManager getRankManager() {
        return rankManager;
    }

    public PermissionsManager getPermissionsManager() {
        return permissionsManager;
    }

    public SCPlayerManager getSCPlayerManager() {
        return scPlayerManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }

    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }

    public Utils getUtils() {
        return utils;
    }

    public Essentials getEssentials() {
        return essentials;
    }

    @Override
    public void onEnable() {

        instance = this;
        saveDefaultConfig();

        essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        if (essentials == null || !essentials.isEnabled()) {
            this.getLogger().info("Essentials was not found on the server, resulting in the server shutting down.");
            this.getServer().shutdown();
        }

        registerManagers();
        registerEvents();
        registerCommands();

        scPlayerManager.loadSCPlayers();

        this.getLogger().info("[SCCore] Plugin has been enabled.");
    }

    @Override
    public void onDisable() {

        instance = null;

        this.getLogger().info("[SCCore] Plugin has been disabled.");

    }

    public void registerEvents() {
        PluginManager pm = Bukkit.getServer().getPluginManager();

        pm.registerEvents(scPlayerManager, this);
        pm.registerEvents(this, this);
        pm.registerEvents(new LockpickListeners(this), this);
        pm.registerEvents(new DuelListeners(this), this);
        pm.registerEvents(new StatListeners(this), this);
        pm.registerEvents(new PunishmentListeners(this), this);
    }

    public void registerCommands() {
        getCommand("rank").setExecutor(new RankCommand(this));
        getCommand("shout").setExecutor(new ShoutCommand(this));
        getCommand("perms").setExecutor(new PermsCommand(this));
        getCommand("arena").setExecutor(new ArenaCommand(this));
        getCommand("duel").setExecutor(new DuelCommand(this));
        getCommand("ban").setExecutor(new BanCommand(this));
        getCommand("tempban").setExecutor(new TempbanCommand(this));
        getCommand("mute").setExecutor(new MuteCommand(this));
        getCommand("tempmute").setExecutor(new TempmuteCommand(this));
        getCommand("unban").setExecutor(new UnbanCommand(this));
        getCommand("unmute").setExecutor(new UnmuteCommand(this));
        getCommand("kick").setExecutor(new KickCommand(this));
        getCommand("warn").setExecutor(new WarnCommand(this));

    }

    public void registerManagers() {
        mySQL = new MySQL(this);
        rankManager = new RankManager(this);
        permissionsManager = new PermissionsManager(this);
        statsManager = new StatsManager(this);
        punishmentManager = new PunishmentManager(this);
        scPlayerManager = new SCPlayerManager(this);
        arenaManager = new ArenaManager(this);
        utils = new Utils();
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        event.setMotd("                §c§lSensation§4§lCraft");
    }
}