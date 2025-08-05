package dev.cephelo.musicbox.handler;

import dev.cephelo.musicbox.MusicBoxMod;
import dev.cephelo.musicbox.screens.custom.MusicboxMenu;
import dev.cephelo.musicbox.screens.custom.MusicboxScreen;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MBToggleButtonPacket(BlockPos pos, int pre, int cra, int playing, int beacon) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MBToggleButtonPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MusicBoxMod.MODID, "mb_togglebutton_data"));

    // Each pair of elements defines the stream codec of the element to encode/decode and the getter for the element to encode
    // 'name' will be encoded and decoded as a string
    // 'age' will be encoded and decoded as an integer
    // The final parameter takes in the previous parameters in the order they are provided to construct the payload object
    public static final StreamCodec<ByteBuf, MBToggleButtonPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            MBToggleButtonPacket::pos,
            ByteBufCodecs.VAR_INT,
            MBToggleButtonPacket::pre,
            ByteBufCodecs.VAR_INT,
            MBToggleButtonPacket::cra,
            ByteBufCodecs.VAR_INT,
            MBToggleButtonPacket::playing,
            ByteBufCodecs.VAR_INT,
            MBToggleButtonPacket::beacon,
            MBToggleButtonPacket::new
    );

    public void handle(IPayloadContext context)
    {
        context.enqueueWork(() -> this.handleMainThread(context));
    }

    private void handleMainThread(IPayloadContext context)
    {
        // Thank you to Commoble for this example block
        Player p = context.player();
        if (!(p instanceof ServerPlayer player) || !(player.containerMenu instanceof MusicboxMenu))
        {
            // don't do anything else if menu isn't open (averts possible spam from bad actors)
            return;
        }

        if (((MusicboxMenu) player.containerMenu).blockEntity.getBlockPos().asLong() != pos.asLong()) return;

        try {
            if (Minecraft.getInstance().screen instanceof MusicboxScreen mbs)
                mbs.toggleButtons(pre == 1, cra == 1, playing == 1, beacon == 1);
        } catch (Exception e) {
            MusicBoxMod.LOGGER.error("MBToggleButtonPacket error: ", e);
        }

    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}