package powercrystals.minefactoryreloaded.transport;

import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import powercrystals.core.position.IRotateableTile;
import powercrystals.core.util.Util;
import powercrystals.minefactoryreloaded.MineFactoryReloadedCore;
import powercrystals.minefactoryreloaded.core.MFRUtil;
import powercrystals.minefactoryreloaded.gui.MFRCreativeTab;

public class BlockConveyor extends BlockContainer
{
	public BlockConveyor(int i, int j)
	{
		super(i, j, Material.circuits);
		setHardness(0.5F);
		setBlockName("factoryConveyor");
		setBlockBounds(0.0F, 0.0F, 0.0F, 0.1F, 0.1F, 0.1F);
		setRequiresSelfNotify();
                setCreativeTab(MFRCreativeTab.tab);
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving entity)
	{
		if(entity == null)
		{
			return;
		}
		int l = MathHelper.floor_double((double)((entity.rotationYaw * 4F) / 360F) + 0.5D) & 3;
		if(l == 0)
		{
			world.setBlockMetadataWithNotify(x, y, z, 1);
		}
		if(l == 1)
		{
			world.setBlockMetadataWithNotify(x, y, z, 2);
		}
		if(l == 2)
		{
			world.setBlockMetadataWithNotify(x, y, z, 3);
		}
		if(l == 3)
		{
			world.setBlockMetadataWithNotify(x, y, z, 0);
		}
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity)
	{
		if(!(entity instanceof EntityItem) && !(entity instanceof EntityLiving) || Util.isRedstonePowered(world.getBlockTileEntity(x, y, z)))
		{
			return;
		}
		
		double xVelocity = 0;
		double yVelocity = 0;
		double zVelocity = 0;
		
		int md = world.getBlockMetadata(x, y, z);
		
		int horizDirection = md & 0x03;
		boolean isUphill = (md & 0x04) != 0;
		boolean isDownhill = (md & 0x08) != 0;
		
		if(isUphill)
		{
			yVelocity = 0.25D;
		}
		
		if(isUphill || isDownhill)
		{
			entity.onGround = false;
		}
		
		
		if(horizDirection == 0)
		{
			xVelocity = 0.1D;
		}
		else if(horizDirection == 1)
		{
			zVelocity = 0.1D;
		}
		else if(horizDirection == 2)
		{
			xVelocity = -0.1D;
		}
		else if(horizDirection == 3)
		{
			zVelocity = -0.1D;
		}
		
		if(horizDirection == 0 || horizDirection == 2)
		{
			if(entity.posZ > z + 0.55D) zVelocity = -0.1D;
			else if(entity.posZ < z + 0.45D) zVelocity = 0.1D;
		}
		else if(horizDirection == 1 || horizDirection == 3)
		{
			if(entity.posX > x + 0.55D) xVelocity = -0.1D;
			else if(entity.posX < x + 0.45D) xVelocity = 0.1D;
		}
		
		setEntityVelocity(entity, xVelocity, yVelocity, zVelocity);
		
		if(entity instanceof EntityLiving)
		{
			((EntityLiving)entity).fallDistance = 0;
		}
	}
	
	@Override
	public int getBlockTexture(IBlockAccess iblockaccess, int x, int y, int z, int side)
	{
		TileEntity te = iblockaccess.getBlockTileEntity(x, y, z);
		if(te != null && te instanceof TileEntityConveyor)
		{
			if(Util.isRedstonePowered(te))
			{
				if(MineFactoryReloadedCore.animateBlockFaces.getBoolean(true))
				{
					return MineFactoryReloadedCore.conveyorOffTexture;
				}
				else
				{
					return MineFactoryReloadedCore.conveyorStillOffTexture;
				}
			}
			else
			{
				return blockIndexInTexture;
			}
		}
		else
		{
			return blockIndexInTexture;
		}
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
	{
		int md = world.getBlockMetadata(x, y, z);

		if((md & 0x0C) == 0)
		{
			return AxisAlignedBB.getAABBPool().addOrModifyAABBInPool(x + 0.05F, y, z + 0.05F, (x + 1) - 0.05F, y + 0.1F, z + 1 - 0.05F);
		}
		else
		{
			return AxisAlignedBB.getAABBPool().addOrModifyAABBInPool(x + 0.2F, y, z + 0.2F, (x + 1) - 0.2F, y + 0.1F, z + 1 - 0.2F);
		}
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z)
	{
		return getCollisionBoundingBoxFromPool(world, x, y, z);
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World world, int i, int j, int k, Vec3 vec3d, Vec3 vec3d1)
	{
		setBlockBoundsBasedOnState(world, i, j, k);
		return super.collisionRayTrace(world, i, j, k, vec3d, vec3d1);
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess iblockaccess, int i, int j, int k)
	{
		int l = iblockaccess.getBlockMetadata(i, j, k);
		if(l >= 4 && l <= 11)
		{
			setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
		}
		else
		{
			setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);
		}
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public int getRenderType()
	{
		return MineFactoryReloadedCore.renderIdConveyor;
	}

	@Override
	public int quantityDropped(Random random)
	{
		return 1;
	}

	@Override
	public boolean canPlaceBlockAt(World world, int i, int j, int k)
	{
		return world.isBlockOpaqueCube(i, j - 1, k);
	}

	@Override
	public void onBlockAdded(World world, int i, int j, int k)
	{
		super.onBlockAdded(world, i, j, k);
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7, float par8, float par9)
	{
		if(MFRUtil.isHoldingWrench(entityplayer))
		{
			TileEntity te = world.getBlockTileEntity(i, j, k);
			if(te != null && te instanceof IRotateableTile)
			{
				((IRotateableTile)te).rotate();
			}
		}
		return true;
	}

	@Override
	public void onNeighborBlockChange(World world, int i, int j, int k, int l)
	{
		if(!world.isRemote && !world.isBlockOpaqueCube(i, j - 1, k))
		{
			dropBlockAsItem(world, i, j, k, world.getBlockMetadata(i, j, k), 0);
			world.setBlockWithNotify(i, j, k, 0);
		}
	}
	
	private void setEntityVelocity(Entity e, double x, double y, double z)
	{
		e.motionX = x;
		e.motionY = y;
		e.motionZ = z;
	}

	@Override
	public String getTextureFile()
	{
		return MineFactoryReloadedCore.terrainTexture;
	}

	@Override
	public boolean canProvidePower()
	{
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World var1)
	{
		return new TileEntityConveyor();
	}
}
