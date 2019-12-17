package com.github.joelgodofwar.sr;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.joelgodofwar.sr.api.Ansi;


public class ShulkerRespawner  extends JavaPlugin implements Listener{
	public final static Logger logger = Logger.getLogger("Minecraft");
	public static String daLang;
	public static boolean UpdateCheck;
	public static boolean debug;
	File langFile;
    FileConfiguration lang;
	public String updateurl = "https://raw.githubusercontent.com/JoelGodOfwar/ShulkerRespawner/master/versions/1.14/version.txt";
	
	@Override // TODO: onEnable
	public void onEnable(){
		daLang = getConfig().getString("lang");
		debug = getConfig().getBoolean("debug");
		langFile = new File(getDataFolder(), "lang.yml");
		if(!langFile.exists()){                                  // checks if the yaml does not exist
			langFile.getParentFile().mkdirs();                  // creates the /plugins/<pluginName>/ directory if not found
			saveResource("lang.yml", true);
			//ConfigAPI.copy(getResource("lang.yml"), langFile); // copies the yaml from your jar to the folder /plugin/<pluginName>
			
        }
		lang = new YamlConfiguration();
		try {
			lang.load(langFile);
		} catch (IOException | InvalidConfigurationException e1) {
			e1.printStackTrace();
		}
		
		/** DEV check **/
		File jarfile = this.getFile().getAbsoluteFile();
		if(jarfile.toString().contains("-DEV")){
			debug = true;
			log("jarfile contains dev, debug set to true.");
		}
		getServer().getPluginManager().registerEvents(this, this);
		consoleInfo(Ansi.Bold + "ENABLED" + Ansi.SANE);
		if(getConfig().getBoolean("debug")==true&&!(jarfile.toString().contains("-DEV"))){
			logDebug("Config.yml dump");
			logDebug("auto_update_check=" + getConfig().getBoolean("auto_update_check"));
			logDebug("debug=" + getConfig().getBoolean("debug"));
			logDebug("lang=" + getConfig().getString("lang"));
		}
		
	}
	
	@Override // TODO: onDisable
	public void onDisable(){
		consoleInfo(Ansi.Bold + "DISABLED" + Ansi.SANE);
	}
	
	public void consoleInfo(String state) {
		PluginDescriptionFile pdfFile = this.getDescription();
		logger.info(Ansi.YELLOW + "**************************************" + Ansi.SANE);
		logger.info(Ansi.GREEN + pdfFile.getName() + " v" + pdfFile.getVersion() + Ansi.SANE + " is " + state);
		logger.info(Ansi.YELLOW + "**************************************" + Ansi.SANE);
	}
	
	public  void log(String dalog){
		logger.info(Ansi.YELLOW + "" + this.getName() + Ansi.SANE + " " + dalog + Ansi.SANE);
	}
	public  void logDebug(String dalog){
		log(" " + this.getDescription().getVersion() + Ansi.RED + Ansi.Bold + " [DEBUG] " + Ansi.SANE + dalog);
	}
	public void logWarn(String dalog){
		log(" " + this.getDescription().getVersion() + Ansi.RED + Ansi.Bold + " [WARNING] " + Ansi.SANE + dalog);
	}
	
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
	    Player p = event.getPlayer();
	    if(p.isOp() && UpdateCheck){	
			try {
			
				URL url = new URL(updateurl);
				final URLConnection conn = url.openConnection();
	            conn.setConnectTimeout(5000);
	            final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	            final String response = reader.readLine();
	            final String localVersion = this.getDescription().getVersion();
	            if(debug){log("response= ." + response + ".");} //TODO: Logger
	            if(debug){log("localVersion= ." + localVersion + ".");} //TODO: Logger
	            if (!response.equalsIgnoreCase(localVersion)) {
					p.sendMessage(ChatColor.YELLOW + this.getName() + ChatColor.RED + " " + lang.get("newversion." + daLang + ""));
				}
			} catch (MalformedURLException e) {
				log("MalformedURLException");
				e.printStackTrace();
			} catch (IOException e) {
				log("IOException");
				e.printStackTrace();
			}catch (Exception e) {
				log("Exception");
				e.printStackTrace();
			}
			
		}
	    if(p.getDisplayName().equals("JoelYahwehOfWar")||p.getDisplayName().equals("JoelGodOfWar")){
	    	p.sendMessage(this.getName() + " " + this.getDescription().getVersion() + " Hello father!");
	    }
	}
	
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent e){ //onEntitySpawn(EntitySpawnEvent e) {
        Entity entity = e.getEntity();
        if(debug){log("entity=" + entity.getType());}
        if (entity instanceof Enderman){
        	log("test");
        	if(debug){logDebug("biome=" + entity.getWorld().getEnvironment().toString());}
        	if(entity.getWorld().getEnvironment() == Environment.THE_END){
        		if(debug){logDebug("block=" + entity.getLocation().getBlock().getType().toString());}
        		if(entity.getLocation().subtract(0, 1, 0).getBlock().getType().toString().contains("PURPUR")||entity.getLocation().getBlock().getType().toString().contains("PURPUR")){
        			Location location = entity.getLocation();
        			World world = entity.getWorld();
        			e.setCancelled(true);
        			log("Enderman tried to spawn at " + location + " and a shulker was spawned in it's place.");
        			world.spawn(location, Shulker.class);
        		}
        	}
        }
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		//Player p = (Player)sender;
	    if (cmd.getName().equalsIgnoreCase("SR")){
	    	if (args.length == 0)
	    	{
	    		/** Check if player has permission */
	            Player player = null;
	            if (sender instanceof Player) {
	                player = (Player) sender;
	                if (!player.hasPermission("shulkerrespawner.op") && !player.isOp()) {
	                    player.sendMessage(ChatColor.DARK_RED + "" + lang.get("noperm." + daLang + ""));
	                    return true;
	                }
	            }
	            /** Command code */
	            sender.sendMessage(ChatColor.GREEN + "[]===============[" + ChatColor.YELLOW + "ShulkerRespawner" + ChatColor.GREEN + "]===============[]");
			    if(sender.isOp()||sender.hasPermission("shulkerrespawner.op")){
			    	sender.sendMessage(ChatColor.GOLD + " OP Commands");
			    	sender.sendMessage(ChatColor.GOLD + " /SR DEBUG true/false - " + lang.get("srdebuguse." + daLang + ""));
			    }else{
			    	sender.sendMessage(ChatColor.GOLD + "" + lang.get("noperm." + daLang + ""));
			    }
			    sender.sendMessage(ChatColor.GREEN + "[]===============[" + ChatColor.YELLOW + "ShulkerRespawner" + ChatColor.GREEN + "]===============[]");
			    return true;
	    	}
	    	if(args[0].equalsIgnoreCase("DEBUG")){
	    		if(args.length< 1){
					return false;
	    		}
	    		/** Check if player has permission */
	            Player player = null;
	            if (sender instanceof Player) {
	                player = (Player) sender;
	                if (!player.hasPermission("shulkerrespawner.op") && !player.isOp()) {
	                    player.sendMessage(ChatColor.DARK_RED + "" + lang.get("noperm." + daLang + ""));
	                    return true;
	                }
	            }
	            /** Command code */
		    	if(!args[1].equalsIgnoreCase("true") & !args[1].equalsIgnoreCase("false")){
					sender.sendMessage(ChatColor.YELLOW + this.getName() + " §c" + lang.get("boolean." + daLang + "") + ": /SR DEBUG True/False");
		    	}else if(args[1].contains("true") || args[1].contains("false")){
					//sender.sendMessage(ChatColor.YELLOW + this.getName() + " " + " " + args[1]);
					if(args[1].contains("false")){
						debug = false;
						sender.sendMessage(ChatColor.YELLOW + this.getName() + " " + lang.get("debugfalse." + daLang + ""));
					}else if(args[1].contains("true")){
						debug = true;
						sender.sendMessage(ChatColor.YELLOW + this.getName() + " " + lang.get("debugtrue." + daLang + ""));
					}
					
					return true;
				}
	    	}
	    }
	    return false;
	}
	
	public static String getMCVersion() {
		String strVersion = Bukkit.getVersion();
		strVersion = strVersion.substring(strVersion.indexOf("MC: "), strVersion.length());
		strVersion = strVersion.replace("MC: ", "").replace(")", "");
		return strVersion;
	}
}