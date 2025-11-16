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
}
