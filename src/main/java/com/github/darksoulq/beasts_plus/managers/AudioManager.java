package com.github.darksoulq.beasts_plus.managers;

import com.github.darksoulq.beasts_plus.BeastsPlus;
import com.github.darksoulq.beasts_plus.content.SoundContent;
import com.github.darksoulq.beasts_plus.content.SoundEntry;
import com.github.darksoulq.beasts_plus.registry.BeastsPlusRegistries;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class AudioManager {
    private final Map<UUID, LivingEntity> activeEntities = new HashMap<>();
    private final Map<UUID, Location> lastLocations = new HashMap<>();
    private final Map<UUID, String> entityToBeastId = new HashMap<>();

    private final Map<UUID, Long> idleCooldowns = new HashMap<>();
    private final Map<UUID, Long> stepCooldowns = new HashMap<>();

    public AudioManager() {
        startTicking();
    }

    public void registerEntity(LivingEntity entity, String beastId) {
        activeEntities.put(entity.getUniqueId(), entity);
        entityToBeastId.put(entity.getUniqueId(), beastId);
    }

    public void unregisterEntity(UUID entityId) {
        activeEntities.remove(entityId);
        lastLocations.remove(entityId);
        entityToBeastId.remove(entityId);
        idleCooldowns.remove(entityId);
        stepCooldowns.remove(entityId);
    }

    public void clearAll() {
        activeEntities.clear();
        lastLocations.clear();
        entityToBeastId.clear();
        idleCooldowns.clear();
        stepCooldowns.clear();
    }

    public void playSound(LivingEntity entity, String beastId, String soundKey) {
        SoundContent sounds = BeastsPlusRegistries.SOUND_CONTENT.get(beastId);
        if (sounds != null) {
            SoundEntry entry = sounds.getSound(soundKey);
            if (entry != null) {
                entity.getWorld().playSound(entity.getLocation(), entry.sound(), entry.volume(), entry.pitch());
            }
        }
    }

    private void startTicking() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                Iterator<Map.Entry<UUID, LivingEntity>> iterator = activeEntities.entrySet().iterator();

                while (iterator.hasNext()) {
                    Map.Entry<UUID, LivingEntity> entry = iterator.next();
                    UUID uuid = entry.getKey();
                    LivingEntity entity = entry.getValue();

                    if (entity == null || !entity.isValid() || entity.isDead()) {
                        lastLocations.remove(uuid);
                        entityToBeastId.remove(uuid);
                        idleCooldowns.remove(uuid);
                        stepCooldowns.remove(uuid);
                        iterator.remove();
                        continue;
                    }

                    String beastId = entityToBeastId.get(uuid);
                    if (beastId == null) continue;

                    long lastIdle = idleCooldowns.getOrDefault(uuid, 0L);
                    if (now - lastIdle > 4000L && ThreadLocalRandom.current().nextDouble() < 0.02) {
                        playSound(entity, beastId, "idle");
                        idleCooldowns.put(uuid, now);
                    }

                    Location current = entity.getLocation();
                    Location last = lastLocations.get(uuid);

                    if (last != null && last.getWorld().equals(current.getWorld())) {
                        double dist = last.distanceSquared(current);
                        long lastStep = stepCooldowns.getOrDefault(uuid, 0L);

                        if (dist > 0.6 && now - lastStep > 400L) {
                            playSound(entity, beastId, "step");
                            lastLocations.put(uuid, current);
                            stepCooldowns.put(uuid, now);
                        } else if (dist > 0.6) {
                            lastLocations.put(uuid, current);
                        }
                    } else if (last == null || !last.getWorld().equals(current.getWorld())) {
                        lastLocations.put(uuid, current);
                    }
                }
            }
        }.runTaskTimer(JavaPlugin.getPlugin(BeastsPlus.class), 20L, 5L);
    }
}