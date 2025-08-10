package dev.cephelo.musicbox.block.custom;

import com.mojang.serialization.MapCodec;
import dev.cephelo.musicbox.block.entity.MusicboxBlockEntity;
import dev.cephelo.musicbox.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class MusicboxBlock extends BaseEntityBlock {
    public static final MapCodec<MusicboxBlock> CODEC = simpleCodec(MusicboxBlock::new);

    public static final EnumProperty<MusicboxStatus> STATUS = EnumProperty.create("status", MusicboxStatus.class);
    public static final BooleanProperty BEACON = BooleanProperty.create("beacon_powered");
    public static final BooleanProperty READY = BooleanProperty.create("ready_to_craft");

    public MusicboxBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(STATUS, MusicboxStatus.IDLE)
                .setValue(BEACON, false)
                .setValue(READY, false)
        );
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
         return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MusicboxBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof MusicboxBlockEntity musicboxBlockEntity) {
                musicboxBlockEntity.drops();
                musicboxBlockEntity.onRemove();
            }
        }

        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack pStack, BlockState pState, Level pLevel, BlockPos pPos,
                                              Player pPlayer, InteractionHand pHand, BlockHitResult pHitResult) {
        if (!pLevel.isClientSide()) {
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            if(entity instanceof MusicboxBlockEntity musicboxBlockEntity) {
                ((ServerPlayer) pPlayer).openMenu(new SimpleMenuProvider(musicboxBlockEntity, Component.literal("Music Box")), pPos);
            } else {
                throw new IllegalStateException("missing container provider...");
            }
        }

        return ItemInteractionResult.sidedSuccess(pLevel.isClientSide());
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) return null;

        return createTickerHelper(blockEntityType, ModBlockEntities.MUSICBOX_BE.get(),
                (pLevel, blockpos, blockstate, blockEntity) -> blockEntity.tick(pLevel, blockpos, blockstate));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STATUS, BEACON, READY);
    }

    // neighborChanged triggers twice (once from setBlockAndUpdate, once from tick) under certain conditions???
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (level.hasNeighborSignal(pos)) {
            MusicboxBlockEntity be = ((MusicboxBlockEntity)level.getBlockEntity(pos));
            if (be != null) be.handleButtonPress(1);
        }
    }

    // Comparator output
    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        int status = switch (state.getValue(STATUS)) {
            case IDLE -> 1;
            case PREVIEW -> 2;
            case CRAFTING -> 3 + 8;
        };

        return status + (state.getValue(BEACON) ? 4 : 0) + (state.getValue(READY) ? 8 : 0);
    }
}
