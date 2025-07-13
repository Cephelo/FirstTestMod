package dev.cephelo.musicbox.block.entity;

import dev.cephelo.musicbox.recipe.ModRecipes;
import dev.cephelo.musicbox.recipe.MusicboxRecipe;
import dev.cephelo.musicbox.recipe.MusicboxRecipeInput;
import dev.cephelo.musicbox.screens.custom.MusicboxMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class MusicboxBlockEntity extends BlockEntity implements MenuProvider {
    public final ItemStackHandler itemHandler = new ItemStackHandler(5) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if(!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    private static final int INPUT1_SLOT = 0;
    private static final int INPUT2_SLOT = 1;
    private static final int INPUT3_SLOT = 2;
    private static final int INPUT4_SLOT = 3;
    private static final int OUTPUT_SLOT = 4;

    protected final ContainerData data;

    private int progress = 0;
    private int maxProgress = 200;
    private ItemStack itemBeingCrafted = null;

    private int previewProgress = 0;
    private int maxPreviewProgress = 160;
    private boolean isPlayingPreviewSound = false;

    private boolean previewButtonEnabled = false;
    private boolean craftButtonEnabled = false;


    public MusicboxBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.MUSICBOX_BE.get(), pos, blockState);
        data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i) {
                    case 0 -> MusicboxBlockEntity.this.progress;
                    case 1 -> MusicboxBlockEntity.this.maxProgress;
                    case 2 -> MusicboxBlockEntity.this.previewProgress;
                    case 3 -> MusicboxBlockEntity.this.maxPreviewProgress;
                    case 4 -> MusicboxBlockEntity.this.previewButtonEnabled ? 1 : 0; // cast bool to int
                    case 5 -> MusicboxBlockEntity.this.craftButtonEnabled ? 1 : 0; // cast bool to int
                    default -> 0;
                };
            }

            @Override
            public void set(int i, int value) {
                switch (i) {
                    case 0: MusicboxBlockEntity.this.progress = value;
                    case 1: MusicboxBlockEntity.this.maxProgress = value;
                    case 2: MusicboxBlockEntity.this.previewProgress = value;
                    case 3: MusicboxBlockEntity.this.maxPreviewProgress = value;
                    case 4: MusicboxBlockEntity.this.previewButtonEnabled = value == 1; // cast int to bool
                    case 5: MusicboxBlockEntity.this.craftButtonEnabled = value == 1; // cast int to bool
                }
            }

            @Override
            public int getCount() {
                return 6;
            }
        };
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.musicbox.musicbox");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new MusicboxMenu(containerId, inventory, this, this.data);
    }

    public void drops() {
        SimpleContainer inv = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inv.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        pTag.put("inventory", itemHandler.serializeNBT(pRegistries));
        pTag.putInt("musicbox.progress", progress);
        pTag.putInt("musicbox.max_progress", maxProgress);
        pTag.putInt("musicbox.preview_progress", previewProgress);
        pTag.putInt("musicbox.max_preview_progress", maxPreviewProgress);
        pTag.putBoolean("musicbox.preview_button_enabled", previewButtonEnabled);
        pTag.putBoolean("musicbox.craft_button_enabled", craftButtonEnabled);

        super.saveAdditional(pTag, pRegistries);
    }

    @Override
    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);

        itemHandler.deserializeNBT(pRegistries, pTag.getCompound("inventory"));
        progress = pTag.getInt("musicbox.progress");
        maxProgress = pTag.getInt("musicbox.max_progress");
        previewProgress = pTag.getInt("musicbox.preview_progress");
        maxPreviewProgress = pTag.getInt("musicbox.max_preview_progress");
        previewButtonEnabled = pTag.getBoolean("musicbox.preview_button_enabled");
        craftButtonEnabled = pTag.getBoolean("musicbox.craft_button_enabled");
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        attemptDiscWrite();

        if (itemBeingCrafted != null) {
            progress++;
            if (progress >= maxProgress) {
                // Reset progress
                progress = 0;

                // Put item in output slot
                itemHandler.setStackInSlot(OUTPUT_SLOT, itemBeingCrafted);

                itemBeingCrafted = null;
                // play finish sound
            }

            if (progress == 180) {
                // play 11 sound
            }
        }

        if (isPlayingPreviewSound) previewProgress++;
        setChanged(level, pos, state);

        if (progress == 0 && itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty()) {
            MusicboxRecipe recipe = checkRecipe();
            enableButtons(recipe != null && recipe.preview(), recipe != null);
            if (recipe != null) {
                // show ghost item in output
            } else {
                // hide ghost item in output
            }
        }

        if (previewProgress >= maxPreviewProgress) stopPreviewSound(true);

    }

    // When Craft button is clicked
    public void attemptDiscWrite() {
        MusicboxRecipe recipe = checkRecipe();
        if (recipe != null && itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty()) {
            stopPreviewSound(false);
            enableButtons(false, false);
            // hide ghost item in output

            // Remove 1 item from each input slot
            itemHandler.extractItem(INPUT1_SLOT, 1, false);
            itemHandler.extractItem(INPUT2_SLOT, 1, false);
            itemHandler.extractItem(INPUT3_SLOT, 1, false);
            itemHandler.extractItem(INPUT4_SLOT, 1, false);

            itemBeingCrafted = recipe.output();
            playPreviewSound(true);
        } else {
            // error sound
        }
    }

    private MusicboxRecipe checkRecipe() {

        NonNullList<ItemStack> itemList = NonNullList.create();
        for (int i = 0; i < 4; i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) {
                itemList.add(itemHandler.getStackInSlot(i));
            }
        }

        Optional<RecipeHolder<MusicboxRecipe>> recipe =
                this.level.getRecipeManager().getRecipeFor(ModRecipes.MUSICBOX_TYPE.get(),
                        new MusicboxRecipeInput(itemList), level);

        return recipe.isEmpty() ? null : recipe.get().value();
    }

    private void playPreviewSound(boolean spedUp) {
        MusicboxRecipe recipe = checkRecipe();
        if (recipe != null && itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty()) {
            stopPreviewSound(false);
            // play tape start sound
            // play recipe.sound on SoundSource, pitch spedUp+1
            isPlayingPreviewSound = true;
        } else {
            // play error sound
        }
    }

    private void stopPreviewSound(boolean playStopSound) {
        // stop SoundSource
        previewProgress = 0;
        isPlayingPreviewSound = false;

        if (playStopSound) {
            // play tape stop sound
        }
    }

    private void enableButtons(boolean enablePreviewButton, boolean enableCraftButton) {
        // enable/disable buttons
        previewButtonEnabled = enablePreviewButton;
        craftButtonEnabled = enableCraftButton;
    }

    // Client-Server Sync
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    // Client-Server Sync
    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
