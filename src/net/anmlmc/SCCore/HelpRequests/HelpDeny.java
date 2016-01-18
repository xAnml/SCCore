package net.anmlmc.SCCore.HelpRequests;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Anml on 12/21/15.
 */

public class HelpDeny implements CommandExecutor {

    private final HelpRequest helpRequest;

    public HelpDeny(final HelpRequest helpRequest) {
        this.helpRequest = helpRequest;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String command, final String[] args) {

        if (!sender.hasPermission("helprequest.deny")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
            return false;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "You must specify a number.");
            return false;
        }

        if (!args[0].matches("[0-9]*")) {
            sender.sendMessage(ChatColor.RED + "You must specify a number.");
            return false;
        }

        final int number = Integer.parseInt(args[0]);
        final String message = this.helpRequest.getRequests().remove(number);

        if (message == null) {
            sender.sendMessage(ChatColor.RED + "The help request you are trying to remove doesn't exist.");
            return false;
        }

        final String[] split = message.split("[:]", 2);
        final Player player = Bukkit.getPlayer(split[0]);

        if (player == null) {
            sender.sendMessage(ChatColor.RED + "The player seems to be offline, resulting in the request being removed.");
            return false;
        }

        player.sendMessage(ChatColor.AQUA + sender.getName() + ChatColor.GRAY + " has denied your help request.");
        sender.sendMessage(ChatColor.GREEN + "You have denied " + ChatColor.AQUA + player.getName() + ChatColor.GREEN + "'s help request.");

        for (final Player broad : Bukkit.getOnlinePlayers())
            if (broad.hasPermission("essentials.getnotified"))
                broad.sendMessage(ChatColor.BLUE + "[STAFF] " + ChatColor.AQUA + sender.getName() + ChatColor.GRAY + " has accepted help request #" + args[0] + ".");
        return true;
    }

}