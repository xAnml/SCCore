package net.anmlmc.SCCore.SCPlayer.Commands;

import net.anmlmc.SCCore.Main;
import net.anmlmc.SCCore.SCPlayer.SCPlayer;
import net.anmlmc.SCCore.SCPlayer.SCPlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Anml on 1/3/16.
 */
public class CombatTimeCommand implements CommandExecutor {

    private Main instance;
    private SCPlayerManager scPlayerManager;

    public CombatTimeCommand(Main instance) {
        this.instance = instance;
        scPlayerManager = instance.getSCPlayerManager();
        instance.getSCPlayerManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {


        if (!(sender instanceof Player)) {
            sender.sendMessage("§cYou must be a player to execute this command.");
            return false;
        }

        if (!sender.hasPermission("sccore.combattime")) {
            sender.sendMessage("§cYou do not have permission to execute this command.");
            return false;
        }

        Player player = (Player) sender;
        SCPlayer scPlayer = scPlayerManager.getSCPlayer(player);
        long time = scPlayer.getCombatTime();

        if (time == 0) {
            sender.sendMessage("§cYou are not currently in combat.");
            return false;
        }

        sender.sendMessage("§aYou are in combat for §e" + time + "§a more second(s).");
        return true;

    }
}