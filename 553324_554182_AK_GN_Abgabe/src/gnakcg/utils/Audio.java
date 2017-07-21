package gnakcg.utils;

import gnakcg.engine.graph.Transformation;
import org.joml.*;
import org.joml.Math;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.system.MemoryStack;

import java.nio.Buffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.LinkedList;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_filename;

import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_memory;
import static org.lwjgl.system.libc.LibCStdlib.free;
import static gnakcg.game.Game.TERRAIN_SCALE;

/**
 * Audio utility class. Plays ogg files using openAL.
 *
 * @author Anton K.
 * @author Gires N.
 */
public class Audio {

    private static final Audio sInstance = new Audio();

    public static Audio getInstance() {
        return sInstance;
    }

    private Audio() {
    }

    private LinkedList<Playable> playables = new LinkedList<>();
    private long contextPointer;
    private long devicePointer;

    public void init() throws RuntimeException {
        String defaultDeviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
        devicePointer = alcOpenDevice(defaultDeviceName);

        int[] attributes = {0};
        contextPointer = alcCreateContext(devicePointer, attributes);
        alcMakeContextCurrent(contextPointer);

        ALCCapabilities alcCapabilities = ALC.createCapabilities(devicePointer);
        ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities);

        if (!alCapabilities.OpenAL10) {
            throw new RuntimeException("OpenAL10 not supported.");
        }
    }

    public void cleanup() {
        alcDestroyContext(contextPointer);
        alcCloseDevice(devicePointer);
        playables.forEach(Playable::close);
    }

    public void setListenerPosition(Vector3f position) {
        alListener3f(AL_POSITION, position.x, position.y, position.z);
    }

    public void setListenerOrientation(Vector2f rotationInDegree, Vector3f position) {
        Matrix4f cameraMatrix = new Matrix4f();
        Transformation.updateGenericViewMatrix(position, rotationInDegree, cameraMatrix);

        Vector3f at = new Vector3f();
        cameraMatrix.positiveZ(at).negate();
        Vector3f up = new Vector3f();
        cameraMatrix.positiveY(up);

        alListenerfv(AL_ORIENTATION, new float[]{at.x, at.y, at.z, up.x, up.y, up.z});
        float[] out = new float[6];
        alGetListenerfv(AL_ORIENTATION, out);

        int error = alGetError();
        if (error == AL_INVALID_VALUE || error == AL_INVALID_ENUM || error == AL_INVALID_OPERATION) {
            throw new RuntimeException("Error");
        }
    }


    public Playable createPlayable(String fileName, boolean relative) {
        Playable playable =new Playable(fileName, relative);
        playables.add(playable);
        return playable;
    }

    public Playable createPlayable(String fileName) {
        return createPlayable(fileName, false);
    }

    public Playable createPlayable(Buffer buffer, boolean relative) {
        return createPlayable(buffer, relative);
    }

    public class Playable implements AutoCloseable {

        private final float FADE_STEP = 0.01f;
        private int sourcePointer;
        private AudioBuffer buffer;

        public long getDurationInMiliSeconds() {
            return buffer.durationInMiliSeconds;
        }

        private Playable(AudioBuffer buffer, boolean relative) {
            this.buffer = buffer;
            //Request a source
            sourcePointer = alGenSources();

            //Assign the buffer to the source
            alSourcei(sourcePointer, AL_BUFFER, buffer.bufferPointer);
            if (relative)
                alSourcei(sourcePointer, AL_SOURCE_RELATIVE, AL_TRUE);

        }

        private Playable(String fileName, boolean relative) throws RuntimeException {
            this(new AudioBuffer(fileName), relative);
        }

        @Override
        public void close() {
            alDeleteSources(sourcePointer);
            buffer.close();

        }

        public void setPosition(Vector3f position) {
            alSource3f(sourcePointer, AL_POSITION, position.x, position.y, position.z);
        }

        public void play() {
            alSourcePlay(sourcePointer);
        }

        public void pause() {
            alSourcePause(sourcePointer);
        }

        public void stop() {
            alSourceStop(sourcePointer);
        }

        public void fade() {
            float currentGain = getGain();
            float newGain = Math.max(currentGain - FADE_STEP, 0);
            if (newGain == 0)
                stop();
            else {
                alSourcef(sourcePointer, AL_MIN_GAIN, 0);
                setGain(newGain);
            }
        }

        public void playIfNotAlreadyPlaying() {
            int state = (alGetSourcei(sourcePointer, AL_SOURCE_STATE));
            if (state != AL_PLAYING)
                this.play();
        }

        public void setGain(float gain) {
            //clip
            gain = Float.min(1, Float.max(0, gain));
            alSourcef(sourcePointer, AL_GAIN, gain);
        }

        public float getGain() {
            return alGetSourcef(sourcePointer, AL_GAIN);
        }

        public void enableSourceSoundDecrease() {
            alDistanceModel(AL_INVERSE_DISTANCE_CLAMPED);
            //if the distance is smaller than this always maximum sound
            alSourcei(sourcePointer, AL_REFERENCE_DISTANCE, TERRAIN_SCALE);
            alSourcef(sourcePointer, AL_MIN_GAIN, 0.1f);
            //beyond this all sounds the same
            alSourcei(sourcePointer, AL_MAX_DISTANCE, TERRAIN_SCALE * 6);
            //factor * distance
            alSourcei(sourcePointer, AL_ROLLOFF_FACTOR, 5);
        }
    }

    public class AudioBuffer implements AutoCloseable {
        public final int bufferPointer;
        public final long durationInMiliSeconds;

        private long computeDuration() {
            int sizeInBytes = alGetBufferi(bufferPointer, AL_SIZE);
            int channels = alGetBufferi(bufferPointer, AL_CHANNELS);
            int bits = alGetBufferi(bufferPointer, AL_BITS);

            int lengthInSamples = sizeInBytes * 8 / (channels * bits);
            int frequency = alGetBufferi(bufferPointer, AL_FREQUENCY);

            float result = (float) lengthInSamples / (float) frequency;
            return (long) (result * 1000L);
        }

        private AudioBuffer(String fileName) {
            //get the correct absolute fileName
            String path = ResourceLoader.getInstance().getResourcePath(fileName);
            //Allocate space to store return information from stb_vorbis_decode_filename
            MemoryStack.stackPush();

            IntBuffer channelsBuffer = MemoryStack.stackMallocInt(1);
            MemoryStack.stackPush();
            IntBuffer sampleRateBuffer = MemoryStack.stackMallocInt(1);
            ShortBuffer rawAudioBuffer = stb_vorbis_decode_filename(path, channelsBuffer, sampleRateBuffer);
            if (rawAudioBuffer == null)
                throw new RuntimeException("failed to create buffer. is the file path correct?" + path);

            //Retrieve the extra information that was stored in the buffers by the function
            int channels = channelsBuffer.get();
            int sampleRate = sampleRateBuffer.get();

            //Free the space we allocated earlier
            MemoryStack.stackPop();
            MemoryStack.stackPop();

            //Find the correct format
            int format = -1;
            if (channels == 1) {
                format = AL_FORMAT_MONO16;
            } else if (channels == 2) {
                format = AL_FORMAT_STEREO16;
                System.err.println("NOTE " + fileName + " is in stereo format and cannot fade with distance." +
                        " This is important only for the background music.");
            }

            //Request space for the buffer
            bufferPointer = alGenBuffers();
            //Send the data to OpenAL
            alBufferData(bufferPointer, format, rawAudioBuffer, sampleRate);

            //Free the memory allocated by STB
            free(rawAudioBuffer);
            durationInMiliSeconds = computeDuration();
        }

        @Override
        public void close() {
            alDeleteBuffers(bufferPointer);
        }
    }

}