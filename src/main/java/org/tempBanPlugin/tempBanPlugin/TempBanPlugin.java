package org.tempBanPlugin.tempBanPlugin;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Instant;
import java.util.Date;

public final class TempBanPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("TempBanPlugin enabled");
        Bukkit.broadcastMessage("TempBanPlugin is Enabled");

    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        String deathMessage = event.getDeathMessage();
        long seconds = 24L * 60 * 60;
        Date expiry = Date.from(Instant.now().plusSeconds(seconds));
        String reason = "§e⚠ §6You've recently died. §7You've been set on a §c24 hour §7cooldown.";
        event.setDeathMessage(null);

        Bukkit.getBanList(BanList.Type.PROFILE)
                .addBan(player.getName(), reason, expiry, getName());

        player.kickPlayer("§4§l☠ YOU DIED! ☠ §7You've been set on a §c24 hour §7cooldown.");

        Bukkit.broadcastMessage("§8==============================");
        Bukkit.broadcastMessage("§c§lBAN ALERT §8» §6" + player.getName() + " §7has died§7!");
        if (deathMessage != null) {
            Bukkit.broadcastMessage("§7Cause: §f" + deathMessage);
        }
        Bukkit.broadcastMessage("§8==============================");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("TempBanPlugin disabled");
    }

}