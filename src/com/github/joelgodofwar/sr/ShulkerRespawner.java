package com.github.joelgodofwar.sr;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.CodeSource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
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

import com.github.joelgodofwar.sr.events.CSEHandler_1_13_1;
import com.github.joelgodofwar.sr.events.CSEHandler_1_13_2;
import com.github.joelgodofwar.sr.events.CSEHandler_1_14;
import com.github.joelgodofwar.sr.events.CSEHandler_1_15_1;
import com.github.joelgodofwar.sr.events.CSEHandler_1_16_1;
import com.github.joelgodofwar.sr.events.CSEHandler_1_16_2;
import com.github.joelgodofwar.sr.events.CSEHandler_1_16_3;
import com.github.joelgodofwar.sr.events.CSEHandler_1_17;
import com.github.joelgodofwar.sr.events.CSEHandler_1_18;
import com.github.joelgodofwar.sr.events.CSEHandler_1_19;
import com.github.joelgodofwar.sr.i18n.Translator;
import com.github.joelgodofwar.sr.util.Ansi;
import com.github.joelgodofwar.sr.util.JarUtils;
import com.github.joelgodofwar.sr.util.Metrics;
import com.github.joelgodofwar.sr.util.SendJsonMessages;
import com.github.joelgodofwar.sr.util.Utils;
import com.github.joelgodofwar.sr.util.VersionChecker;
import com.github.joelgodofwar.sr.util.YmlConfiguration;

public class ShulkerRespawner  extends JavaPlugin implements Listener{
	/** Languages: čeština (cs_CZ), Deutsch (de_DE), English (en_US), Español (es_ES), Español (es_MX), Français (fr_FR), Italiano (it_IT), Magyar (hu_HU), 日本語 (ja_JP), 한국어 (ko_KR), Lolcat (lol_US), Melayu (my_MY), Nederlands (nl_NL), Polski (pl_PL), Português (pt_BR), Русский (ru_RU), Svenska (sv_SV), Türkçe (tr_TR), 中文(简体) (zh_CN), 中文(繁體) (zh_TW) */
	public final static Logger logger = Logger.getLogger("Minecraft");
	static String THIS_NAME;
	static String THIS_VERSION;
	/** update checker variables */
	public int projectID = 73638; // https://spigotmc.org/resources/71236
	public String githubURL = "https://github.com/JoelGodOfwar/ShulkerRespawner/raw/master/versioncheck/1.13/versions.xml";
	boolean UpdateAvailable =  false;
	public String UColdVers;
	public String UCnewVers;
	public static boolean UpdateCheck;
	public String DownloadLink = "https://www.spigotmc.org/resources/shulkerrespawner.73638";
	/** end update checker variables */
	public static String daLang;
	public static boolean debug;
	File langFile;
    FileConfiguration lang;
    YmlConfiguration config = new YmlConfiguration();
	YamlConfiguration oldconfig = new YamlConfiguration();
	public final NamespacedKey NAME_KEY = new NamespacedKey(this, "shulker");
	String configVersion = "1.0.1";
	String pluginName = THIS_NAME;
	Translator lang2;
	private Set<String> triggeredPlayers = new HashSet<>();
	
	@Override // TODO: onEnable
	public void onEnable(){
		long startTime = System.currentTimeMillis();
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
		
		logger.info(Ansi.YELLOW + "**************************************" + Ansi.RESET);
		logger.info(Ansi.GREEN + THIS_NAME + " v" + THIS_VERSION + Ansi.RESET + " Loading...");
	log(": Loading config file...");
		/**  Check for config */
		try{
			if(!this.getDataFolder().exists()){
				log(": Data Folder doesn't exist");
				log(": Creating Data Folder");
				this.getDataFolder().mkdirs();
				log(": Data Folder Created at " + this.getDataFolder());
			}
			File  file = new File(this.getDataFolder(), "config.yml");
			this.getLogger().info("" + file);
			if(!file.exists()){
				log(": config.yml not found, creating!");
				saveResource("config.yml", true);
			}
		}catch(Exception e){
			debug = true;
			if(debug){log("debug has been set to true due to an exception error.");}
			e.printStackTrace();
		}
		/**  Check config version */
		try {
			oldconfig.load(new File(getDataFolder(), "config.yml"));
		} catch (IOException | InvalidConfigurationException e1) {
			logWarn("Could not load config.yml");
			e1.printStackTrace();
		}
		String checkconfigversion = oldconfig.getString("version", "1.0.0");
		if(checkconfigversion != null){
			if(!checkconfigversion.equalsIgnoreCase(configVersion)){
				try {
					copyFile_Java7(getDataFolder() + "" + File.separatorChar + "config.yml", getDataFolder() + "" + File.separatorChar + "old_config.yml");
				} catch (IOException e) {
					e.printStackTrace();
				}
				saveResource("config.yml", true);
				
				try {
					config.load(new File(getDataFolder(), "config.yml"));
				} catch (IOException | InvalidConfigurationException e1) {
					logWarn("Could not load config.yml");
					e1.printStackTrace();
				}
				try {
					oldconfig.load(new File(getDataFolder(), "old_config.yml"));
				} catch (IOException | InvalidConfigurationException e1) {
					e1.printStackTrace();
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
				} catch (IOException e) {
					logWarn("Could not save old settings to config.yml");
					e.printStackTrace();
				}
				log(": config.yml Updated! old config saved as old_config.yml");
			}
		}
		
		/** Lang file check */
		/**if(debug){logDebug("datafolder=" + getDataFolder());}
		langFile = new File(getDataFolder() + "" + File.separatorChar + "lang" + File.separatorChar, daLang + ".yml");//\
		if(debug){logDebug("langFilePath=" + langFile.getPath());}
		if(!langFile.exists()){                                  // checks if the yaml does not exist
			langFile.getParentFile().mkdirs();                  // creates the /plugins/<pluginName>/ directory if not found
			saveResource("lang" + File.separatorChar + "cs_CZ.yml", true);
			saveResource("lang" + File.separatorChar + "de_DE.yml", true);
			saveResource("lang" + File.separatorChar + "en_US.yml", true);
			saveResource("lang" + File.separatorChar + "fr_FR.yml", true);
			saveResource("lang" + File.separatorChar + "lol_US.yml", true);
			saveResource("lang" + File.separatorChar + "nl_NL.yml", true);
			saveResource("lang" + File.separatorChar + "pt_BR.yml", true);
			saveResource("lang" + File.separatorChar + "zh_CN.yml", true);
			log("Updating lang files! copied cs_CZ.yml, de_DE.yml, en_US.yml, fr_FR.yml, lol_US.yml, nl_NL.yml, pt_BR.yml, and zh_CN.yml to "
			+ getDataFolder() + "" + File.separatorChar + "lang");
			//ConfigAPI.copy(getResource("lang.yml"), langFile); // copies the yaml from your jar to the folder /plugin/<pluginName>
        }
		lang = new YamlConfiguration();
		try {
			lang.load(langFile);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
		String checklangversion = lang.getString("version", "1.0.0");
		if(checklangversion != null){
			if(!checklangversion.equalsIgnoreCase("1.0.2")){
				saveResource("lang" + File.separatorChar + "cs_CZ.yml", true);
				saveResource("lang" + File.separatorChar + "de_DE.yml", true);
				saveResource("lang" + File.separatorChar + "en_US.yml", true);
				saveResource("lang" + File.separatorChar + "fr_FR.yml", true);
				saveResource("lang" + File.separatorChar + "lol_US.yml", true);
				saveResource("lang" + File.separatorChar + "nl_NL.yml", true);
				saveResource("lang" + File.separatorChar + "pt_BR.yml", true);
				saveResource("lang" + File.separatorChar + "zh_CN.yml", true);
				log("Updating lang files! copied cs_CZ.yml, de_DE.yml, en_US.yml, fr_FR.yml, lol_US.yml, nl_NL.yml, pt_BR.yml, and zh_CN.yml to "
						+ getDataFolder() + "" + File.separatorChar + "lang");
			}
		}else{
			saveResource("lang" + File.separatorChar + "cs_CZ.yml", true);
			saveResource("lang" + File.separatorChar + "de_DE.yml", true);
			saveResource("lang" + File.separatorChar + "en_US.yml", true);
			saveResource("lang" + File.separatorChar + "fr_FR.yml", true);
			saveResource("lang" + File.separatorChar + "lol_US.yml", true);
			saveResource("lang" + File.separatorChar + "nl_NL.yml", true);
			saveResource("lang" + File.separatorChar + "pt_BR.yml", true);
			saveResource("lang" + File.separatorChar + "zh_CN.yml", true);
			log("Updating lang files! copied cs_CZ.yml, de_DE.yml, en_US.yml, fr_FR.yml, lol_US.yml, nl_NL.yml, pt_BR.yml, and zh_CN.yml to "
					+ getDataFolder() + "" + File.separatorChar + "lang");
		}//*/
		/** Lang file check */
		
		/** DEV check **/
		File jarfile = this.getFile().getAbsoluteFile();
		if(jarfile.toString().contains("-DEV")){
			debug = true;
			log("jarfile contains dev, debug set to true.");
		}
		getServer().getPluginManager().registerEvents(this, this);
		
		if(getConfig().getBoolean("debug")==true&&!(jarfile.toString().contains("-DEV"))){
			logDebug("Config.yml dump");
			logDebug("auto_update_check=" + getConfig().getBoolean("auto_update_check"));
			logDebug("debug=" + getConfig().getBoolean("debug"));
			logDebug("lang=" + getConfig().getString("lang"));
			logDebug("chance_double_shulker=" + getConfig().getBoolean("chance_double_shulker"));
			logDebug("chance_double_rate=" + getConfig().get("chance_double_rate"));
			logDebug("radius_between_spawns=" + getConfig().get("radius_between_spawns"));
		}
		
		/** Update Checker */
		if(UpdateCheck){
			log("Checking for updates...");
			try {
				VersionChecker updater = new VersionChecker(this, projectID, githubURL);
				if(updater.checkForUpdates()) {
					/** Update available */
					UpdateAvailable = true; // TODO: Update Checker
					UColdVers = updater.oldVersion();
					UCnewVers = updater.newVersion();
					
					log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
					log("* " + get("sr.version..message").toString().replace("<MyPlugin>", THIS_NAME) );
					log("* " + get("sr.version..old_vers") + ChatColor.RED + UColdVers );
					log("* " + get("sr.version..new_vers") + ChatColor.GREEN + UCnewVers );
					log("*");
					log("* " + get("sr.version..please_update") );
					log("*");
					log("* " + get("sr.version..download") + ": " + DownloadLink + "/history");
					log("* " + get("sr.version..donate.message") + ": https://ko-fi.com/joelgodofwar");
					log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");

				}else{
					/** Up to date */
					log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
					log("* " + get("sr.version.curvers"));
					log("* " + get("sr.version.donate") + ": https://ko-fi.com/joelgodofwar");
					log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
					UpdateAvailable = false;
				}
			}catch(Exception e) {
				/** Error */
				log(get("sr.version.update.error"));
				e.printStackTrace();
			}
		}else {
			/** auto_update_check is false so nag. */
			log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
			log("* " + get("sr.version.donate.message") + ": https://ko-fi.com/joelgodofwar");
			log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
		}

		String packageName = this.getServer().getClass().getPackage().getName();
    	String version = packageName.substring(packageName.lastIndexOf('.') + 2);
    	if(debug)logDebug("version=" + version);
    	if( version.contains("1_13_R1") ){
    		getServer().getPluginManager().registerEvents( new CSEHandler_1_13_1(this), this);
		}else if( version.contains("1_13_R2") ){
    		getServer().getPluginManager().registerEvents( new CSEHandler_1_13_2(this), this);
		}else if( version.contains("1_14_R1") ){
    		getServer().getPluginManager().registerEvents( new CSEHandler_1_14(this), this);
		}else if( version.contains("1_15_R1") ){
    		getServer().getPluginManager().registerEvents( new CSEHandler_1_15_1(this), this);
		}else if( version.contains("1_16_R1") ){
    		getServer().getPluginManager().registerEvents( new CSEHandler_1_16_1(this), this);
		}else if( version.contains("1_16_R2") ){
    		getServer().getPluginManager().registerEvents( new CSEHandler_1_16_2(this), this);
		}else if( version.contains("1_16_R3") ){
    		getServer().getPluginManager().registerEvents( new CSEHandler_1_16_3(this), this);
		}else if( version.contains("1_17_R1") ){
			getServer().getPluginManager().registerEvents( new CSEHandler_1_17(this), this);
		}else if( version.contains("1_18_R") ){
			getServer().getPluginManager().registerEvents( new CSEHandler_1_18(this), this);
		}else if( version.contains("1_19_R") ){
			getServer().getPluginManager().registerEvents( new CSEHandler_1_19(this), this);
		}else if( version.contains("1_20_R") ){
			getServer().getPluginManager().registerEvents( new CSEHandler_1_19(this), this);
		}else{
			logWarn(get("sr.message.notcompatible") + version);
			getServer().getPluginManager().disablePlugin(this);
		}
		
		consoleInfo(Ansi.BOLD + "ENABLED" + Ansi.RESET + " - Loading took " + LoadTime(startTime));
		
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
		}catch (Exception e){
			debug = true;
			if(debug){log("debug has been set to true due to an exception error.");}
			// Failed to submit the stats
		}
		
	
	}
	
	public boolean checkLibs(String fileName) { // ShulkerRespawnerLib-1.0.2.jar
		CodeSource codeSource = ShulkerRespawner.class.getProtectionDomain().getCodeSource();
		File jarFile = null;
		try {
			jarFile = new File(codeSource.getLocation().toURI().getPath());
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String jarDir = jarFile.getParentFile().getPath();
		
		try {
            final File[] libs = new File[] { new File(getDataFolder(), fileName) };
            File libFile = new File(jarDir + File.separatorChar + fileName);
            for (final File lib : libs) {
                if (!libFile.exists()) {
                    boolean didIt = JarUtils.extractFromJar(lib.getName(),
                    		jarDir + File.separatorChar + fileName);
                    getLogger().info(fileName + " copied to plugins=" + didIt);
                    logWarn("ShulkerRespawner must be realoaded for ShulkerRespawnerLib to work.");
                }else {
                	if(debug){log(fileName + " exists!");}
                	return true;
                }
            }
            
                if (!libFile.exists()) {
                	if(debug){getLogger().warning(
                            "There was a critical error loading My plugin! Could not find lib: "
                                    + libFile.getName());}
                    Bukkit.getServer().getPluginManager().disablePlugin(this);
                    return false;
                }
                
            
        } catch (final Exception e) {
            e.printStackTrace();
        }
		return false;
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
	
	/**
	private void addClassPath(final URL url) throws IOException {
        final URLClassLoader sysloader = (URLClassLoader) ClassLoader
                .getSystemClassLoader();
        final Class<URLClassLoader> sysclass = URLClassLoader.class;
        try {
            final Method method = sysclass.getDeclaredMethod("addURL",
                    new Class[] { URL.class });
            method.setAccessible(true);
            method.invoke(sysloader, new Object[] { url });
        } catch (final Throwable t) {
            t.printStackTrace();
            throw new IOException("Error adding " + url
                    + " to system classloader");
        }
    }//*/
	
	@Override // TODO: onDisable
	public void onDisable(){
		consoleInfo(Ansi.BOLD + "DISABLED" + Ansi.RESET);
	}
	
	public void consoleInfo(String state) {
		logger.info(Ansi.YELLOW + "**************************************" + Ansi.RESET);
		logger.info(Ansi.GREEN + THIS_NAME + " v" + THIS_VERSION + Ansi.RESET + " is " + state);
		logger.info(Ansi.YELLOW + "**************************************" + Ansi.RESET);
	}
	
	public  void log(String dalog){
		logger.info(Ansi.YELLOW + "" + pluginName + Ansi.RESET + " " + dalog + Ansi.RESET);
	}
	public  void logDebug(String dalog){
		log(" " + THIS_VERSION + Ansi.RED + Ansi.BOLD + " [DEBUG] " + Ansi.RESET + dalog);
	}
	public void logWarn(String dalog){
		log(" " + THIS_VERSION + Ansi.RED + Ansi.BOLD + " [WARNING] " + Ansi.RESET + dalog);
	}
	public void broadcastmsg(String message){
		
		SendJsonMessages.SendAllJsonMessage(message);
	}
	public	void log(Level level, String dalog){
		logger.log(level, ChatColor.YELLOW + "" + dalog );
	}
	
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		if(UpdateAvailable&&(p.isOp()||p.hasPermission("shulkerrespawner.showUpdateAvailable"))){
			// TODO: UpdateCheck onPlayerJoin
			String links = "[\"\",{\"text\":\"<Download>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"<DownloadLink>/history\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<please_update>\"}},{\"text\":\" \",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<please_update>\"}},{\"text\":\"| \"},{\"text\":\"<Donate>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://ko-fi.com/joelgodofwar\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<Donate_msg>\"}},{\"text\":\" | \"},{\"text\":\"<Notes>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"<DownloadLink>/updates\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<Notes_msg>\"}}]";
			links = links.replace("<DownloadLink>", DownloadLink).replace("<Download>", get("sr.version.download"))
					.replace("<Donate>", get("sr.version.donate")).replace("<please_update>", get("sr.version.please_update"))
					.replace("<Donate_msg>", get("sr.version.donate.message")).replace("<Notes>", get("sr.version.notes"))
					.replace("<Notes_msg>", get("sr.version.notes.message"));
			String versions = "" + ChatColor.GRAY + get("sr.version.new_vers") + ": " + ChatColor.GREEN + "{nVers}" + ChatColor.GRAY + " | " + get("sr.version.old_vers") + ": " + ChatColor.RED + "{oVers}";
			p.sendMessage("" + ChatColor.WHITE + get("sr.version.message").toString().replace("<MyPlugin>", ChatColor.GOLD + THIS_NAME + ChatColor.WHITE) );
			Utils.sendJson(p, links);
			p.sendMessage(versions.replace("{nVers}", UCnewVers).replace("{oVers}", UColdVers));
	    }
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd");
		LocalDate localDate = LocalDate.now();
		String daDay = dtf.format(localDate);

		if (daDay.equals("04/16")) {
		    Player player = event.getPlayer();
		    String playerId = player.getUniqueId().toString();
		    if (!triggeredPlayers.contains(playerId)) {
		        if (isPluginRequired(THIS_NAME)) {
		            player.sendTitle("Happy Birthday Mom", "I miss you - 4/16/1954-12/23/2022", 10, 70, 20);
		        }
		        triggeredPlayers.add(playerId);
		    }
		}
		
	    if(p.getDisplayName().equals("JoelYahwehOfWar")||p.getDisplayName().equals("JoelGodOfWar")){
	    	p.sendMessage(THIS_NAME + " " + THIS_VERSION + " Hello father!");
	    }
	}
	public void sendJson(Player player, String string) {
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tellraw \"" + player.getName() + 
		        "\" " + string);
	}
	/**
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent e){ //onEntitySpawn(EntitySpawnEvent e) {
        Entity entity = e.getEntity();
        if(debug){logDebug("CSE entity=" + entity.getType());}
        if (entity instanceof Enderman){
        	//log("test");
        	if(debug){logDebug("CSE Environment=" + entity.getWorld().getEnvironment().toString());}
        	if(debug){logDebug("CSE Biome=" + entity.getLocation().getBlock().getBiome().toString());}
        	if(debug){logDebug("CSE isEndCity=" + isEndCity(entity.getLocation().getBlock()));}
        	//if(entity.getWorld().getEnvironment() == Environment.THE_END){
        	if(entity.getWorld().getEnvironment() == Environment.THE_END&&
        			(entity.getLocation().getBlock().getBiome() == Biome.END_HIGHLANDS||
        			entity.getLocation().getBlock().getBiome() == Biome.END_MIDLANDS)&&
        			isEndCity(entity.getLocation().getBlock()) ){
        		if(debug){logDebug("CSE block=" + entity.getLocation().getBlock().getType().toString());}
        		if(debug){logDebug("CSE " + Ansi.GREEN + "isEndCity=" + isEndCity(entity.getLocation().getBlock()) + Ansi.RESET);}
        		if(entity.getLocation().subtract(0, 1, 0).getBlock().getType().toString().contains("PURPUR")||entity.getLocation().getBlock().getType().toString().contains("PURPUR")){
        			Location location = entity.getLocation();
        			World world = entity.getWorld();
        			if(debug){logDebug("CSE radius_between_spawns=" + getConfig().getInt("radius_between_spawns", 10));}
        			if(!checkradius(entity, getConfig().getInt("radius_between_spawns", 10))){ //5
        				Location endcity = world.locateNearestStructure(location, StructureType.END_CITY, 16, false);
	        			if(endcity != null){
	        				log("CSE ENDCITY=" + endcity.toString());
	        				log("CSE Distance=" + endcity.distance(location));
	        				log("CSE radius=" + getConfig().getInt("radius_between_spawns", 10));
	        				
	        				
	        				if(endcity.distance(location) <= getConfig().getInt("radius_between_spawns", 10)){
	        					log("distance=" + endcity.distance(location));
	        				}else{
	        					log("distance > config");
	        					return;
	        				}
	        			}else{
	        				log("endcity not found");
	        				return;
	        			}
	        			boolean spawnCheck = SpawnIt(getConfig().getDouble("enderman_to_shulker_chance.rate", 0.75));
	        			if(spawnCheck) {
	        				e.setCancelled(true);
		        			if(debug){logDebug(Ansi.GREEN + "CSE Enderman tried to spawn at " + location + " and a shulker was spawned in it's place.");}
		        			world.spawn(location, Shulker.class);
	        			}else {
	        				if(debug){logDebug(Ansi.GREEN + "CSE chance failed Enderman spawned at " + location);}
	        			}
        			}else{
        				log("CSE Radius too close");
        			}
        		}else {
        			log("CSE Block Error.");
        		}
        	}else{
        		log("CSE NOT Highlands/Midlands");
        	}
        	log("CSE End CSE");
        }
	}*/
	
	public boolean SpawnIt(double chancepercent){
		if(!getConfig().getBoolean("enderman_to_shulker_chance.enabled", false)) {
			if(debug){logDebug("SI  enderman_to_shulker_chance.enabled=false, returning trueline:344");}
			return true;
		}
		double chance = Math.random();
			if(debug){logDebug("SI chance=" + chance + " line:348");}
			if(debug){logDebug("SI chancepercent=" + chancepercent + " line:349");}
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
					if(debug){logDebug("Killer was a player");}
					ItemStack shulkershell = new ItemStack(Material.SHULKER_SHELL, 1);
					//event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), shulkershell);
					ItemStack itemstack = event.getEntity().getKiller().getInventory().getItemInMainHand();
					if(itemstack != null){
						if(debug){logDebug("itemstack=" + itemstack.getType().toString() + " line:290");}
						int enchantmentlevel = itemstack.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);//.containsEnchantment(Enchantment.LOOT_BONUS_MOBS);
						if(debug){logDebug("enchantmentlevel=" + enchantmentlevel + " line:292");}
						double enchantmentlevelpercent = ((double)enchantmentlevel / 100);
						if(debug){logDebug("enchantmentlevelpercent=" + enchantmentlevelpercent + " line:294");}
						double chance = Math.random();
						if(debug){logDebug("chance=" + chance + " line:296");}
						double chancepercent = getConfig().getDouble("double_shulker_chance.rate", 0.50); /** Set to check config.yml later*/ // TODO:
						if(debug){logDebug("chancepercent=" + chancepercent + " line:298");}
						chancepercent = chancepercent + enchantmentlevelpercent;
						if(debug){logDebug("chancepercent2=" + chancepercent + " line:300");}
						//if(chancepercent > 0.00 && chancepercent < 0.99){
						    if (chancepercent > chance){
						    	if(event.getDrops().contains(shulkershell)){
						    		event.getDrops().add(shulkershell);
						    		//broadcastmsg("");
						    		if(debug){logDebug(Ansi.GREEN + "Shulker shell added to Drops" + Ansi.RESET);}
						    	}else{
						    		event.getDrops().add(shulkershell);
						    		event.getDrops().add(shulkershell);
						    		if(debug){logDebug(Ansi.GREEN + "2 Shulker shells added to Drops" + Ansi.RESET);}
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
							Bukkit.getConsoleSender().sendMessage("Checking for updates...");
							VersionChecker updater = new VersionChecker(this, projectID, githubURL);
							if(updater.checkForUpdates()) {
								/** Update available */
								UpdateAvailable = true; // TODO: Update Checker
								UColdVers = updater.oldVersion();
								UCnewVers = updater.newVersion();
								
								log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
								log("* " + get("sr.version.message").toString().replace("<MyPlugin>", THIS_NAME) );
								log("* " + get("sr.version.old_vers") + ChatColor.RED + UColdVers );
								log("* " + get("sr.version.new_vers") + ChatColor.GREEN + UCnewVers );
								log("*");
								log("* " + get("sr.version.please_update") );
								log("*");
								log("* " + get("sr.version.download") + ": " + DownloadLink + "/history");
								log("* " + get("sr.version.donate.message") + ": https://ko-fi.com/joelgodofwar");
								log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
								//Bukkit.getConsoleSender().sendMessage(newVerMsg.replace("{oVer}", UColdVers).replace("{nVer}", UCnewVers));
								//Bukkit.getConsoleSender().sendMessage(Ansi.GREEN + UpdateChecker.getResourceUrl() + Ansi.RESET);
							}else{
								/** Up to date */
								UpdateAvailable = false;
								log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
								log("* " + ChatColor.YELLOW + THIS_NAME + ChatColor.RESET + " " + get("sr.version.curvers") + ChatColor.RESET );
								log("* " + get("sr.version.donate.message") + ": https://ko-fi.com/joelgodofwar");
								log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
							}
						}catch(Exception e) {
							/** Error */
							Bukkit.getConsoleSender().sendMessage(ChatColor.RED + get("sr.version.update.error"));
							e.printStackTrace();
						}
						/** end update checker */
						return true;
					}	
					if((sender.isOp()||sender.hasPermission("shulkerrespawner.showUpdateAvailable"))){
						BukkitTask updateTask = this.getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
							public void run() {
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
								}catch(Exception e) {
									sender.sendMessage(ChatColor.RED + get("sr.version.update.error"));
									e.printStackTrace();
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
		UpdateCheck = getConfig().getBoolean("auto_update_check", true);
		debug = getConfig().getBoolean("debug", false);
		daLang = getConfig().getString("lang", "en_US");
		oldconfig = new YamlConfiguration();
		
		logger.info(Ansi.YELLOW + "**************************************" + Ansi.RESET);
		logger.info(Ansi.GREEN + THIS_NAME + " v" + THIS_VERSION + Ansi.RESET + " Loading...");
	log(": Loading config file...");
		/**  Check for config */
		try{
			if(!this.getDataFolder().exists()){
				log(": Data Folder doesn't exist");
				log(": Creating Data Folder");
				this.getDataFolder().mkdirs();
				log(": Data Folder Created at " + this.getDataFolder());
			}
			File  file = new File(this.getDataFolder(), "config.yml");
			this.getLogger().info("" + file);
			if(!file.exists()){
				log(": config.yml not found, creating!");
				saveResource("config.yml", true);
			}
		}catch(Exception e){
			debug = true;
			if(debug){log("debug has been set to true due to an exception error.");}
			e.printStackTrace();
		}
		/**  Check config version */
		try {
			oldconfig.load(new File(getDataFolder(), "config.yml"));
		} catch (IOException | InvalidConfigurationException e1) {
			logWarn("Could not load config.yml");
			e1.printStackTrace();
		}
		String checkconfigversion = oldconfig.getString("version", "1.0.0");
		if(checkconfigversion != null){
			if(!checkconfigversion.equalsIgnoreCase("1.0.1")){
				try {
					copyFile_Java7(getDataFolder() + "" + File.separatorChar + "config.yml", getDataFolder() + "" + File.separatorChar + "old_config.yml");
				} catch (IOException e) {
					e.printStackTrace();
				}
				saveResource("config.yml", true);
				
				try {
					config.load(new File(getDataFolder(), "config.yml"));
				} catch (IOException | InvalidConfigurationException e1) {
					logWarn("Could not load config.yml");
					e1.printStackTrace();
				}
				try {
					oldconfig.load(new File(getDataFolder(), "old_config.yml"));
				} catch (IOException | InvalidConfigurationException e1) {
					e1.printStackTrace();
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
				} catch (IOException e) {
					logWarn("Could not save old settings to config.yml");
					e.printStackTrace();
				}
				log(": config.yml Updated! old config saved as old_config.yml");
			}
		}
		
		/** Lang file check */
		/**if(debug){logDebug("datafolder=" + getDataFolder());}
		langFile = new File(getDataFolder() + "" + File.separatorChar + "lang" + File.separatorChar, daLang + ".yml");//\
		if(debug){logDebug("langFilePath=" + langFile.getPath());}
		if(!langFile.exists()){                                  // checks if the yaml does not exist
			langFile.getParentFile().mkdirs();                  // creates the /plugins/<pluginName>/ directory if not found
			saveResource("lang" + File.separatorChar + "cs_CZ.yml", true);
			saveResource("lang" + File.separatorChar + "de_DE.yml", true);
			saveResource("lang" + File.separatorChar + "en_US.yml", true);
			saveResource("lang" + File.separatorChar + "fr_FR.yml", true);
			saveResource("lang" + File.separatorChar + "lol_US.yml", true);
			saveResource("lang" + File.separatorChar + "nl_NL.yml", true);
			saveResource("lang" + File.separatorChar + "pt_BR.yml", true);
			saveResource("lang" + File.separatorChar + "zh_CN.yml", true);
			log("Updating lang files! copied cs_CZ.yml, de_DE.yml, en_US.yml, fr_FR.yml, lol_US.yml, nl_NL.yml, pt_BR.yml, and zh_CN.yml to "
			+ getDataFolder() + "" + File.separatorChar + "lang");
			//ConfigAPI.copy(getResource("lang.yml"), langFile); // copies the yaml from your jar to the folder /plugin/<pluginName>
        }
		lang = new YamlConfiguration();
		try {
			lang.load(langFile);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
		String checklangversion = lang.getString("langversion");
		if(checklangversion != null){
			if(!checklangversion.equalsIgnoreCase("1.0.2")){
				saveResource("lang" + File.separatorChar + "cs_CZ.yml", true);
				saveResource("lang" + File.separatorChar + "de_DE.yml", true);
				saveResource("lang" + File.separatorChar + "en_US.yml", true);
				saveResource("lang" + File.separatorChar + "fr_FR.yml", true);
				saveResource("lang" + File.separatorChar + "lol_US.yml", true);
				saveResource("lang" + File.separatorChar + "nl_NL.yml", true);
				saveResource("lang" + File.separatorChar + "pt_BR.yml", true);
				saveResource("lang" + File.separatorChar + "zh_CN.yml", true);
				log("Updating lang files! copied cs_CZ.yml, de_DE.yml, en_US.yml, fr_FR.yml, lol_US.yml, nl_NL.yml, pt_BR.yml, and zh_CN.yml to "
						+ getDataFolder() + "" + File.separatorChar + "lang");
			}
		}else{
			saveResource("lang" + File.separatorChar + "cs_CZ.yml", true);
			saveResource("lang" + File.separatorChar + "de_DE.yml", true);
			saveResource("lang" + File.separatorChar + "en_US.yml", true);
			saveResource("lang" + File.separatorChar + "fr_FR.yml", true);
			saveResource("lang" + File.separatorChar + "lol_US.yml", true);
			saveResource("lang" + File.separatorChar + "nl_NL.yml", true);
			saveResource("lang" + File.separatorChar + "pt_BR.yml", true);
			saveResource("lang" + File.separatorChar + "zh_CN.yml", true);
			log("Updating lang files! copied cs_CZ.yml, de_DE.yml, en_US.yml, fr_FR.yml, lol_US.yml, nl_NL.yml, pt_BR.yml, and zh_CN.yml to "
					+ getDataFolder() + "" + File.separatorChar + "lang");
		}//*/
		/** Lang file check */
		
		/** DEV check **/
		File jarfile = this.getFile().getAbsoluteFile();
		if(jarfile.toString().contains("-DEV")){
			debug = true;
			log("jarfile contains dev, debug set to true.");
		}

		String packageName = this.getServer().getClass().getPackage().getName();
    	String version = packageName.substring(packageName.lastIndexOf('.') + 2);
    	if(debug)logDebug("version=" + version);
    	//String vers = getMCVersion();
    	if( version.contains("1_13_R1") ){
    		getServer().getPluginManager().registerEvents( new CSEHandler_1_13_1(this), this);
		}else if( version.contains("1_13_R2") ){
    		getServer().getPluginManager().registerEvents( new CSEHandler_1_13_2(this), this);
		}else if( version.contains("1_14_R1") ){
    		getServer().getPluginManager().registerEvents( new CSEHandler_1_14(this), this);
		}else if( version.contains("1_15_R1") ){
    		getServer().getPluginManager().registerEvents( new CSEHandler_1_15_1(this), this);
		}else if( version.contains("1_16_R1") ){
    		getServer().getPluginManager().registerEvents( new CSEHandler_1_16_1(this), this);
		}else if( version.contains("1_16_R2") ){
    		getServer().getPluginManager().registerEvents( new CSEHandler_1_16_2(this), this);
		}else if( version.contains("1_16_R3") ){
    		getServer().getPluginManager().registerEvents( new CSEHandler_1_16_3(this), this);
		}else if( version.contains("1_17_R1") ){
			/**if(vers.equals("1.17")) {
				checkLibs("ShulkerRespawnerLib117-1.0.0.jar");
			}else if(vers.equals("1.17.1")) {
				checkLibs("ShulkerRespawnerLib1171-1.0.0.jar");
			}//*/
			getServer().getPluginManager().registerEvents( new CSEHandler_1_17(this), this);
		}else if( version.contains("1_18_R") ){
			/**if(vers.equals("1.18")) {
				checkLibs("ShulkerRespawnerLib118-1.0.0.jar");
			}else if(vers.equals("1.18.1")) {
				checkLibs("ShulkerRespawnerLib1181-1.0.0.jar");
			}else if(vers.equals("1.18.2")) {
				checkLibs("ShulkerRespawnerLib1182-1.0.0.jar");
			}//*/
			getServer().getPluginManager().registerEvents( new CSEHandler_1_18(this), this);
		}else if( version.contains("1_19_R") ){
			/**if(vers.equals("1.19")) {
				checkLibs("ShulkerRespawnerLib119-1.0.0.jar");
			}//*/
			getServer().getPluginManager().registerEvents( new CSEHandler_1_19(this), this);
		}else{
			logWarn(get("sr.message.notcompatible") + version);
			getServer().getPluginManager().disablePlugin(this);
		}
		
		consoleInfo(Ansi.BOLD + "RELOADED" + Ansi.RESET);
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
	        if (getServer().getPluginManager().getPlugin(requiredPlugin) != null && getServer().getPluginManager().isPluginEnabled(requiredPlugin)) {
	            if (requiredPlugin.equals(pluginName)) {
	                return true;
	            } else {
	                return false;
	            }
	        }
	    }
	    return true;
	}
	
}