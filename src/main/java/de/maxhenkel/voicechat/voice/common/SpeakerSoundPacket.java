package de.maxhenkel.voicechat.voice.common;

import de.maxhenkel.voicechat.util.FriendlyByteBuf;
import org.bukkit.Location;

import java.util.UUID;

public class SpeakerSoundPacket extends SoundPacket<SpeakerSoundPacket> {

    protected Location location;

    public SpeakerSoundPacket(UUID sender, Location location, byte[] data, long sequenceNumber) {
        super(sender, data, sequenceNumber);
        this.location = location;
    }

    public SpeakerSoundPacket() {

    }

    public Location getLocation() {
        return location;
    }

    @Override
    public SpeakerSoundPacket fromBytes(FriendlyByteBuf buf) {
        SpeakerSoundPacket soundPacket = new SpeakerSoundPacket();
        soundPacket.sender = buf.readUUID();
        soundPacket.location = new Location(null, buf.readDouble(), buf.readDouble(), buf.readDouble());
        soundPacket.data = buf.readByteArray();
        soundPacket.sequenceNumber = buf.readLong();
        return soundPacket;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(sender);
        buf.writeDouble(location.getX());
        buf.writeDouble(location.getY());
        buf.writeDouble(location.getZ());
        buf.writeByteArray(data);
        buf.writeLong(sequenceNumber);
    }
}
