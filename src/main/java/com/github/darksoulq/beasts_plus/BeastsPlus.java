package com.github.darksoulq.beasts_plus;

import com.github.darksoulq.abyssallib.server.event.EventBus;
import com.github.darksoulq.beasts_plus.managers.ContentManager;
import com.github.darksoulq.beasts_plus.events.BeastSoundEvents;
import com.github.darksoulq.beasts_plus.events.BeastSpawnEvents;
import com.github.darksoulq.beasts_plus.events.FMMEvents;
import com.github.darksoulq.beasts_plus.managers.AudioManager;
import com.github.darksoulq.beasts_plus.managers.BossBarManager;
import com.github.darksoulq.beasts_plus.registry.BeastsPlusRegistries;
import org.bukkit.plugin.java.JavaPlugin;

public final class BeastsPlus extends JavaPlugin {
    private ContentManager contentManager;

    @Override
    public void onEnable() {
        BeastsPlusRegistries.init();

        BossBarManager bossBarManager = new BossBarManager();
        AudioManager audioManager = new AudioManager();
        contentManager = new ContentManager(bossBarManager, audioManager);

        contentManager.loadContent();

        EventBus eventBus = new EventBus(this);
        eventBus.register(new BeastSpawnEvents(contentManager));
        eventBus.register(new FMMEvents(contentManager));
        eventBus.register(new BeastSoundEvents(audioManager));
        eventBus.register(bossBarManager);
    }

    @Override
    public void onDisable() {
        if (contentManager != null) {
            contentManager.despawnAll();
        }
    }

    public static BeastsPlus getInstance() {
        return JavaPlugin.getPlugin(BeastsPlus.class);
    }

    public ContentManager getContentManager() {
        return contentManager;
    }
}