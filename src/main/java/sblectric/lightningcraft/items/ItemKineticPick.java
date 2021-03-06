package sblectric.lightningcraft.items;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import sblectric.lightningcraft.api.IKineticGear;
import sblectric.lightningcraft.items.base.ItemPickaxeLC;
import sblectric.lightningcraft.ref.LCText;
import sblectric.lightningcraft.util.ISimpleLEUser;
import sblectric.lightningcraft.util.InventoryLE;
import sblectric.lightningcraft.util.InventoryLE.LECharge;

/** The kinetic pickaxe */
public class ItemKineticPick extends ItemPickaxeLC implements ISimpleLEUser, IKineticGear {
	
	public static final double attackDamage = 4;

	public ItemKineticPick(ToolMaterial mat) {
		super(mat);
	}
	
	/** Update method: get and set stored power for mining */
	@Override
	public void onUpdate(ItemStack a, World world, Entity player, int par4, boolean par5) {		
		// LE operations (can't do them in the efficiency method)
		if(!a.hasTagCompound()) a.setTagCompound(new NBTTagCompound());
		InventoryLE.updateToolPower(a, (EntityPlayer)player);
	}
	
	/** Custom attack damage */
	@Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase entity, EntityLivingBase user) {
		InventoryLE.hitEntity(stack, entity, user, attackDamage, false);
    	return super.hitEntity(stack, entity, user);
    }
	
	/** Override standard pickaxe efficiency */
	@Override
    public float getDestroySpeed(ItemStack stack, IBlockState state) {
		float base = super.getDestroySpeed(stack, state);
		return InventoryLE.getEfficiency(stack, base);
	}
	
	/** Take power away when breaking blocks */
	@Override
	public boolean onBlockDestroyed(ItemStack stack, World world, IBlockState block, BlockPos pos, EntityLivingBase user) {
		float base = super.getDestroySpeed(stack, world.getBlockState(pos));
		InventoryLE.onBlockBreak(stack, (EntityPlayer)user, base);
		return super.onBlockDestroyed(stack, world, block, pos, user);
	}
	
	/** No standard attack damage */
	@Override
	public Multimap getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
        return HashMultimap.create();
    }

	/** item lore */
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List list, ITooltipFlag flag) {
		LECharge charge = new LECharge();
		boolean charged = InventoryLE.addInformation(stack, world, list, flag, charge);
		double damage = 0;

		// add more lore if there exists an LE source that isn't empty
		if(charged && charge.getCharge() > 0) {
			list.add(LCText.getAutoRepair2Lore());
			damage = Math.min(charge.getCharge(), attackDamage);
		}
		
		// attack damage
		list.add("");
		list.add(LCText.secSign + "9+" + LCText.af.format(damage) + " Attack Damage");
	}
	
	/** LE rarity */
	@Override
	@SideOnly(Side.CLIENT)
	public EnumRarity getRarity(ItemStack stack) {
		return ILERarity;
	}

}
