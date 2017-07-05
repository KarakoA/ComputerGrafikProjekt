package org.lwjglb.engine.services;

import org.joml.Vector3f;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.system.MemoryStack;
import org.lwjglb.engine.graph.Texture;

import java.net.URISyntaxException;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_filename;

import static org.lwjgl.system.libc.LibCStdlib.free;

/**
 * Audio utility class. Plays ogg files using openAL.
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

    public Playable createPlayable(String fileName) {
        return new Playable(fileName);
    }

    public class Playable implements AutoCloseable {

        private int sourcePointer;
        private int bufferPointer;
        private long durationInMiliSeconds;

        private Playable(String fileName) throws RuntimeException {
            //get the correct absolute fileName
            Path p;
            try {
                p = Paths.get(Texture.class.getResource(fileName).toURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            String path = p.toAbsolutePath().toString();

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
                System.err.println("Warning Audio is in stereo format. Cannot fade with distance.");
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
            durationInMiliSeconds = computeDuration();
        }

        private long computeDuration() {
            int sizeInBytes = alGetBufferi(bufferPointer, AL_SIZE);
            int channels = alGetBufferi(bufferPointer, AL_CHANNELS);
            int bits = alGetBufferi(bufferPointer, AL_BITS);

            int lengthInSamples = sizeInBytes * 8 / (channels * bits);
            int frequency = alGetBufferi(bufferPointer, AL_FREQUENCY);

            float result = (float) lengthInSamples / (float) frequency;

            return (long)(result*1000L);
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
        //0-1 float
        public void setGain(float gain){
            if(gain < 0)
                gain=0;
            if (gain>1)
                gain=1;
            alSourcef(sourcePointer,AL_GAIN,gain);
        }
        public void testVelocityAndGain(){
           // alSourcei(sourcePointer, AL_REFERENCE_DISTANCE, 1.0f);
            // alSourcei(sourcePointer,AL_GAIN,0);
            alSourcei(sourcePointer, AL_MAX_DISTANCE, 10);
        }
    }
}