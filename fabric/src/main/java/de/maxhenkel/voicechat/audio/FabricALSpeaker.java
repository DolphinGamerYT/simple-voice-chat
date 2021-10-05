package de.maxhenkel.voicechat.audio;

import com.sonicether.soundphysics.SoundPhysics;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.voice.client.ALSpeaker;
import de.maxhenkel.voicechat.voice.client.SoundManager;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class FabricALSpeaker extends ALSpeaker {

    private long lastUpdate;
    private Vec3 lastPos;

    public FabricALSpeaker(SoundManager soundManager, int sampleRate, int bufferSize) {
        super(soundManager, sampleRate, bufferSize);
    }

    @Override
    protected void setPositionSync(@Nullable Vec3 soundPos) {
        super.setPositionSync(soundPos);
        if (soundManager instanceof FabricSoundManager manager) {
            if (manager.isSoundPhysicsLoaded()) {
                processSoundPhysics(soundPos);
            }
        }
    }

    private void processSoundPhysics(@Nullable Vec3 soundPos) {
        if (soundPos == null) {
            if (soundManager instanceof FabricSoundManager manager) {
                manager.resetEnvironment(source);
            }
            return;
        }

        long time = System.currentTimeMillis();

        if (time - lastUpdate < 1000 && (lastPos != null && lastPos.distanceTo(soundPos) < 1D)) {
            return;
        }
        Voicechat.logDebug("Updating soundphysics");
        SoundPhysics.setLastSoundCategoryAndName(SoundSource.MASTER, Voicechat.MODID);
        SoundPhysics.onPlaySound(soundPos.x(), soundPos.y(), soundPos.z(), source);

        lastUpdate = time;
        lastPos = soundPos;
    }
}