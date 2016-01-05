package net.anmlmc.SCCore.Ranks.Commands;

import com.earth2me.essentials.User;
import net.anmlmc.SCCore.Main;
import net.anmlmc.SCCore.Ranks.Rank;
import net.anmlmc.SCCore.SCPlayer.SCPlayer;
import net.anmlmc.SCCore.SCPlayer.SCPlayerManager;
import net.anmlmc.SCCore.Utils.Fanciful.FancyMessage;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Created by Anml on 12/29/15.
 */
public class PermsCommand implements CommandExecutor {

    Main instance;
    SCPlayerManager scPlayerManager;

    public PermsCommand(Main instance) {
        this.instance = instance;
        scPlayerManager = instance.getSCPlayerManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {

        if (!sender.hasPermission("sccore.perms")) {
            sender.sendMessage("§cYou do not have permission to execute this command.");
            return false;
        }

        String usage = "§4Usage: " +
                "§c/perms r:add <rank> <node>\n" +
                "         /perms r:remove <rank>\n" +
                "         /perms r:get <rank>\n" +
                "         /perms p:add <player> <node>\n" +
                "         /perms p:remove <player> <node>\n" +
                "         /perms p:get <player>";

        if (args.length == 0 || args.length < getMinArgs(args[0])) {
            sender.sendMessage(usage);
            return false;
        }

        if (args[0].equalsIgnoreCase("p:get") || args[0].equalsIgnoreCase("p:add") || args[0].equalsIgnoreCase
                ("p:remove")) {

            OfflinePlayer player = instance.getServer().getOfflinePlayer(args[1]);

            if (player != null) {
                User user = instance.getEssentials().getOfflineUser(args[1]);

                if (user == null) {
                    sender.sendMessage("§cNo player with the given name found.");
                    return false;
                }

                SCPlayer scPlayer;
                if (user.getBase().isOnline()) {
                    scPlayer = scPlayerManager.getSCPlayer(user.getBase());
                } else {
                    scPlayer = new SCPlayer(user.getBase());
                }

                if (args[0].equalsIgnoreCase("p:add")) {
                    boolean task = scPlayer.addPersonalPermission(args[2]);

                    if (task) {
                        FancyMessage message = new FancyMessage("§aThe permission node '" + args[2] + "' has been added " +
                                "to ").then(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then("§a.");
                        message.send(sender);

                        instance.getLogger().info(sender.getName() + " has added the permission node '" + args[2] +
                                "' to the player " + scPlayer.getBase().getName() + ".");
                        return true;
                    } else {
                        FancyMessage message = new FancyMessage("§cThe player ").then(scPlayer.getTag()).tooltip
                                (scPlayer.getHoverText()).then(" §calready has the permission node '" + args[2] + "'.");
                        message.send(sender);
                        return false;
                    }
                } else if (args[0].equalsIgnoreCase("p:remove")) {
                    boolean task = scPlayer.removePersonalPermission(args[2]);

                    if (task) {
                        FancyMessage message = new FancyMessage("§aThe permission node '" + args[2] + "' has been " +
                                "removed from ").then(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then("§a.");
                        message.send(sender);

                        instance.getLogger().info(sender.getName() + " has removed the permission node '" + args[2] +
                                "' from the player " + scPlayer.getBase().getName() + ".");
                        return true;
                    } else {
                        FancyMessage message = new FancyMessage(scPlayer.getTag()).tooltip(scPlayer.getHoverText())
                                .then(" §cdoesn't have the permission node '" + args[2] + "'.");
                        message.send(sender);
                        return false;
                    }

                } else {
                    List<String> permissions = scPlayer.getPersonalPermissions();

                    if (permissions.isEmpty()) {
                        FancyMessage message = new FancyMessage("§cThe player ").then(scPlayer.getTag()).tooltip
                                (scPlayer
                                        .getHoverText())
                                .then(" §chas no personal permissions.");
                        message.send(sender);
                        return false;
                    } else {
                        FancyMessage message = new FancyMessage(scPlayer.getTag()).tooltip(scPlayer.getHoverText())
                                .then("§a's Personal Permissions:");
                        message.send(sender);

                        for (String permission : permissions) {
                            sender.sendMessage("§f   - §a" + permission);
                        }
                        return true;
                    }
                }
            } else {
                sender.sendMessage("§cNo player with the given name found.");
                return false;
            }
        } else if (args[0].equalsIgnoreCase("r:get") || args[0].equalsIgnoreCase("r:add") || args[0].equalsIgnoreCase
                ("r:remove")) {

            Rank rank = translateRank(args[1]);

            if (rank == null) {
                sender.sendMessage("§cNo rank with the given name found.");
                return false;
            }

            if (args[0].equalsIgnoreCase("r:add")) {
                boolean task = rank.addPermission(args[2]);

                if (task) {
                    sender.sendMessage("§aThe permission node '" + args[2].toLowerCase() + "' has been added to the " +
                            "rank " + rank.getName() + "§a.");

                    instance.getLogger().info(sender.getName() + " has added the permission node '" + args[2] +
                            "' to the rank " + rank.name() + ".");
                    return true;
                } else {
                    sender.sendMessage("§cThe rank " + rank.getName() + " §calready has the permission node '" +
                            args[2].toLowerCase() + "§c'.");
                    return false;
                }
            } else if (args[0].equalsIgnoreCase("r:remove")) {
                boolean task = rank.removePermission(args[2]);

                if (task) {
                    sender.sendMessage("§aThe permission node '" + args[2].toLowerCase() + "' has been removed from " +
                            "the rank " + rank.getName() + "§a.");

                    instance.getLogger().info(sender.getName() + " has removed the permission node '" + args[2] +
                            "' from the rank " + rank.name() + ".");
                    return true;
                } else {
                    sender.sendMessage("§cThe rank " + rank.getName() + " §cdoesnt have the permission node '" +
                            args[2].toLowerCase() + "§c'.");
                    return false;
                }
            } else {
                List<String> permissions = rank.getPermissions();

                if (permissions.isEmpty()) {
                    sender.sendMessage("§cThe rank " + rank.getName() + " §chas no permissions.");
                    return false;
                } else {

                    sender.sendMessage(rank.getName() + "§a's Permissions:");
                    for (String permission : permissions) {
                        sender.sendMessage("§f   - §a" + permission);
                    }
                    return true;
                }

            }

        } else {
            sender.sendMessage(usage);
            return false;
        }
    }

    public Rank translateRank(String name) {
        for (Rank rank : Rank.values()) {
            if (rank.name().equalsIgnoreCase(name) || rank.getAlias().equalsIgnoreCase(name)) {
                return rank;
            }
        }

        return null;
    }

    public int getMinArgs(String subcommand) {
        switch (subcommand) {
            case "p:add":
                return 3;
            case "r:add":
                return 3;
            case "p:remove":
                return 3;
            case "r:remove":
                return 3;
            case "p:get":
                return 2;
            case "r:get":
                return 2;
            default:
                return 100;
        }
    }
}
