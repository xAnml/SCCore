package net.anmlmc.SCCore.HelpRequests;


import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Created by Anml on 12/21/15.
 */

public class HelpRead implements CommandExecutor {

    private final HelpRequest helpRequest;

    public HelpRead(final HelpRequest helpRequest) {
        this.helpRequest = helpRequest;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String command, final String[] args) {

        if (!sender.hasPermission("helprequest.read")) {
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
        final String message = this.helpRequest.getRequests().get(number);

        if (message == null) {
            sender.sendMessage(ChatColor.RED + "The help request you are trying to remove doesn't exist.");
            return false;
        }

        final StringBuilder sb = new StringBuilder("\n").append(ChatColor.GREEN).append(number).append(". ").append(ChatColor.RESET).append(message).append("\n");
        sender.sendMessage(sb.toString());
        return true;
    }

}