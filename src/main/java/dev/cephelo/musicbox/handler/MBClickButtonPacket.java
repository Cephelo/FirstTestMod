package dev.cephelo.musicbox.handler;

import dev.cephelo.musicbox.MusicBoxMod;
import dev.cephelo.musicbox.block.entity.MusicboxBlockEntity;
import dev.cephelo.musicbox.screens.custom.MusicboxMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MBClickButtonPacket(BlockPos pos, int id) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MBClickButtonPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MusicBoxMod.MODID, "mb_clickbutton_data"));

    // Each pair of elements defines the stream codec of the element to encode/decode and the getter for the element to encode
    // 'name' will be encoded and decoded as a string
    // 'age' will be encoded and decoded as an integer
    // The final parameter takes in the previous parameters in the order they are provided to construct the payload object
    public static final StreamCodec<ByteBuf, MBClickButtonPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            MBClickButtonPacket::pos,
            ByteBufCodecs.VAR_INT,
            MBClickButtonPacket::id,
            MBClickButtonPacket::new
    );

    public void handle(IPayloadContext context)
    {
        context.enqueueWork(() -> this.handleMainThread(context));
    }

    private void handleMainThread(IPayloadContext context)
    {
        // Thank you to Commoble for this example block
        Player p = context.player();
        if (!(p instanceof ServerPlayer player) || !(player.containerMenu instanceof MusicboxMenu menu))
        {
            // don't do anything else if menu isn't open (averts possible spam from bad actors)
            return;
        }

        ServerLevel level = player.serverLevel();
        try {
            ((MusicboxBlockEntity)level.getBlockEntity(pos)).handleButtonPress(id());
        } catch (Exception e) {
            MusicBoxMod.LOGGER.info("naw shit broke");
        }

    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}