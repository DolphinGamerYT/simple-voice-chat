package de.maxhenkel.voicechat.models;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class VoiceRestrictions {

    private final String permission;
    private final List<UUID> mutedPlayers;
    private boolean allMuted;
    private final List<UUID> speakers;

    public VoiceRestrictions() {
        this.permission = "squidvoice.bypass";
        this.mutedPlayers = Collections.synchronizedList(new ArrayList<>());
        this.allMuted = false;
        this.speakers = Collections.synchronizedList(new ArrayList<>());
    }

    public boolean checkPlayer(Player player) {
        if (this.mutedPlayers.contains(player.getUniqueId())) return false;
        if (player.hasPermission(this.permission)) return true;
        if (this.allMuted) return false;
        return true;
    }

    public boolean isSpeaker(Player player) {
        return this.speakers.contains(player.getUniqueId());
    }

    public String getPermission() {
        return permission;
    }

    public boolean isAllMuted() {
        return allMuted;
    }

    public void setAllMuted(boolean allMuted) {
        this.allMuted = allMuted;
    }

    public void addMutedPlayer(UUID uuid) {
        this.mutedPlayers.add(uuid);
    }

    public void removeMutedPlayer(UUID uuid) {
        this.mutedPlayers.remove(uuid);
    }

    public boolean isPlayerMuted(UUID uuid) {
        return this.mutedPlayers.contains(uuid);
    }

    public void addSpeaker(UUID uuid) {
        this.speakers.add(uuid);
    }

    public void removeSpeaker(UUID uuid) {
        this.speakers.remove(uuid);
    }
}
