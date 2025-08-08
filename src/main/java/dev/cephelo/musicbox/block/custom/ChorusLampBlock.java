package dev.cephelo.musicbox.block.custom;

import dev.cephelo.musicbox.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ChorusLampBlock extends RedstoneLampBlock {
    public ChorusLampBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(LIT, false));
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Will sing if lit and signal strength is 15
        if (state.getValue(LIT) && level.getDirectSignalTo(pos) >= Config.LAMP_SING_STRENGTH.get())
            sing(level, pos, random);
    }

    // Same as RedstoneLampBlock but with one addition
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            boolean flag = state.getValue(LIT);
            if (flag != level.hasNeighborSignal(pos)) {
                if (flag) {
                    level.scheduleTick(pos, this, 4);
                } else {
                    level.setBlock(pos, state.cycle(LIT), 2);
                    if (flag == false) sing(level, pos, level.random);
                }
            }
        }
    }

    private void sing(Level level, BlockPos pos, RandomSource random) {
        ChorusOreBlock.sing(level, pos, random, Config.LAMP_SING_CHANCE.get());
    }
}
