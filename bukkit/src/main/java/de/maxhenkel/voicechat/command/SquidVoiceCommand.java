package de.maxhenkel.voicechat.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.Subcommand;
import de.maxhenkel.voicechat.Voicechat;
import org.bukkit.command.CommandSender;

public class SquidVoiceCommand extends BaseCommand {

    private double divisor;
    private double distance;

    public SquidVoiceCommand() {
        this.divisor = 2.0;
        this.distance = Voicechat.SERVER_CONFIG.voiceChatDistance.get();
    }

    @Subcommand("distance")
    public void onDistance(CommandSender sender, Double distance) {
        this.distance = distance;
        this.updateVolumes();
    }

    @Subcommand("divisor")
    public void onDivisor(CommandSender sender, Double divisor) {
        this.divisor = divisor;
        this.updateVolumes();
    }

    private void updateVolumes() {
        double d = (this.distance / divisor);
        Voicechat.SERVER_CONFIG.voiceChatDistance.set(d);
        Voicechat.SERVER_CONFIG.voiceChatFadeDistance.set(this.distance -d);
    }

}