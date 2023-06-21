package com.github.joelgodofwar.sr.util;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.SectionPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public class MyStructureManager {
	@SuppressWarnings("rawtypes")
	public StructureStart getStructureAt(BlockPosition var1, StructureFeature<?, ?> var2, WorldServer ws) {
	      Iterator var3 = this.startsForFeature(SectionPosition.a(var1), var2, ws).iterator();

	      StructureStart var4;
	      do {
	         if (!var3.hasNext()) {
	            return StructureStart.b;
	         }

	         var4 = (StructureStart)var3.next();
	      } while(!var4.a().b(var1));

	      return var4;
	   }
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<StructureStart> startsForFeature(SectionPosition var1, StructureFeature<?, ?> var2, WorldServer ws) {
		//LongSet var3 = IChunkProvider.a(var1.a(), var1.b(), ChunkStatus.e, false).getReferencesForFeature(var2);

	      Builder var4 = ImmutableList.builder();
	      Objects.requireNonNull(var4);
	      //this.fillStartsForFeature(var2, var3, var4::add);
	      return var4.build();
	   }
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
