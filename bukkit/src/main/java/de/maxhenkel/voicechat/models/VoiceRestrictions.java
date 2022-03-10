package de.maxhenkel.voicechat.models;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VoiceRestrictions {

    private final String permission;
    private final List<UUID> mutedPlayers;
    private final List<Whispers> whispers;
    private boolean allMuted;
    private final ConcurrentHashMap<UUID, Double> speakers;

    public VoiceRestrictions() {
        this.permission = "squidvoice.bypass";
        this.mutedPlayers = Collections.synchronizedList(new ArrayList<>());
        this.whispers = Collections.synchronizedList(new ArrayList<>());
        this.allMuted = false;
        this.speakers = new ConcurrentHashMap<>();
    }

    public boolean checkPlayer(Player player) {
        if (this.mutedPlayers.contains(player.getUniqueId())) return false;
        if (player.hasPermission(this.permission)) return true;
        if (this.allMuted) return false;
        return true;
    }

    public List<UUID> getPlayerWhispers(UUID playerUUID) {
        Whispers w = getPlayerWhisper(playerUUID, false);
        return w != null ? w.getPlayers() : new ArrayList<>(0);
    }

    private Whispers getPlayerWhisper(UUID uuid, boolean createIfNot) {
        for (Whispers whisper : this.whispers) {
            if (whisper.getMainPlayer().equals(uuid)) return whisper;
        }
        if (createIfNot) {
            Whispers whisper = new Whispers(uuid);
            this.whispers.add(whisper);
            return whisper;
        }
        return null;
    }

    private void checkToRemoveWhisper(Whispers w) {
        if (w.getPlayers().isEmpty()) {
            this.whispers.remove(w);
        }
    }

    public void addWhisperer(UUID mainPlayer, UUID whisperPlayer) {
        Whispers whisper = getPlayerWhisper(mainPlayer, true);
        whisper.addPlayer(whisperPlayer);
    }

    public void removeWhisperer(UUID mainPlayer, UUID whisperPlayer) {
        Whispers whisper = getPlayerWhisper(mainPlayer, true);
        whisper.removePlayer(whisperPlayer);

        this.checkToRemoveWhisper(whisper);
    }

    public boolean isWhispering(UUID playerUUID, UUID targetUUID) {
        Whispers whisper = getPlayerWhisper(playerUUID, false);
        return whisper != null && whisper.containsPlayer(targetUUID);
    }

    public List<UUID> getSpeakers() {
        return Collections.synchronizedList(new ArrayList<>(this.speakers.keySet()));
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

    public List<UUID> getMutedPlayers() {
        return new ArrayList<>(this.mutedPlayers);
    }
}
