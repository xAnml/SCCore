package net.anmlmc.SCCore.Ranks;

import net.anmlmc.SCCore.Main;
import net.anmlmc.SCCore.SCPlayer.SCPlayerManager;

/**
 * Created by kishanpatel on 12/6/15.
 */

public enum Rank {

    OWNER(9, "§6Owner", "§6%s", 100),
    DEV(8, "§6Dev", "§6%s", 100),
    ADMINPLUS(7, "§4Admin§e+", "Admin+", "§4%s§e+", 100),
    ADMIN(6, "§4Admin", "§4%s", 100),
    MOD(5, "§3Mod", "§3%s", 80),

    PREMIUMPLUS(4, "§9Premium§e+", "Premium+", "§9%s§e+", 80),
    PREMIUM(3, "§9Premium", "§9%s", 60),
    VIPPLUS(2, "§aVIP§e+", "VIP+", "§a%s§e+", 40),
    VIP(1, "§aVIP", "§a%s", 25),

    DEFAULT(0, "§fDefault", "§f%s", 10);

    int id;
    String name;
    String alias;
    String tag;
    int lockpickChance;
    SCPlayerManager scPlayerManager = Main.getInstance().getSCPlayerManager();

    Rank(int id, String name, String alias, String tag, int lockpickChance) {
        this.id = id;
        this.name = name;
        this.alias = alias;
        this.tag = tag;
        this.lockpickChance = lockpickChance;
    }

    Rank(int id, String name, String tag, int lockpickChance) {
        this.id = id;
        this.name = name;
        this.alias = name();
        this.tag = tag;
        this.lockpickChance = lockpickChance;
    }

    public String getName() {
        return name;
    }

    public String getAlias() {
        return alias;
    }

    public String getTag() {
        return tag;
    }

    public int getId() {
        return id;
    }

    public int getLockpickChance() {
        return lockpickChance;
    }


}
