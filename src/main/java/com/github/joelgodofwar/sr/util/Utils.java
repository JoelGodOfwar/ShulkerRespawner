package com.github.joelgodofwar.sr.util;

import com.github.joelgodofwar.sr.ShulkerRespawner;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Utils {
	private static ShulkerRespawner plugin;

	public Utils(ShulkerRespawner plugin){
		Utils.plugin = plugin;
	}
	public static void sendJson(CommandSender player, String string){
		plugin.coreUtils.sendJsonMessage((Player) player, string);
	}
	public static void sendJson(Player player, String string){
		plugin.coreUtils.sendJsonMessage(player, string);
	}

	/**
	 * Smart converter: feed it anything (0.50, "0.75", 60, "50", etc.) → always returns 0–100 int
	 */
	public static int toPercent(Object input) {
		if (input == null) return 50; // or whatever default you want

		try {
			// 1. If it's already an Integer → done
			if (input instanceof Integer i) {
				return Math.min(100, Math.max(0, i));
			}

			// 2. If it's a Double
			if (input instanceof Double d) {
				if (d >= 0.0 && d <= 1.0) {
					return (int) Math.round(d * 100.0);           // 0.50 → 50
				}
				if (d > 1.0 && d <= 100.0) {
					return (int) Math.round(d);                  // 75.0 → 75
				}
			}

			// 3. If it's a String → parse intelligently
			if (input instanceof String s) {
				double parsed = Double.parseDouble(s.trim());
				if (parsed >= 0.0 && parsed <= 1.0) {
					return (int) Math.round(parsed * 100.0);      // "0.50" → 50
				}
				if (parsed > 1.0 && parsed <= 100.0) {
					return (int) Math.round(parsed);             // "75" → 75
				}
			}
		} catch (Exception ignored) {}

		// Fallback → treat as old decimal default
		return 50; // or whatever makes sense for that feature
	}
}
