package com.lightningcraft.items;

import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.lightningcraft.ref.RefStrings;
import com.lightningcraft.registry.IRegistryItem;
import com.lightningcraft.util.LCMisc;

/** A basic LightningCraft hoe */
public class ItemHoeLC extends ItemHoe implements IRegistryItem {
	
	private EnumRarity rarity;
	private ToolMaterial mat;
	
	public ItemHoeLC(ToolMaterial mat, EnumRarity rarity) {
		super(mat);
		this.mat = mat;
		this.rarity = rarity;
	}
	
	public ItemHoeLC(ToolMaterial mat) {
		this(mat, DYNAMIC);
	}

	@Override
	public String getShorthandName() {
		return this.getUnlocalizedName().substring(5);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerRender(ItemModelMesher mesher) {
		mesher.register(this, 0, new ModelResourceLocation(RefStrings.MODID + ":" + this.getShorthandName(), "inventory"));
	}
	
	@Override
	public void setRarity() {
		if(rarity == DYNAMIC) rarity = LCMisc.getRarityFromStack(mat.getRepairItemStack());
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public EnumRarity getRarity(ItemStack stack){
		return rarity;
	}

}