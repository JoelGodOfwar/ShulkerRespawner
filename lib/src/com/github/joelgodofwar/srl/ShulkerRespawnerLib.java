package com.github.joelgodofwar.srl;

import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.StructureType;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.joelgodofwar.srl.util.Ansi;

import net.minecraft.core.BaseBlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.levelgen.feature.StructureGenerator;
import net.minecraft.world.level.levelgen.structure.StructurePiece;

public class ShulkerRespawnerLib  extends JavaPlugin{
	
	@Override
	public void onEnable(){
		log("ShulkerRespawnerLib has been loaded.");
	}
	
	public final static Logger logger = Logger.getLogger("Minecraft");
	
	public  void log(String dalog){
		logger.info(Ansi.YELLOW + "" + this.getName() + Ansi.RESET + " " + dalog + Ansi.RESET);
	}
	public  void logDebug(String dalog){
		log(" " + this.getDescription().getVersion() + Ansi.RED + Ansi.BOLD + " [DEBUG] " + Ansi.RESET + dalog);
	}
	public void logWarn(String dalog){
		log(" " + this.getDescription().getVersion() + Ansi.RED + Ansi.BOLD + " [WARNING] " + Ansi.RESET + dalog);
	}
	
	/**
	 * 
	 * @param entity
	 * @return
	 */
	public static boolean playerInsideStructure(Entity entity) {
		return playerInsideStructure(entity, StructureType.END_CITY, StructureGenerator.p);
	}
	
	/**
	 * 
	 * @param entity
	 * @param structureType
	 * @param nmsType
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static boolean playerInsideStructure(Entity entity, StructureType structureType, StructureGenerator nmsType) {
        Location loc = entity.getWorld().locateNearestStructure(entity.getLocation(), structureType, 100, false); // (location, radius, findUnexplored)

        if (loc == null) {
            return false;
        }
        org.bukkit.Chunk locChunk = loc.getChunk();
        int chunkX = locChunk.getX();
        int chunkZ = locChunk.getZ();
        
        WorldServer world = ((CraftWorld) entity.getWorld()).getHandle();
        Chunk chunk = world.getChunkIfLoaded(chunkX, chunkZ);
        if (chunk.a(nmsType) != null) { //Checking if this chunk is the starting point of the structure
            for (StructurePiece piece : chunk.a(nmsType).i()) { //Iterating through every piece of the structure
                if (piece.f().b(new BaseBlockPosition(entity.getLocation().getBlockX(), entity.getLocation().getY(), entity.getLocation().getBlockZ()))) { //Getting the piece's bounding box and then checking if the player is inside
                    return true; //If all this is true, then the player is standing inside the structure
                }
            }
        }

        return false;
    }
	
}
