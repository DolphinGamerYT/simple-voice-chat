package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.debug.CooldownTimer;
import de.maxhenkel.voicechat.voice.common.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AudioChannel extends Thread {

    private final Minecraft minecraft;
    private final Client client;
    private final UUID uuid;
    private final BlockingQueue<SoundPacket> queue;
    private final AudioPacketBuffer packetBuffer;
    private long lastPacketTime;
    private SourceDataLine speaker;
    private FloatControl gainControl;
    private boolean stopped;
    private final OpusDecoder decoder;
    private long lastSequenceNumber;

    public AudioChannel(Client client, UUID uuid) {
        this.client = client;
        this.uuid = uuid;
        this.queue = new LinkedBlockingQueue<>();
        this.packetBuffer = new AudioPacketBuffer(VoicechatClient.CLIENT_CONFIG.audioPacketThreshold.get());
        this.lastPacketTime = System.currentTimeMillis();
        this.stopped = false;
        this.decoder = new OpusDecoder(client.getAudioChannelConfig().getSampleRate(), client.getAudioChannelConfig().getFrameSize(), client.getMtuSize());
        this.lastSequenceNumber = -1L;
        this.minecraft = Minecraft.getInstance();
        setDaemon(true);
        setName("AudioChannelThread-" + uuid.toString());
        Voicechat.LOGGER.info("Creating audio channel for " + uuid);
    }

    public boolean canKill() {
        return System.currentTimeMillis() - lastPacketTime > 30_000L;
    }

    public void closeAndKill() {
        Voicechat.LOGGER.info("Closing audio channel for " + uuid);
        stopped = true;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void addToQueue(SoundPacket<?> p) {
        queue.add(p);
    }

    @Override
    public void run() {
        try {
            AudioFormat af = client.getAudioChannelConfig().getStereoFormat();
            speaker = DataLines.getSpeaker(af);
            speaker.open(af);
            gainControl = (FloatControl) speaker.getControl(FloatControl.Type.MASTER_GAIN);
            while (!stopped) {

                if (VoicechatClient.CLIENT.getPlayerStateManager().isDisabled()) {
                    speaker.stop();
                    queue.clear();
                    closeAndKill();
                    flushRecording();
                    return;
                }

                // Stopping the data line when the buffer is empty
                // to prevent the last sound getting repeated
                if (speaker.isActive() && speaker.getBufferSize() - speaker.available() <= 0) {
                    speaker.stop();
                    lastSequenceNumber = -1L;
                    flushRecording();
                }

                // Flush the speaker if the buffer is too full to avoid too big delays
                if (VoicechatClient.CLIENT_CONFIG.clearFullAudioBuffer.get() && speaker.isActive() && speaker.getBufferSize() - speaker.available() > client.getAudioChannelConfig().maxSpeakerBufferSize()) {
                    CooldownTimer.run("clear_audio_buffer", () -> {
                        Voicechat.LOGGER.warn("Clearing buffers to avoid audio delay");
                    });
                    speaker.stop();
                    speaker.flush();
                    lastSequenceNumber = -1L;
                    flushRecording();
                }

                SoundPacket<?> packet = packetBuffer.poll(queue);
                if (packet == null) {
                    continue;
                }
                lastPacketTime = System.currentTimeMillis();

                if (lastSequenceNumber >= 0 && packet.getSequenceNumber() <= lastSequenceNumber) {
                    continue;
                }

                // Filling the speaker with silence for one packet size
                // to build a small buffer to compensate for network latency
                if (speaker.getBufferSize() - speaker.available() <= 0) {
                    byte[] data = new byte[Math.min(client.getAudioChannelConfig().getFrameSize() * VoicechatClient.CLIENT_CONFIG.outputBufferSize.get(), speaker.getBufferSize() - client.getAudioChannelConfig().getFrameSize())];
                    speaker.write(data, 0, data.length);
                }
                if (minecraft.level == null || minecraft.player == null) {
                    continue;
                }

                client.getTalkCache().updateTalking(uuid);

                if (lastSequenceNumber >= 0) {
                    int packetsToCompensate = (int) (packet.getSequenceNumber() - (lastSequenceNumber + 1));
                    for (int i = 0; i < packetsToCompensate; i++) {
                        if (speaker.available() < client.getAudioChannelConfig().getFrameSize()) {
                            Voicechat.LOGGER.warn("Could not compensate more than " + i + " audio packets");
                            break;
                        }
                        writeToSpeaker(packet, decoder.decode(null));
                    }
                }

                lastSequenceNumber = packet.getSequenceNumber();

                byte[] decodedAudio = decoder.decode(packet.getData());

                writeToSpeaker(packet, decodedAudio);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (speaker != null) {
                speaker.stop();
                speaker.flush();
                speaker.close();
            }
            decoder.close();
            flushRecording();
            Voicechat.LOGGER.info("Closed audio channel for " + uuid);
        }
    }

    private void flushRecording() {
        AudioRecorder recorder = client.getRecorder();
        if (recorder == null) {
            return;
        }
        recorder.writeChunkThreaded(uuid);
    }

    private void writeToSpeaker(Packet<?> packet, byte[] monoData) {
        @Nullable Player player = minecraft.level.getPlayerByUUID(uuid);

        byte[] stereo = new byte[0];

        if (packet instanceof GroupSoundPacket) {
            stereo = Utils.convertToStereo(monoData, 1F, 1F);
        } else if (packet instanceof PlayerSoundPacket) {
            if (player == null) {
                return;
            }
            stereo = convertLocationalPacketToStereo(player.position().add(0D, player.getEyeHeight(), 0D), monoData);
        } else if (packet instanceof LocationSoundPacket) {
            LocationSoundPacket p = (LocationSoundPacket) packet;
            stereo = convertLocationalPacketToStereo(p.getLocation(), monoData);
        } else if (packet instanceof SpeakerSoundPacket) {
            SpeakerSoundPacket p = (SpeakerSoundPacket) packet;
            stereo = Utils.convertToStereo(monoData, 1, 1);
        }

        gainControl.setValue(Math.min(Math.max(Utils.percentageToDB(VoicechatClient.CLIENT_CONFIG.voiceChatVolume.get().floatValue() * (float) VoicechatClient.VOLUME_CONFIG.getVolume(uuid)), gainControl.getMinimum()), gainControl.getMaximum()));
        if (client.getRecorder() != null) {
            try {
                client.getRecorder().appendChunk(player != null ? player.getGameProfile() : null, System.currentTimeMillis(), stereo);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        speaker.write(stereo, 0, stereo.length);
        speaker.start();
    }

    private byte[] convertLocationalPacketToStereo(Vec3 pos, byte[] monoData) {
        float distance = (float) pos.distanceTo(minecraft.player.position());
        float fadeDistance = (float) client.getVoiceChatFadeDistance();
        float maxDistance = (float) client.getVoiceChatDistance();

        float percentage = 1F;
        if (distance > fadeDistance) {
            percentage = 1F - Math.min((distance - fadeDistance) / (maxDistance - fadeDistance), 1F);
        }

        if (VoicechatClient.CLIENT_CONFIG.stereo.get()) {
            Pair<Float, Float> stereoVolume = Utils.getStereoVolume(minecraft, pos, client.getVoiceChatDistance());
            return Utils.convertToStereo(monoData, percentage * stereoVolume.getLeft(), percentage * stereoVolume.getRight());
        } else {
            return Utils.convertToStereo(monoData, percentage, percentage);
        }
    }

    public boolean isClosed() {
        return stopped;
    }

}