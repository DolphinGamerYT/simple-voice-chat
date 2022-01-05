package de.maxhenkel.voicechat.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import de.maxhenkel.voicechat.Voicechat;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("squidvoice")
@CommandPermission("squidvoice.command")
public class SquidVoiceCommand extends BaseCommand {

    private final String prefix;

    private double divisor;
    private double distance;

    public SquidVoiceCommand() {
        this.prefix = "§8[§9SquidVoice§8] §r";

        this.divisor = Voicechat.SERVER_CONFIG.voiceChatFadeDistance.get();
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
    @Syntax("<distance>")
    public void onDistance(CommandSender sender, Double distance) {
        this.distance = distance;
        this.updateVolumes();
        sender.sendMessage(this.prefix + "§eDistancia actualizada a " + this.distance);
    }

    @Subcommand("divisor")
    @CommandCompletion("@nothing")
    @Syntax("<divisor>")
    public void onDivisor(CommandSender sender, Double divisor) {
        this.divisor = divisor;
        this.updateVolumes();
        sender.sendMessage(this.prefix + "§eDivisor actualizado a " + this.divisor);
    }

    private void updateVolumes() {
        double d = (this.distance / divisor);
        Voicechat.SERVER_CONFIG.voiceChatDistance.set(this.distance);
        Voicechat.SERVER_CONFIG.voiceChatDistance.save();
        Voicechat.SERVER_CONFIG.voiceChatFadeDistance.set(this.distance - d);
        Voicechat.SERVER_CONFIG.voiceChatFadeDistance.save();
    }

    @Subcommand("globalmute")
    @CommandCompletion("@bool")
    @Syntax("<true/false>")
    public void onGlobalmute(CommandSender sender, boolean bool) {
        Voicechat.SERVER.getVoiceRestrictions().setAllMuted(bool);
        sender.sendMessage(this.prefix + "§eSe ha " + (bool ? "§cmuteado" : "§adesmuteado") + " §eel voice chat.");
    }

    @Subcommand("mute")
    @CommandCompletion("@players")
    @Syntax("<player>")
    public void onMute(CommandSender sender, Player player) {
        Voicechat.SERVER.getVoiceRestrictions().addMutedPlayer(player.getUniqueId());
        sender.sendMessage(this.prefix + "§cJugador " + player.getName() + " muteado.");
    }

    @Subcommand("unmute")
    @CommandCompletion("@players")
    @Syntax("<player>")
    public void onUnmute(CommandSender sender, Player player) {
        Voicechat.SERVER.getVoiceRestrictions().removeMutedPlayer(player.getUniqueId());
        sender.sendMessage(this.prefix + "§aJugador " + player.getName() + " desmuteado.");
    }

    @Subcommand("speaker")
    @CommandCompletion("@bool")
    @Syntax("<true/false>")
    public void onSpeaker(Player player, boolean bool) {
        if (bool) {
            Voicechat.SERVER.getVoiceRestrictions().addSpeaker(player.getUniqueId());
            player.sendMessage(this.prefix + "§aAhora eres speaker.");
        } else {
            Voicechat.SERVER.getVoiceRestrictions().removeSpeaker(player.getUniqueId());
            player.sendMessage(this.prefix + "§cYa no eres speaker.");
        }
    }

}