package de.maxhenkel.voicechat.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Whispers {

    private final UUID mainPlayer;
    private final List<UUID> players;

    public Whispers(UUID mainPlayer) {
        this.mainPlayer = mainPlayer;
        this.players = new ArrayList<>();
    }

    public UUID getMainPlayer() {
        return mainPlayer;
    }

    public List<UUID> getPlayers() {
        return players;
    }

    public void addPlayer(UUID player) {
        players.add(player);
    }

    public void removePlayer(UUID player) {
        players.remove(player);
    }

    public boolean containsPlayer(UUID player) {
        return players.contains(player);
    }

    public boolean containsPlayer(String player) {
        return players.contains(UUID.fromString(player));
    }

}
