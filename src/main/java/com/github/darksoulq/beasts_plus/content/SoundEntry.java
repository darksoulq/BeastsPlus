package com.github.darksoulq.beasts_plus.content;

import com.github.darksoulq.abyssallib.common.serialization.Codec;
import com.github.darksoulq.abyssallib.common.serialization.Codecs;
import com.github.darksoulq.abyssallib.common.serialization.RecordBuilder;

public record SoundEntry(String sound, float volume, float pitch) {
    public static final Codec<SoundEntry> CODEC = RecordBuilder.create(instance -> instance.group(
        Codecs.STRING.fieldOf("sound").forGetter(SoundEntry::sound),
        Codecs.FLOAT.optionalFieldOf("volume", 1.0f).forGetter(SoundEntry::volume),
        Codecs.FLOAT.optionalFieldOf("pitch", 1.0f).forGetter(SoundEntry::pitch)
    ).apply(instance, SoundEntry::new));
}