package sblectric.lightningcraft.dimensions;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockPortal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

import com.google.common.collect.Lists;
import sblectric.lightningcraft.blocks.BlockStone;
import sblectric.lightningcraft.init.LCBlocks;

/** Mod teleporter class */
public class TeleporterLC extends Teleporter {

	private final int dimFrom;
	private final WorldServer worldServerInstance;
	/** A private Random() function in Teleporter */
	private final Random random;
	private final Long2ObjectMap<PortalPosition> destinationCoordinateCache = new Long2ObjectOpenHashMap(4096);
	private final List<Long> destinationCoordinateKeys = Lists.newArrayList();

	public TeleporterLC(int dimFrom, WorldServer worldIn) {
		super(worldIn);
		this.dimFrom = dimFrom;
		this.worldServerInstance = worldIn;
		this.random = new Random(worldIn.getSeed());
	}

	/** Place the entity in an existing portal, or make a new one as needed */
	@Override
	public void placeInPortal(Entity entityIn, float rotationYaw) {
		if (!this.placeInExistingPortal(entityIn, rotationYaw)) {
			this.makePortal(entityIn);
			this.placeInExistingPortal(entityIn, rotationYaw);
		}
	}

	/** Place the entity in an existing portal */
	@Override
	public boolean placeInExistingPortal(Entity entityIn, float rotationYaw) {
		int i = 128;
		double d0 = -1.0D;
		int j = MathHelper.floor(entityIn.posX);
		int k = MathHelper.floor(entityIn.posZ);
		boolean flag = true;
		BlockPos blockpos = BlockPos.ORIGIN;
		long l = ChunkPos.asLong(j, k);

		if (this.destinationCoordinateCache.containsKey(l)) {
			PortalPosition ppos = this.destinationCoordinateCache.get(l);
			d0 = 0.0D;
			blockpos = ppos;
			ppos.lastUpdateTime = this.worldServerInstance.getTotalWorldTime();
			flag = false;
		}
		else
		{
			BlockPos bp3 = new BlockPos(entityIn);

			for (int i1 = -128; i1 <= 128; ++i1) {
				BlockPos bp2;

				for (int j1 = -128; j1 <= 128; ++j1) {
					for (BlockPos bp1 = bp3.add(i1, this.worldServerInstance.getActualHeight() - 1 - bp3.getY(), j1); bp1.getY() >= 0; bp1 = bp2) {
						bp2 = bp1.down();

						if (this.worldServerInstance.getBlockState(bp1).getBlock() == LCBlocks.underPortal) {
							while (this.worldServerInstance.getBlockState(bp2 = bp1.down()).getBlock() == LCBlocks.underPortal)
							{
								bp1 = bp2;
							}

							double d1 = bp1.distanceSq(bp3);

							if (d0 < 0.0D || d1 < d0)
							{
								d0 = d1;
								blockpos = bp1;
							}
						}
					}
				}
			}
		}

		if (d0 >= 0.0D)
		{
			if (flag)
			{
				this.destinationCoordinateCache.put(l, new PortalPosition(blockpos, this.worldServerInstance.getTotalWorldTime()));
				this.destinationCoordinateKeys.add(l);
			}

			double d5 = blockpos.getX() + 0.5D;
			double d6 = blockpos.getY() + 0.5D;
			double d7 = blockpos.getZ() + 0.5D;
			
			if (entityIn instanceof EntityPlayerMP) {
				EntityPlayerMP ply = (EntityPlayerMP)entityIn;
				ply.connection.setPlayerLocation(d5, d6, d7, ply.rotationYaw, ply.rotationPitch);
			} else {
				entityIn.setLocationAndAngles(d5, d6, d7, entityIn.rotationYaw, entityIn.rotationPitch);
			}

			entityIn.motionX = entityIn.motionY = entityIn.motionZ = 0;
			
			return true;
		}
		else
		{
			return false;
		}
	}

	/** Construct the portal if needed */
	@Override
	public boolean makePortal(Entity ent) {
		int i = 16;
		double d0 = -1.0D;
		int j = MathHelper.floor(ent.posX);
		int k = MathHelper.floor(ent.posY);
		int l = MathHelper.floor(ent.posZ);
		int i1 = j;
		int j1 = k;
		int k1 = l;
		int l1 = 0;
		int i2 = this.random.nextInt(4);
		MutableBlockPos mpos = new MutableBlockPos();

		for (int j2 = j - i; j2 <= j + i; ++j2)
		{
			double d1 = j2 + 0.5D - ent.posX;

			for (int l2 = l - i; l2 <= l + i; ++l2)
			{
				double d2 = l2 + 0.5D - ent.posZ;
				label142:

					for (int j3 = this.worldServerInstance.getActualHeight() - 1; j3 >= 0; --j3)
					{
						if (this.worldServerInstance.isAirBlock(mpos.setPos(j2, j3, l2)))
						{
							while (j3 > 0 && this.worldServerInstance.isAirBlock(mpos.setPos(j2, j3 - 1, l2)))
							{
								--j3;
							}

							for (int k3 = i2; k3 < i2 + 4; ++k3)
							{
								int l3 = k3 % 2;
								int i4 = 1 - l3;

								if (k3 % 4 >= 2)
								{
									l3 = -l3;
									i4 = -i4;
								}

								for (int j4 = 0; j4 < 3; ++j4)
								{
									for (int k4 = 0; k4 < 4; ++k4)
									{
										for (int l4 = -1; l4 < 4; ++l4)
										{
											int i5 = j2 + (k4 - 1) * l3 + j4 * i4;
											int j5 = j3 + l4;
											int k5 = l2 + (k4 - 1) * i4 - j4 * l3;
											mpos.setPos(i5, j5, k5);

											IBlockState state = this.worldServerInstance.getBlockState(mpos);
											if (l4 < 0 && !state.getBlock().getMaterial(state).isSolid() || 
													l4 >= 0 && !this.worldServerInstance.isAirBlock(mpos))
											{
												continue label142;
											}
										}
									}
								}

								double d5 = j3 + 0.5D - ent.posY;
								double d7 = d1 * d1 + d5 * d5 + d2 * d2;

								if (d0 < 0.0D || d7 < d0)
								{
									d0 = d7;
									i1 = j2;
									j1 = j3;
									k1 = l2;
									l1 = k3 % 4;
								}
							}
						}
					}
			}
		}

		if (d0 < 0.0D)
		{
			for (int l5 = j - i; l5 <= j + i; ++l5)
			{
				double d3 = l5 + 0.5D - ent.posX;

				for (int j6 = l - i; j6 <= l + i; ++j6)
				{
					double d4 = j6 + 0.5D - ent.posZ;
					label562:

						for (int i7 = this.worldServerInstance.getActualHeight() - 1; i7 >= 0; --i7)
						{
							if (this.worldServerInstance.isAirBlock(mpos.setPos(l5, i7, j6)))
							{
								while (i7 > 0 && this.worldServerInstance.isAirBlock(mpos.setPos(l5, i7 - 1, j6)))
								{
									--i7;
								}

								for (int k7 = i2; k7 < i2 + 2; ++k7)
								{
									int j8 = k7 % 2;
									int j9 = 1 - j8;

									for (int j10 = 0; j10 < 4; ++j10)
									{
										for (int j11 = -1; j11 < 4; ++j11)
										{
											int j12 = l5 + (j10 - 1) * j8;
											int i13 = i7 + j11;
											int j13 = j6 + (j10 - 1) * j9;
											mpos.setPos(j12, i13, j13);

											IBlockState state = this.worldServerInstance.getBlockState(mpos);
											if (j11 < 0 && !state.getBlock().getMaterial(state).isSolid() || 
													j11 >= 0 && !this.worldServerInstance.isAirBlock(mpos))
											{
												continue label562;
											}
										}
									}

									double d6 = i7 + 0.5D - ent.posY;
									double d8 = d3 * d3 + d6 * d6 + d4 * d4;

									if (d0 < 0.0D || d8 < d0)
									{
										d0 = d8;
										i1 = l5;
										j1 = i7;
										k1 = j6;
										l1 = k7 % 2;
									}
								}
							}
						}
				}
			}
		}

		int i6 = i1;
		int k2 = j1;
		int k6 = k1;
		int l6 = l1 % 2;
		int i3 = 1 - l6;

		if (l1 % 4 >= 2)
		{
			l6 = -l6;
			i3 = -i3;
		}

		if (d0 < 0.0D)
		{
			j1 = MathHelper.clamp(j1, 70, this.worldServerInstance.getActualHeight() - 10);
			k2 = j1;

			for (int j7 = -1; j7 <= 1; ++j7)
			{
				for (int l7 = 1; l7 < 3; ++l7)
				{
					for (int k8 = -1; k8 < 3; ++k8)
					{
						int k9 = i6 + (l7 - 1) * l6 + j7 * i3;
						int k10 = k2 + k8;
						int k11 = k6 + (l7 - 1) * i3 - j7 * l6;
						boolean flag = k8 < 0;
						this.worldServerInstance.setBlockState(new BlockPos(k9, k10, k11), flag ? LCBlocks.stoneBlock.getStateFromMeta(BlockStone.DEMON) : 
							Blocks.AIR.getDefaultState());
					}
				}
			}
		}

		IBlockState iblockstate = LCBlocks.underPortal.getDefaultState().withProperty(BlockPortal.AXIS, l6 != 0 ? EnumFacing.Axis.X : EnumFacing.Axis.Z);

		for (int i8 = 0; i8 < 4; ++i8)
		{
			for (int l8 = 0; l8 < 4; ++l8)
			{
				for (int l9 = -1; l9 < 4; ++l9)
				{
					int l10 = i6 + (l8 - 1) * l6;
					int l11 = k2 + l9;
					int k12 = k6 + (l8 - 1) * i3;
					boolean flag1 = l8 == 0 || l8 == 3 || l9 == -1 || l9 == 3;
					this.worldServerInstance.setBlockState(new BlockPos(l10, l11, k12), flag1 ? LCBlocks.stoneBlock.getStateFromMeta(BlockStone.DEMON) : 
						iblockstate, 2);
				}
			}

			for (int i9 = 0; i9 < 4; ++i9)
			{
				for (int i10 = -1; i10 < 4; ++i10)
				{
					int i11 = i6 + (i9 - 1) * l6;
					int i12 = k2 + i10;
					int l12 = k6 + (i9 - 1) * i3;
					BlockPos blockpos = new BlockPos(i11, i12, l12);
					this.worldServerInstance.notifyNeighborsOfStateChange(blockpos, this.worldServerInstance.getBlockState(blockpos).getBlock(), false);
				}
			}
		}

		return true;
	}

	/**
	 * called periodically to remove out-of-date portal locations from the cache list. Argument par1 is a
	 * WorldServer.getTotalWorldTime() value.
	 */
	 @Override
	public void removeStalePortalLocations(long worldTime)
	{
		if (worldTime % 100L == 0L)
		{
			Iterator<Long> iterator = this.destinationCoordinateKeys.iterator();
			long i = worldTime - 300L;

			while (iterator.hasNext())
			{
				Long olong = iterator.next();
				PortalPosition teleporter$portalposition = this.destinationCoordinateCache.get(olong.longValue());

				if (teleporter$portalposition == null || teleporter$portalposition.lastUpdateTime < i)
				{
					iterator.remove();
					this.destinationCoordinateCache.remove(olong.longValue());
				}
			}
		}
	} 

	/** Portal position class */
	public class PortalPosition extends Teleporter.PortalPosition
	{
		public PortalPosition(BlockPos pos, long lastUpdate)
		{
			super(pos, lastUpdate);
		}
	}
}