package com.ldtteam.multipiston.network;

import com.ldtteam.multipiston.MultiPiston;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import static com.ldtteam.multipiston.MultiPiston.MOD_ID;

/**
 * Our wrapper for Forge network layer
 */
public class NetworkChannel
{
    private static final String LATEST_PROTO_VER = "1.0";
    private static final String        ACCEPTED_PROTO_VERS = LATEST_PROTO_VER;
    /**
     * Forge network channel
     */

    private final        SimpleChannel rawChannel;

    /**
     * Creates a new instance of network channel.
     *
     * @param channelName unique channel name
     * @throws IllegalArgumentException if channelName already exists
     */
    public NetworkChannel(final String channelName)
    {
        rawChannel = NetworkRegistry.newSimpleChannel(new ResourceLocation(MOD_ID, channelName), () -> LATEST_PROTO_VER, ACCEPTED_PROTO_VERS::equals, ACCEPTED_PROTO_VERS::equals);
    }

    /**
     * Registers all common messages.
     */
    public void registerCommonMessages()
    {
        int idx = 0;
        registerMessage(++idx, MultiPistonChangeMessage.class);
    }

    /**
     * Register a message into rawChannel.
     *
     * @param <MSG>    message class type
     * @param id       network id
     * @param msgClazz message class
     */
    private <MSG extends IMessage> void registerMessage(final int id, final Class<MSG> msgClazz)
    {
        rawChannel.registerMessage(id, msgClazz, (msg, buf) -> msg.toBytes(buf), (buf) ->
        {
            try
            {
                final MSG msg = msgClazz.newInstance();
                msg.fromBytes(buf);
                return msg;
            } catch (final InstantiationException | IllegalAccessException e)
            {
                e.printStackTrace();
            }
            return null;
        }, (msg, ctxIn) ->
        {
            final NetworkEvent.Context ctx = ctxIn.get();
            final LogicalSide packetOrigin = ctx.getDirection().getOriginationSide();
            ctx.setPacketHandled(true);
            if (msg.getExecutionSide() != null && packetOrigin.equals(msg.getExecutionSide()))
            {
                MultiPiston.LOGGER.warn("Receving {} at wrong side!", msg.getClass().getName());
                return;
            }
            // boolean param MUST equals true if packet arrived at logical server
            ctx.enqueueWork(() -> msg.onExecute(ctx, packetOrigin.equals(LogicalSide.CLIENT)));
        });
    }

    /**
     * Sends to everyone in circle made using given target point.
     *
     * @param msg message to send
     * @param pos target position and radius
     * @see PacketDistributor.TargetPoint
     */
    public void sendToPosition(final IMessage msg, final PacketDistributor.TargetPoint pos)
    {
        rawChannel.send(PacketDistributor.NEAR.with(() -> pos), msg);
    }

    /**
     * Sends to server.
     *
     * @param msg message to send
     */
    public void sendToServer(final IMessage msg)
    {
        rawChannel.sendToServer(msg);
    }
}
