package net.anmlmc.SCCore.Punishments;

/**
 * Created by Anml on 1/8/16.
 */
public enum PunishmentType {
    BAN(5),
    TEMPBAN(4),
    MUTE(3),
    TEMPMUTE(2),
    WARNING(1),
    KICK(0);

    int id;

    PunishmentType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
