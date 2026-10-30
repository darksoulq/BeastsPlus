package com.github.darksoulq.beasts_plus.events;

import com.MT.xxxtrigger50xxx.Beasts.Beast;
import com.github.darksoulq.abyssallib.server.event.SubscribeEvent;
import com.github.darksoulq.beasts_plus.managers.AudioManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.MetadataValue;

public class BeastSoundEvents {
    private final AudioManager audioManager;

    public BeastSoundEvents(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (!entity.hasMetadata("Beast Tag")) return;

        for (MetadataValue value : entity.getMetadata("Beast Tag")) {
            Beast beast = Beast.getBeastByID(value.asInt());
            if (beast != null && beast.getType() != null) {
                audioManager.playSound(entity, beast.getType().getInternalName(), "hurt");
                break;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!event.getEntity().hasMetadata("Beast Tag")) return;

        for (MetadataValue value : event.getEntity().getMetadata("Beast Tag")) {
            Beast beast = Beast.getBeastByID(value.asInt());
            if (beast != null && beast.getType() != null) {
                audioManager.playSound(event.getEntity(), beast.getType().getInternalName(), "death");
                break;
            }
        }
    }
}