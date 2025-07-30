package dev.cephelo.musicbox.block.custom;

import com.mojang.serialization.MapCodec;
import dev.cephelo.musicbox.block.entity.PedestalBlockEntity;
import dev.cephelo.musicbox.recipe.*;
import dev.cephelo.musicbox.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PedestalBlock extends BaseEntityBlock {
    public static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 13, 12);
    public static final MapCodec<PedestalBlock> CODEC = simpleCodec(PedestalBlock::new);

    public PedestalBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    /* BLOCK ENTITY STUFF */

    // Would be invisible without this
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PedestalBlockEntity(pos, state);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        // Check if this is the same block
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof PedestalBlockEntity pedestalBlockEntity) {
                pedestalBlockEntity.drops();
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        //return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        if(level.getBlockEntity(pos) instanceof PedestalBlockEntity pedestalBlockEntity) {
            if (pedestalBlockEntity.inventory.getStackInSlot(0).isEmpty() && !stack.isEmpty()) {
                pedestalBlockEntity.inventory.insertItem(0, stack.copy(), false);
                stack.shrink(1);
                level.playSound(player, pos, ModSounds.PEDESTAL_ITEM_PLACE.get(), SoundSource.BLOCKS);
            } else if (!pedestalBlockEntity.inventory.getStackInSlot(0).isEmpty() && !stack.isEmpty()) {
                ItemStack stackOnPedestal = pedestalBlockEntity.inventory.extractItem(0, 1, false);

                PedestalRecipe recipe = checkRecipe(level, stack, stackOnPedestal);
                if (recipe != null && !level.isClientSide()) {
                    if (recipe.consume()) stack.shrink(1);
                    pedestalBlockEntity.clearContents();
                    pedestalBlockEntity.inventory.insertItem(0, recipe.output().copy(), false);
                    level.playSound(null, pos, ModSounds.PEDESTAL_CRAFT.get(), SoundSource.BLOCKS, 1,
                            (level.random.nextFloat() - level.random.nextFloat()) * 0.1F + 1);
                } else if (!level.isClientSide()) {
                    player.addItem(stackOnPedestal);
                    pedestalBlockEntity.clearContents();
                } else level.playSound(player, pos, ModSounds.PEDESTAL_ITEM_PICKUP.get(), SoundSource.BLOCKS);

            } else if (!pedestalBlockEntity.inventory.getStackInSlot(0).isEmpty() && stack.isEmpty()) {
                ItemStack stackOnPedestal = pedestalBlockEntity.inventory.extractItem(0, 1, false);
                player.setItemInHand(InteractionHand.MAIN_HAND, stackOnPedestal);
                pedestalBlockEntity.clearContents();
                level.playSound(player, pos, ModSounds.PEDESTAL_ITEM_PICKUP.get(), SoundSource.BLOCKS);
            }
        }

        return ItemInteractionResult.SUCCESS;
    }

    private PedestalRecipe checkRecipe(Level level, ItemStack stack, ItemStack stackOnPedestal) {
        NonNullList<ItemStack> itemList = NonNullList.create();
        itemList.add(stackOnPedestal);
        itemList.add(stack);

        Optional<RecipeHolder<PedestalRecipe>> recipe =
                level.getRecipeManager().getRecipeFor(ModRecipes.PEDESTAL_TYPE.get(),
                        new PedestalRecipeInput(itemList), level);

        return recipe.isEmpty() ? null : recipe.get().value();
    }

}
