package com.github.darksoulq.beasts_plus.events;

import com.MT.xxxtrigger50xxx.Beasts.Beast;
import com.github.darksoulq.abyssallib.server.event.SubscribeEvent;
import com.github.darksoulq.beasts_plus.managers.ContentManager;
import com.magmaguy.freeminecraftmodels.api.FmmReloadedEvent;
import com.magmaguy.freeminecraftmodels.api.ModeledEntityHitByProjectileEvent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.MetadataValue;

public class FMMEvents {
    private final ContentManager contentManager;

    public FMMEvents(ContentManager contentManager) {
        this.contentManager = contentManager;
    }

    @SubscribeEvent
    public void onFmmReload(FmmReloadedEvent event) {
        contentManager.despawnAll();

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getLivingEntities()) {
                if (entity.hasMetadata("Beast Tag")) {
                    for (MetadataValue value : entity.getMetadata("Beast Tag")) {
                        Beast beast = Beast.getBeastByID(value.asInt());
                        if (beast != null && beast.getType() != null) {
                            contentManager.applyModel((LivingEntity) entity, beast.getType().getInternalName());
                            break;
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onFmmProjectileHit(ModeledEntityHitByProjectileEvent event) {
        Entity underlying = event.getEntity().getUnderlyingEntity();
        if (underlying == null || !underlying.hasMetadata("Beast Tag")) return;

        Bukkit.getPluginManager().callEvent(new ProjectileHitEvent(event.getProjectile(), underlying, null, null));

        if (event.getEntity().getSkeleton() != null) {
            event.getEntity().getSkeleton().tint();
        }
    }

    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof LivingEntity damager)) return;
        if (!damager.hasMetadata("Beast Tag")) return;

        for (MetadataValue value : damager.getMetadata("Beast Tag")) {
            Beast beast = Beast.getBeastByID(value.asInt());
            if (beast != null && beast.getType() != null) {
                contentManager.playAbilityAnimation(damager, beast.getType().getInternalName(), "attack");
                break;
            }
        }
    }
}