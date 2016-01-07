package net.anmlmc.SCCore.Punishments.Commands;

import com.earth2me.essentials.User;
import net.anmlmc.SCCore.Main;
import net.anmlmc.SCCore.Punishments.PunishmentEntry;
import net.anmlmc.SCCore.Punishments.PunishmentType;
import net.anmlmc.SCCore.SCPlayer.SCPlayer;
import net.anmlmc.SCCore.SCPlayer.SCPlayerManager;
import net.anmlmc.SCCore.Utils.Fanciful.FancyMessage;
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
public class BanCommand implements CommandExecutor {

    private Main instance;
    private SCPlayerManager scPlayerManager;

    public BanCommand(Main instance) {
        this.instance = instance;
        scPlayerManager = instance.getSCPlayerManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {

        if (!sender.hasPermission("sccore.ban")) {
            sender.sendMessage("§cYou do not have permission to execute this command.");
            return false;
        }

        String usage = "§4Usage: §c/ban <player> <reason>";

        if (args.length == 0 || args.length < getMinArgs(args[0])) {
            sender.sendMessage(usage);
            return false;
        }

        OfflinePlayer player = instance.getServer().getOfflinePlayer(args[1]);

        if (player != null) {
            User user = instance.getEssentials().getOfflineUser(args[1]);

            if (user == null) {
                sender.sendMessage("§cNo player with the given name found.");
                return false;
            }

            SCPlayer scPlayer = scPlayerManager.getSCPlayer(player.getPlayer());

            List<PunishmentEntry> currentBans = scPlayer.getPunishments(PunishmentType.BAN);

            for (PunishmentEntry b : currentBans) {
                if (!b.hasExpired()) {
                    sender.sendMessage("§cYou are not permitted to ban someone who is already permanently banned.");
                    return false;
                }
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                if (i != args.length - 1)
                    sb.append(args[i] + " ");
                else
                    sb.append(args[i]);
            }

            String reason = sb.toString();
            UUID creator = (sender instanceof Player) ? ((Player) sender).getUniqueId() : null;

            PunishmentEntry ban = new PunishmentEntry(instance, PunishmentType.BAN, player.getUniqueId(), creator,
                    System.currentTimeMillis(), -1, reason);

            List<PunishmentEntry> punishments = scPlayer.getCachedPunishments();
            punishments.add(ban);
            scPlayer.setCachedPunishments(punishments);

            boolean hover = creator != null;

            if (player.isOnline())
                player.getPlayer().kickPlayer("§4You have been permanently banned!\n" + reason + "§4- "
                        + (hover ? scPlayerManager.getSCPlayer((Player) sender).getTag() : "§6Console"));

            FancyMessage message = new FancyMessage("§9[STAFF] ");

            if (hover) {
                SCPlayer senderSCPlayer = scPlayerManager.getSCPlayer((Player) sender);
                message = message.then(senderSCPlayer.getTag()).tooltip(senderSCPlayer.getHoverText()).then(" §7has " +
                        "banned ").then(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then(" §7for §o" +
                        reason + "§7.");
            } else {
                message = message.then("§6Console §7has " +
                        "banned ").then(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then(" §7for §o" +
                        reason + "§7.");
            }

            scPlayerManager.staff(message);
            return true;

        } else {
            sender.sendMessage("§cNo player with the given name found.");
            return false;
        }
    }

    public int getMinArgs(String subcommand) {
        switch (subcommand) {
            case "ban":
                return 2;
            default:
                return 100;
        }
    }
}
