package com.github.joelgodofwar.sr.common;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.github.joelgodofwar.sr.ShulkerRespawner;

/**
 * Custom logger for the ShulkerRespawner plugin.
 */
public class PluginLogger {
	private Logger logger = Logger.getLogger("Minecraft");
	private ShulkerRespawner sr;

	/**
	 * Constructor for the PluginLogger class.
	 * @param plugin The MoreMobHeads plugin instance.
	 */
	public PluginLogger(ShulkerRespawner plugin) {
		logger = plugin.getLogger();
		sr = plugin;
	}

	/**
	 * Logs a message to console with the INFO level.
	 * @param message Message to log.
	 */
	public void log(String message) {
		log(message, "");
	}

	/**
	 * Logs a message to console with a given level.
	 * @param level Logging level
	 * @param message Message to log
	 * @param args Arguments to format in
	 */
	public void log(Level level, String message, Object... args) {
		//String pluginName = ChatColor.RESET + "[" + ChatColor.YELLOW + dde.getName() + ChatColor.RESET + "] ";
		message = ChatColor.RESET + "[" + ChatColor.YELLOW + sr.getName() + ChatColor.RESET + "] " + message;
		if(!sr.colorful_console) {
			message = ChatColor.stripColor(message);
		}
		if (!areAllArgsBlank(args)) {
			//logger.log(level, applyColorFormatting(MessageFormat.format(message, args)));
			Bukkit.getConsoleSender().sendMessage(String.format(message, args));
		} else {
			//logger.log(level, applyColorFormatting(message));
			Bukkit.getConsoleSender().sendMessage(message);
		}
	}

	/**
	 * Logs a method to console with the INFO level.
	 * @param message Message to log
	 * @param args Arguments to format in
	 */
	public void log(String message, Object... args) {
		log(Level.INFO, message, args);
	}

	/**
	 * Logs a message to console with a given level and exception.
	 * @param level Logging level
	 * @param message Message to log
	 * @param ex Exception to log
	 */
	public void log(Level level, String message, Throwable ex) {
		logger.log(level, message, ex);
	}

	/**
	 * Logs a debug message to console if debugging is enabled.
	 * @param message Debug message to log.
	 */
	public void debug(String message) {
		debug(message, "");
	}

	/**
	 * Logs a debug message to console with arguments if debugging is enabled.
	 * @param message Debug message to log.
	 * @param args Arguments to format in the debug message.
	 */
	public void debug(String message, Object... args) {
		if (sr.debug) {
			log("[Debug] " + message, args);
		}
	}

	/**
	 * Logs a debug message to console with an exception if debugging is enabled.
	 * @param message Debug message to log.
	 * @param ex Exception to log.
	 */
	public void debug(String message, Throwable ex) {
		if (sr.debug) {
			log(Level.WARNING, "[Debug] " + message, ex);
		}
	}

	/**
	 * Logs a warning message to console with the WARNING level.
	 * @param message Warning message to log.
	 */
	public void warn(String message) {
		warn(message, "");
	}

	/**
	 * Logs a warning message to console with the WARNING level and arguments.
	 * @param message Warning message to log.
	 * @param args Arguments to format in the warning message.
	 */
	public void warn(String message, Object... args) {
		log(Level.WARNING, "[Warning] " + message, args);
	}

	/**
	 * Logs a warning message to console with the WARNING level if the Minecraft version is at or above a specified version.
	 * @param version The Minecraft version to compare against.
	 * @param message Warning message to log.
	 * @param args Arguments to format in the warning message.
	 */
	public void warnAbove(MinecraftVersion version, String message, Object... args) {
		if (version.atOrAbove()) {
			log(Level.WARNING, message, args);
		}
	}

	/**
	 * Checks if all arguments are null or blank.
	 * @param args Arguments to check
	 * @return true if all arguments are null or blank, false otherwise
	 */
	private boolean areAllArgsBlank(Object... args) {
		if ((args == null) || (args.length == 0)) {
			return true; // Null or empty arguments are considered blank
		}
		for (Object arg : args) {
			if ((arg == null) || ((arg instanceof String) && ((String) arg).isEmpty())) {
				continue; // Skip null or empty string
			} else {
				return false; // Found a non-blank argument
			}
		}
		return true; // All arguments are null or blank
	}


}