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

public class HelpCancel implements CommandExecutor {

    private final HelpRequest helpRequest;

    public HelpCancel(final HelpRequest helpRequest) {
        this.helpRequest = helpRequest;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String command, final String[] args) {

        if (!sender.hasPermission("helprequest.cancel")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
            return false;
        }

        final String helpRequest = this.helpRequest.removeRequest(sender.getName());

        if (helpRequest != null)
            sender.sendMessage(ChatColor.GREEN + "Your help request has been removed.");
        else
            sender.sendMessage(ChatColor.RED + "You currently do not have a submitted help request.");

        for (final Player player : Bukkit.getOnlinePlayers())
            if (player.hasPermission("essentials.getnotified"))
                player.sendMessage(ChatColor.BLUE + "[STAFF] " + ChatColor.AQUA + sender.getName() + ChatColor.GRAY + " has cancelled their help request.");
        return true;
    }

}