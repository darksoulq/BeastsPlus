package com.github.darksoulq.beasts_plus.registry;

import com.github.darksoulq.abyssallib.server.registry.Registry;
import com.github.darksoulq.beasts_plus.content.BeastContent;
import com.github.darksoulq.beasts_plus.content.BossBarContent;
import com.github.darksoulq.beasts_plus.content.SoundContent;

public class BeastsPlusRegistries {
    public static final Registry<BeastContent> BEAST_CONTENT = new Registry<>();
    public static final Registry<SoundContent> SOUND_CONTENT = new Registry<>();
    public static final Registry<BossBarContent> BOSS_BAR_CONTENT = new Registry<>();

    public static void init() {
    }
}