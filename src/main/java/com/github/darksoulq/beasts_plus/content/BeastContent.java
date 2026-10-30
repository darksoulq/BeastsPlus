package com.github.darksoulq.beasts_plus.content;

import com.github.darksoulq.abyssallib.common.serialization.Codec;
import com.github.darksoulq.abyssallib.common.serialization.Codecs;
import com.github.darksoulq.abyssallib.common.serialization.RecordBuilder;

public class BeastContent {
    private final String beastId;
    private final ModelContent model;
    private final SoundContent sounds;
    private final BossBarContent bossBar;

    public BeastContent(String beastId, ModelContent model, SoundContent sounds, BossBarContent bossBar) {
        this.beastId = beastId;
        this.model = model;
        this.sounds = sounds;
        this.bossBar = bossBar;
    }

    public String getBeastId() {
        return beastId;
    }

    public ModelContent getModel() {
        return model;
    }

    public SoundContent getSounds() {
        return sounds;
    }

    public BossBarContent getBossBar() {
        return bossBar;
    }

    public static final Codec<BeastContent> CODEC = RecordBuilder.create(instance -> instance.group(
        Codecs.STRING.fieldOf("beast_id").forGetter(BeastContent::getBeastId),
        ModelContent.CODEC.optionalFieldOf("model", null).forGetter(BeastContent::getModel),
        SoundContent.CODEC.optionalFieldOf("sounds", null).forGetter(BeastContent::getSounds),
        BossBarContent.CODEC.optionalFieldOf("boss_bar", null).forGetter(BeastContent::getBossBar)
    ).apply(instance, BeastContent::new));
}