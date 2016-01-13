package net.anmlmc.SCCore.Stats;

/**
 * Created by Anml on 1/12/16.
 */
public enum Stat {

    KILLS("Kills"),
    DEATHS("Deaths"),
    WINS("Wins"),
    LOSSES("Losses");

    private String name;

    Stat(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
