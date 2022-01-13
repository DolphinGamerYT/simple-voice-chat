package de.maxhenkel.voicechat.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import de.maxhenkel.voicechat.Voicechat;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("squidvoice")
@CommandPermission("squidvoice.command")
public class SquidVoiceCommand extends BaseCommand {

    private final String prefix;

    private double fadeDistance;
    private double distance;

    public SquidVoiceCommand() {
        this.prefix = "§0[§5SquidVoice§0] §r";

        this.fadeDistance = Voicechat.SERVER_CONFIG.voiceChatFadeDistance.get();
        this.distance = Voicechat.SERVER_CONFIG.voiceChatDistance.get();
    }

    @Subcommand("help")
    @Default
    @CatchUnknown
    public void onHelp(CommandSender sender) {
        String helpMessage = "&8&l========== &9&lSquidVoice &8&l==========\n" +
                "&7&l/squidvoice distance <distance>\n" +
                "&7&l/squidvoice divisor <divisor>\n" +
                "&7&l/squidvoice globalmute <true/false>\n" +
                "&7&l/squidvoice mute <player>\n" +
                "&7&l/squidvoice unmute <player>\n" +
                "&7&l/squidvoice speaker <true/false>";
        sender.sendMessage(helpMessage);
    }

    @Subcommand("distance")
    @CommandCompletion("@nothing")
    @Syntax("[distance]")
    public void onDistance(CommandSender sender, @Optional Double distance) {
        if (distance == null) {
            sender.sendMessage(this.prefix + "§eDistancia actual: " + this.distance);
            return;
        }

        this.distance = distance;
        this.updateVolumes();
        sender.sendMessage(this.prefix + "§eDistancia actualizada a " + this.distance);
    }

    @Subcommand("fade-distance")
    @CommandCompletion("@nothing")
    @Syntax("[fadeDistance]")
    public void onDivisor(CommandSender sender, @Optional Double fadeDistance) {
        if (fadeDistance == null) {
            sender.sendMessage(this.prefix + "§eFade distance actual: " + this.fadeDistance);
            return;
        }

        this.fadeDistance = fadeDistance;
        this.updateVolumes();
        sender.sendMessage(this.prefix + "§eFade distance actualizado a " + this.fadeDistance);
    }

    private void updateVolumes() {
        Voicechat.SERVER_CONFIG.voiceChatDistance.set(this.distance);
        Voicechat.SERVER_CONFIG.voiceChatDistance.save();
        Voicechat.SERVER_CONFIG.voiceChatFadeDistance.set(this.fadeDistance);
        Voicechat.SERVER_CONFIG.voiceChatFadeDistance.save();

        Bukkit.getOnlinePlayers().forEach(p -> Voicechat.SERVER.sendDistanceConfig(p, this.distance, this.fadeDistance));
    }

    @Subcommand("globalmute")
    @CommandCompletion("@bool")
    @Syntax("<true/false>")
    public void onGlobalmute(CommandSender sender, boolean bool) {
        Voicechat.SERVER.getVoiceRestrictions().setAllMuted(bool);
        sender.sendMessage(this.prefix + "§eSe ha " + (bool ? "§cmuteado" : "§adesmuteado") + " §eel voice chat.");
        Bukkit.getOnlinePlayers().forEach(p -> Voicechat.SERVER.updateIconStatus(p));
    }

    @Subcommand("mute")
    @CommandCompletion("@players")
    @Syntax("<player>")
    public void onMute(CommandSender sender, String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            sender.sendMessage(this.prefix + "§cJugador no encontrado.");
            return;
        }

        Voicechat.SERVER.getVoiceRestrictions().addMutedPlayer(player.getUniqueId());
        sender.sendMessage(this.prefix + "§cJugador " + player.getName() + " muteado.");
        Voicechat.SERVER.updateIconStatus(player);
    }

    @Subcommand("unmute")
    @CommandCompletion("@players")
    @Syntax("<player>")
    public void onUnmute(CommandSender sender, String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            sender.sendMessage(this.prefix + "§cJugador no encontrado.");
            return;
        }

        Voicechat.SERVER.getVoiceRestrictions().removeMutedPlayer(player.getUniqueId());
        sender.sendMessage(this.prefix + "§aJugador " + player.getName() + " desmuteado.");
        Voicechat.SERVER.updateIconStatus(player);
    }

    @Subcommand("speaker")
    @CommandCompletion("@bool @nothing")
    @Syntax("<true/false> [distance]")
    public void onSpeaker(Player player, boolean bool, @Optional Double distance) {
        if (bool) {
            if (distance == null || distance <= 0) {
                Voicechat.SERVER.getVoiceRestrictions().addSpeaker(player.getUniqueId(), -1D);
                player.sendMessage(this.prefix + "§aAhora eres speaker.");
            } else {
                Voicechat.SERVER.updateIconStatus(player);
                player.sendMessage(this.prefix + "§aAhora eres speaker. La distancia es de " + distance + " bloques.");
            }
        } else {
            Voicechat.SERVER.getVoiceRestrictions().removeSpeaker(player.getUniqueId());
            player.sendMessage(this.prefix + "§cYa no eres speaker.");
        }
        Voicechat.SERVER.updateIconStatus(player);
    }

}