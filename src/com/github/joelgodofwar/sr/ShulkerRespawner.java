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
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.plugin.PluginDescriptionFile;
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
import com.github.joelgodofwar.sr.util.Ansi;
import com.github.joelgodofwar.sr.util.Metrics;
import com.github.joelgodofwar.sr.util.SendJsonMessages;
import com.github.joelgodofwar.sr.util.UpdateChecker;
import com.github.joelgodofwar.sr.util.YmlConfiguration;


public class ShulkerRespawner  extends JavaPlugin implements Listener{
	public final static Logger logger = Logger.getLogger("Minecraft");
	/** update checker variables */
	public String updateurl = "https://github.com/JoelGodOfwar/ShulkerRespawner/raw/master/versioncheck/{vers}/version.txt";
	public String newVerMsg;// = Ansi.YELLOW + this.getName() + Ansi.MAGENTA + " v{oVer}" + Ansi.RESET +" " + lang.get("newversion") + Ansi.GREEN + " v{nVer}" + Ansi.RESET;
	public int updateVersion = 73638; // https://spigotmc.org/resources/73638
	boolean UpdateAvailable =  false;
	public String UColdVers;
	public String UCnewVers;
	public static boolean UpdateCheck;
	public String thisName = this.getName();
	public String thisVersion = this.getDescription().getVersion();
	/** end update checker variables */
	public static String daLang;
	public static boolean debug;
	File langFile;
    FileConfiguration lang;
    YmlConfiguration config = new YmlConfiguration();
	YamlConfiguration oldconfig = new YamlConfiguration();
	
	
	@Override // TODO: onEnable
	public void onEnable(){
		UpdateCheck = getConfig().getBoolean("auto_update_check", true);
		debug = getConfig().getBoolean("debug", false);
		daLang = getConfig().getString("lang", "en_US");
		oldconfig = new YamlConfiguration();
		
		PluginDescriptionFile pdfFile = this.getDescription();
		logger.info(Ansi.YELLOW + "**************************************" + Ansi.RESET);
		logger.info(Ansi.GREEN + pdfFile.getName() + " v" + pdfFile.getVersion() + Ansi.RESET + " Loading...");
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
		if(debug){logDebug("datafolder=" + getDataFolder());}
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
			if(!checklangversion.equalsIgnoreCase("1.0.1")){
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
		}
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
		
		newVerMsg = Ansi.YELLOW + this.getName() + Ansi.MAGENTA + " v{oVer}" + Ansi.RESET +" " + lang.get("newversion") + Ansi.GREEN + " v{nVer}" + Ansi.RESET;
		/** Update Checker */
		if(UpdateCheck){
			try {
						Bukkit.getConsoleSender().sendMessage("Checking for updates...");
						UpdateChecker updater = new UpdateChecker(this, updateVersion, updateurl);
				if(updater.checkForUpdates()) {
					UpdateAvailable = true; // TODO: Update Checker
					UColdVers = updater.oldVersion();
					UCnewVers = updater.newVersion();
					Bukkit.getConsoleSender().sendMessage(newVerMsg.replace("{oVer}", UColdVers).replace("{nVer}", UCnewVers));
					Bukkit.getConsoleSender().sendMessage(Ansi.GREEN + UpdateChecker.getResourceUrl() + Ansi.RESET);
				}else{
					UpdateAvailable = false;
				}
			}catch(Exception e) {
				Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Could not process update check");
				e.printStackTrace();
			}
		}
		/** end update checker */
		
		/**
		1.8		1_8_R1		1.8.3	1_8_R2		1.8.8 	1_8_R3
		1.9		1_9_R1		1.9.4	1_9_R2	
		1.10	1_10_R1
		1.11	1_11_R1
		1.12	1_12_R1
		1.13	1_13_R1		1.13.1	1_13_R2
		1.14	1_14_R1
		1.15	1_15_R1
		1.16.1	1_16_R1		1.16.2	1_16_R2		1.16.3 1_16_R3
		1.17	1_17_R1
		*/
		/** Register EventHandler */ // TODO: Register Events
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
		}else{
			logWarn("Not compatible with this version of Minecraft:" + version);
			getServer().getPluginManager().disablePlugin(this);
		}
		
		consoleInfo(Ansi.BOLD + "ENABLED" + Ansi.RESET);
		
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
	
	@Override // TODO: onDisable
	public void onDisable(){
		consoleInfo(Ansi.BOLD + "DISABLED" + Ansi.RESET);
	}
	
	public void consoleInfo(String state) {
		PluginDescriptionFile pdfFile = this.getDescription();
		logger.info(Ansi.YELLOW + "**************************************" + Ansi.RESET);
		logger.info(Ansi.GREEN + pdfFile.getName() + " v" + pdfFile.getVersion() + Ansi.RESET + " is " + state);
		logger.info(Ansi.YELLOW + "**************************************" + Ansi.RESET);
	}
	
	public  void log(String dalog){
		logger.info(Ansi.YELLOW + "" + this.getName() + Ansi.RESET + " " + dalog + Ansi.RESET);
	}
	public  void logDebug(String dalog){
		log(" " + this.getDescription().getVersion() + Ansi.RED + Ansi.BOLD + " [DEBUG] " + Ansi.RESET + dalog);
	}
	public void logWarn(String dalog){
		log(" " + this.getDescription().getVersion() + Ansi.RED + Ansi.BOLD + " [WARNING] " + Ansi.RESET + dalog);
	}
	public void broadcastmsg(String message){
		
		SendJsonMessages.SendAllJsonMessage(message);
	}
	
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		if(UpdateAvailable&&(p.isOp()||p.hasPermission("shulkerrespawner.showUpdateAvailable"))){
	    	p.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + " " + lang.get("newversion") + 
	    			" \n" + ChatColor.GREEN + UpdateChecker.getResourceUrl() + ChatColor.RESET);
	    }
	    
	    if(p.getDisplayName().equals("JoelYahwehOfWar")||p.getDisplayName().equals("JoelGodOfWar")){
	    	p.sendMessage(this.getName() + " " + this.getDescription().getVersion() + " Hello father!");
	    }
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
	
	public boolean SpawnIt(double chancepercent){// TODO: DropIt
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
		    		sender.sendMessage(ChatColor.WHITE + " /sr reload - " + lang.get("reload"));//subject to server admin approval");
		    	}
		    	if(sender.hasPermission("shulkerrespawner.toggledebug")||!(sender instanceof Player)){
		    		sender.sendMessage(ChatColor.WHITE + " /sr toggledebug - " + lang.get("toggledebug"));//Cancels SinglePlayerSleep");
		    	}
		    	if(sender.hasPermission("shulkerrespawner.showUpdateAvailable")||!(sender instanceof Player)){
					sender.sendMessage(ChatColor.RESET + " /sr update - " + lang.get("update"));//Checks if there is an update.
				}
			    sender.sendMessage(ChatColor.GREEN + "[]===============[" + ChatColor.YELLOW + "ShulkerRespawner" + ChatColor.GREEN + "]===============[]");
			    return true;
	    	}
	    	if(args[0].equalsIgnoreCase("reload")){
				  if(sender.isOp()||sender.hasPermission("shulkerrespawner.reload")||!(sender instanceof Player)){
					  //ConfigAPI.Reloadconfig(this, p);
					  getServer().getPluginManager().disablePlugin(this);
	                  getServer().getPluginManager().enablePlugin(this);
	                  reloadConfig();
	                  sender.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + " " + lang.get("reloaded"));
	                  return true;
				  }else if(!sender.hasPermission("shulkerrespawner.reload")){
					  sender.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + " " + lang.get("noperm"));
					  return false;
				  }
		      }
		      if(args[0].equalsIgnoreCase("toggledebug")||args[0].equalsIgnoreCase("td")){
				  if(sender.isOp()||sender.hasPermission("shulkerrespawner.toggledebug")||!(sender instanceof Player)){
					  debug = !debug;
					  sender.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + " " + lang.get("debugtrue").toString().replace("boolean", "" + debug));
					  return true;
				  }else if(!sender.hasPermission("shulkerrespawner.toggledebug")){
					  sender.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + " " + lang.get("noperm"));
					  return false;
				  }
		      }
		      if(args[0].equalsIgnoreCase("update")){ // TODO: Command update
					// Player must be OP and auto-update-check must be true
				//if(sender.isOp() && UpdateCheck||sender.hasPermission("sps.op") && UpdateCheck||sender.hasPermission("sps.*") && UpdateCheck){	
					if((sender.isOp()||sender.hasPermission("shulkerrespawner.showUpdateAvailable"))){
					    
						BukkitTask updateTask = this.getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {

							public void run() {
								try {
									Bukkit.getConsoleSender().sendMessage("Checking for updates...");
									UpdateChecker updater = new UpdateChecker(thisVersion, updateVersion, updateurl);
									if(updater.checkForUpdates()) {
										UpdateAvailable = true; // TODO: Update Checker
										UColdVers = updater.oldVersion();
										UCnewVers = updater.newVersion();
										Bukkit.getConsoleSender().sendMessage(newVerMsg.replace("{oVer}", UColdVers).replace("{nVer}", UCnewVers));
										Bukkit.getConsoleSender().sendMessage(Ansi.GREEN + UpdateChecker.getResourceUrl() + Ansi.RESET);
									}else{
										sender.sendMessage(ChatColor.YELLOW + thisName + ChatColor.RED + " v" + thisVersion + ChatColor.RESET + " Up to date." + ChatColor.RESET);
										UpdateAvailable = false;
									}
								}catch(Exception e) {
									sender.sendMessage(ChatColor.RED + "Could not process update check");
									Bukkit.getConsoleSender().sendMessage(Ansi.RED + "Could not process update check");
									e.printStackTrace();
								}
							}
							
						});
									
						return true;	
					}else{
						sender.sendMessage(ChatColor.YELLOW + this.getName() + lang.get("noperm"));
						return false;
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
}