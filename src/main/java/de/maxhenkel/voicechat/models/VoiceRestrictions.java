package de.maxhenkel.voicechat.models;

import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class VoiceRestrictions {

    private final String permission;
    private final List<UUID> mutedPlayers;
    private boolean allMuted;
    private final ConcurrentHashMap<UUID, Double> speakers;

    public VoiceRestrictions() {
        this.permission = "squidvoice.bypass";
        this.mutedPlayers = Collections.synchronizedList(new ArrayList<>());
        this.allMuted = false;
        this.speakers = new ConcurrentHashMap<>();
    }

    public boolean checkPlayer(Player player) {
        if (this.mutedPlayers.contains(player.getUniqueId())) return false;
        if (player.hasPermission(this.permission)) return true;
        if (this.allMuted) return false;
        return true;
    }

    public boolean isSpeaker(Player player) {
        return this.speakers.containsKey(player.getUniqueId());
    }

    public Double getSpeakerDistance(Player player) {
        return this.speakers.get(player.getUniqueId());
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

    public void addSpeaker(UUID uuid, Double distance) {
        this.speakers.put(uuid, distance != null ? distance : -1);
    }

    public void removeSpeaker(UUID uuid) {
        this.speakers.remove(uuid);
    }
}
