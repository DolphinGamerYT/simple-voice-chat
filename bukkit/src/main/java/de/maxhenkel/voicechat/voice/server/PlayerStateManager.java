package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.net.PlayerStatePacket;
import de.maxhenkel.voicechat.net.PlayerStatesPacket;
import de.maxhenkel.voicechat.net.UpdateStatePacket;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerStateManager implements Listener {

    private final ConcurrentHashMap<UUID, PlayerState> states;

    public PlayerStateManager() {
        states = new ConcurrentHashMap<>();
    }

    public void onUpdateStatePacket(Player player, UpdateStatePacket packet) {
        PlayerState state = states.get(player.getUniqueId());

        if (state == null) {
            state = defaultDisconnectedState(player);
        }

        state.setDisconnected(packet.isDisconnected());
        state.setDisabled(packet.isDisabled());

        states.put(player.getUniqueId(), state);

        broadcastState(state);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        removePlayer(event.getPlayer());
    }

    private void broadcastState(PlayerState state) {
        PlayerStatePacket packet = new PlayerStatePacket(state);
        Voicechat.INSTANCE.getServer().getOnlinePlayers().forEach(p -> NetManager.sendToClient(p, packet));
    }

    public void onPlayerCompatibilityCheckSucceeded(Player player) {
        PlayerState state = states.getOrDefault(player.getUniqueId(), defaultDisconnectedState(player));
        states.put(player.getUniqueId(), state);
        PlayerStatesPacket packet = new PlayerStatesPacket(states);
        NetManager.sendToClient(player, packet);
    }

    private void removePlayer(Player player) {
        states.remove(player.getUniqueId());
        broadcastState(new PlayerState(player.getUniqueId(), player.getName(), true, true));
    }

    @Nullable
    public PlayerState getState(UUID playerUUID) {
        return states.get(playerUUID);
    }

    public static PlayerState defaultDisconnectedState(Player player) {
        return new PlayerState(player.getUniqueId(), player.getName(), false, true);
    }

    public Collection<PlayerState> getStates() {
        return states.values();
    }

}
