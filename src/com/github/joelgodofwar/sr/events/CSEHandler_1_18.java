package com.github.joelgodofwar.sr.events;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.Squid;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import com.github.joelgodofwar.sr.ShulkerRespawner;
import com.github.joelgodofwar.sr.ShulkerRespawnerLib;
import com.github.joelgodofwar.sr.util.Ansi;
/**
1.8		1_8_R1		1.8.3	1_8_R2
1.8.8 	1_8_R3
1.9		1_9_R1		1.9.4	1_9_R2	
1.10	1_10_R1
1.11	1_11_R1
1.12	1_12_R1
1.13	1_13_R1		1.13.1	1_13_R2
1.14	1_14_R1
1.15	1_15_R1
1.16.1	1_16_R1		1.16.2	1_16_R2
1.17.1	1_17_R1		1.18	1_18_R1
1.18.2	1_18_R2		1.19	1_19_R1
*/

public class CSEHandler_1_18 implements Listener {
	ShulkerRespawner SR;
	boolean debug;
	
	@SuppressWarnings("static-access")
	public CSEHandler_1_18(final ShulkerRespawner plugin){
		SR = plugin;
		debug = plugin.debug;
	}
	
	@SuppressWarnings("static-access")
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event){ //onEntitySpawn(EntitySpawnEvent e) {
        Entity entity = event.getEntity();
        if(entity.getWorld().getEnvironment() != Environment.THE_END) {
        	return;
        }
        
        if ( entity instanceof Enderman || entity instanceof Squid ){
        	if(debug){SR.logDebug("CSE 1.18.* entity=\"" + entity.getType() + "\"");}
        	if(debug){SR.logDebug("CSE Environment=\"" + entity.getWorld().getEnvironment().toString() + "\"");}
        	if(debug){SR.logDebug("CSE Biome=\"" + entity.getLocation().getBlock().getBiome().toString() + "\"");}
        	if(debug){SR.logDebug("CSE isEndCity=\"" + isEndCity(entity.getLocation().getBlock()) + "\"");}
        	
        	Location location = entity.getLocation();
        	SR.log("location=" + location);
        	if(debug){SR.logDebug("CSE block=" + entity.getLocation().getBlock().getType().toString());}
        	if(debug){SR.logDebug("CSE " + Ansi.GREEN + "isEndCity=" + isEndCity(entity.getLocation().getBlock()) + Ansi.RESET);}
        	if(entity.getLocation().subtract(0, 3, 0).getBlock().getType().toString().contains("PURPUR")||
        			entity.getLocation().subtract(0, 2, 0).getBlock().getType().toString().contains("PURPUR")||
        			entity.getLocation().subtract(0, 1, 0).getBlock().getType().toString().contains("PURPUR")
        			||entity.getLocation().getBlock().getType().toString().contains("PURPUR")){
        			
        		World world = entity.getWorld();
        		if(debug){SR.logDebug("CSE radius_between_spawns=" + SR.getConfig().getInt("radius_between_spawns", 10));}
	        	if(!checkradius(entity, SR.getConfig().getInt("radius_between_spawns", 10))){ //5
	        				
	        				
		        	if(isEndCity(entity.getLocation().getBlock())){
		        				if(debug){SR.logDebug("CSE radius=" + SR.getConfig().getInt("radius_between_spawns", 10));}
		        				
		        				String packageName = SR.getServer().getClass().getPackage().getName();
		        		    	String version = packageName.substring(packageName.lastIndexOf('.') + 2);
		        		    	if(SR.getMCVersion().equals("1.18")) {
		        		    		
			        					boolean result = ShulkerRespawnerLib.playerInsideStructure(entity, version, debug);
			        					if(debug){SR.logDebug("result=" + result);}
										if(!result) {
											if(debug){SR.logDebug("Enderman is not in an EndCity.");}
											return;
										}
			        				
		        		    	}else if(SR.getMCVersion().equals("1.18.1")) {
		        		    		
			        					boolean result = ShulkerRespawnerLib.playerInsideStructure(entity, version, debug);
			        					if(debug){SR.logDebug("result=" + result);}
										if(!result) {
											if(debug){SR.logDebug("Enderman is not in an EndCity.");}
											return;
										}
			        				
		        		    	}else if(SR.getMCVersion().equals("1.18.2")) {
		        		    		
			        					boolean result = ShulkerRespawnerLib.playerInsideStructure(entity, version, debug);
			        					if(debug){SR.logDebug("result=" + result);}
										if(!result) {
											if(debug){SR.logDebug("Enderman is not in an EndCity.");}
											return;
										}
			        				
		        		    	}
		        			}else{
		        				if(debug){SR.logDebug("endcity not found");}
		        				return;
		        			}
		        			boolean spawnCheck = SpawnIt(SR.getConfig().getDouble("enderman_to_shulker_chance.rate", 0.75));
		        			if(spawnCheck) {
		        				event.setCancelled(true);
			        			if(debug){SR.logDebug(Ansi.GREEN + "CSE Enderman tried to spawn at " + location + " and a shulker was spawned in it's place.");}
			        			world.spawn(location, Shulker.class);
		        			}else {
		        				if(debug){SR.logDebug(Ansi.GREEN + "CSE chance failed Enderman spawned at " + location);}
		        			}
	        	}else{
	        		if(debug){SR.logDebug("CSE Radius too close");}
	        	}
        	}else {
        			if(debug){SR.logDebug("CSE Block Error.");}
        		}
        	if(debug){SR.logDebug("CSE End CSE");}
        }
	}
	
	public boolean SpawnIt(double chancepercent){// TODO: DropIt
		if(!SR.getConfig().getBoolean("enderman_to_shulker_chance.enabled", false)) {
			if(debug){SR.logDebug("SI  enderman_to_shulker_chance.enabled=false, returning trueline:344");}
			return true;
		}
		double chance = Math.random();
			if(debug){SR.logDebug("SI chance=" + chance + " line:348");}
			if(debug){SR.logDebug("SI chancepercent=" + chancepercent + " line:349");}
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
	
	
}
