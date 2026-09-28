package com.github.joelgodofwar.sr.events;

import com.github.joelgodofwar.sr.ShulkerRespawner;
import lib.github.joelgodofwar.coreutils.CoreUtils;
import lib.github.joelgodofwar.coreutils.util.common.PluginLogger;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

public class ShulkerDeathHandler implements Listener {

    private final ShulkerRespawner plugin;
    private final PluginLogger LOGGER;

    public ShulkerDeathHandler(ShulkerRespawner plugin) {
        this.plugin = plugin;
        this.LOGGER = plugin.logger;
    }

    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent event) {
        if (!plugin.getConfig().getBoolean("double_shulker_chance.enabled", true)) return;

        if (!(event.getEntity() instanceof Shulker shulker)) return;
        if (!(shulker.getKiller() instanceof Player)) return;
        Player player = event.getEntity().getKiller();

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand == null || mainHand.getType() == Material.AIR) return;

        int lootingLevel = mainHand.getEnchantmentLevel(CoreUtils.LOOTING);

        int baseChancePercent = plugin.getConfig().getInt("double_shulker_chance.rate", 50);
        int finalChancePercent = Math.min(100, Math.max(0, baseChancePercent + lootingLevel));

        double finalChance = finalChancePercent / 100.0;

        // ← THIS IS THE IMPORTANT PART
        Random seededRandom = plugin.chanceRandoms.computeIfAbsent(player,
                p -> new Random(p.getUniqueId().hashCode()));

        if (seededRandom.nextDouble() < finalChance) {
            ItemStack shell = new ItemStack(Material.SHULKER_SHELL, 1);

            boolean alreadyDropping = event.getDrops().stream()
                    .anyMatch(item -> item.getType() == Material.SHULKER_SHELL);

            if (alreadyDropping) {
                event.getDrops().add(shell);
                LOGGER.debug("§aExtra shulker shell added (total 2)§r");
            } else {
                event.getDrops().add(shell);
                event.getDrops().add(shell);
                LOGGER.debug("§aForced 2 shulker shells§r");
            }
        }

        // Optional cleanup: remove player after they quit to avoid memory leak
        // You can do this in a PlayerQuitEvent listener if you want
    }
}