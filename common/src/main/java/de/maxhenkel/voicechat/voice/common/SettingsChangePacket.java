package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SettingsChangePacket implements Packet<SettingsChangePacket> {

    public enum SettingType {
        DISTANCE((byte) 1, false),
        FADEDISTANCE((byte) 2, false),
        CROUCHMULTIPLIER((byte) 3, false),
        WHISPERMULTIPLIER((byte) 4, false),;

        private final byte id;
        private final boolean hasUUID;

        SettingType(byte id, boolean hasUUID) {
            this.id = id;
            this.hasUUID = hasUUID;
        }

        public byte getId() {
            return id;
        }

        public boolean hasUUID() {
            return hasUUID;
        }

        public static SettingType getById(byte id) {
            for (SettingType setting : values()) {
                if (setting.getId() == id) {
                    return setting;
                }
            }
            return null;
        }
    }

    public static class Setting {
        private final SettingType type;
        private UUID uuid;
        private double value;

        public Setting(SettingType type, @Nullable UUID uuid, double value) {
            this.type = type;
            this.uuid = uuid;
            this.value = value;
        }

        public SettingType getType() {
            return type;
        }

        public UUID getUUID() {
            return uuid;
        }

        public double getValue() {
            return value;
        }

        public boolean getAsBoolean() {
            return value > 0;
        }

        public void setAsBoolean(boolean value) {
            this.value = value ? 1 : 0;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public void setUUID(UUID uuid) {
            this.uuid = uuid;
        }
    }

    private final ArrayList<Setting> settings;

    public SettingsChangePacket(Setting... settings) {
        this.settings = new ArrayList<>(List.of(settings));
    }

    public SettingsChangePacket() {
        this.settings = new ArrayList<>();
    }

    public ArrayList<Setting> getSettings() {
        return settings;
    }

    @Override
    public SettingsChangePacket fromBytes(FriendlyByteBuf buf) {
        SettingsChangePacket soundPacket = new SettingsChangePacket();

        int count = buf.readInt();
        for (int i = 0; i < count; i++) {
            SettingType type = SettingType.getById(buf.readByte());
            UUID uuid = type.hasUUID() ? buf.readUUID() : null;
            soundPacket.settings.add(new Setting(type, uuid, buf.readDouble()));
        }

        return soundPacket;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(settings.size());
        for (Setting setting : settings) {
            buf.writeByte(setting.getType().getId());
            if (setting.getType().hasUUID()) {
                buf.writeUUID(setting.getUUID());
            }
            buf.writeDouble(setting.getValue());
        }
    }
}
