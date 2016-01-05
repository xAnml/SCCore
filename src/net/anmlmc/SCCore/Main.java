package net.anmlmc.SCCore;

import com.earth2me.essentials.Essentials;
import net.anmlmc.SCCore.Chat.Commands.ShoutCommand;
import net.anmlmc.SCCore.Duels.ArenaManager;
import net.anmlmc.SCCore.Duels.Commands.ArenaCommand;
import net.anmlmc.SCCore.Duels.Commands.DuelCommand;
import net.anmlmc.SCCore.Duels.Commands.SpectateCommand;
import net.anmlmc.SCCore.Duels.DuelListeners;
import net.anmlmc.SCCore.Lockpicks.LockpickListeners;
import net.anmlmc.SCCore.MySQL.MySQL;
import net.anmlmc.SCCore.Ranks.Commands.PermsCommand;
import net.anmlmc.SCCore.Ranks.Commands.RankCommand;
import net.anmlmc.SCCore.SCPlayer.Commands.CombatTimeCommand;
import net.anmlmc.SCCore.SCPlayer.SCPlayerManager;
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
    private MySQL mySQL;

    public static Main getInstance() {
        return instance;
    }

    public MySQL getMySQL() {
        return mySQL;
    }

    public SCPlayerManager getSCPlayerManager() {
        return scPlayerManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
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

        this.getLogger().info("[SCGeneral] Plugin has been enabled.");
    }

    @Override
    public void onDisable() {

        instance = null;

    }

    public void registerEvents() {
        PluginManager pm = Bukkit.getServer().getPluginManager();

        pm.registerEvents(scPlayerManager, this);
        pm.registerEvents(this, this);
        pm.registerEvents(new LockpickListeners(this), this);
        pm.registerEvents(new DuelListeners(this), this);
    }

    public void registerCommands() {
        getCommand("rank").setExecutor(new RankCommand(this));
        getCommand("shout").setExecutor(new ShoutCommand(this));
        getCommand("perms").setExecutor(new PermsCommand(this));
        getCommand("arena").setExecutor(new ArenaCommand(this));
        getCommand("duel").setExecutor(new DuelCommand(this));
        getCommand("spectate").setExecutor(new SpectateCommand(this));
        getCommand("combattime").setExecutor(new CombatTimeCommand(this));

    }

    public void registerManagers() {
        mySQL = new MySQL(this);
        scPlayerManager = new SCPlayerManager(this);
        arenaManager = new ArenaManager(this);
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        event.setMotd("                §c§lSensation§4§lCraft\n             §a§lCatBlocker 2.0 Installed");
    }
}