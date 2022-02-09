package de.maxhenkel.voicechat.voice.common;

import de.maxhenkel.voicechat.util.FriendlyByteBuf;

public class DistanceChangePacket implements Packet<DistanceChangePacket> {

    private double voiceChatDistance;
    private double voiceChatFadeDistance;

    public DistanceChangePacket(double voiceChatDistance, double voiceChatFadeDistance) {
        this.voiceChatDistance = voiceChatDistance;
        this.voiceChatFadeDistance = voiceChatFadeDistance;
    }

    public DistanceChangePacket() {

    }

    public double getVoiceChatDistance() {
        return voiceChatDistance;
    }

    public double getVoiceChatFadeDistance() {
        return voiceChatFadeDistance;
    }

    @Override
    public DistanceChangePacket fromBytes(FriendlyByteBuf buf) {
        DistanceChangePacket soundPacket = new DistanceChangePacket();
        soundPacket.voiceChatDistance = buf.readDouble();
        soundPacket.voiceChatFadeDistance = buf.readDouble();
        return soundPacket;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeDouble(this.voiceChatDistance);
        buf.writeDouble(this.voiceChatFadeDistance);
    }
}
