package dev.cephelo.musicbox.block.custom;

import dev.cephelo.musicbox.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import static dev.cephelo.musicbox.block.custom.MusicboxBlock.BEACON;

public class ChorusOreBlock extends Block {
    public ChorusOreBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState());
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        sing(level, pos, random, 0.9);
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
    ) {
        if (level instanceof ServerLevel serverLevel) sing(serverLevel, pos, level.getRandom(), 0.9);

        return stack.getItem() instanceof BlockItem && new BlockPlaceContext(player, hand, stack, hitResult).canPlace()
                ? ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION
                : ItemInteractionResult.SUCCESS;
    }

    public static void sing(ServerLevel level, BlockPos pos, RandomSource random, double chance) {
        if (Math.random() <= chance)  {
            // Pitch is random between 0.55 and 0.85 or 1.4 and 1.7
            level.playSound(null, pos, ModSounds.ORE_SING.get(), SoundSource.BLOCKS, 1,
                    (random.nextFloat() - random.nextFloat()) * 0.15F + 0.7F + (random.nextBoolean() ? 0.85F : 0));

            // Music note particle when singing
            Vec3 vec3 = Vec3.atBottomCenterOf(pos).add(0.0, 1.2F, 0.0);
            float f = level.getRandom().nextInt(4) / 24.0F + 0.4F;
            level.sendParticles(ParticleTypes.NOTE, vec3.x(), vec3.y(), vec3.z(), 0, f, 0.0, 0.0, 1.0);
        }
    }
}
