package net.anmlmc.SCCore.Punishments;

import net.anmlmc.SCCore.Main;
import net.anmlmc.SCCore.MySQL.MySQL;
import net.anmlmc.SCCore.SCPlayer.SCPlayerManager;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Created by Anml on 1/8/16.
 */
public class Punishment {

    Main instance;
    PunishmentManager punishmentManager;
    SCPlayerManager scPlayerManager;
    MySQL mySQL;

    PunishmentType type;
    UUID target;
    UUID punisher;
    long created;
    long expires;
    String reason;
    boolean executed = false;

    public Punishment(PunishmentType type, UUID target, UUID punisher, long expires, String reason) {
        this.type = type;
        this.target = target;
        this.punisher = punisher;
        created = System.currentTimeMillis();
        this.expires = expires;
        this.reason = reason;

        instance = Main.getInstance();
        punishmentManager = instance.getPunishmentManager();
        scPlayerManager = instance.getSCPlayerManager();
        mySQL = instance.getMySQL();
    }

    public PunishmentType getType() {
        return type;
    }

    public UUID getTarget() {
        return target;
    }

    public UUID getPunisher() {
        return punisher;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long value) {
        created = value;
    }

    public long getExpires() {
        return expires;
    }

    public void setExpires(long value) {
        expires = value;
    }

    public boolean hasExpired() {
        return expires == 0L || (expires != -1L && (created + expires) <= System.currentTimeMillis());
    }

    public String getEndTimestamp() {
        final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("EST"));
        return DATE_FORMAT.format(new Date(created + expires));
    }

    public String getReason() {
        return reason;
    }

    public String getMessage() {
        String message = "";
        String tag = punisher != null ? scPlayerManager.getSCPlayer(punisher).getTag() : "§6Console";
        switch (type.getId()) {
            case 5:
                message = "§7You are permanently banned from §cSensationCraft§7:\n\n" +
                        "§7Reason: §f" + reason + " §8- " + tag;
                break;
            case 4:
                message = "§7You are temporarily banned from §cSensationCraft§7:\n\n" +
                        "§7Reason: §f" + reason + " §8- " + tag + "\n" +
                        "§7Unban Info: §f" + getEndTimestamp() + "\n\n";
                break;
            case 3:
                message = "§7You have been muted by " + tag + " §7for: §a" + reason + "§7.";
                break;
            case 2:
                message = "§7You have been temporarily muted for §c" + getEndTimestamp() + " §7by " + tag + " §7for: §a" + reason + "§7.";
                break;
            case 1:
                message = "§7You have been warned by " + tag + " §7with reason: §a" + reason + "§7.";
                break;
            case 0:
                message = "§7You have been kicked from §cSensationCraft§7:\n\n" +
                        "§7Reason: §f" + reason + " §8- " + tag;
                break;
            default:
                message = "§cError in Ban Type! Contact an administrator of SensationCraft.";
                break;
        }

        return message;
    }

    public boolean isExecuted() {
        return executed;
    }

    public void setExecuted(boolean value) {
        executed = value;
    }

    public void execute() {

        if (executed) {
            try {
                mySQL.executeUpdate("UPDATE SCPunishments SET Expires='" + expires + "' WHERE Target='" + target + "' AND Created='" + created + "'");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return;
        }

        try {
            String punisherUUID = punisher == null ? "Console" : punisher.toString();
            mySQL.executeUpdate("INSERT INTO `SCPunishments` (`Type`, `Target`, `Punisher`, `Created`, `Expires`, `Reason`) " +
                    "VALUES ('" + type.name() + "','" + target + "', '" + punisherUUID + "', '" + created + "', '" + expires + "', '" + reason + "')");
            executed = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
