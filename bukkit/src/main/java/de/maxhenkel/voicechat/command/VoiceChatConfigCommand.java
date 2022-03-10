package de.maxhenkel.voicechat.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.voice.common.SettingsChangePacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("voicechat|vc")
@Subcommand("config")
@CommandPermission("voicechat.config")
public class VoiceChatConfigCommand extends BaseCommand {

    private final Voicechat plugin;

    private final String prefix;

    private double fadeDistance;
    private double distance;
    private double crouchMultiplier;
    private double whisperMultiplier;

    public VoiceChatConfigCommand() {
        plugin = Voicechat.INSTANCE;

        this.prefix = "§0[§3Voicechat§0] §r";

        this.fadeDistance = Voicechat.SERVER_CONFIG.voiceChatFadeDistance.get();
        this.distance = Voicechat.SERVER_CONFIG.voiceChatDistance.get();
        this.crouchMultiplier = Voicechat.SERVER_CONFIG.crouchDistanceMultiplier.get();
        this.whisperMultiplier = Voicechat.SERVER_CONFIG.whisperDistanceMultiplier.get();
    }

    @Subcommand("distance")
    @Syntax("<distance>")
    public void setDistance(Player player, @Optional Double distance) {
        if (distance == null) {
            player.sendMessage(prefix + "§eDistance is set to " + this.distance);
            return;
        }

        if (distance <= 0) {
            player.sendMessage(prefix + "§cDistance must be greater than 0");
            return;
        }

        this.distance = distance;
        Voicechat.SERVER_CONFIG.voiceChatDistance.set(distance);
        Voicechat.SERVER_CONFIG.voiceChatDistance.save();

        this.sendSettingsChange(SettingsChangePacket.SettingType.DISTANCE, distance);
        player.sendMessage(prefix + "Distance set to " + distance);
    }

    @Subcommand("fade-distance")
    @Syntax("<distance>")
    public void setFadeDistance(Player player, @Optional Double distance) {
        if (distance == null) {
            player.sendMessage(prefix + "§eFade distance is set to " + this.fadeDistance);
            return;
        }

        if (distance < 0) {
            player.sendMessage(prefix + "§cDistance must be greater or equal to 0");
            return;
        }

        this.fadeDistance = distance;
        Voicechat.SERVER_CONFIG.voiceChatFadeDistance.set(distance);
        Voicechat.SERVER_CONFIG.voiceChatFadeDistance.save();

        this.sendSettingsChange(SettingsChangePacket.SettingType.FADEDISTANCE, distance);
        player.sendMessage(prefix + "Fade distance set to " + distance);
    }

    @Subcommand("crouch-multiplier")
    @Syntax("<multiplier>")
    public void setCrouchMultiplier(Player player, @Optional Double multiplier) {
        if (multiplier == null) {
            player.sendMessage(prefix + "§eCrouch multiplier is set to " + this.crouchMultiplier);
            return;
        }

        if (multiplier < 0) {
            player.sendMessage(prefix + "§cMultiplier must be greater or equal to 0");
            return;
        }

        this.crouchMultiplier = multiplier;
        Voicechat.SERVER_CONFIG.crouchDistanceMultiplier.set(multiplier);
        Voicechat.SERVER_CONFIG.crouchDistanceMultiplier.save();

        this.sendSettingsChange(SettingsChangePacket.SettingType.CROUCHMULTIPLIER, multiplier);
        player.sendMessage(prefix + "Crouch multiplier set to " + multiplier);
    }

    @Subcommand("whisper-multiplier")
    @Syntax("<multiplier>")
    public void setWhisperMultiplier(Player player, @Optional Double multiplier) {
        if (multiplier == null) {
            player.sendMessage(prefix + "§eWhisper multiplier is set to " + this.whisperMultiplier);
            return;
        }

        if (multiplier < 0) {
            player.sendMessage(prefix + "§cMultiplier must be greater or equal to 0");
            return;
        }

        this.whisperMultiplier = multiplier;
        Voicechat.SERVER_CONFIG.whisperDistanceMultiplier.set(multiplier);
        Voicechat.SERVER_CONFIG.whisperDistanceMultiplier.save();

        this.sendSettingsChange(SettingsChangePacket.SettingType.WHISPERMULTIPLIER, multiplier);
        player.sendMessage(prefix + "Whisper multiplier set to " + multiplier);
    }

    private void sendSettingsChange(SettingsChangePacket.SettingType type, double value) {
        SettingsChangePacket packet = new SettingsChangePacket(new SettingsChangePacket.Setting(type, null, value));

        Bukkit.getOnlinePlayers().forEach(p -> Voicechat.SERVER.sendSettingsChange(p, packet));
    }

}
