package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.*;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;

public class MicTestButton extends AbstractButton {

    private static final Component TEST_UNAVAILABLE = new TranslatableComponent("message.voicechat.mic_test_unavailable");
    private static final Component TEST_ON = new TranslatableComponent("message.voicechat.mic_test_on");
    private static final Component TEST_OFF = new TranslatableComponent("message.voicechat.mic_test_off");

    private boolean micActive;
    @Nullable
    private VoiceThread voiceThread;
    private final MicListener micListener;
    @Nullable
    private final ClientVoicechat client;

    public MicTestButton(int xIn, int yIn, int widthIn, int heightIn, MicListener micListener) {
        super(xIn, yIn, widthIn, heightIn, TextComponent.EMPTY);
        this.micListener = micListener;
        this.client = ClientManager.getClient();
        active = client == null || client.getSoundManager() != null;
        updateText();
    }

    private void updateText() {
        if (!active) {
            setMessage(TEST_UNAVAILABLE);
            return;
        }
        if (micActive) {
            setMessage(TEST_ON);
        } else {
            setMessage(TEST_OFF);
        }
    }

    @Override
    public void render(PoseStack matrixStack, int x, int y, float partialTicks) {
        super.render(matrixStack, x, y, partialTicks);
        if (voiceThread != null) {
            voiceThread.updateLastRender();
        }
    }

    public void setMicActive(boolean micActive) {
        this.micActive = micActive;
        updateText();
    }

    @Override
    public void onPress() {
        setMicActive(!micActive);
        if (micActive) {
            if (voiceThread != null) {
                voiceThread.close();
                voiceThread = null;
            }
            try {
                voiceThread = new VoiceThread();
                voiceThread.start();
            } catch (Exception e) {
                setMicActive(false);
                active = false;
                e.printStackTrace();
            }
        } else {
            if (voiceThread != null) {
                voiceThread.close();
                voiceThread = null;
            }
        }
        updateText();
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    private class VoiceThread extends Thread {

        private final ALSpeaker speaker;
        private boolean running;
        private long lastRender;
        private MicThread micThread;
        private boolean usesOwnMicThread;
        @Nullable
        private SoundManager ownSoundManager;

        public VoiceThread() throws SpeakerException, MicrophoneException, NativeDependencyException {
            this.running = true;
            setDaemon(true);
            setName("VoiceTestingThread");

            micThread = client != null ? client.getMicThread() : null;
            if (micThread == null) {
                micThread = new MicThread(client, null);
                usesOwnMicThread = true;
            }

            SoundManager soundManager;
            if (client == null) {
                soundManager = new SoundManager(VoicechatClient.CLIENT_CONFIG.speaker.get());
                ownSoundManager = soundManager;
            } else {
                soundManager = client.getSoundManager();
            }

            if (soundManager == null) {
                throw new SpeakerException("No sound manager");
            }

            speaker = new ALSpeaker(soundManager, SoundManager.SAMPLE_RATE, SoundManager.FRAME_SIZE);

            speaker.open();

            updateLastRender();
            setMicLocked(true);
        }

        @Override
        public void run() {
            while (running) {
                if (System.currentTimeMillis() - lastRender > 500L) {
                    break;
                }
                short[] buff = micThread.pollMic();
                if (buff == null) {
                    continue;
                }

                micListener.onMicValue(Utils.dbToPerc(Utils.getHighestAudioLevel(buff)));

                speaker.write(buff, VoicechatClient.CLIENT_CONFIG.voiceChatVolume.get().floatValue(), null);
            }
            speaker.close();
            setMicLocked(false);
            micListener.onMicValue(0D);
            if (usesOwnMicThread) {
                micThread.close();
            }
            if (ownSoundManager != null) {
                ownSoundManager.close();
            }
            Voicechat.LOGGER.info("Mic test audio channel closed");
        }

        public void updateLastRender() {
            lastRender = System.currentTimeMillis();
        }

        private void setMicLocked(boolean locked) {
            micThread.setMicrophoneLocked(locked);
        }

        public void close() {
            if (!running) {
                return;
            }
            Voicechat.LOGGER.info("Stopping mic test audio channel");
            running = false;
            try {
                join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public interface MicListener {
        void onMicValue(double percentage);
    }
}
