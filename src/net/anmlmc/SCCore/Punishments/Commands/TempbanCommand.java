package net.anmlmc.SCCore.Punishments.Commands;

import net.anmlmc.SCCore.Main;
import net.anmlmc.SCCore.Punishments.Punishment;
import net.anmlmc.SCCore.Punishments.PunishmentManager;
import net.anmlmc.SCCore.Punishments.PunishmentType;
import net.anmlmc.SCCore.SCPlayer.SCPlayer;
import net.anmlmc.SCCore.SCPlayer.SCPlayerManager;
import net.anmlmc.SCCore.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * Created by Anml on 1/7/16.
 */
public class TempbanCommand implements CommandExecutor {

    private Main instance;
    private SCPlayerManager scPlayerManager;
    private PunishmentManager punishmentManager;
    private Utils utils;

    public TempbanCommand(Main instance) {
        this.instance = instance;
        scPlayerManager = instance.getSCPlayerManager();
        punishmentManager = instance.getPunishmentManager();
        utils = instance.getUtils();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {

        if (!sender.hasPermission("sccore.tempban")) {
            sender.sendMessage("§cYou do not have permission to execute this command.");
            return false;
        }

        String usage = "§4Usage: §c/tempban <player> <length> <reason>";

        if (args.length < 3) {
            sender.sendMessage(usage);
            return false;
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);

        if (offlinePlayer == null) {
            sender.sendMessage("§cNo player with the given name found.");
            return false;
        }

        SCPlayer scPlayer = scPlayerManager.getSCPlayer(offlinePlayer.getUniqueId());

        List<Punishment> punishments = punishmentManager.getPunishments(offlinePlayer.getUniqueId());

        for (Punishment punishment : punishments) {
            if (punishment.getType().equals(PunishmentType.BAN) || punishment.getType().equals(PunishmentType.TEMPBAN)) {
                if (!punishment.hasExpired()) {
                    sender.sendMessage("§cThe target player is already banned.");
                    return false;
                }
            }
        }

        long length = utils.longLength(args[1]);

        if (length == 0) {
            sender.sendMessage("§cYou must enter a correct length.");
            return false;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            if (i != args.length - 1)
                sb.append(args[i] + " ");
            else
                sb.append(args[i]);
        }

        String reason = sb.toString();
        UUID creator = (sender instanceof Player) ? ((Player) sender).getUniqueId() : null;

        Punishment tempban = new Punishment(PunishmentType.TEMPBAN, offlinePlayer.getUniqueId(), creator, length, reason);
        punishmentManager.addPunishment(tempban);

        String sName = creator == null ? "§6Console" : scPlayerManager.getSCPlayer(creator).getTag();
        scPlayerManager.staff("§9[STAFF] " + sName + " §7has globally temp-banned " + scPlayer.getTag() + " §7for §3" + utils.actualLength(args[1]) + " §7with reason: §a" + reason + "§7.");

        if (offlinePlayer.isOnline()) {
            offlinePlayer.getPlayer().kickPlayer(tempban.getMessage());
        }

        return true;
    }
}
