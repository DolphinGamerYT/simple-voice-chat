package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.command.VoiceChatCommands;
import de.maxhenkel.voicechat.models.VoiceRestrictions;
import de.maxhenkel.voicechat.net.DistancePacket;
import de.maxhenkel.voicechat.net.IconPacket;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.net.SecretPacket;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class ServerVoiceEvents implements Listener {

    private Server server;

    // Dolphln SquidGame Added Vars
    private VoiceRestrictions voiceRestrictions;

    public ServerVoiceEvents(org.bukkit.Server mcServer) {
        this.voiceRestrictions = new VoiceRestrictions();
        server = new Server(Voicechat.SERVER_CONFIG.voiceChatPort.get(), mcServer, voiceRestrictions);
        server.start();
    }

    public void initializePlayerConnection(Player player) {
        if (server == null) {
            return;
        }

        if (!player.hasPermission(VoiceChatCommands.CONNECT_PERMISSION)) {
            Voicechat.LOGGER.info("Player {} has no permission to connect to the voice chat", player.getName());
            return;
        }

        UUID secret = server.getSecret(player.getUniqueId());

        boolean hasGroupPermission = player.hasPermission(VoiceChatCommands.GROUPS_PERMISSION);

        NetManager.sendToClient(player, new SecretPacket(secret, hasGroupPermission, Voicechat.SERVER_CONFIG));
        Voicechat.LOGGER.info("Sent secret to {}", player.getName());
    }

    public void sendDistanceConfig(Player player, double distance, double fadeDistance) {
        NetManager.sendToClient(player, new DistancePacket(distance, fadeDistance));
    }

    public void updateIconStatus(Player player) {
        IconPacket.IconStatus status = IconPacket.IconStatus.NORMAL;

        if (this.voiceRestrictions.isSpeaker(player)) {
            status = IconPacket.IconStatus.SPEAKER;
        } else if (!player.hasPermission("squidvoice.bypass")) {
            if (this.voiceRestrictions.isAllMuted()) {
                status = IconPacket.IconStatus.GLOBALMUTED;
            } else if (this.voiceRestrictions.isPlayerMuted(player.getUniqueId())) {
                status = IconPacket.IconStatus.MUTED;
            } else if (player.getGameMode().equals(GameMode.SPECTATOR)) {
                status = IconPacket.IconStatus.SPECTATOR;
            }
        }

        NetManager.sendToClient(player, new IconPacket(status));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerLoggedOut(event.getPlayer());
    }

    public void playerLoggedOut(Player player) {
        if (server == null) {
            return;
        }

        server.disconnectClient(player.getUniqueId());
        Voicechat.LOGGER.info("Disconnecting client " + player.getName());
    }

    public Server getServer() {
        return server;
    }

    public VoiceRestrictions getVoiceRestrictions() {
        return voiceRestrictions;
    }
}
