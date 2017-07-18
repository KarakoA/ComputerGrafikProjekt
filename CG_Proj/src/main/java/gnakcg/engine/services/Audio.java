package gnakcg.engine.services;

import gnakcg.engine.Utils;
import gnakcg.engine.graph.Transformation;
import org.joml.*;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_filename;

import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_memory;
import static org.lwjgl.system.libc.LibCStdlib.free;
import static gnakcg.game.Game.TERRAIN_SCALE;

/**
 * Audio utility class. Plays ogg files using openAL.
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
    }

    public void setListenerPosition(Vector3f position) {
        alListener3f(AL_POSITION, position.x, position.y, position.z);
    }

    public void setListenerOrientation(Vector3f rotationInDegree, Vector3f position) {
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

    public Playable createPlayable(String fileName) {
        return createPlayable(fileName, false);
    }

    public Playable createPlayable(String fileName, boolean relative) {
        return new Playable(fileName, relative);
    }

    public class Playable implements AutoCloseable {

        private int sourcePointer;
        private int bufferPointer;
        private long durationInMiliSeconds;

        public long getDurationInMiliSeconds() {
            return durationInMiliSeconds;
        }

        private Playable(String fileName, boolean relative) throws RuntimeException {
            //get the correct absolute fileName
            String path =Utils.getResourceAbsolutePath(fileName);

            //Allocate space to store return information from stb_vorbis_decode_filename
            MemoryStack.stackPush();
            IntBuffer channelsBuffer = MemoryStack.stackMallocInt(1);
            MemoryStack.stackPush();
            IntBuffer sampleRateBuffer = MemoryStack.stackMallocInt(1);
            ShortBuffer rawAudioBuffer = stb_vorbis_decode_filename(path, channelsBuffer, sampleRateBuffer);
            if (rawAudioBuffer == null) {
                throw new RuntimeException("failed to create buffer. is the file path correct?");
            }

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
                System.err.println("Warning " + fileName + " is in stereo format. Cannot fade with distance.");
            }

            //Request space for the buffer
            bufferPointer = alGenBuffers();
            //Send the data to OpenAL
            alBufferData(bufferPointer, format, rawAudioBuffer, sampleRate);

            //Free the memory allocated by STB
            free(rawAudioBuffer);

            //Request a source
            sourcePointer = alGenSources();

            //Assign the sound we just loaded to the source
            alSourcei(sourcePointer, AL_BUFFER, bufferPointer);
            if (relative)
                alSourcei(sourcePointer, AL_SOURCE_RELATIVE, AL_TRUE);
            durationInMiliSeconds = computeDuration();
        }

        private long computeDuration() {
            int sizeInBytes = alGetBufferi(bufferPointer, AL_SIZE);
            int channels = alGetBufferi(bufferPointer, AL_CHANNELS);
            int bits = alGetBufferi(bufferPointer, AL_BITS);

            int lengthInSamples = sizeInBytes * 8 / (channels * bits);
            int frequency = alGetBufferi(bufferPointer, AL_FREQUENCY);

            float result = (float) lengthInSamples / (float) frequency;
            return (long) (result * 1000L);
        }

        @Override
        public void close() {
            alDeleteSources(sourcePointer);
            alDeleteBuffers(bufferPointer);

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

        public void playIfNotAlreadyPlaying() {
            int state = (alGetSourcei(sourcePointer, AL_SOURCE_STATE));
            if (state != AL_PLAYING)
                this.play();
        }

        public void setGain(float gain) {
            //clip to 0-1
            gain = Float.max(1, Float.min(0, gain));
            alSourcef(sourcePointer, AL_GAIN, gain);
        }

        public void enableSourceSoundDecrease() {
            alDistanceModel(AL_INVERSE_DISTANCE_CLAMPED);
            //if the distance is smaller than this always maximum sound
            alSourcei(sourcePointer, AL_REFERENCE_DISTANCE, TERRAIN_SCALE);
            alSourcef(sourcePointer, AL_MIN_GAIN, 0.05f);
            //beyond this all sounds the same
            alSourcei(sourcePointer, AL_MAX_DISTANCE, TERRAIN_SCALE * 6);
            //factor * distance
            alSourcei(sourcePointer, AL_ROLLOFF_FACTOR, 5);
        }
    }
}