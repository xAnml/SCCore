package net.anmlmc.SCCore.HelpRequests;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Created by Anml on 12/21/15.
 */

public class HelpList implements CommandExecutor {

    private final HelpRequest helpRequest;

    public HelpList(final HelpRequest helpRequest) {
        this.helpRequest = helpRequest;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String command, final String[] args) {

        if (!sender.hasPermission("helprequest.list")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
            return false;
        }

        final StringBuilder sb = new StringBuilder().append("\n").append(ChatColor.GRAY).append("Current Help requests:\n");
        for (final Integer i : this.helpRequest.getRequests().keySet())
            sb.append(ChatColor.GREEN).append(i).append(". ").append(ChatColor.RESET).append(this.helpRequest.getRequests().get(i)).append("\n");

        sender.sendMessage(sb.toString());
        return false;
    }

}