package net.anmlmc.SCCore.Chat.Commands;

import net.anmlmc.SCCore.Main;
import net.anmlmc.SCCore.Punishments.PunishmentManager;
import net.anmlmc.SCCore.SCPlayer.SCPlayer;
import net.anmlmc.SCCore.SCPlayer.SCPlayerManager;
import net.anmlmc.SCCore.Utils.Fanciful.FancyMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Anml on 1/7/16.
 */
public class StaffCommand implements CommandExecutor {

    private Main instance;
    private SCPlayerManager scPlayerManager;
    private PunishmentManager punishmentManager;

    public StaffCommand(Main instance) {
        this.instance = instance;
        scPlayerManager = instance.getSCPlayerManager();
        punishmentManager = instance.getPunishmentManager();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {

        if (!sender.hasPermission("sccore.staff")) {
            sender.sendMessage("§cYou do not have permission to execute this command.");
            return false;
        }

        String usage = "§4Usage: §c/staff <message>";

        if (args.length == 0) {
            sender.sendMessage(usage);
            return false;
        }


        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i != args.length - 1)
                sb.append(args[i] + " ");
            else
                sb.append(args[i]);
        }

        boolean hover = sender instanceof Player ? true : false;
        FancyMessage message = new FancyMessage("§9[STAFF] ");

        if (hover) {
            SCPlayer senderSCPlayer = scPlayerManager.getSCPlayer(((Player) sender).getUniqueId());
            message = message.then(senderSCPlayer.getTag()).tooltip(senderSCPlayer.getHoverText()).then("§f: §e§l" + sb);
        } else {
            message = message.then("§6Console").then("§f: §e§l" + sb);
        }

        scPlayerManager.staff(message);

        return true;
    }
}
