package net.anmlmc.SCCore.HelpRequests;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Anml on 12/21/15.
 */

public class HelpRequest implements CommandExecutor {

    private final Map<Integer, String> requests = new TreeMap<Integer, String>();
    private int counter = 1;

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String command, final String[] args) {

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "You must enter a reason on why you are in need of help.");
            return false;
        }

        if (this.hasRequest(sender.getName())) {
            sender.sendMessage(ChatColor.RED + "You have previously submitted a help request. Please wait until a response is given.");
            return false;
        }

        final String message = sender.getName() + ": " + this.translate(args);
        this.requests.put(this.counter, message);

        //TODO

        this.counter++;
        sender.sendMessage(ChatColor.GREEN + "Your help request has been sent to the staff members. Please wait until a response is given.");
        return true;
    }

    private String translate(final String[] args) {
        String message = "";
        for (final String arg : args)
            message += arg.concat(" ");
        message = message.trim();
        message = ChatColor.stripColor(message);
        return message;
    }

    public boolean hasRequest(final String name) {
        for (final Integer i : this.requests.keySet()) {
            final String string = this.requests.get(i);
            if (string.split("[:]")[0].equals(name)) return true;
        }
        return false;
    }

    public Map<Integer, String> getRequests() {
        return this.requests;
    }

    public String removeRequest(final String name) {
        for (final Integer i : this.requests.keySet()) {
            final String string = this.requests.get(i);
            if (string.split("[:]")[0].equals(name))
                return this.requests.remove(i);
        }
        return null;
    }

}