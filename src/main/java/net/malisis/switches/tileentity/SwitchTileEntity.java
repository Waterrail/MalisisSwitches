/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Ordinastie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.malisis.switches.tileentity;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Set;

import net.malisis.switches.MalisisSwitches;
import net.malisis.switches.PowerManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import com.google.common.collect.Sets;

/**
 * @author Ordinastie
 *
 */
public class SwitchTileEntity extends TileEntity
{
	Set<BlockPos> linkedPos = Sets.newHashSet();

	public Set<BlockPos> linkedPositions()
	{
		return linkedPos;
	}

	public void linkPosition(BlockPos pos)
	{
		linkedPos.add(pos);
	}

	public void unlinkPosition(BlockPos pos)
	{
		linkedPos.remove(pos);
		PowerManager.setPower(getWorld(), pos, 0);
		getWorld().notifyBlockOfStateChange(pos, MalisisSwitches.Blocks.switchBlock);
		getWorld().notifyNeighborsOfStateChange(pos, MalisisSwitches.Blocks.switchBlock);
	}

	public void setPower(int power)
	{
		for (BlockPos pos : linkedPos)
		{
			PowerManager.setPower(getWorld(), pos, power);
			getWorld().notifyBlockOfStateChange(pos, MalisisSwitches.Blocks.switchBlock);
			getWorld().notifyNeighborsOfStateChange(pos, MalisisSwitches.Blocks.switchBlock);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);
		ByteBuf bytes = Unpooled.buffer(linkedPos.size() * 8);
		for (BlockPos pos : linkedPos)
			bytes.writeLong(pos.toLong());
		tag.setByteArray("linkedPos", bytes.array());
		System.out.println("Saved " + linkedPos.size());
	}

	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT(tag);
		ByteBuf bytes = Unpooled.copiedBuffer(tag.getByteArray("linkedPos"));
		for (int i = 0; i < bytes.capacity() / 8; i++)
			linkedPos.add(BlockPos.fromLong(bytes.readLong()));
		System.out.println("Read " + linkedPos.size());
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
	{
		return oldState.getBlock() != newSate.getBlock();
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		return INFINITE_EXTENT_AABB;
	}
}
