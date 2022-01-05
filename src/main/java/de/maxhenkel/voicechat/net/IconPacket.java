package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class IconPacket implements Packet<IconPacket> {

    public static final ResourceLocation SECRET = new ResourceLocation(Voicechat.MODID, "secret");

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
    public ResourceLocation getID() {
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