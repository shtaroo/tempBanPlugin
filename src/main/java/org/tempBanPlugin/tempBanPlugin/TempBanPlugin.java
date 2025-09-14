package org.tempBanPlugin.tempBanPlugin;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Instant;
import java.util.Date;

public final class TempBanPlugin extends JavaPlugin implements Listener {
    private Connection connection;

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("TempBanPlugin enabled");
        Bukkit.broadcastMessage("TempBanPlugin is Enabled");

        // DATABASE SETUP
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        File db = new File(getDataFolder(), "playerlog.db");
        try {
            if (!db.exists()) {
                db.createNewFile();
                getLogger().info("Created new database file: " + db.getAbsolutePath());
            }

            // CONNECT TO SQLITE
            String url = "jdbc:sqlite:" + db.getAbsolutePath();
            connection = DriverManager.getConnection(url);
            getLogger().info("Connected to SQLite database.");

            Statement stmt = connection.createStatement();
            stmt.executeUpdate("create table if not exists deaths (" +
                    "playerName string primary key," +
                    "deathCount int default 0," +
                    ")"
            );
            stmt.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
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

        // add death to db
        addDeathToDB(player.getName());

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

    void addDeathToDB(String playerName) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "insert into deaths (playerName, deathCount) values (?,1) " +
                            "on conflict(playerName) do update set deathCount = deathCount + 1"
            );
            ps.setString(1, playerName);
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}