package net.anmlmc.SCCore.Punishments;

import com.earth2me.essentials.Console;
import net.anmlmc.SCCore.Main;
import net.anmlmc.SCCore.MySQL.MySQL;
import net.anmlmc.SCCore.SCPlayer.SCPlayerManager;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by Anml on 1/5/16.
 */
public class PunishmentEntry {

    Main instance;
    SCPlayerManager scPlayerManager;
    MySQL mySQL;

    private PunishmentType type;

    private UUID target;
    private UUID punisher;

    private long created;
    private long expires;

    private String reason;

    private boolean executed = false;

    public PunishmentEntry(Main instance, PunishmentType type, UUID target, UUID punisher, long created, long expires, String reason) {
        this.instance = instance;
        scPlayerManager = instance.getSCPlayerManager();
        mySQL = instance.getMySQL();
        this.type = type;
        this.target = target;
        this.punisher = punisher;
        this.created = created;
        this.expires = (type.equals(PunishmentType.BAN) || type.equals(PunishmentType.MUTE)) ? -1 : (type.equals
                (PunishmentType.KICK) ? 0 : expires);
        this.reason = reason;
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

    public long getExpired() {
        return expires;
    }

    public void setExpired(long time) {
        expires = time;
    }

    public boolean hasExpired() {
        return expires == 0 || (expires != -1 && expires <= System.currentTimeMillis());
    }

    public String getReason() {
        return reason;
    }

    public void setExecuted(boolean value) {
        executed = value;
    }

    public void execute() {

        if (executed) {
            try {
                mySQL.executeUpdate("UPDATE SCPunishments SET Expires='" + expires + "' WHERE UUID='" + target + "' AND " +
                        "Created='" + created + "'");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return;
        }

        try {
            String insert = "INSERT INTO `SCPunishments`(`Type`, `Target`, `Punisher`, `Created`, `Expires`, " +
                    "`Reason`) VALUES (" + type.name() + ", " + target + ", " + punisher == null ? Console.NAME :
                    punisher + ", " + created + ", " + expires + ", " + reason + "')";
            mySQL.executeUpdate(insert);
            executed = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


}
