package net.anmlmc.SCCore.Duels.Commands;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MFlag;
import com.massivecraft.factions.entity.MPlayerColl;
import net.anmlmc.SCCore.Duels.Arena;
import net.anmlmc.SCCore.Main;
import net.anmlmc.SCCore.SCPlayer.SCPlayer;
import net.anmlmc.SCCore.SCPlayer.SCPlayerManager;
import net.anmlmc.SCCore.Utils.Fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Created by Anml on 1/3/16.
 */
public class DuelCommand implements CommandExecutor {

    private Main instance;
    private SCPlayerManager scPlayerManager;
    private Arena arena;

    public DuelCommand(Main instance) {
        this.instance = instance;
        scPlayerManager = instance.getSCPlayerManager();
        arena = instance.getArenaManager().getArena();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {


        if (!(sender instanceof Player)) {
            sender.sendMessage("§cYou must be a player to execute this command.");
            return false;
        }

        Player player = (Player) sender;
        SCPlayer scPlayer = scPlayerManager.getSCPlayer(player.getUniqueId());

        if (!sender.hasPermission("sccore.duel")) {
            sender.sendMessage("§cYou do not have permission to execute this command.");
            return false;
        }


        String usage = "§4Usage: " +
                "§c/duel request <player>\n" +
                "         §c/duel accept <player>\n" +
                "         §c/duel deny <player>\n" +
                "         §c/duel cancel <player>\n" +
                "         §c/duel requests";

        if (args.length == 0 || args.length < getMinArgs(args[0])) {
            sender.sendMessage(usage);
            return false;
        }

        if (args[0].equalsIgnoreCase("request") || args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase
                ("deny") || args[0].equalsIgnoreCase("cancel")) {

            Player target = Bukkit.getPlayer(args[1]);

            if (target == null) {
                sender.sendMessage("§cYou must enter a valid online player name.");
                return false;
            }

            SCPlayer scTarget = scPlayerManager.getSCPlayer(target.getUniqueId());

            if (args[0].equalsIgnoreCase("request")) {

                if (player == target) {
                    sender.sendMessage("§cYou are not permitted to duel yourself.");
                    return false;
                }

                if (scTarget.getDuelRequests().containsKey(player)) {
                    FancyMessage message = new FancyMessage("§cYou have already sent a duel request to ").then
                            (scTarget.getTag()).tooltip(scTarget.getHoverText()).then("§c.");
                    message.send(player);
                    return false;
                }

                scTarget.addDuelRequest(player.getUniqueId());

                FancyMessage message = new FancyMessage("§aYou have sent a duel request to ").then(scTarget.getTag()
                ).tooltip(scTarget.getHoverText()).then("§a which will expire in 5 minutes.");
                message.send(sender);

                message = new FancyMessage("§aYou have received a duel request from ").then(scPlayer.getTag())
                        .tooltip(scPlayer.getHoverText()).then("§a. You have 5 minutes to respond to this request " +
                                "before it is cancelled.");
                message.send(target);
                return true;
            } else if (args[0].equalsIgnoreCase("accept")) {
                if (!scPlayer.getDuelRequests().containsKey(target.getUniqueId())) {
                    FancyMessage message = new FancyMessage("§cYou have not received a duel request from ").then
                            (scTarget.getTag()).tooltip(scTarget.getHoverText()).then("§c.");
                    message.send(sender);
                    return false;
                }

                if (arena.isRunning()) {
                    sender.sendMessage("§cYou are not permitted to accept a duel request while another duel is taking" +
                            " place.");
                    return false;
                }

                if (!arena.allValidLocations()) {
                    sender.sendMessage("§cDuels will not take place without all location types being set.");
                    return false;
                }

                if (scTarget.isCombatTagged()) {
                    FancyMessage message = new FancyMessage(scTarget.getTag()).tooltip(scTarget.getHoverText()).then
                            (" §cis currently in combat which prohibits you from dueling each other.");
                    message.send(sender);
                    return false;
                }

                final Faction pFaction = MPlayerColl.get().get(player).getFaction();
                final Faction tFaction = MPlayerColl.get().get(target).getFaction();

                if (pFaction.getFlag(MFlag.ID_PEACEFUL)) {
                    sender.sendMessage("§cYou are in a peaceful faction which prohibits you from dueling.");
                    return false;
                }
                if (tFaction.getFlag(MFlag.ID_PEACEFUL)) {
                    FancyMessage message = new FancyMessage(scTarget.getTag()).tooltip(scTarget.getHoverText()).then
                            (" §cis in a peaceful faction prohibiting you from dueling each other.");
                    message.send(sender);
                    return false;
                }
                if (pFaction.getRelationTo(tFaction) == Rel.MEMBER && !pFaction.isNone()) {
                    FancyMessage message = new FancyMessage(scTarget.getTag()).tooltip(scTarget.getHoverText()).then
                            (" §cis a member of your faction.");
                    message.send(sender);
                    return false;
                }
                if (pFaction.getRelationTo(tFaction) == Rel.ALLY) {
                    FancyMessage message = new FancyMessage(scTarget.getTag()).tooltip(scTarget.getHoverText()).then
                            (" §cis s a member of an allied faction.");
                    message.send(sender);
                    return false;
                }

                if (instance.getEssentials().getUser(target).isGodModeEnabled()) {
                    FancyMessage message = new FancyMessage(scTarget.getTag()).tooltip(scTarget.getHoverText()).then
                            (" §chas god-mode enabled which prohibits you from dueling each other.");
                    message.send(sender);
                    return false;
                }

                if (instance.getEssentials().getUser(player).isGodModeEnabled()) {
                    sender.sendMessage("§cYou must disable god-mode prior to accepting a duel request.");
                    return false;
                }

                if (target.getGameMode().equals(GameMode.CREATIVE)) {
                    FancyMessage message = new FancyMessage(scTarget.getTag()).tooltip(scTarget.getHoverText()).then
                            (" §cis currently in creative which prohibits you from dueling each other.");
                    message.send(sender);
                    return false;
                }

                if (player.getGameMode().equals(GameMode.CREATIVE)) {
                    sender.sendMessage("§cYou are not allowed to be in creative prior to accepting a duel request.");
                    return false;
                }

                scPlayer.removeDuelRequest(target.getUniqueId());
                arena.startMatch(player, target);

                FancyMessage message = new FancyMessage(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then(" " +
                        "§ahas accepted your duel request.");
                message.send(target);

                message = new FancyMessage("§aYou have accepted ").then(scTarget.getTag()).tooltip(scTarget
                        .getHoverText()).then("§a's duel request.");
                message.send(sender);
                return true;
            } else if (args[0].equalsIgnoreCase("deny")) {
                if (!scPlayer.getDuelRequests().containsKey(target.getUniqueId())) {
                    FancyMessage message = new FancyMessage("§cYou have not received a duel request from ").then
                            (scTarget.getTag()).tooltip(scTarget.getHoverText()).then("§c.");
                    message.send(sender);
                    return false;
                }

                scPlayer.removeDuelRequest(target.getUniqueId());

                FancyMessage message = new FancyMessage(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then(" " +
                        "§chas declined your duel request.");
                message.send(target);

                message = new FancyMessage("§cYou have declined ").then(scTarget.getTag()).tooltip(scTarget
                        .getHoverText()).then("§c's duel request.");
                message.send(sender);
                return true;
            } else {
                if (!scTarget.getDuelRequests().containsKey(player.getUniqueId())) {
                    FancyMessage message = new FancyMessage("§cYou have not sent a duel request to ").then
                            (scTarget.getTag()).tooltip(scTarget.getHoverText()).then("§c.");
                    message.send(sender);
                    return false;
                }

                scTarget.removeDuelRequest(player.getUniqueId());

                FancyMessage message = new FancyMessage(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then(" " +
                        "§ahas cancelled his duel request.");
                message.send(target);

                message = new FancyMessage("§cYou have cancelled your duel request to ").then(scTarget.getTag()).tooltip
                        (scTarget.getHoverText()).then("§c.");
                message.send(sender);
                return true;
            }
        } else if (args[0].equalsIgnoreCase("requests")) {
            if (scPlayer.getDuelRequests().size() != 0) {
                sender.sendMessage("§aDuel Requests:");
                for (UUID p : scPlayer.getDuelRequests().keySet()) {
                    SCPlayer scP = scPlayerManager.getSCPlayer(p);
                    FancyMessage message = new FancyMessage("§f   - ").then(scP.getTag()).tooltip(scP.getHoverText());
                    message.send(sender);
                }
            } else {
                sender.sendMessage("§cYou currently do not have any duel requests.");
            }
            return true;
        } else {
            sender.sendMessage(usage);
            return false;
        }
    }

    public int getMinArgs(String subcommand) {
        switch (subcommand) {
            case "request":
                return 2;
            case "deny":
                return 2;
            case "accept":
                return 2;
            case "cancel":
                return 2;
            case "requests":
                return 1;
            default:
                return 100;
        }
    }
}
