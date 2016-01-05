package net.anmlmc.SCCore.Duels.Commands;

import net.anmlmc.SCCore.Duels.Arena;
import net.anmlmc.SCCore.Duels.ArenaLocationType;
import net.anmlmc.SCCore.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Created by Anml on 1/3/16.
 */
public class SpectateCommand implements CommandExecutor {

    private Main instance;
    private Arena arena;

    public SpectateCommand(Main instance) {
        this.instance = instance;
        arena = instance.getArenaManager().getArena();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {


        if (!(sender instanceof Player)) {
            sender.sendMessage("§cYou must be a player to execute this command.");
            return false;
        }

        Player player = (Player) sender;

        if (!sender.hasPermission("sccore.spectate")) {
            sender.sendMessage("§cYou do not have permission to execute this command.");
            return false;
        }

        if (!arena.isValidLocation(ArenaLocationType.Spectate)) {
            sender.sendMessage("§cThere is no location set for the type §eSpectate§c.");
            return false;
        }

        player.teleport(arena.getLocation(ArenaLocationType.Spectate), PlayerTeleportEvent.TeleportCause.PLUGIN);
        sender.sendMessage("§aYou have been teleported to the spectate location of the duel arena.");
        return true;
    }
}
