package de.maxhenkel.voicechat.voice.common;

import de.maxhenkel.voicechat.util.FriendlyByteBuf;

import java.util.UUID;

public class GroupSoundPacket extends SoundPacket<GroupSoundPacket> {

    public GroupSoundPacket(UUID sender, byte[] data, long sequenceNumber) {
        super(sender, data, sequenceNumber);
    }

    public GroupSoundPacket() {

    }

    @Override
    public GroupSoundPacket fromBytes(FriendlyByteBuf buf) {
        GroupSoundPacket soundPacket = new GroupSoundPacket();
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
