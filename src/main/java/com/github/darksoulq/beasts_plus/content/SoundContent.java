package com.github.darksoulq.beasts_plus.content;

import com.github.darksoulq.abyssallib.common.serialization.Codec;
import com.github.darksoulq.abyssallib.common.serialization.Codecs;

import java.util.Map;

public class SoundContent {
    private final Map<String, SoundEntry> sounds;

    public SoundContent(Map<String, SoundEntry> sounds) {
        this.sounds = sounds;
    }

    public Map<String, SoundEntry> getSounds() {
        return sounds;
    }

    public SoundEntry getSound(String key) {
        return sounds.get(key);
    }

    public static final Codec<SoundContent> CODEC = Codec.map(Codecs.STRING, SoundEntry.CODEC)
        .xmap(SoundContent::new, SoundContent::getSounds);
}