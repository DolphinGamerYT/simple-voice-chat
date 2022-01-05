package de.maxhenkel.voicechat.net;

import com.comphenix.protocol.wrappers.MinecraftKey;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.util.FriendlyByteBuf;

import java.util.UUID;

public class IconPacket implements Packet<IconPacket> {

    public static final MinecraftKey SECRET = new MinecraftKey(Voicechat.MODID, "secret");

    public enum IconStatus {
        NORMAL(0),
        SPEAKER(1),
        MUTED(2),
        GLOBALMUTED(3),
        SPECTATOR(4);

        private int id;

        IconStatus(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static IconStatus fromId(int id) {
            for (IconStatus status : values()) {
                if (status.getId() == id) {
                    return status;
                }
            }
            return NORMAL;
        }
    }

    private IconStatus iconStatus;

    public IconPacket() {

    }

    public IconPacket(IconStatus iconStatus) {
        this.iconStatus = iconStatus;
    }

    public IconStatus getIconStatus() {
        return iconStatus;
    }

    @Override
    public MinecraftKey getID() {
        return SECRET;
    }

    @Override
    public IconPacket fromBytes(FriendlyByteBuf buf) {
        this.iconStatus = IconStatus.fromId(buf.readInt());
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.iconStatus.getId());
    }

}
