package dev.cephelo.musicbox.block.entity;

import dev.cephelo.musicbox.Config;
import dev.cephelo.musicbox.MusicBoxMod;
import dev.cephelo.musicbox.block.custom.MusicboxStatus;
import dev.cephelo.musicbox.handler.MBToggleButtonPacket;
import dev.cephelo.musicbox.recipe.ModRecipes;
import dev.cephelo.musicbox.recipe.MusicboxRecipe;
import dev.cephelo.musicbox.recipe.MusicboxRecipeInput;
import dev.cephelo.musicbox.screens.custom.MusicboxMenu;
import dev.cephelo.musicbox.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static dev.cephelo.musicbox.block.custom.MusicboxBlock.STATUS;
import static dev.cephelo.musicbox.block.custom.MusicboxBlock.BEACON;
import static dev.cephelo.musicbox.block.custom.MusicboxBlock.READY;

public class MusicboxBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, StackedContentsCompatible {
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
    private int maxProgress;
    private ItemStack itemBeingCrafted = ItemStack.EMPTY;

    private int previewProgress = 0;
    private int maxPreviewProgress;
    private boolean isPlayingPreviewSound = false;

    private final SoundManager manager = Minecraft.getInstance().getSoundManager();
    private SimpleSoundInstance previewSound;
    private final SimpleSoundInstance shudderSound = new SimpleSoundInstance(ModSounds.CRAFTING_SHUDDER.get(), SoundSource.RECORDS, 1, 1, SoundInstance.createUnseededRandom(), this.getBlockPos());

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
                }
            }

            @Override
            public int getCount() {
                return 4;
            }
        };

        maxProgress = Config.MUSICBOX_DEFAULT_CRAFT_TIME.get();
        maxPreviewProgress = Config.MUSICBOX_DEFAULT_PREVIEW_TIME.get();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.musicbox.musicbox");
    }

    @Override
    protected Component getDefaultName() {
        return getDisplayName();
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        NonNullList<ItemStack> itemList = NonNullList.create();
        for (int i = 0; i < 5; i++) {
            itemList.add(itemHandler.getStackInSlot(i));
        }
        return itemList;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        itemHandler.setStackInSlot(slot, stack);
        stack.limitSize(this.getMaxStackSize(stack));
        this.setChanged();
    }

    @Override
    protected void setItems(NonNullList<ItemStack> nonNullList) {
        for (int i = 0; i < 5; i++) {
            itemHandler.setStackInSlot(i, nonNullList.get(i));
        }
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new MusicboxMenu(containerId, inventory, this, this.data);
    }

    @Override
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return createMenu(i, inventory, null);
    }

    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.DOWN) return new int[]{4};
        else return new int[]{0, 1, 2, 3};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStack, @javax.annotation.Nullable Direction direction) {
        return direction != Direction.DOWN;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return direction == Direction.DOWN;
    }

    @Override
    public int getContainerSize() {
        return 4;
    }

    @Override
    public void fillStackedContents(StackedContents stackedContents) {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            stackedContents.accountStack(itemHandler.getStackInSlot(i));
        }
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
        pTag.putBoolean("musicbox.is_playing_preview_sound", isPlayingPreviewSound);
        pTag.put("musicbox.item_being_crafted", ItemStack.OPTIONAL_CODEC.encodeStart(pRegistries.createSerializationContext(NbtOps.INSTANCE), itemBeingCrafted).result().get());

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
        isPlayingPreviewSound = pTag.getBoolean("musicbox.is_playing_preview_sound");
        itemBeingCrafted = ItemStack.OPTIONAL_CODEC.decode(pRegistries.createSerializationContext(NbtOps.INSTANCE),
                        pTag.get("musicbox.item_being_crafted")).result().get().getFirst();
    }

    public void tick(Level level, BlockPos pos, BlockState state) {

        // Display music note particles like jukebox
        if (isPlayingPreviewSound && previewProgress % (itemBeingCrafted == ItemStack.EMPTY ? 20 : 10) == 0) {
            if (level instanceof ServerLevel serverlevel) {
                Vec3 vec3 = Vec3.atBottomCenterOf(pos).add(0.0, 1.2F, 0.0);
                float f = level.getRandom().nextInt(4) / 24.0F + (this.getBlockState().getValue(BEACON) ? 0.65F : 0.35F);
                serverlevel.sendParticles(ParticleTypes.NOTE, vec3.x(), vec3.y(), vec3.z(), 0, f, 0.0, 0.0, 1.0);
            }
        }

        // Beacon check
        boolean beacon = level.getBlockEntity(this.getBlockPos().below()) instanceof BeaconBlockEntity bea && bea.levels >= 4 && !bea.getBeamSections().isEmpty();
        if (this.getBlockState().getValue(BEACON) != beacon) {

            // Only deactivates if not crafting, otherwise changes normally (for texture aesthetics)
            if (progress == 0 || beacon) level.setBlock(pos, state.setValue(BEACON, beacon), 3);

            // If beacon turned off, stop playing preview sound
            if (!beacon && previewProgress > 0 && progress == 0) {
                this.stopPreviewSound(true, false);
                level.playSound(null, this.getBlockPos(), ModSounds.BEACON_FAIL.get(), SoundSource.RECORDS);
                return;
            }
        }

        if (itemBeingCrafted != ItemStack.EMPTY || progress > 0) {
            progress++;

            if (level instanceof ServerLevel serverLevel && progress <= maxProgress - Math.min(maxProgress, 40))
                serverLevel.sendParticles(ParticleTypes.PORTAL, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, progress / 10, 0, 0, 0, 0.25);

            if (progress >= maxProgress) {
                // Reset progress
                progress = 0;

                // Put item in output slot
                itemHandler.insertItem(OUTPUT_SLOT, itemBeingCrafted, false);

                itemBeingCrafted = ItemStack.EMPTY;

                // play finish sound and display particles
                manager.stop(shudderSound);
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.playSound(null, this.getBlockPos(), ModSounds.CRAFTING_DONE.get(), SoundSource.RECORDS);
                    serverLevel.sendParticles(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 20, 0.5, 0.5, 0.5, 0.1);
                }

                level.setBlock(pos, state.setValue(STATUS, MusicboxStatus.IDLE), 3);
            }

            if (progress == maxProgress - Math.min(maxProgress, 50)) {
                manager.play(shudderSound); // play 11 sound
            }
        }

        if (isPlayingPreviewSound) {
            previewProgress++;
            // particle
            if (level instanceof ServerLevel serverLevel && previewProgress <= maxPreviewProgress - Math.min(maxPreviewProgress, 40))
                serverLevel.sendParticles(ParticleTypes.PORTAL, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0, 0, 0, 0.25);

            // Loop preview sound if it's too short
            if (!manager.isActive(previewSound)) playPreviewSound(progress >= 2 && Config.MUSICBOX_CRAFT_SPEEDUP.get(), false, progress > 0);
        }

        if (progress == 0) { // && itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty()) {
            MusicboxRecipe recipe = checkRecipe();
            enableButtons(recipe != null && recipe.previewTime() != 0 && canOutputItem(recipe.output()),
                    recipe != null && canOutputItem(recipe.output()));

//            if (recipe != null) {
//                // show ghost item in output
//            } else {
//                // hide ghost item in output
//            }
        }

        if (previewProgress >= maxPreviewProgress) stopPreviewSound(true, false);

        setChanged(level, pos, state);

    }

    public void handleButtonPress(int id) {
        if (this.level == null || this.level.isClientSide()) return;

        switch (id) {
            case 0: { // previewButton
                this.previewButton();
                break;
            }
            case 1: { // craftButton
                this.attemptDiscWrite();
                break;
            }
        }
    }

    private void previewButton() {
        if (itemBeingCrafted != ItemStack.EMPTY) return;

        if (previewProgress < 2) this.playPreviewSound(Config.MUSICBOX_PREVIEW_SPEEDUP.get(), true, false);
        else this.stopPreviewSound(true, false);
    }

    // When Craft button is clicked
    private void attemptDiscWrite() {
        if (itemBeingCrafted != ItemStack.EMPTY) return;

        MusicboxRecipe recipe = checkRecipe();

        if (recipe != null && canOutputItem(recipe.output())) { //itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty()) {

            if (!beaconCheck(recipe.beacon())) return;

            enableButtons(false, false);
            // hide ghost item in output

            // Remove 1 item from each input slot
            itemHandler.extractItem(INPUT1_SLOT, 1, false);
            itemHandler.extractItem(INPUT2_SLOT, 1, false);
            itemHandler.extractItem(INPUT3_SLOT, 1, false);
            itemHandler.extractItem(INPUT4_SLOT, 1, false);

            maxProgress = recipe.craftingTime() == -1 ? Config.MUSICBOX_DEFAULT_CRAFT_TIME.get() : recipe.craftingTime();;
            itemBeingCrafted = recipe.output().copy();
            playPreviewSound(Config.MUSICBOX_CRAFT_SPEEDUP.get(), true, true, recipe);

            if (level != null) level.setBlock(this.getBlockPos(), this.getBlockState().setValue(STATUS, MusicboxStatus.CRAFTING), 3);
        } else {
            // play error sound
            if (level != null) this.level.playSound(null, this.getBlockPos(), ModSounds.ERROR.get(), SoundSource.RECORDS);
            MusicBoxMod.LOGGER.info("null recipe");
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

        return recipe.map(RecipeHolder::value).orElse(null);//recipe.isEmpty() ? null : recipe.get().value();
    }

    private void playPreviewSound(boolean spedUp, boolean isFirstLoop, boolean isCrafting) {
        playPreviewSound(spedUp, isFirstLoop, isCrafting, null);
    }

    private void playPreviewSound(boolean spedUp, boolean isFirstLoop, boolean isCrafting, MusicboxRecipe recipe) {
        if (recipe == null) recipe = checkRecipe();
        if (recipe != null && canOutputItem(recipe.output())) { //itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty()) {
            if (isFirstLoop) stopPreviewSound(false, isCrafting);

            if (!beaconCheck(recipe.beacon())) return;

            if (isFirstLoop) {
                if (isCrafting) maxPreviewProgress = recipe.craftingTime() == -1 ? Config.MUSICBOX_DEFAULT_CRAFT_TIME.get() : recipe.craftingTime();
                else maxPreviewProgress = recipe.previewTime() == -1 ? Config.MUSICBOX_DEFAULT_PREVIEW_TIME.get() : recipe.previewTime();
            }

            if (this.level != null && isFirstLoop)
                this.level.playSound(null, this.getBlockPos(), ModSounds.PREVIEW_START.get(), SoundSource.RECORDS);

            // play recipe.sound on SoundSource, pitch spedUp+1
            SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.tryParse(recipe.sound()));
            if (sound != null) {
                previewSound = new SimpleSoundInstance(sound, SoundSource.RECORDS, 1, (spedUp ? 2 : 1), SoundInstance.createUnseededRandom(), this.getBlockPos());
                manager.play(previewSound);
                isPlayingPreviewSound = true;

                if (!spedUp && level != null) level.setBlock(this.getBlockPos(), this.getBlockState().setValue(STATUS, MusicboxStatus.PREVIEW), 3);
            }
        } else {
            // play error sound
            if (level != null) this.level.playSound(null, this.getBlockPos(), ModSounds.ERROR.get(), SoundSource.RECORDS);
            if (recipe != null) MusicBoxMod.LOGGER.info("could not play sound {} | spedUp: {}", recipe.sound(), spedUp);
            else MusicBoxMod.LOGGER.info("could not play sound [null] | spedUp: {}", spedUp);
        }
    }

    private void stopPreviewSound(boolean playStopSound, boolean craftStop) {
        manager.stop(previewSound);

        if (this.level != null) {
            // Record Scratch sound plays if crafting is started while preview is playing
            if (previewProgress < maxPreviewProgress && previewProgress > 0 && craftStop)
                this.level.playSound(null, this.getBlockPos(), ModSounds.RECORD_SCRATCH.get(), SoundSource.RECORDS);
            // Otherwise stop sound is played
            else if (playStopSound)
                this.level.playSound(null, this.getBlockPos(), ModSounds.PREVIEW_STOP.get(), SoundSource.RECORDS);

            this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(STATUS, MusicboxStatus.IDLE), 3);
        }

        previewProgress = 0;
        isPlayingPreviewSound = false;

    }

    private boolean canOutputItem(ItemStack output) {
        ItemStack slot = itemHandler.getStackInSlot(OUTPUT_SLOT);
        return slot.isEmpty() || (slot.is(output.getItem()) && slot.getCount() < output.getMaxStackSize());
    }

    private boolean beaconCheck(boolean needsBeacon) {
        if (!needsBeacon) return true;

        // Play beacon deactivate sound if recipe needs Lvl4 beacon but does not have one
        if (!this.getBlockState().getValue(BEACON) && level != null) this.level.playSound(null, this.getBlockPos(), ModSounds.BEACON_FAIL.get(), SoundSource.RECORDS);
        return this.getBlockState().getValue(BEACON);
    }

    private void enableButtons(boolean enablePreviewButton, boolean enableCraftButton) {
        // enable/disable buttons
        PacketDistributor.sendToServer(new MBToggleButtonPacket(this.getBlockPos(), enablePreviewButton ? 1 : 0, enableCraftButton ? 1 : 0, isPlayingPreviewSound ? 1 : 0, this.getBlockState().getValue(BEACON) ? 1 : 0));

        if (this.getBlockState().getValue(READY) != enableCraftButton)
            this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(READY, enableCraftButton), 3);
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

    public void onRemove() {
        manager.stop(previewSound);
        manager.stop(shudderSound);
    }

    // Prevents preview/craft sounds from playing in the main menu
    @Override
    public void onChunkUnloaded() {
        manager.stop(previewSound);
        manager.stop(shudderSound);
        super.onChunkUnloaded();
    }
}