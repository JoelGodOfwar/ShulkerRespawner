package com.github.joelgodofwar.sr;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
//import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.github.joelgodofwar.sr.common.PluginLibrary;
import com.github.joelgodofwar.sr.common.PluginLogger;
import com.github.joelgodofwar.sr.common.error.DetailedErrorReporter;
import com.github.joelgodofwar.sr.common.error.Report;
import com.github.joelgodofwar.sr.events.CSEHandler_1_17;
import com.github.joelgodofwar.sr.events.CSEHandler_1_18;
import com.github.joelgodofwar.sr.events.CSEHandler_1_19;
import com.github.joelgodofwar.sr.i18n.Translator;
import com.github.joelgodofwar.sr.util.Metrics;
import com.github.joelgodofwar.sr.util.SendJsonMessages;
import com.github.joelgodofwar.sr.util.Utils;
import com.github.joelgodofwar.sr.util.Version;
import com.github.joelgodofwar.sr.util.VersionChecker;
import com.github.joelgodofwar.sr.util.YmlConfiguration;
import com.tcoded.folialib.FoliaLib;

import net.md_5.bungee.api.chat.TextComponent;


public class ShulkerRespawner  extends JavaPlugin implements Listener{
	/** Languages: čeština (cs_CZ), Deutsch (de_DE), English (en_US), Español (es_ES), Español (es_MX), Français (fr_FR), Italiano (it_IT), Magyar (hu_HU), 日本語 (ja_JP), 한국어 (ko_KR), Lolcat (lol_US), Melayu (my_MY), Nederlands (nl_NL), Polski (pl_PL), Português (pt_BR), Русский (ru_RU), Svenska (sv_SV), Türkçe (tr_TR), 中文(简体) (zh_CN), 中文(繁體) (zh_TW) */
	//public final static Logger logger = Logger.getLogger("Minecraft");
	static String THIS_NAME;
	static String THIS_VERSION;
	/** update checker variables */
	public int projectID = 73638; // https://spigotmc.org/resources/71236
	public String githubURL = "https://github.com/JoelGodOfwar/ShulkerRespawner/raw/master/versioncheck/1.17/versions.xml";
	boolean UpdateAvailable =  false;
	public String UColdVers;
	public String UCnewVers;
	public static boolean UpdateCheck;
	public String DownloadLink = "https://www.spigotmc.org/resources/shulkerrespawner.73638";
	/** end update checker variables */
	public static String daLang;
	public boolean debug;
	File langFile;
	FileConfiguration lang;
	YmlConfiguration config = new YmlConfiguration();
	YamlConfiguration oldconfig = new YamlConfiguration();
	public final NamespacedKey NAME_KEY = new NamespacedKey(this, "shulker");
	String configVersion = "1.0.1";
	String pluginName = THIS_NAME;
	Translator lang2;
	public FoliaLib foliaLib;
	public String jarfilename = this.getFile().getAbsoluteFile().toString();
	public static DetailedErrorReporter reporter;
	public boolean colorful_console = true;
	public PluginLogger LOGGER;

	@SuppressWarnings("unused") @Override // TODO: onEnable
	public void onEnable(){
		long startTime = System.currentTimeMillis();
		LOGGER = new PluginLogger(this);
		reporter = new DetailedErrorReporter(this);
		UpdateCheck = getConfig().getBoolean("auto_update_check", true);
		debug = getConfig().getBoolean("debug", false);
		daLang = getConfig().getString("lang", "en_US");
		oldconfig = new YamlConfiguration();
		lang2 = new Translator(daLang, getDataFolder().toString());
		THIS_NAME = this.getDescription().getName();
		THIS_VERSION = this.getDescription().getVersion();
		if(!getConfig().getBoolean("console.longpluginname", true)) {
			pluginName = "SR";
		}else {
			pluginName = THIS_NAME;
		}

		foliaLib = new FoliaLib(this);

		LOGGER.log(ChatColor.YELLOW + "**************************************" + ChatColor.RESET);
		LOGGER.log(ChatColor.GREEN + " v" + THIS_VERSION + ChatColor.RESET + " Loading...");
		LOGGER.log("Server Version: " + getServer().getVersion().toString());

		Version checkVersion = this.verifyMinecraftVersion();
		LOGGER.log("Loading config file...");
		/**  Check for config */
		try{
			if(!this.getDataFolder().exists()){
				LOGGER.log("Data Folder doesn't exist");
				LOGGER.log("Creating Data Folder");
				this.getDataFolder().mkdirs();
				LOGGER.log("Data Folder Created at " + this.getDataFolder());
			}
			File  file = new File(this.getDataFolder(), "config.yml");
			this.getLogger().info("" + file);
			if(!file.exists()){
				LOGGER.log("config.yml not found, creating!");
				saveResource("config.yml", true);
			}
		}catch(Exception exception){
			debug = true;
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_CHECK_CONFIG).error(exception));
			LOGGER.debug(ChatColor.RED + "debug has been set to true due to an exception.");
		}
		/**  Check config version */
		try {
			oldconfig.load(new File(getDataFolder(), "config.yml"));
		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_LOAD_CONFIG).error(exception));
		}
		String checkconfigversion = oldconfig.getString("version", "1.0.0");
		if(checkconfigversion != null){
			if(!checkconfigversion.equalsIgnoreCase(configVersion)){
				try {
					copyFile_Java7(getDataFolder() + "" + File.separatorChar + "config.yml", getDataFolder() + "" + File.separatorChar + "old_config.yml");
				} catch (Exception exception) {
					reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_COPY_FILE).error(exception));
				}
				saveResource("config.yml", true);

				try {
					config.load(new File(getDataFolder(), "config.yml"));
				} catch (Exception exception) {
					reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_LOAD_CONFIG).error(exception));
				}
				try {
					oldconfig.load(new File(getDataFolder(), "old_config.yml"));
				} catch (Exception exception) {
					reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_LOAD_CONFIG).error(exception));
				}
				config.set("auto_update_check", oldconfig.get("auto_update_check", true));
				config.set("debug", oldconfig.get("debug", false));
				config.set("lang", oldconfig.get("lang", "en_US"));
				config.set("double_shulker_chance.enabled", oldconfig.get("double_shulker_chance.enabled", true));
				config.set("double_shulker_chance.rate", oldconfig.get("double_shulker_chance.rate", 0.50));

				config.set("enderman_to_shulker_chance.enabled", oldconfig.get("enderman_to_shulker_chance.enabled", false));
				config.set("enderman_to_shulker_chance.rate", oldconfig.get("enderman_to_shulker_chance.rate", 0.75));
				config.set("enderman_to_shulker_chance.spawn_enderman_on_fail", oldconfig.get("enderman_to_shulker_chance.spawn_enderman_on_fail", true));
				config.set("radius_between_spawns", oldconfig.get("radius_between_spawns", 10));
				try {
					config.save(new File(getDataFolder(), "config.yml"));
				} catch (Exception exception) {
					reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_SAVE_CONFIG).error(exception));
				}
				LOGGER.log("config.yml has been updated");
			}
		}

		/** DEV check **/
		File jarfile = this.getFile().getAbsoluteFile();
		if(jarfile.toString().contains("-DEV")){
			debug = true;
			LOGGER.log("jarfile contains dev, debug set to true.");
		}
		getServer().getPluginManager().registerEvents(this, this);

		/** Update Checker */
		if(UpdateCheck){
			LOGGER.log("Checking for updates...");
			try {
				VersionChecker updater = new VersionChecker(this, projectID, githubURL);
				if(updater.checkForUpdates()) {
					/** Update available */
					UpdateAvailable = true; // TODO: Update Checker
					UColdVers = updater.oldVersion();
					UCnewVers = updater.newVersion();

					LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
					LOGGER.log("* " + get("sr.version.message").toString().replace("<MyPlugin>", THIS_NAME) );
					LOGGER.log("* " + get("sr.version.old_vers") + ChatColor.RED + UColdVers );
					LOGGER.log("* " + get("sr.version.new_vers") + ChatColor.GREEN + UCnewVers );
					LOGGER.log("*");
					LOGGER.log("* " + get("sr.version.please_update") );
					LOGGER.log("*");
					LOGGER.log("* " + get("sr.version.download") + ": " + DownloadLink + "/history");
					LOGGER.log("* " + get("sr.version.donate.message") + ": https://ko-fi.com/joelgodofwar");
					LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");

				}else{
					/** Up to date */
					LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
					LOGGER.log("* " + get("sr.version.curvers"));
					LOGGER.log("* " + get("sr.version.donate") + ": https://ko-fi.com/joelgodofwar");
					LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
					UpdateAvailable = false;
				}
			}catch(Exception exception) {
				/** Error */
				LOGGER.log(get("sr.version.update.error"));
				reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_UPDATE_PLUGIN).error(exception));
			}
		}else {
			/** auto_update_check is false so nag. */
			LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
			LOGGER.log("* " + get("sr.version.donate.message") + ": https://ko-fi.com/joelgodofwar");
			LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
		}

		String packageName = this.getServer().getClass().getPackage().getName();
		String version = Version.extractVersion(Bukkit.getServer().getVersion());
		Version current = new Version(this.getServer());
		LOGGER.debug("version=" + version);
		Version is_1_20 = new Version("1.20");
		if( current.atOrAbove(is_1_20)){
			getServer().getPluginManager().registerEvents( new CSEHandler_1_19(this), this);
		}else if( version.contains("1.17") ){
			getServer().getPluginManager().registerEvents( new CSEHandler_1_17(this), this);
		}else if( version.contains("1.18") ){
			getServer().getPluginManager().registerEvents( new CSEHandler_1_18(this), this);
		}else if( version.contains("1.19") ){
			getServer().getPluginManager().registerEvents( new CSEHandler_1_19(this), this);
		}else{
			LOGGER.warn(get("sr.message.notcompatible") + version);
			getServer().getPluginManager().disablePlugin(this);
		}

		consoleInfo(ChatColor.GREEN + "ENABLED" + ChatColor.RESET + " - Loading took " + LoadTime(startTime));

		try {
			//PluginBase plugin = this;
			Metrics metrics  = new Metrics(this, 6091);
			// New chart here
			// myPlugins()
			metrics.addCustomChart(new Metrics.AdvancedPie("my_other_plugins", new Callable<Map<String, Integer>>() {
				@Override
				public Map<String, Integer> call() throws Exception {
					Map<String, Integer> valueMap = new HashMap<>();

					if(getServer().getPluginManager().getPlugin("DragonDropElytra") != null){valueMap.put("DragonDropElytra", 1);}
					if(getServer().getPluginManager().getPlugin("NoEndermanGrief") != null){valueMap.put("NoEndermanGrief", 1);}
					if(getServer().getPluginManager().getPlugin("PortalHelper") != null){valueMap.put("PortalHelper", 1);}
					//if(getServer().getPluginManager().getPlugin("ShulkerRespawner") != null){valueMap.put("ShulkerRespawner", 1);}
					if(getServer().getPluginManager().getPlugin("MoreMobHeads") != null){valueMap.put("MoreMobHeads", 1);}
					if(getServer().getPluginManager().getPlugin("SilenceMobs") != null){valueMap.put("SilenceMobs", 1);}
					if(getServer().getPluginManager().getPlugin("SinglePlayerSleep") != null){valueMap.put("SinglePlayerSleep", 1);}
					if(getServer().getPluginManager().getPlugin("VillagerWorkstationHighlights") != null){valueMap.put("VillagerWorkstationHighlights", 1);}
					if(getServer().getPluginManager().getPlugin("RotationalWrench") != null){valueMap.put("RotationalWrench", 1);}
					return valueMap;
				}
			}));
			metrics.addCustomChart(new Metrics.SimplePie("auto_update_check", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getString("auto_update_check").toUpperCase();
				}
			}));
			metrics.addCustomChart(new Metrics.SimplePie("var_debug", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getString("debug").toUpperCase();
				}
			}));
			metrics.addCustomChart(new Metrics.SimplePie("var_lang", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getString("lang").toUpperCase();
				}
			}));
			metrics.addCustomChart(new Metrics.SimplePie("chance_double_shulker", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getString("chance_double_shulker").toUpperCase();
				}
			}));
			metrics.addCustomChart(new Metrics.SimplePie("chance_double_rate", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getString("chance_double_rate").toUpperCase();
				}
			}));
			metrics.addCustomChart(new Metrics.SimplePie("radius_between_spawns", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getString("radius_between_spawns").toUpperCase();
				}
			}));
		}catch (Exception exception){
			debug = true;
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_METRICS_LOAD_ERROR).error(exception));
		}


	}

	public static Plugin getPluginByName(String string)	{
		for(Plugin i : Bukkit.getPluginManager().getPlugins())
		{
			if(i.getName().toLowerCase().equals(string.toLowerCase()))
			{
				return i;
			}
		}

		for(Plugin i : Bukkit.getPluginManager().getPlugins())
		{
			if(i.getName().toLowerCase().contains(string.toLowerCase()))
			{
				return i;
			}
		}

		return null;
	}

	@Override // TODO: onDisable
	public void onDisable(){
		consoleInfo(ChatColor.BOLD + "DISABLED" + ChatColor.RESET);
	}

	public void consoleInfo(String state) {
		//LOGGER.log(ChatColor.YELLOW + "**************************************" + ChatColor.RESET);
		LOGGER.log(ChatColor.YELLOW + " v" + THIS_VERSION + ChatColor.RESET + " is " + state  + ChatColor.RESET);
		//LOGGER.log(ChatColor.YELLOW + "**************************************" + ChatColor.RESET);
	}

	public void broadcastmsg(String message){
		Bukkit.getServer().spigot().broadcast(new TextComponent(TextComponent.fromLegacyText(message)));
		SendJsonMessages.SendAllJsonMessage(message);
	}

	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		if(UpdateAvailable&&(player.isOp()||player.hasPermission("shulkerrespawner.showUpdateAvailable"))){
			// TODO: UpdateCheck onPlayerJoin
			String links = "[\"\",{\"text\":\"<Download>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"<DownloadLink>/history\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<please_update>\"}},{\"text\":\" \",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<please_update>\"}},{\"text\":\"| \"},{\"text\":\"<Donate>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://ko-fi.com/joelgodofwar\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<Donate_msg>\"}},{\"text\":\" | \"},{\"text\":\"<Notes>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"<DownloadLink>/updates\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<Notes_msg>\"}}]";
			links = links.replace("<DownloadLink>", DownloadLink).replace("<Download>", get("sr.version.download"))
					.replace("<Donate>", get("sr.version.donate")).replace("<please_update>", get("sr.version.please_update"))
					.replace("<Donate_msg>", get("sr.version.donate.message")).replace("<Notes>", get("sr.version.notes"))
					.replace("<Notes_msg>", get("sr.version.notes.message"));
			String versions = "" + ChatColor.GRAY + get("sr.version.new_vers") + ": " + ChatColor.GREEN + "{nVers}" + ChatColor.GRAY + " | " + get("sr.version.old_vers") + ": " + ChatColor.RED + "{oVers}";
			player.sendMessage("" + ChatColor.WHITE + get("sr.version.message").toString().replace("<MyPlugin>", ChatColor.GOLD + THIS_NAME + ChatColor.WHITE) );
			Utils.sendJson(player, links);
			player.sendMessage(versions.replace("{nVers}", UCnewVers).replace("{oVers}", UColdVers));
		}

		if(player.getDisplayName().equals("JoelYahwehOfWar")||player.getDisplayName().equals("JoelGodOfWar")){
			player.sendMessage(THIS_NAME + " " + THIS_VERSION + " Hello father!");
		}
	}
	public void sendJson(Player player, String string) {
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tellraw \"" + player.getName() +
				"\" " + string);
	}

	public boolean SpawnIt(double chancepercent){
		if(!getConfig().getBoolean("enderman_to_shulker_chance.enabled", false)) {
			LOGGER.debug("SI  enderman_to_shulker_chance.enabled=false, returning trueline:344");
			return true;
		}
		double chance = Math.random();
		LOGGER.debug("SI chance=" + chance + " line:348");
		LOGGER.debug("SI chancepercent=" + chancepercent + " line:349");
		if (chancepercent > chance){
			return true;
		}
		return false;
	}

	public boolean checkradius(Entity entity, int radius){
		Block block = entity.getLocation().getBlock();
		for(Entity en : block.getWorld().getEntities()) {
			if(en instanceof Shulker) {
				Shulker shulker = (Shulker) en;
				double distance = shulker.getLocation().distance(block.getLocation());
				if(distance < radius) {
					return true;
					//shulker.teleport(block.getLocation());
				}
			}
		}
		return false;
	}

	@EventHandler
	public void onEntityDeathEvent(EntityDeathEvent event){
		if(getConfig().getBoolean("double_shulker_chance.enabled", true)){
			if(event.getEntity() instanceof Shulker){
				//if(debug){logDebug("Shulker killed by " + event.getEntity().getKiller().getName());}
				if(event.getEntity().getKiller() instanceof Player){
					LOGGER.debug("Killer was a player");
					ItemStack shulkershell = new ItemStack(Material.SHULKER_SHELL, 1);
					//event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), shulkershell);
					ItemStack itemstack = event.getEntity().getKiller().getInventory().getItemInMainHand();
					if(itemstack != null){
						LOGGER.debug("itemstack=" + itemstack.getType().toString() + " line:290");
						int enchantmentlevel = itemstack.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);//.containsEnchantment(Enchantment.LOOT_BONUS_MOBS);
						LOGGER.debug("enchantmentlevel=" + enchantmentlevel + " line:292");
						double enchantmentlevelpercent = ((double)enchantmentlevel / 100);
						LOGGER.debug("enchantmentlevelpercent=" + enchantmentlevelpercent + " line:294");
						double chance = Math.random();
						LOGGER.debug("chance=" + chance + " line:296");
						double chancepercent = getConfig().getDouble("double_shulker_chance.rate", 0.50); /** Set to check config.yml later*/ // TODO:
						LOGGER.debug("chancepercent=" + chancepercent + " line:298");
						chancepercent = chancepercent + enchantmentlevelpercent;
						LOGGER.debug("chancepercent2=" + chancepercent + " line:300");
						//if(chancepercent > 0.00 && chancepercent < 0.99){
						if (chancepercent > chance){
							if(event.getDrops().contains(shulkershell)){
								event.getDrops().add(shulkershell);
								//broadcastmsg("");
								LOGGER.debug(ChatColor.GREEN + "Shulker shell added to Drops" + ChatColor.RESET);
							}else{
								event.getDrops().add(shulkershell);
								event.getDrops().add(shulkershell);
								LOGGER.debug(ChatColor.GREEN + "2 Shulker shells added to Drops" + ChatColor.RESET);
							}

							//event.getDrops().add(new ItemStack(Material.CREEPER_HEAD, 1));
						}
					}
				}
			}
		}
	}

	public boolean isEndCity (Block block){ // &&isEndCity(entity.getLocation().getBlock())
		Location block1;
		int daCount = 0;
		block1 = block.getLocation(); // Entity block
		if(block1.getBlock().getType().toString().contains("PURPUR")){daCount++;}
		block1 = block1.getBlock().getLocation().add(1, 0, 0); // move forward
		if(block1.getBlock().getType().toString().contains("PURPUR")){daCount++;}
		block1 = block1.getBlock().getLocation().add(0, 0, 1); // move right .add(, , )
		if(block1.getBlock().getType().toString().contains("PURPUR")){daCount++;}
		block1 = block1.getBlock().getLocation().subtract(1, 0, 0); // move back .getLocation().subtract(1, 0, 0) .subtract(, , )
		if(block1.getBlock().getType().toString().contains("PURPUR")){daCount++;}
		block1 = block1.getBlock().getLocation().subtract(1, 0, 0); // move back .getLocation().subtract(2, 0, 0)
		if(block1.getBlock().getType().toString().contains("PURPUR")){daCount++;}
		block1 = block1.getBlock().getLocation().subtract(0, 0, 1); // move left .getLocation().subtract(0, 0, 1)
		if(block1.getBlock().getType().toString().contains("PURPUR")){daCount++;}
		block1 = block1.getBlock().getLocation().subtract(0, 0, 1); // move left .getLocation().subtract(0, 0, 2)
		if(block1.getBlock().getType().toString().contains("PURPUR")){daCount++;}
		block1 = block1.getBlock().getLocation().add(1, 0, 0); // move forward
		if(block1.getBlock().getType().toString().contains("PURPUR")){daCount++;}
		block1 = block1.getBlock().getLocation().add(1, 0, 0); // move forward
		if(block1.getBlock().getType().toString().contains("PURPUR")){daCount++;}
		if(daCount >= 3){
			return true;
		}
		block1 = block.getLocation().subtract(0, 1, 0); // Entity block
		if(block1.getBlock().getType().toString().contains("PURPUR")){daCount++;}
		block1 = block1.getBlock().getLocation().add(1, 0, 0); // move forward
		if(block1.getBlock().getType().toString().contains("PURPUR")){daCount++;}
		block1 = block1.getBlock().getLocation().add(0, 0, 1); // move right .add(, , )
		if(block1.getBlock().getType().toString().contains("PURPUR")){daCount++;}
		block1 = block1.getBlock().getLocation().subtract(1, 0, 0); // move back .getLocation().subtract(1, 0, 0) .subtract(, , )
		if(block1.getBlock().getType().toString().contains("PURPUR")){daCount++;}
		block1 = block1.getBlock().getLocation().subtract(1, 0, 0); // move back .getLocation().subtract(2, 0, 0)
		if(block1.getBlock().getType().toString().contains("PURPUR")){daCount++;}
		block1 = block1.getBlock().getLocation().subtract(0, 0, 1); // move left .getLocation().subtract(0, 0, 1)
		if(block1.getBlock().getType().toString().contains("PURPUR")){daCount++;}
		block1 = block1.getBlock().getLocation().subtract(0, 0, 1); // move left .getLocation().subtract(0, 0, 2)
		if(block1.getBlock().getType().toString().contains("PURPUR")){daCount++;}
		block1 = block1.getBlock().getLocation().add(1, 0, 0); // move forward
		if(block1.getBlock().getType().toString().contains("PURPUR")){daCount++;}
		block1 = block1.getBlock().getLocation().add(1, 0, 0); // move forward
		if(block1.getBlock().getType().toString().contains("PURPUR")){daCount++;}
		if(daCount >= 3){
			return true;
		}
		return false;
	}

	@SuppressWarnings("unused")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		//Player p = (Player)sender;
		if (cmd.getName().equalsIgnoreCase("SR")){
			if (args.length == 0){
				/** Command code */
				sender.sendMessage(ChatColor.GREEN + "[]===============[" + ChatColor.YELLOW + "ShulkerRespawner" + ChatColor.GREEN + "]===============[]");
				if(sender.isOp()||sender.hasPermission("shulkerrespawner.reload")||!(sender instanceof Player)){
					sender.sendMessage(ChatColor.WHITE + " /sr reload - " + get("sr.command.reload"));//subject to server admin approval");
				}
				if(sender.hasPermission("shulkerrespawner.toggledebug")||!(sender instanceof Player)){
					sender.sendMessage(ChatColor.WHITE + " /sr toggledebug - " + get("sr.message.toggledebug"));//Cancels SinglePlayerSleep");
				}
				if(sender.hasPermission("shulkerrespawner.showUpdateAvailable")||!(sender instanceof Player)){
					sender.sendMessage(ChatColor.RESET + " /sr update - " + get("sr.command.update"));//Checks if there is an update.
				}
				sender.sendMessage(ChatColor.GREEN + "[]===============[" + ChatColor.YELLOW + "ShulkerRespawner" + ChatColor.GREEN + "]===============[]");
				return true;
			}
			if(args[0].equalsIgnoreCase("reload")){
				if(sender.isOp()||sender.hasPermission("shulkerrespawner.reload")||!(sender instanceof Player)){
					//ConfigAPI.Reloadconfig(this, p);

					this.onReload();
					sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("sr.message.reloaded").replace("<MyPlugin>", THIS_NAME));
					return true;
				}else if(!sender.hasPermission("shulkerrespawner.reload")){
					sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("sr.message.noperm"));
					return false;
				}
			}
			if(args[0].equalsIgnoreCase("toggledebug")||args[0].equalsIgnoreCase("td")){
				if(sender.isOp()||sender.hasPermission("shulkerrespawner.toggledebug")||!(sender instanceof Player)){
					debug = !debug;
					sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("sr.message.debugtrue").toString().replace("<boolean>", "" + debug));
					return true;
				}else if(!sender.hasPermission("shulkerrespawner.toggledebug")){
					sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("sr.message.noperm"));
					return false;
				}
			}
			if(args[0].equalsIgnoreCase("update")){ // TODO: Command update
				if(!(sender instanceof Player)) {
					/** Console */
					try {
						VersionChecker updater = new VersionChecker(this, projectID, githubURL);
						if(updater.checkForUpdates()) {
							/** Update available */
							UpdateAvailable = true; // TODO: Update Checker
							UColdVers = updater.oldVersion();
							UCnewVers = updater.newVersion();

							LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
							LOGGER.log("* " + get("sr.version.message").toString().replace("<MyPlugin>", THIS_NAME) );
							LOGGER.log("* " + get("sr.version.old_vers") + ChatColor.RED + UColdVers );
							LOGGER.log("* " + get("sr.version.new_vers") + ChatColor.GREEN + UCnewVers );
							LOGGER.log("*");
							LOGGER.log("* " + get("sr.version.please_update") );
							LOGGER.log("*");
							LOGGER.log("* " + get("sr.version.download") + ": " + DownloadLink + "/history");
							LOGGER.log("* " + get("sr.version.donate.message") + ": https://ko-fi.com/joelgodofwar");
							LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");

						}else{
							/** Up to date */
							LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
							LOGGER.log("* " + get("sr.version.curvers"));
							LOGGER.log("* " + get("sr.version.donate") + ": https://ko-fi.com/joelgodofwar");
							LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
							UpdateAvailable = false;
						}
					}catch(Exception exception) {
						reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_UPDATE_PLUGIN).error(exception));
					}
					/** end update checker */
					return true;
				}
				if((sender.isOp()||sender.hasPermission("shulkerrespawner.showUpdateAvailable"))){
					BukkitTask updateTask = this.getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
						@Override public void run() {
							try {
								Bukkit.getConsoleSender().sendMessage("Checking for updates...");
								VersionChecker updater = new VersionChecker(THIS_VERSION, projectID, githubURL);
								if(updater.checkForUpdates()) {
									UpdateAvailable = true;
									UColdVers = updater.oldVersion();
									UCnewVers = updater.newVersion();
									String links = "[\"\",{\"text\":\"<Download>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"<DownloadLink>/history\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<please_update>\"}},{\"text\":\" \",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<please_update>\"}},{\"text\":\"| \"},{\"text\":\"<Donate>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://ko-fi.com/joelgodofwar\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<Donate_msg>\"}},{\"text\":\" | \"},{\"text\":\"<Notes>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"<DownloadLink>/updates\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<Notes_msg>.\"}}]";
									links = links.replace("<DownloadLink>", DownloadLink).replace("<Download>", get("sr.version.download"))
											.replace("<Donate>", get("sr.version.donate")).replace("<please_update>", get("sr.version.please_update"))
											.replace("<Donate_msg>", get("sr.version.donate.message")).replace("<Notes>", get("sr.version.notes"))
											.replace("<Notes_msg>", get("sr.version.notes.message"));
									String versions = "" + ChatColor.GRAY + get("sr.version.new_vers") + ": " + ChatColor.GREEN + "{nVers} | " + get("sr.version.old_vers") + ": " + ChatColor.RED + "{oVers}";
									sender.sendMessage("" + ChatColor.GRAY + get("sr.version.message").toString().replace("<MyPlugin>", ChatColor.GOLD + THIS_NAME + ChatColor.GRAY) );
									Utils.sendJson(sender, links);
									sender.sendMessage(versions.replace("{nVers}", UCnewVers).replace("{oVers}", UColdVers));
								}else{
									String links = "{\"text\":\"<Donate>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://ko-fi.com/joelgodofwar\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<Donate_msg>\"}}";
									links = links.replace("<Donate>", get("sr.version.donate")).replace("<Donate_msg>", get("sr.version.donate.message"));
									Utils.sendJson(sender, links);
									sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " v" + THIS_VERSION + ChatColor.RESET + " " + get("sr.version.curvers") + ChatColor.RESET);
									UpdateAvailable = false;
								}
							}catch(Exception exception) {
								sender.sendMessage(ChatColor.RED + get("sr.version.update.error"));
								reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_UPDATE_PLUGIN).error(exception));
							}
						}
					});
					return true;
				}else{
					sender.sendMessage(ChatColor.YELLOW + THIS_NAME + " " + get("sr.message.noperm"));
					return false;
				}
			}
			if(args[0].equalsIgnoreCase("check")){ // TODO: Shulker Check command
				if(!(sender instanceof Player)) {
					return false;
				}
				return false;/**
		    	  Player player = (Player) sender;
		    	  List<Entity> eList = player.getNearbyEntities(100, 100, 100);
		    	  log("eList=" + eList.toString());
		    	for (Entity e : eList) {
      				if(e instanceof Shulker) {
      					log("PD=\"" + e.getPersistentDataContainer().get(NAME_KEY, PersistentDataType.STRING) + "\"");
      					if(e.getPersistentDataContainer().get(NAME_KEY, PersistentDataType.STRING) == "ShulkerRespawner") {
      						e.setGlowing(true);
      					}
      				}
		    	}

		    	player.sendMessage("Shulkers that have been spawned using ShulkerRespawner have been given Glowing Effect for 30 seconds.");

		    	Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
		    		for (Entity e : eList) {
			    		if(e instanceof Shulker) {
			    			if(e.getPersistentDataContainer().get(NAME_KEY, PersistentDataType.STRING) == "ShulkerRespawner") {
	      						e.setGlowing(false);
	      					}
			    		}
			    	}
		    		player.sendMessage("Shulkers that have been spawned using ShulkerRespawner have had Glowing Effect removed.");
	                }
		    	, 600);//*/


			}
			if (args[0].equalsIgnoreCase("glow")){
				if(!(sender instanceof Player)) {
					Player player = (Player) sender;
					if(player.getDisplayName().equals("JoelYahwehOfWar")||player.getDisplayName().equals("JoelGodOfWar")){
						player.getServer().dispatchCommand(Bukkit.getConsoleSender(), "effect give @e[type=minecraft:shulker] minecraft:glowing infinite");
						return true;
					}
				}else {
					Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "effect give @e[type=minecraft:shulker] minecraft:glowing infinite");
					return true;
				}
			}
			if (args[0].equalsIgnoreCase("glowoff")){
				if(!(sender instanceof Player)) {
					Player player = (Player) sender;
					if(player.getDisplayName().equals("JoelYahwehOfWar")||player.getDisplayName().equals("JoelGodOfWar")){
						player.getServer().dispatchCommand(Bukkit.getConsoleSender(), "effect clear @e[type=minecraft:shulker] minecraft:glowing");
						return true;
					}
				}else {
					Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "effect clear @e[type=minecraft:shulker] minecraft:glowing");
					return true;
				}
			}
			if (args[0].equalsIgnoreCase("enderman")){
				if(!(sender instanceof Player)) {
					Player player = (Player) sender;
					if(player.getDisplayName().equals("JoelYahwehOfWar")||player.getDisplayName().equals("JoelGodOfWar")){
						player.getServer().dispatchCommand(Bukkit.getConsoleSender(), "kill @e[type=minecraft:enderman]");
						return true;
					}
				}else {
					Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "kill @e[type=minecraft:enderman]");
					return true;
				}
			}
			if (args[0].equalsIgnoreCase("shulker")){
				if(!(sender instanceof Player)) {
					Player player = (Player) sender;
					if(player.getDisplayName().equals("JoelYahwehOfWar")||player.getDisplayName().equals("JoelGodOfWar")){
						player.getServer().dispatchCommand(Bukkit.getConsoleSender(), "kill @e[type=minecraft:shulker]");
						return true;
					}
				}else {
					Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "kill @e[type=minecraft:shulker]");
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) { // TODO: Tab Complete
		if (command.getName().equalsIgnoreCase("SR")) {
			List<String> autoCompletes = new ArrayList<>(); //create a new string list for tab completion
			if (args.length == 1) { // reload, toggledebug, playerheads, customtrader, headfix
				if(sender.isOp()||sender.hasPermission("shulkerrespawner.reload")||!(sender instanceof Player)){
					autoCompletes.add("reload");
				}
				if(sender.hasPermission("shulkerrespawner.toggledebug")||!(sender instanceof Player)){
					autoCompletes.add("toggledebug");
				}
				if(sender.hasPermission("shulkerrespawner.showUpdateAvailable")||!(sender instanceof Player)){
					autoCompletes.add("update");
				}
				return autoCompletes; // then return the list
			}
		}
		return null;
	}

	public static void copyFile_Java7(String origin, String destination) throws IOException {
		Path FROM = Paths.get(origin);
		Path TO = Paths.get(destination);
		//overwrite the destination file if it exists, and copy
		// the file attributes, including the rwx permissions
		CopyOption[] options = new CopyOption[]{
				StandardCopyOption.REPLACE_EXISTING,
				StandardCopyOption.COPY_ATTRIBUTES
		};
		Files.copy(FROM, TO, options);
	}

	public static String getMCVersion() {
		String strVersion = Bukkit.getVersion();
		strVersion = strVersion.substring(strVersion.indexOf("MC: "), strVersion.length());
		strVersion = strVersion.replace("MC: ", "").replace(")", "");
		return strVersion;
	}

	public void onReload() {
		long startTime = System.currentTimeMillis();
		UpdateCheck = getConfig().getBoolean("auto_update_check", true);
		debug = getConfig().getBoolean("debug", false);
		daLang = getConfig().getString("lang", "en_US");
		oldconfig = new YamlConfiguration();

		LOGGER.log(ChatColor.YELLOW + "**************************************" + ChatColor.RESET);
		LOGGER.log(ChatColor.GREEN + THIS_NAME + " v" + THIS_VERSION + ChatColor.RESET + " Reloading...");

		LOGGER.log("Loading config file...");
		/**  Check for config */
		try{
			if(!this.getDataFolder().exists()){
				LOGGER.log("Data Folder doesn't exist");
				LOGGER.log("Creating Data Folder");
				this.getDataFolder().mkdirs();
				LOGGER.log("Data Folder Created at " + this.getDataFolder());
			}
			File  file = new File(this.getDataFolder(), "config.yml");
			this.getLogger().info("" + file);
			if(!file.exists()){
				LOGGER.log("config.yml not found, creating!");
				saveResource("config.yml", true);
			}
		}catch(Exception exception){
			debug = true;
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_CHECK_CONFIG).error(exception));
			LOGGER.debug("debug has been set to true due to an exception.");
		}
		/**  Check config version */
		try {
			oldconfig.load(new File(getDataFolder(), "config.yml"));
		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_LOAD_CONFIG).error(exception));
		}
		String checkconfigversion = oldconfig.getString("version", "1.0.0");
		if(checkconfigversion != null){
			if(!checkconfigversion.equalsIgnoreCase(configVersion)){
				try {
					copyFile_Java7(getDataFolder() + "" + File.separatorChar + "config.yml", getDataFolder() + "" + File.separatorChar + "old_config.yml");
				} catch (Exception exception) {
					reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_SAVE_CONFIG).error(exception));
				}
				saveResource("config.yml", true);

				try {
					config.load(new File(getDataFolder(), "config.yml"));
				} catch (Exception exception) {
					reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_LOAD_CONFIG).error(exception));
				}
				try {
					oldconfig.load(new File(getDataFolder(), "old_config.yml"));
				} catch (Exception exception) {
					reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_LOAD_CONFIG).error(exception));
				}
				config.set("auto_update_check", oldconfig.get("auto_update_check", true));
				config.set("debug", oldconfig.get("debug", false));
				config.set("lang", oldconfig.get("lang", "en_US"));
				config.set("double_shulker_chance.enabled", oldconfig.get("double_shulker_chance.enabled", true));
				config.set("double_shulker_chance.rate", oldconfig.get("double_shulker_chance.rate", 0.50));

				config.set("enderman_to_shulker_chance.enabled", oldconfig.get("enderman_to_shulker_chance.enabled", false));
				config.set("enderman_to_shulker_chance.rate", oldconfig.get("enderman_to_shulker_chance.rate", 0.75));
				config.set("enderman_to_shulker_chance.spawn_enderman_on_fail", oldconfig.get("enderman_to_shulker_chance.spawn_enderman_on_fail", true));
				config.set("radius_between_spawns", oldconfig.get("radius_between_spawns", 10));
				try {
					config.save(new File(getDataFolder(), "config.yml"));
				} catch (Exception exception) {
					reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_SAVE_CONFIG).error(exception));
				}
				LOGGER.log("config.yml has been updated");
			}
		}

		/** DEV check **/
		File jarfile = this.getFile().getAbsoluteFile();
		if(jarfile.toString().contains("-DEV")){
			debug = true;
			LOGGER.log("jarfile contains -DEV, debug set to true.");
		}

		String packageName = this.getServer().getClass().getPackage().getName();
		String version = packageName.substring(packageName.lastIndexOf('.') + 2);
		LOGGER.debug("version=" + version);

		if( version.contains("1_17_R1") ){
			getServer().getPluginManager().registerEvents( new CSEHandler_1_17(this), this);
		}else if( version.contains("1_18_R") ){
			getServer().getPluginManager().registerEvents( new CSEHandler_1_18(this), this);
		}else if( version.contains("1_19_R") ){
			getServer().getPluginManager().registerEvents( new CSEHandler_1_19(this), this);
		}else if( version.contains("1_20_R") ){
			getServer().getPluginManager().registerEvents( new CSEHandler_1_19(this), this);
		}else{
			LOGGER.warn(get("sr.message.notcompatible") + version);
			getServer().getPluginManager().disablePlugin(this);
		}

		consoleInfo(ChatColor.GREEN + "ENABLED" + ChatColor.RESET + " - Reloading took " + LoadTime(startTime));
	}

	public String MCVersion(String string) {
		switch (string){
		case "1.13":
			return "1_13_R1";
		case "1.13.1":
			return "1_13_R2";
		case "1.14":
			return "1_14_R1";
		case "1.15":
			return "1_15_R1";
		case "1.16":
			return "1_16_R1";
		case "1.16.1":
			return "1_16_R2";
		case "1.16.2":
			return "1_16_R3";
		case "1.17":
			return "1_17_R1";
		case "1.17.1":
			return "1_17_1_R1";
		case "1.18":
			return "1_18_R1";
		case "1.18.1":
			return "1_18_1_R1";
		case "1.19":
			return "1_19_R1";

		}
		return string;
	}

	public String LoadTime(long startTime) {
		long elapsedTime = System.currentTimeMillis() - startTime;
		long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60;
		long milliseconds = elapsedTime % 1000;

		if (minutes > 0) {
			return String.format("%d min %d s %d ms.", minutes, seconds, milliseconds);
		} else if (seconds > 0) {
			return String.format("%d s %d ms.", seconds, milliseconds);
		} else {
			return String.format("%d ms.", elapsedTime);
		}
	}

	@SuppressWarnings("static-access")
	public String get(String key, String... defaultValue) {
		return lang2.get(key, defaultValue);
	}

	public boolean isPluginRequired(String pluginName) {
		String[] requiredPlugins = {"SinglePlayerSleep", "MoreMobHeads", "NoEndermanGrief", "ShulkerRespawner", "DragonDropElytra", "RotationalWrench", "SilenceMobs", "VillagerWorkstationHighlights"};
		for (String requiredPlugin : requiredPlugins) {
			if ((getServer().getPluginManager().getPlugin(requiredPlugin) != null) && getServer().getPluginManager().isPluginEnabled(requiredPlugin)) {
				if (requiredPlugin.equals(pluginName)) {
					return true;
				} else {
					return false;
				}
			}
		}
		return true;
	}

	public String MCName() {
		return Bukkit.getName();
	}

	// Used to check Minecraft version
	private Version verifyMinecraftVersion() {
		Version minimum = new Version(PluginLibrary.MINIMUM_MINECRAFT_VERSION);
		Version maximum = new Version(PluginLibrary.MAXIMUM_MINECRAFT_VERSION);
		try {
			Version current = new Version(this.getServer());

			// We'll just warn the user for now
			if (current.compareTo(minimum) < 0) {
				LOGGER.warn("Version " + current + " is lower than the minimum " + minimum);
			}
			if (current.compareTo(maximum) > 0) {
				LOGGER.warn(ChatColor.RED + "Version " + current + " has not yet been tested! Proceed with caution." + ChatColor.RESET);
			}

			return current;
		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_PARSE_MINECRAFT_VERSION).error(exception).messageParam(maximum));
			// Unknown version - just assume it is the latest
			return maximum;
		}
	}

	public String getjarfilename() {
		return jarfilename;
	}

	public boolean getDebug() {
		return debug;
	}

	public static ShulkerRespawner getInstance() {
		return getPlugin(ShulkerRespawner.class);
	}

}