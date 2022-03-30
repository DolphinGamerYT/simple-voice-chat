package de.maxhenkel.voicechat.voice.common;

import de.maxhenkel.voicechat.util.FriendlyByteBuf;

import java.util.UUID;

public class SpeakerSoundPacket extends SoundPacket<SpeakerSoundPacket> {

    public SpeakerSoundPacket(UUID sender, byte[] data, long sequenceNumber) {
        super(sender, data, sequenceNumber);
    }

    public SpeakerSoundPacket() {

    }

    @Override
    public SpeakerSoundPacket fromBytes(FriendlyByteBuf buf) {
        SpeakerSoundPacket soundPacket = new SpeakerSoundPacket();
        soundPacket.sender = buf.readUUID();
        soundPacket.data = buf.readByteArray();
        soundPacket.sequenceNumber = buf.readLong();
        return soundPacket;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(sender);
        buf.writeByteArray(data);
        buf.writeLong(sequenceNumber);
    }
}
