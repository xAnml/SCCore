package net.anmlmc.SCCore.Lockpicks;

import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.massivecore.ps.PS;
import net.anmlmc.SCCore.Main;
import net.anmlmc.SCCore.SCPlayer.SCPlayer;
import net.anmlmc.SCCore.SCPlayer.SCPlayerManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Anml on 12/30/15.
 */
public class LockpickListeners implements Listener {

    Main instance;
    SCPlayerManager scPlayerManager;

    public LockpickListeners(Main instance) {
        this.instance = instance;
        scPlayerManager = instance.getSCPlayerManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDamageEvent(final EntityDamageEvent e) {
        final Entity entity = e.getEntity();
        if ((entity instanceof Player)) {
            Player player = (Player) entity;
            final LockpickRunnable task = scPlayerManager.getLockpicking().remove(player.getUniqueId());
            if (task != null) {
                task.cancel();
                player.sendMessage("§cYour attempt at lockpicking was cancelled due to damage.");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMoveEvent(final PlayerMoveEvent e) {
        if (e.getFrom().getX() != e.getTo().getX() || e.getFrom().getY() != e.getTo().getY() || e.getFrom().getZ() != e.getTo().getZ()) {
            final Player player = e.getPlayer();
            final LockpickRunnable task = scPlayerManager.getLockpicking().remove(player.getUniqueId());
            if (task != null) {
                task.cancel();
                player.sendMessage("§cYour attempt at lockpicking was cancelled due to movement.");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(final PlayerInteractEvent e) {
        Action action = e.getAction();
        Player player = e.getPlayer();
        SCPlayer scPlayer = scPlayerManager.getSCPlayer(player.getUniqueId());
        ItemStack itemStack = player.getItemInHand();
        Block block = e.getClickedBlock();
        final Location location = block.getLocation();
        Faction faction = BoardColl.get().getFactionAt(PS.valueOf(location.getChunk()));

        if (!action.equals(Action.RIGHT_CLICK_BLOCK))
            return;

        if (itemStack == null || itemStack.getType().equals(Material.AIR) || itemStack.getType().getId() != 383)
            return;

        if (!itemStack.getItemMeta().getDisplayName().equals("§bLockpick")) {
            return;
        }

        if (e.getClickedBlock().getTypeId() != 68)
            return;

        final Sign sign = (Sign) e.getClickedBlock().getState();
        if (!sign.getLine(0).equalsIgnoreCase("[Private]"))
            return;

        if (!faction.isNone()) {
            player.sendMessage("§cLockpicks are only usable in unclaimed chunks.");
            return;
        }

        if (scPlayer.isLockpicking()) {
            player.sendMessage("§cYou are allowed to lockpick one block at a time.");
            return;
        }

        itemStack.setAmount(itemStack.getAmount() - 1);
        if (itemStack.getAmount() == 0)
            itemStack.setType(Material.AIR);

        player.getInventory().setItemInHand(itemStack);

        player.sendMessage("§aCommencing start of lockpicking the target block - ");

        LockpickRunnable runnable = new LockpickRunnable(instance, block, player.getUniqueId(), 1);
        scPlayerManager.getLockpicking().put(player.getUniqueId(), runnable);
        runnable.runTaskTimer(instance, 10L, 50L);


    }
}
