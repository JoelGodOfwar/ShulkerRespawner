package com.github.joelgodofwar.sr.common;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.plugin.Plugin;

import com.github.joelgodofwar.sr.common.error.BasicErrorReporter;
import com.github.joelgodofwar.sr.common.error.ErrorReporter;
import com.github.joelgodofwar.sr.common.error.ReportType;
import com.google.common.collect.ImmutableList;

public class PluginLibrary {

	/**
	 * The minimum version MoreMobHeads has been tested with.
	 */
	public static final String MINIMUM_MINECRAFT_VERSION = "1.13";

	/**
	 * The maximum version MoreMobHeads has been tested with.
	 */
	public static final String MAXIMUM_MINECRAFT_VERSION = "1.21";

	/**
	 * The date (with ISO 8601 or YYYY-MM-DD) when the most recent version (1.20.4) was released.
	 */
	public static final String MINECRAFT_LAST_RELEASE_DATE = "2023-12-07";

	/**
	 * Plugins that are currently incompatible with MoreMobHeads.
	 */
	public static final List<String> INCOMPATIBLE = ImmutableList.of("");

	private static Plugin plugin;

	private static boolean updatesDisabled;
	private static boolean initialized;
	private static ErrorReporter reporter = new BasicErrorReporter();

	protected static void init(Plugin plugin, ErrorReporter reporter) {
		Validate.isTrue(!initialized, "MoreMobHeads has already been initialized.");
		PluginLibrary.plugin = plugin;
		PluginLibrary.reporter = reporter;

		initialized = true;
	}

	public static final ReportType REPORT_CANNOT_DELETE_CONFIG = new ReportType("Cannot delete old DragonDropElytra configuration.");
	public static final ReportType REPORT_CANNOT_COPY_FILE = new ReportType("Cannot copy file.");
	public static final ReportType REPORT_METRICS_LOAD_ERROR = new ReportType("Cannot load bStats Metrics.");

	public static final ReportType REPORT_PLUGIN_LOAD_ERROR = new ReportType("Cannot load DragonDropElytra.");
	public static final ReportType REPORT_CANNOT_LOAD_CONFIG = new ReportType("Cannot load configuration");
	public static final ReportType REPORT_CANNOT_CHECK_CONFIG = new ReportType("Cannot check configuration");
	public static final ReportType REPORT_CANNOT_SAVE_CONFIG = new ReportType("Cannot save configuration");
	public static final ReportType REPORT_PLUGIN_ENABLE_ERROR = new ReportType("Cannot enable DragonDropElytra.");
	public static final ReportType REPORT_PLUGIN_UNKNOWN_ERROR = new ReportType("Unknown Error");

	public static final ReportType REPORT_CANNOT_PARSE_MINECRAFT_VERSION = new ReportType("Unable to retrieve current Minecraft version. Assuming %s");
	public static final ReportType REPORT_CANNOT_DETECT_CONFLICTING_PLUGINS = new ReportType("Unable to detect conflicting plugin versions.");
	public static final ReportType REPORT_CANNOT_REGISTER_COMMAND = new ReportType("Cannot register command %s: %s");

	//public static final ReportType REPORT_CANNOT_CREATE_TIMEOUT_TASK = new ReportType("Unable to create packet timeout task.");
	public static final ReportType REPORT_CANNOT_UPDATE_PLUGIN = new ReportType("Cannot perform automatic updates.");

	public static final ReportType ERROR_PARSING_DRAGON_DEATH = new ReportType("Error parsing dragon death.");
	public static final ReportType ERROR_BLOCKING_DRAGON_EGG = new ReportType("Error blocking dragon egg.");
	public static final ReportType ERROR_RUNNING_DRAGON_DEATH_COMMAND = new ReportType("Error running command after dragon death.");

	public static final ReportType ERROR_HANDLING_CREATURESPAWNEVENT = new ReportType("Error handling CreatureSpawnEvent.");

	/**
	 * Gets the MoreMobHeads plugin instance.
	 * @return The plugin instance
	 */
	public static Plugin getPlugin() {
		return plugin;
	}

	/**
	 * Disables the MoreMobHeads update checker.
	 */
	public static void disableUpdates() {
		updatesDisabled = true;
	}

	/**
	 * Retrieve the current error reporter.
	 * @return Current error reporter.
	 */
	public static ErrorReporter getErrorReporter() {
		return reporter;
	}

	/**
	 * Whether updates are currently disabled.
	 * @return True if it is, false if not
	 */
	public static boolean updatesDisabled() {
		return updatesDisabled;
	}
}