package com.hfr.blocks.machine;

import java.util.List;

import com.hfr.blocks.ModBlocks;
import com.hfr.data.MarketData;
import com.hfr.lib.RefStrings;
import com.hfr.main.MainRegistry;
import com.hfr.packet.PacketDispatcher;
import com.hfr.packet.tile.MarketNameSyncPacket;
import com.hfr.packet.tile.OfferPacket;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class MachineMarket extends BlockContainer {

	//todo: figure out why markets work clientside, but not serverside

	@SideOnly(Side.CLIENT)
	private IIcon iconTop;
	@SideOnly(Side.CLIENT)
	private IIcon iconBottom;
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.iconTop = iconRegister.registerIcon(RefStrings.MODID + ":market_top");
		this.iconBottom = iconRegister.registerIcon(RefStrings.MODID + ":market_bottom");
		this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":market_side");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		
		return side == 1 ? this.iconTop : (side == 0 ? this.iconBottom : this.blockIcon);
	}

	public MachineMarket(Material p_i45386_1_) {
		super(p_i45386_1_);
	}
	
    public static String name = "";

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if (!world.isRemote) {
			TileEntityMarket market = (TileEntityMarket) world.getTileEntity(x, y, z);
			if (market == null) return false;

			// Handle renaming the market with a name tag
			if (player.getHeldItem() != null && player.getHeldItem().getItem() == Items.name_tag && player.getHeldItem().hasDisplayName()) {
				market.setName(player.getHeldItem().getDisplayName());
				world.markBlockForUpdate(x, y, z);  // Ensure the block updates in both singleplayer and multiplayer
				return true;
			}

			// Get offers from JSON-based MarketData
			List<ItemStack[]> offers = MarketData.getOffers(market.getName());

			// Create NBTTagCompound to send offer data
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString("market", market.getName());
			nbt.setInteger("offercount", offers.size());

			for (int i = 0; i < offers.size(); i++) {
				NBTTagList list = new NBTTagList();
				ItemStack[] offerArray = offers.get(i);

				for (int j = 0; j < offerArray.length; j++) {
					if (offerArray[j] != null) {
						NBTTagCompound itemTag = new NBTTagCompound();
						offerArray[j].writeToNBT(itemTag);
						list.appendTag(itemTag);
					}
				}
				nbt.setTag("items" + i, list);
			}

			// Send updated market offers to client
			PacketDispatcher.wrapper.sendTo(new OfferPacket(nbt), (EntityPlayerMP) player);

			return true;
		} else if (!player.isSneaking()) {
			// Open GUI for Market
			FMLNetworkHandler.openGui(player, MainRegistry.instance, ModBlocks.guiID_market, world, x, y, z);
			return true;
		} else {
			return false;
		}
	}


	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityMarket();
	}

	public static class TileEntityMarket extends TileEntity {
		private String name = "";

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
			markDirty();
			if (!worldObj.isRemote) {
				syncName();
			}
		}

		private void syncName() {
			NBTTagCompound nbt = new NBTTagCompound();
			writeToNBT(nbt);
			PacketDispatcher.wrapper.sendToAllAround(new MarketNameSyncPacket(xCoord, yCoord, zCoord, nbt), new NetworkRegistry.TargetPoint(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, 64));
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			name = nbt.getString("name");
		}

		@Override
		public void writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			nbt.setString("name", name);
		}

		@Override
		public void updateEntity() {
			if (!worldObj.isRemote) {
				markDirty(); // Forces a save
			}
		}
	}


}
