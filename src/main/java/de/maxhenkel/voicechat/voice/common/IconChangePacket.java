package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.FriendlyByteBuf;


public class IconChangePacket implements Packet<IconChangePacket> {

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

    public IconChangePacket(IconStatus iconStatus) {
        this.iconStatus = iconStatus;
    }

    public IconChangePacket() {

    }

    public IconStatus getIconStatus() {
        return iconStatus;
    }

    @Override
    public IconChangePacket fromBytes(FriendlyByteBuf buf) {
        IconChangePacket iconPacket = new IconChangePacket();
        iconPacket.iconStatus = IconStatus.fromId(buf.readInt());
        return iconPacket;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.iconStatus.getId());
    }
}
