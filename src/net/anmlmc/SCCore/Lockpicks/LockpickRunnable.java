package net.anmlmc.SCCore.Lockpicks;

import net.anmlmc.SCCore.Main;
import net.anmlmc.SCCore.SCPlayer.SCPlayer;
import net.anmlmc.SCCore.SCPlayer.SCPlayerManager;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by Anml on 12/31/15.
 */
public class LockpickRunnable extends BukkitRunnable {
    private int counter;
    private Main instance;
    private SCPlayerManager scPlayerManager;
    private SCPlayer scPlayer;
    private Block block;
    private Player player;

    public LockpickRunnable(Main instance, Block block, SCPlayer scPlayer, int counter) {
        this.counter = counter;
        this.instance = instance;
        scPlayerManager = instance.getSCPlayerManager();
        this.scPlayer = scPlayer;
        this.block = block;
        player = scPlayer.getBase();
    }

    @Override
    public void run() {
        if (counter == 5) {
            if (scPlayer.lockpickAttempt()) {
                block.breakNaturally();
                player.sendMessage("§aThe luck was in your favor, resulting in a successful lockpick.");
            } else
                player.sendMessage("§cThe luck was not in your favor, resulting in an unsuccessful lockpick.");
            final LockpickRunnable task = scPlayerManager.getLockpicking().remove(player);
            if (task != null) {
                task.cancel();
            }
        } else {
            counter++;
            Location smoke = block.getLocation();
            smoke.setY(smoke.getY() + 1);
            smoke.getWorld().playEffect(smoke, Effect.SMOKE, 80);

        }
    }

}
