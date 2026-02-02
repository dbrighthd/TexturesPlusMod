package com.dbrighthd.texturesplusmod.datapack;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;

public record ItemCategory(Block block, BlockPos position, Direction direction, boolean excluded) {
    public ItemCategory(Block block, BlockPos position, Direction direction) {
        this(block, position, direction, false);
    }
}
