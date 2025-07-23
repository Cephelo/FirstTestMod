package dev.cephelo.musicbox.screens.custom;

import dev.cephelo.musicbox.MusicBoxMod;
import dev.cephelo.musicbox.block.ModBlocks;
import dev.cephelo.musicbox.block.entity.MusicboxBlockEntity;
import dev.cephelo.musicbox.handler.MBClickButtonPacket;
import dev.cephelo.musicbox.screens.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;

public class MusicboxMenu extends AbstractContainerMenu {
    public final MusicboxBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public MusicboxMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(4));
    }

    public MusicboxMenu(int pContainerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.MUSICBOX_MENU.get(), pContainerId);
        this.blockEntity = ((MusicboxBlockEntity) entity);
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        // Create Slots
        this.addSlot(new SlotItemHandler(blockEntity.itemHandler, 0, 34, 24));
        this.addSlot(new SlotItemHandler(blockEntity.itemHandler, 1, 54, 24));
        this.addSlot(new SlotItemHandler(blockEntity.itemHandler, 2, 34, 44));
        this.addSlot(new SlotItemHandler(blockEntity.itemHandler, 3, 54, 44));
        this.addSlot(new SlotItemHandler(blockEntity.itemHandler, 4, 104, 34));

        addDataSlots(data);
    }

    public boolean isCrafting() {
        // data 0 is progress, aka if progress > 0 then crafting
        return data.get(0) > 0;
    }

    public int getScaledArrowProgress() {
        int progress = this.data.get(0);
        int maxProgress = this.data.get(1);
        int arrowPixelSize = 24;

        return (maxProgress != 0 && progress != 0) ? progress * arrowPixelSize / maxProgress : 0;
    }

    public boolean isPlayingPreviewSound() {
        // data 2 is previewProgress
        return data.get(2) > 0;
    }

    public void pressPreviewButton() {
        MusicBoxMod.LOGGER.info("pressPreviewButton");
        PacketDistributor.sendToServer(new MBClickButtonPacket(this.blockEntity.getBlockPos(), 0));
        //blockEntity.previewButton();
    }

    public void pressCraftButton() {
        MusicBoxMod.LOGGER.info("pressCraftButton");
        PacketDistributor.sendToServer(new MBClickButtonPacket(this.blockEntity.getBlockPos(), 1));
        //blockEntity.craftButton();
    }

    // CREDIT GOES TO: diesieben07 | https://github.com/diesieben07/SevenCommons
    // must assign a slot number to each of the slots used by the GUI.
    // For this container, we can see both the tile inventory's slots as well as the player inventory slots and the hotbar.
    // Each time we add a Slot to the container, it automatically increases the slotIndex, which means
    //  0 - 35 = hotbar / player inventory slots (which map to the InventoryPlayer slot numbers 0 - 35)
    //  36 - 40 = TileInventory slots, which map to our TileEntity slot numbers 0 - 4)
    private static final int PLAYER_SLOT_COUNT = 36;
    // THIS YOU HAVE TO DEFINE!
    private static final int TE_INV_SLOT_COUNT = 5;  // must be the number of slots you have!
    @Override
    public ItemStack quickMoveStack(Player playerIn, int pIndex) {
        Slot sourceSlot = slots.get(pIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;  //EMPTY_ITEM
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Check if the slot clicked is one of the vanilla container slots
        if (pIndex < PLAYER_SLOT_COUNT) {
            // This is a vanilla container slot so merge the stack into the tile inventory
            if (!moveItemStackTo(sourceStack, PLAYER_SLOT_COUNT, PLAYER_SLOT_COUNT + TE_INV_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (pIndex < PLAYER_SLOT_COUNT + TE_INV_SLOT_COUNT) {
            // This is a TE slot so merge the stack into the players inventory
            if (!moveItemStackTo(sourceStack, 0, PLAYER_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            System.out.println("Invalid slotIndex:" + pIndex);
            return ItemStack.EMPTY;
        }
        // If stack size == 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                pPlayer, ModBlocks.MUSICBOX.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}
