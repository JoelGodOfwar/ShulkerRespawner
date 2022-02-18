package com.github.joelgodofwar.sr.events;

import org.bukkit.Location;
import org.bukkit.StructureType;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Shulker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import com.github.joelgodofwar.sr.ShulkerRespawner;
import com.github.joelgodofwar.sr.util.Ansi;

import net.minecraft.core.BaseBlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.levelgen.feature.StructureGenerator;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
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
1.17	1_17_R1
*/

public class CSEHandler_1_17 implements Listener {
	ShulkerRespawner SR;
	boolean debug;
	
	@SuppressWarnings("static-access")
	public CSEHandler_1_17(final ShulkerRespawner plugin){
		SR = plugin;
		debug = plugin.debug;
	}
	
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event){ //onEntitySpawn(EntitySpawnEvent e) {
        Entity entity = event.getEntity();
        if(entity.getWorld().getEnvironment() != Environment.THE_END) {
        	return;
        }
        if(debug){SR.logDebug("CSE 1.17 entity=\"" + entity.getType() + "\"");}
        if (entity instanceof Enderman){
        	//log("test");
        	if(debug){SR.logDebug("CSE Environment=\"" + entity.getWorld().getEnvironment().toString() + "\"");}
        	if(debug){SR.logDebug("CSE Biome=\"" + entity.getLocation().getBlock().getBiome().toString() + "\"");}
        	if(debug){SR.logDebug("CSE isEndCity=\"" + isEndCity(entity.getLocation().getBlock()) + "\"");}
        	//if(entity.getWorld().getEnvironment() == Environment.THE_END){
        	/**if(entity.getWorld().getEnvironment() == Environment.THE_END&&
        			(entity.getLocation().getBlock().getBiome() == Biome.END_HIGHLANDS||
        			entity.getLocation().getBlock().getBiome() == Biome.END_MIDLANDS) ){//*/
        	
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
	        				/**if(playerInsideStructure(entity, StructureType.END_CITY, StructureGenerator.b)) {
	        					if(debug){SR.logDebug("Answer is b");
	        				}
	        				if(playerInsideStructure(entity, StructureType.END_CITY, StructureGenerator.c)) {
	        					if(debug){SR.logDebug("Answer is c");
	        				}
	        				if(playerInsideStructure(entity, StructureType.END_CITY, StructureGenerator.d)) {
	        					if(debug){SR.logDebug("Answer is d");
	        				}
	        				if(playerInsideStructure(entity, StructureType.END_CITY, StructureGenerator.e)) {
	        					if(debug){SR.logDebug("Answer is e");
	        				}
	        				if(playerInsideStructure(entity, StructureType.END_CITY, StructureGenerator.f)) {
	        					if(debug){SR.logDebug("Answer is f");
	        				}
	        				if(playerInsideStructure(entity, StructureType.END_CITY, StructureGenerator.g)) {
	        					if(debug){SR.logDebug("Answer is g");
	        				}
	        				if(playerInsideStructure(entity, StructureType.END_CITY, StructureGenerator.h)) {
	        					if(debug){SR.logDebug("Answer is h");
	        				}
	        				if(playerInsideStructure(entity, StructureType.END_CITY, StructureGenerator.i)) {
	        					if(debug){SR.logDebug("Answer is i");
	        				}
	        				if(playerInsideStructure(entity, StructureType.END_CITY, StructureGenerator.k)) {
	        					if(debug){SR.logDebug("Answer is k");
	        				}
	        				if(playerInsideStructure(entity, StructureType.END_CITY, StructureGenerator.l)) {
	        					if(debug){SR.logDebug("Answer is l");
	        				}
	        				if(playerInsideStructure(entity, StructureType.END_CITY, StructureGenerator.m)) {
	        					if(debug){SR.logDebug("Answer is m");
	        				}
	        				if(playerInsideStructure(entity, StructureType.END_CITY, StructureGenerator.n)) {
	        					if(debug){SR.logDebug("Answer is n");
	        				}
	        				if(playerInsideStructure(entity, StructureType.END_CITY, StructureGenerator.o)) {
	        					if(debug){SR.logDebug("Answer is o");
	        				}
	        				if(playerInsideStructure(entity, StructureType.END_CITY, StructureGenerator.p)) {
	        					if(debug){SR.logDebug("Answer is p");
	        				}
	        				if(playerInsideStructure(entity, StructureType.END_CITY, StructureGenerator.q)) {
	        					if(debug){SR.logDebug("Answer is q");
	        				}
	        				if(playerInsideStructure(entity, StructureType.END_CITY, StructureGenerator.r)) {
	        					if(debug){SR.logDebug("Answer is r");
	        				}
	        				if(playerInsideStructure(entity, StructureType.END_CITY, StructureGenerator.s)) {
	        					if(debug){SR.logDebug("Answer is s");
	        				}//*/
	        				
	        				
	        				
	        				if(!playerInsideStructure(entity, StructureType.END_CITY, StructureGenerator.o)) {
	        					if(debug){SR.logDebug("Enderman is not in an EndCity.");}
	        					return;
	        				}
	        				
	        				/**if(endcity.distance(location) <= SR.getConfig().getInt("radius_between_spawns", 10)){
	        					if(debug){SR.logDebug("distance=" + endcity.distance(location));
	        				}else{
	        					if(debug){SR.logDebug("distance > config");
	        					return;
	        				}*/
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
        	/**}else{
        		if(debug){SR.logDebug("CSE NOT Highlands/Midlands");
        	}//*/
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
	
	@SuppressWarnings("rawtypes")
	public boolean playerInsideStructure(Entity p, StructureType type, StructureGenerator nmsType) {
        Location loc = p.getWorld().locateNearestStructure(p.getLocation(), type, 100, false); // (location, radius, findUnexplored)

        if (loc == null) {
            return false;
        }
     
        WorldServer world = ((CraftWorld) p.getWorld()).getHandle();
        Chunk chunk = world.getChunkAt(loc.getChunk().getX(), loc.getChunk().getZ());
        if (chunk.a(nmsType) != null) { //Checking if this chunk is the starting point of the structure
            for (StructurePiece piece : chunk.a(nmsType).d()) { //Iterating through every piece of the structure
                if (piece.f().b(new BaseBlockPosition(p.getLocation().getBlockX(), p.getLocation().getY(), p.getLocation().getBlockZ()))) { //Getting the piece's bounding box and then checking if the player is inside
                    return true; //If all this is true, then the player is standing inside the structure
                }
            }
        }
        return false;
    }
	
}
