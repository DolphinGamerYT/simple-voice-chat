package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.config.ServerConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class DistancePacket implements Packet<DistancePacket> {

    public static final ResourceLocation SECRET = new ResourceLocation(Voicechat.MODID, "secret");

    private double voiceChatDistance;
    private double voiceChatFadeDistance;

    public DistancePacket() {

    }

    public DistancePacket(double voiceChatDistance, double voiceChatFadeDistance) {
        this.voiceChatDistance = voiceChatDistance;
        this.voiceChatFadeDistance = voiceChatFadeDistance;
    }

    public DistancePacket(UUID secret, boolean hasGroupPermission, ServerConfig serverConfig) {
        this.voiceChatDistance = serverConfig.voiceChatDistance.get();
        this.voiceChatFadeDistance = serverConfig.voiceChatFadeDistance.get();
    }

    public double getVoiceChatDistance() {
        return voiceChatDistance;
    }

    public double getVoiceChatFadeDistance() {
        return voiceChatFadeDistance;
    }

    @Override
    public ResourceLocation getID() {
        return SECRET;
    }

    @Override
    public DistancePacket fromBytes(FriendlyByteBuf buf) {
        voiceChatDistance = buf.readDouble();
        voiceChatFadeDistance = buf.readDouble();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeDouble(voiceChatDistance);
        buf.writeDouble(voiceChatFadeDistance);
    }

}

