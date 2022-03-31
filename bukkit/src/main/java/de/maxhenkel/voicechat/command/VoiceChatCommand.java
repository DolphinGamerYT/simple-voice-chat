package de.maxhenkel.voicechat.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import de.maxhenkel.voicechat.Voicechat;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

@CommandAlias("voicechat|vc")
@CommandPermission("voicechat.command")
public class VoiceChatCommand extends BaseCommand {

    private final String prefix;
    private final String helpCommand;

    public VoiceChatCommand() {
        this.prefix = "§0[§3Voicechat§0] §r";
        this.helpCommand = "help";
    }

    @Default
    @Subcommand("help")
    @CatchUnknown
    public void onHelp(CommandSender sender) {
        sender.sendMessage(this.helpCommand);
    }

    @Subcommand("speaker")
    public void speaker(Player player) {
        player.sendMessage(this.prefix + "§aSpeaker mode: " + (Voicechat.VOICE_RESTRICTIONS.isSpeaker(player) ? "§aEnabled" : "§cDisabled") + (Voicechat.VOICE_RESTRICTIONS.getSpeakerDistance(player) > 0 ? "§7 (Distance: " + Voicechat.VOICE_RESTRICTIONS.getSpeakerDistance(player) + ")" : ""));
        player.sendMessage(this.prefix + "§fTo enable speaker mode, use §e/vc speaker on§f.");
        player.sendMessage(this.prefix + "§fTo disable speaker mode, use §e/vc speaker off§f.");
        player.sendMessage(this.prefix + "§fTo set the distance, use §e/vc speaker distance <distance>§f.");
        player.sendMessage(this.prefix + "§6[NOTE] &eYou can add a playername on the end to modify the speaker status of that player.");
    }

    @Subcommand("speaker on")
    @Syntax("[playerName]")
    @CommandCompletion("@players")
    public void speakerOn(Player player, @Optional String playerName) {
        Player target = player;
        if (playerName != null) {
            Player pp = Bukkit.getPlayer(playerName);
            if (pp == null) {
                player.sendMessage(this.prefix + "§cPlayer not found.");
            } else {
                target = pp;
            }
        }

        Voicechat.VOICE_RESTRICTIONS.addSpeaker(target.getUniqueId(), -1D);

        target.sendMessage(this.prefix + "§aSpeaker mode enabled.");
        if (target != player) {
            player.sendMessage(this.prefix + "§aSpeaker mode enabled for " + target.getName());
        }

        this.updateIcon(target);
    }

    @Subcommand("speaker off")
    @Syntax("[playerName]")
    @CommandCompletion("@players")
    public void speakerOff(Player player, @Optional String playerName) {
        Player target = player;
        if (playerName != null) {
            Player pp = Bukkit.getPlayer(playerName);
            if (pp == null) {
                player.sendMessage(this.prefix + "§cPlayer not found.");
            } else {
                target = pp;
            }
        }

        if (Voicechat.VOICE_RESTRICTIONS.isSpeaker(target)) {
            Voicechat.VOICE_RESTRICTIONS.removeSpeaker(player.getUniqueId());
            target.sendMessage(this.prefix + "§aSpeaker mode disabled.");
            if (target != player) {
                player.sendMessage(this.prefix + "§aSpeaker mode disabled for §e" + target.getName() + "§a.");
            }

        } else {
            if (target == player) {
                player.sendMessage(this.prefix + "§cSpeaker mode is already disabled.");
            } else {
                player.sendMessage(this.prefix + "§cSpeaker mode is already disabled for " + target.getName() + ".");
            }
        }

        this.updateIcon(target);
    }

    @Subcommand("speaker distance")
    @Syntax("<distance> [playerName]")
    @CommandCompletion("NumberOfDistance @players")
    public void speakerDistance(Player player, Double distance, @Optional String playerName) {
        if (distance == null || distance <= 0) {
            player.sendMessage(this.prefix + "§cInvalid distance. It must be greater than 0.");
            return;
        }

        Player target = player;
        if (playerName != null) {
            Player pp = Bukkit.getPlayer(playerName);
            if (pp == null) {
                player.sendMessage(this.prefix + "§cPlayer not found.");
            } else {
                target = pp;
            }
        }

        Voicechat.VOICE_RESTRICTIONS.addSpeaker(target.getUniqueId(), distance);

        target.sendMessage(this.prefix + "§aSpeaker mode enabled." + "§7 (Distance: " + distance + ")");
        if (target != player) {
            player.sendMessage(this.prefix + "§aSpeaker mode enabled for " + target.getName() + "§7 (Distance: " + distance + ")");
        }

        this.updateIcon(target);
    }

    @Subcommand("mute")
    @CommandCompletion("@players")
    @Syntax("<playerName>")
    public void mute(Player player, String playerName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            player.sendMessage(this.prefix + "§cPlayer not found.");
            return;
        }

        if (Voicechat.VOICE_RESTRICTIONS.isPlayerMuted(target.getUniqueId())) {
            player.sendMessage(this.prefix + "§cPlayer §e" + target.getName() + "§c is already muted.");
            return;
        }

        Voicechat.VOICE_RESTRICTIONS.addMutedPlayer(target.getUniqueId());
        player.sendMessage(this.prefix + "§aMuted §e" + target.getName() + "§a.");

        this.updateIcon(target);
    }

    @Subcommand("unmute")
    @CommandCompletion("@players")
    @Syntax("<playerName>")
    public void unmute(Player player, String playerName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            player.sendMessage(this.prefix + "§cPlayer not found.");
            return;
        }

        if (!Voicechat.VOICE_RESTRICTIONS.isPlayerMuted(target.getUniqueId())) {
            player.sendMessage(this.prefix + "§cPlayer §e" + target.getName() + "§c is already unmuted.");
            return;
        }

        Voicechat.VOICE_RESTRICTIONS.removeMutedPlayer(target.getUniqueId());
        player.sendMessage(this.prefix + "§aUnmuted §e" + target.getName() + "§a.");

        this.updateIcon(target);
    }

    @Subcommand("globalmute")
    @CommandCompletion("on|off")
    @Syntax("<on|off>")
    public void globalmute(Player player, String onOrOff) {
        if (onOrOff.equalsIgnoreCase("off")) {
            if (!Voicechat.VOICE_RESTRICTIONS.isAllMuted()) {
                player.sendMessage(this.prefix + "§cGlobal mute is already disabled.");
                return;
            }

            Voicechat.VOICE_RESTRICTIONS.setAllMuted(false);
            player.sendMessage(this.prefix + "§aGlobal mute disabled.");
            return;
        }

        if (!onOrOff.equalsIgnoreCase("on")) {
            player.sendMessage(this.prefix + "§cInvalid argument.");
            return;
        }

        if (Voicechat.VOICE_RESTRICTIONS.isAllMuted()) {
            player.sendMessage(this.prefix + "§cGlobal mute is already enabled.");
            return;
        }

        Voicechat.VOICE_RESTRICTIONS.setAllMuted(true);
        player.sendMessage(this.prefix + "§aGlobal mute enabled.");

        Bukkit.getOnlinePlayers().forEach(this::updateIcon);
    }

    @Subcommand("whisper")
    @CommandCompletion("@players")
    @Syntax("<playerName>")
    public void whisper(Player player, String playerName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            player.sendMessage(this.prefix + "§cPlayer not found.");
            return;
        }

        if (Voicechat.VOICE_RESTRICTIONS.isWhispering(player.getUniqueId(), target.getUniqueId())) {
            player.sendMessage(this.prefix + "§cYou're already whispering §e" + target.getName() + "§c.");
            return;
        }

        Voicechat.VOICE_RESTRICTIONS.addWhisperer(player.getUniqueId(), target.getUniqueId());
        player.sendMessage(this.prefix + "§aYou're now whispering §e" + target.getName() + "§a.");
    }

    @Subcommand("unwhisper")
    @CommandCompletion("@players")
    @Syntax("<playerName>")
    public void unwhisper(Player player, String playerName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            player.sendMessage(this.prefix + "§cPlayer not found.");
            return;
        }

        if (!Voicechat.VOICE_RESTRICTIONS.isWhispering(player.getUniqueId(), target.getUniqueId())) {
            player.sendMessage(this.prefix + "§cYou're already not whispering §e" + target.getName() + "§c.");
            return;
        }

        Voicechat.VOICE_RESTRICTIONS.removeWhisperer(player.getUniqueId(), target.getUniqueId());
        player.sendMessage(this.prefix + "§aYou're no longer whispering §e" + target.getName() + "§a.");
    }

    @Subcommand("list")
    public void list(Player player) {
        this.whisperlist(player);
        this.mutedlist(player);
        this.speakerlist(player);
    }

    @Subcommand("list whisper")
    public void onWhisperlist(Player player) {
        this.whisperlist(player);
    }

    @Subcommand("list muted")
    public void onMutedlist(Player player) {
        this.mutedlist(player);
    }

    @Subcommand("list speakers")
    public void onSpeakerlist(Player player) {
        this.speakerlist(player);
    }

    private void whisperlist(Player player) {
        List<Player> players = Voicechat.VOICE_RESTRICTIONS.getPlayerWhispers(player.getUniqueId()).stream().map(Bukkit::getPlayer).toList();

        if (players.isEmpty()) {
            player.sendMessage(this.prefix + "§cYou're not whispering anyone.");
        } else {
            player.sendMessage(this.prefix + "§aYou're whispering: §e" + players.stream().map(Player::getName).collect(Collectors.joining(", ")));
        }
    }

    private void mutedlist(Player player) {
        List<Player> players = Voicechat.VOICE_RESTRICTIONS.getMutedPlayers().stream().map(Bukkit::getPlayer).toList();

        if (players.isEmpty()) {
            player.sendMessage(this.prefix + "§cThere are no muted players.");
        } else {
            player.sendMessage(this.prefix + "§aMuted players: §e" + players.stream().map(Player::getName).collect(Collectors.joining(", ")));
        }
    }

    private void speakerlist(Player player) {
        List<Player> players = Voicechat.VOICE_RESTRICTIONS.getSpeakers().stream().map(Bukkit::getPlayer).toList();

        if (players.isEmpty()) {
            player.sendMessage(this.prefix + "§cThere are no speakers.");
        } else {
            player.sendMessage(this.prefix + "§aSpeakers: §e" + players.stream().map(Player::getName).collect(Collectors.joining(", ")));
        }
    }

    private void updateIcon(Player player) {
        Voicechat.SERVER.updateIconStatus(player);
    }
}