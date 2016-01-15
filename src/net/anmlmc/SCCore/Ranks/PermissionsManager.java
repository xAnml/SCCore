package net.anmlmc.SCCore.Ranks;

import net.anmlmc.SCCore.Main;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.*;

/**
 * Created by Anml on 1/12/16.
 */

public class PermissionsManager {

    private Main instance;
    private RankManager rankManager;
    private Map<UUID, PermissionAttachment> attachments;

    public PermissionsManager(Main instance) {
        this.instance = instance;
        rankManager = instance.getRankManager();
        attachments = new HashMap<>();
    }

    public Map<UUID, PermissionAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachment(Player player) {
        PermissionAttachment attachment = player.addAttachment(instance);

        for (String permission : getRollingPermissions(rankManager.getRank(player.getUniqueId()))) {
            attachment.setPermission(permission, true);
        }

        for (String permission : getPermissions(player.getUniqueId())) {
            if (!attachment.getPermissions().containsKey(permission))
                attachment.setPermission(permission, true);
        }

        attachments.put(player.getUniqueId(), attachment);
    }

    public void removeAttachment(UUID uuid) {
        if (attachments.containsKey(uuid)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.removeAttachment(attachments.get(uuid));
            }
            attachments.remove(uuid);
        }
    }

    public void updateAttachment(UUID uuid) {

        removeAttachment(uuid);
        setAttachment(Bukkit.getPlayer(uuid));
    }

    public void updateAttachments(Rank rank) {
        for (UUID uuid : attachments.keySet()) {
            if (rankManager.getRank(uuid).equals(rank)) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    player.removeAttachment(attachments.get(uuid));
                    attachments.remove(uuid);
                    setAttachment(player);
                }
            }
        }
    }

    public List<String> getPermissions(UUID uuid) {
        List<String> permissions = new ArrayList<>();

        String path = "Permissions.Player." + uuid;
        FileConfiguration config = instance.getConfig();
        if (config.contains(path)) {
            for (String permission : config.getStringList(path)) {
                if (!permissions.contains(permission.toLowerCase()))
                    permissions.add(permission);
            }
        }

        return permissions;
    }

    public List<String> getPermissions(Rank rank) {
        List<String> permissions = new ArrayList<>();

        String path = "Permissions.Rank." + rank.name();
        FileConfiguration config = instance.getConfig();
        if (config.contains(path)) {
            for (String permission : config.getStringList(path)) {
                if (!permissions.contains(permission.toLowerCase()))
                    permissions.add(permission);
            }
        }

        return permissions;
    }

    public List<String> getRollingPermissions(Rank rank) {
        List<String> permissions = new ArrayList<>();

        for (int i = rank.getId(); i >= 0; i--) {
            for (String permission : getPermissions(rankManager.getRankById(i))) {
                if (!permissions.contains(permission.toLowerCase()))
                    permissions.add(permission);
            }
        }

        return permissions;
    }


    public boolean addPermission(Rank rank, String node) {
        node = node.toLowerCase();

        List<String> permissions = new ArrayList<>(getPermissions(rank));
        if (permissions.contains(node))
            return false;

        String path = "Permissions.Rank." + rank.getName();
        FileConfiguration config = instance.getConfig();
        permissions.add(node);
        config.set(path, permissions);
        instance.saveConfig();

        updateAttachments(rank);

        return true;
    }

    public boolean removePermission(Rank rank, String node) {
        node = node.toLowerCase();

        List<String> permissions = new ArrayList<>(getPermissions(rank));
        if (!permissions.contains(node))
            return false;

        String path = "Permissions.Rank." + rank.getName();
        FileConfiguration config = instance.getConfig();
        permissions.remove(node);
        config.set(path, permissions);
        instance.saveConfig();

        updateAttachments(rank);

        return true;
    }

    public boolean addPermission(UUID uuid, String node) {
        node = node.toLowerCase();

        List<String> permissions = new ArrayList<>(getPermissions(uuid));
        if (permissions.contains(node))
            return false;

        String path = "Permissions.Player." + uuid;
        FileConfiguration config = instance.getConfig();
        permissions.add(node);
        config.set(path, permissions);
        instance.saveConfig();

        updateAttachment(uuid);

        return true;
    }

    public boolean removePermission(UUID uuid, String node) {
        node = node.toLowerCase();
        if (!getPermissions(uuid).contains(node))
            return false;

        String path = "Permissions.Player." + uuid;
        FileConfiguration config = instance.getConfig();
        List<String> permissions = new ArrayList<>(getPermissions(uuid));
        permissions.remove(node);
        config.set(path, permissions);
        instance.saveConfig();

        updateAttachment(uuid);

        return true;
    }


}
