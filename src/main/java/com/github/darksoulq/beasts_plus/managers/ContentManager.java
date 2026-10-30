package com.github.darksoulq.beasts_plus.managers;

import com.github.darksoulq.abyssallib.common.serialization.DataResult;
import com.github.darksoulq.abyssallib.common.serialization.ops.YamlOps;
import com.github.darksoulq.beasts_plus.BeastsPlus;
import com.github.darksoulq.beasts_plus.content.BeastContent;
import com.github.darksoulq.beasts_plus.content.BossBarContent;
import com.github.darksoulq.beasts_plus.registry.BeastsPlusRegistries;
import com.magmaguy.freeminecraftmodels.customentity.DynamicEntity;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.LivingEntity;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ContentManager {
    private final Map<UUID, DynamicEntity> activeModels = new HashMap<>();
    private final Map<UUID, String> entityToId = new HashMap<>();
    private final Map<String, String> animationNameCache = new HashMap<>();

    private final BossBarManager bossBarManager;
    private final AudioManager audioManager;

    public ContentManager(BossBarManager bossBarManager, AudioManager audioManager) {
        this.bossBarManager = bossBarManager;
        this.audioManager = audioManager;
    }

    public void loadContent() {
        BeastsPlusRegistries.BEAST_CONTENT.clear();
        BeastsPlusRegistries.SOUND_CONTENT.clear();
        BeastsPlusRegistries.BOSS_BAR_CONTENT.clear();
        animationNameCache.clear();

        File contentFolder = new File(BeastsPlus.getInstance().getDataFolder(), "content");
        if (!contentFolder.exists()) {
            contentFolder.mkdirs();
        }

        loadYamlRecursive(contentFolder);
    }

    private void loadYamlRecursive(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                loadYamlRecursive(file);
            } else if (file.getName().endsWith(".yml")) {
                try {
                    String yamlText = Files.readString(file.toPath());
                    Object parsedYaml = YamlOps.INSTANCE.parse(yamlText);

                    DataResult<BeastContent> result = BeastContent.CODEC.decode(YamlOps.INSTANCE, parsedYaml);
                    if (result.isSuccess()) {
                        BeastContent content = result.getOrThrow();
                        BeastsPlusRegistries.BEAST_CONTENT.register(content.getBeastId(), content);

                        if (content.getSounds() != null) {
                            BeastsPlusRegistries.SOUND_CONTENT.register(content.getBeastId(), content.getSounds());
                        }
                        if (content.getBossBar() != null) {
                            BeastsPlusRegistries.BOSS_BAR_CONTENT.register(content.getBeastId(), content.getBossBar());
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    public void applyModel(LivingEntity entity, String beastId) {
        BeastContent content = BeastsPlusRegistries.BEAST_CONTENT.get(beastId);
        entityToId.put(entity.getUniqueId(), beastId);

        if (BeastsPlusRegistries.SOUND_CONTENT.contains(beastId)) {
            entity.setSilent(true);
            audioManager.registerEntity(entity, beastId);
        }

        if (BeastsPlusRegistries.BOSS_BAR_CONTENT.contains(beastId)) {
            BossBarContent barContent = BeastsPlusRegistries.BOSS_BAR_CONTENT.get(beastId);
            bossBarManager.registerBar(entity, barContent);
        }

        if (content != null && content.getModel() != null) {
            DynamicEntity dynamicEntity = DynamicEntity.create(content.getModel().getModelId(), entity);
            if (dynamicEntity != null) {
                String displayName = entity.customName() != null ? PlainTextComponentSerializer.plainText().serialize(entity.customName()) : entity.getName();
                dynamicEntity.setDamagesOnContact(false);
                dynamicEntity.setDisplayName(displayName);
                dynamicEntity.setDisplayNameVisible(entity.isCustomNameVisible());
                activeModels.put(entity.getUniqueId(), dynamicEntity);
            }
        }
    }

    public void playAbilityAnimation(LivingEntity entity, String beastId, String abilityName) {
        DynamicEntity dynamicEntity = activeModels.get(entity.getUniqueId());
        if (dynamicEntity != null) {
            String snakeCaseAbility = animationNameCache.computeIfAbsent(abilityName, k -> k.toLowerCase().replace(" ", "_"));
            if (dynamicEntity.hasAnimation(snakeCaseAbility)) {
                dynamicEntity.playAnimation(snakeCaseAbility, false, false);
            }
        }
    }

    public void removeModel(UUID entityId) {
        DynamicEntity dynamicEntity = activeModels.remove(entityId);
        if (dynamicEntity != null) {
            dynamicEntity.remove();
        }
        bossBarManager.unregisterBar(entityId);
        audioManager.unregisterEntity(entityId);
        entityToId.remove(entityId);
    }

    public String getBeastId(UUID uuid) {
        return entityToId.get(uuid);
    }

    public DynamicEntity getModel(UUID entityId) {
        return activeModels.get(entityId);
    }

    public Iterable<UUID> getActiveEntities() {
        return entityToId.keySet();
    }

    public void despawnAll() {
        for (DynamicEntity dynamicEntity : activeModels.values()) {
            dynamicEntity.remove();
        }
        activeModels.clear();
        entityToId.clear();
        bossBarManager.clearAll();
        audioManager.clearAll();
    }
}