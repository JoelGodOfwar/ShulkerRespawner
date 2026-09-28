package com.github.joelgodofwar.sr.events;

import com.github.joelgodofwar.sr.ShulkerRespawner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitEventHandler implements Listener {

    private final ShulkerRespawner plugin;

    public PlayerQuitEventHandler(ShulkerRespawner plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.chanceRandoms.remove(player);
    }
}