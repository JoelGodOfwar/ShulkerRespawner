package com.github.joelgodofwar.sr.events;

import org.bukkit.Location;
import org.bukkit.StructureType;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Shulker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import com.github.joelgodofwar.sr.ShulkerRespawner;
import com.github.joelgodofwar.sr.util.Ansi;

public class CSEHandler_1_15 implements Listener {
	ShulkerRespawner SR;
	boolean debug;
	
	@SuppressWarnings("static-access")
	public CSEHandler_1_15(final ShulkerRespawner plugin){
		SR = plugin;
		debug = plugin.debug;
	}
	
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent e){ //onEntitySpawn(EntitySpawnEvent e) {
        Entity entity = e.getEntity();
        if(debug){SR.logDebug("CSE entity=" + entity.getType());}
        if (entity instanceof Enderman){
        	//log("test");
        	if(debug){SR.logDebug("CSE Environment=" + entity.getWorld().getEnvironment().toString());}
        	if(debug){SR.logDebug("CSE Biome=" + entity.getLocation().getBlock().getBiome().toString());}
        	if(debug){SR.logDebug("CSE isEndCity=" + isEndCity(entity.getLocation().getBlock()));}
        	//if(entity.getWorld().getEnvironment() == Environment.THE_END){
        	if(entity.getWorld().getEnvironment() == Environment.THE_END&&
        			(entity.getLocation().getBlock().getBiome() == Biome.END_HIGHLANDS||
        			entity.getLocation().getBlock().getBiome() == Biome.END_MIDLANDS)&&
        			isEndCity(entity.getLocation().getBlock()) ){
        		if(debug){SR.logDebug("CSE block=" + entity.getLocation().getBlock().getType().toString());}
        		if(debug){SR.logDebug("CSE " + Ansi.GREEN + "isEndCity=" + isEndCity(entity.getLocation().getBlock()) + Ansi.RESET);}
        		if(entity.getLocation().subtract(0, 1, 0).getBlock().getType().toString().contains("PURPUR")||entity.getLocation().getBlock().getType().toString().contains("PURPUR")){
        			Location location = entity.getLocation();
        			World world = entity.getWorld();
        			if(debug){SR.logDebug("CSE radius_between_spawns=" + SR.getConfig().getInt("radius_between_spawns", 10));}
        			if(!checkradius(entity, SR.getConfig().getInt("radius_between_spawns", 10))){ //5
        				Location endcity = world.locateNearestStructure(location, StructureType.END_CITY, 16, false);
	        			if(endcity != null){
	        				if(debug){SR.logDebug("CSE ENDCITY=" + endcity.toString());}
	        				if(debug){SR.logDebug("CSE Distance=" + endcity.distance(location));}
	        				if(debug){SR.logDebug("CSE radius=" + SR.getConfig().getInt("radius_between_spawns", 10));}
	        				
	        				if(endcity.distance(location) <= SR.getConfig().getInt("radius_between_spawns", 10)){
	        					if(debug){SR.logDebug("distance=" + endcity.distance(location));}
	        				}else{
	        					if(debug){SR.logDebug("distance > config");}
	        					return;
	        				}
	        			}else{
	        				if(debug){SR.logDebug("endcity not found");}
	        				return;
	        			}
	        			boolean spawnCheck = SpawnIt(SR.getConfig().getDouble("enderman_to_shulker_chance.rate", 0.75));
	        			if(spawnCheck) {
	        				e.setCancelled(true);
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
        	}else{
        		if(debug){SR.logDebug("CSE NOT Highlands/Midlands");}
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
