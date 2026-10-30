package com.github.darksoulq.beasts_plus.events;

import com.MT.xxxtrigger50xxx.Beasts.Beast;
import com.github.darksoulq.abyssallib.server.event.SubscribeEvent;
import com.github.darksoulq.beasts_plus.BeastsPlus;
import com.github.darksoulq.beasts_plus.managers.ContentManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayDeque;
import java.util.Queue;

public class BeastSpawnEvents {
    private final ContentManager contentManager;
    private final Queue<LivingEntity> pendingSpawns = new ArrayDeque<>();

    public BeastSpawnEvents(ContentManager contentManager) {
        this.contentManager = contentManager;
        startSpawnProcessor();
    }

    private void startSpawnProcessor() {
        new BukkitRunnable() {
            @Override
            public void run() {
                int size = pendingSpawns.size();
                for (int i = 0; i < size; i++) {
                    LivingEntity entity = pendingSpawns.poll();
                    if (entity == null || !entity.isValid() || entity.isDead()) continue;

                    if (entity.hasMetadata("Beast Tag")) {
                        for (MetadataValue value : entity.getMetadata("Beast Tag")) {
                            Beast beast = Beast.getBeastByID(value.asInt());
                            if (beast != null && beast.getType() != null) {
                                contentManager.applyModel(entity, beast.getType().getInternalName());
                                break;
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(BeastsPlus.getInstance(), 1L, 1L);
    }

    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof LivingEntity entity) {
            pendingSpawns.add(entity);
        }
    }

    @SubscribeEvent(priority = EventPriority.MONITOR)
    public void onEntityRemove(EntityRemoveEvent event) {
        contentManager.removeModel(event.getEntity().getUniqueId());
    }
}