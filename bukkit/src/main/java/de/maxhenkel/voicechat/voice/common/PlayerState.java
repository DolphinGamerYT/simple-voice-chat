package de.maxhenkel.voicechat.voice.common;

import de.maxhenkel.voicechat.util.FriendlyByteBuf;

import java.util.UUID;

public class PlayerState {

    private UUID uuid;
    private String name;
    private boolean disabled;
    private boolean disconnected;

    public PlayerState(UUID uuid, String name, boolean disabled, boolean disconnected) {
        this.uuid = uuid;
        this.name = name;
        this.disabled = disabled;
        this.disconnected = disconnected;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isDisconnected() {
        return disconnected;
    }

    public void setDisconnected(boolean disconnected) {
        this.disconnected = disconnected;
    }

    @Override
    public String toString() {
        return "{" +
                "disabled=" + disabled +
                ", disconnected=" + disconnected +
                ", uuid=" + uuid +
                ", name=" + name +
                '}';
    }

    public static PlayerState fromBytes(FriendlyByteBuf buf) {
        boolean disabled = buf.readBoolean();
        boolean disconnected = buf.readBoolean();
        UUID uuid = buf.readUUID();
        String name = buf.readUtf(32767);

        return new PlayerState(uuid, name, disabled, disconnected);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(disabled);
        buf.writeBoolean(disconnected);
        buf.writeUUID(uuid);
        buf.writeUtf(name);
    }

}
