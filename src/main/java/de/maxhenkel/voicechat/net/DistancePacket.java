package de.maxhenkel.voicechat.net;

import com.comphenix.protocol.wrappers.MinecraftKey;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.util.FriendlyByteBuf;

import java.util.UUID;

public class DistancePacket implements Packet<DistancePacket> {

    public static final MinecraftKey SECRET = new MinecraftKey(Voicechat.MODID, "secret");

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
    public MinecraftKey getID() {
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
