package net.anmlmc.SCCore.Punishments.Commands;

import net.anmlmc.SCCore.Main;
import net.anmlmc.SCCore.Punishments.Punishment;
import net.anmlmc.SCCore.Punishments.PunishmentManager;
import net.anmlmc.SCCore.Punishments.PunishmentType;
import net.anmlmc.SCCore.SCPlayer.SCPlayer;
import net.anmlmc.SCCore.SCPlayer.SCPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by Anml on 1/7/16.
 */
public class UnmuteCommand implements CommandExecutor {

    private Main instance;
    private SCPlayerManager iPlayerManager;
    private PunishmentManager punishmentManager;

    public UnmuteCommand(Main instance) {
        this.instance = instance;
        iPlayerManager = instance.getSCPlayerManager();
        punishmentManager = instance.getPunishmentManager();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {

        if (!sender.hasPermission("sccore.unmute")) {
            sender.sendMessage("§cYou do not have permission to execute this command.");
            return false;
        }

        String usage = "§4Usage: §c/unmute <player>";

        if (args.length < 1) {
            sender.sendMessage(usage);
            return false;
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);

        if (offlinePlayer == null) {
            sender.sendMessage("§cNo player with the given name found.");
            return false;
        }

        SCPlayer scPlayer = iPlayerManager.getSCPlayer(offlinePlayer.getUniqueId());

        List<Punishment> punishments = punishmentManager.getPunishments(offlinePlayer.getUniqueId());

        for (Punishment punishment : punishments) {
            if (punishment.getType().equals(PunishmentType.MUTE) || punishment.getType().equals(PunishmentType.TEMPMUTE)) {
                if (!punishment.hasExpired()) {
                    punishment.setExpires(0L);
                    punishment.execute();

                    String sName = !(sender instanceof Player) ? "§6Console" : iPlayerManager.getSCPlayer(((Player) sender).getUniqueId()).getTag();
                    iPlayerManager.staff("§9[STAFF] " + sName + " §7has globally unmuted " + scPlayer.getTag() + " §7.");
                    return true;
                }
            }
        }

        sender.sendMessage("§cThe target player is currently not muted.");
        return false;
    }
}
