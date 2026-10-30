package com.github.darksoulq.beasts_plus.managers;

import com.github.darksoulq.abyssallib.server.event.SubscribeEvent;
import com.github.darksoulq.beasts_plus.BeastsPlus;
import com.github.darksoulq.beasts_plus.content.BossBarContent;
import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.bossbar.BossBar;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BossBarManager {
    private final Map<UUID, LivingEntity> activeEntities = new HashMap<>();
    private final Map<UUID, ServerBossEvent> activeBars = new HashMap<>();
    private final Map<UUID, BossBarContent> barConfigs = new HashMap<>();
    private final Map<UUID, Set<UUID>> barViewers = new HashMap<>();

    public BossBarManager() {
        startTicking();
    }

    private BossEvent.BossBarColor mapToBossBarColor(BossBar.Color color) {
        return switch (color) {
            case RED -> BossEvent.BossBarColor.RED;
            case BLUE -> BossEvent.BossBarColor.BLUE;
            case GREEN -> BossEvent.BossBarColor.GREEN;
            case YELLOW -> BossEvent.BossBarColor.YELLOW;
            case PINK, PURPLE -> BossEvent.BossBarColor.PURPLE;
            case WHITE -> BossEvent.BossBarColor.WHITE;
        };
    }

    private BossEvent.BossBarOverlay mapToBossBarOverlay(BossBar.Overlay overlay) {
        return switch (overlay) {
            case PROGRESS -> BossEvent.BossBarOverlay.PROGRESS;
            case NOTCHED_6 -> BossEvent.BossBarOverlay.NOTCHED_6;
            case NOTCHED_10 -> BossEvent.BossBarOverlay.NOTCHED_10;
            case NOTCHED_12 -> BossEvent.BossBarOverlay.NOTCHED_12;
            case NOTCHED_20 -> BossEvent.BossBarOverlay.NOTCHED_20;
        };
    }

    public void registerBar(LivingEntity entity, BossBarContent content) {
        net.minecraft.network.chat.Component vanillaText = PaperAdventure.asVanilla(content.getTitle());
        ServerBossEvent bar = new ServerBossEvent(UUID.randomUUID(), vanillaText, mapToBossBarColor(content.getColor()), mapToBossBarOverlay(content.getOverlay()));
        bar.setProgress(1.0f);

        activeEntities.put(entity.getUniqueId(), entity);
        activeBars.put(entity.getUniqueId(), bar);
        barConfigs.put(entity.getUniqueId(), content);
        barViewers.put(entity.getUniqueId(), new HashSet<>());
    }

    public void unregisterBar(UUID entityId) {
        ServerBossEvent bar = activeBars.remove(entityId);
        barViewers.remove(entityId);

        if (bar != null) {
            bar.removeAllPlayers();
        }

        activeEntities.remove(entityId);
        barConfigs.remove(entityId);
    }

    public void clearAll() {
        for (ServerBossEvent bar : activeBars.values()) {
            bar.removeAllPlayers();
        }
        activeBars.clear();
        activeEntities.clear();
        barConfigs.clear();
        barViewers.clear();
    }

    @SubscribeEvent(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        for (Map.Entry<UUID, ServerBossEvent> entry : activeBars.entrySet()) {
            Set<UUID> viewers = barViewers.get(entry.getKey());
            if (viewers != null && viewers.remove(player.getUniqueId())) {
                entry.getValue().removePlayer(((CraftPlayer) player).getHandle());
            }
        }
    }

    private void startTicking() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Map<UUID, Location> bossLocations = new HashMap<>();
                Map<UUID, Double> bossRadii = new HashMap<>();
                Map<UUID, Double> bossHealthPct = new HashMap<>();

                Iterator<Map.Entry<UUID, LivingEntity>> iterator = activeEntities.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<UUID, LivingEntity> entry = iterator.next();
                    UUID uuid = entry.getKey();
                    LivingEntity entity = entry.getValue();

                    if (entity == null || !entity.isValid() || entity.isDead()) {
                        unregisterBar(uuid);
                        iterator.remove();
                        continue;
                    }

                    bossLocations.put(uuid, entity.getLocation());

                    BossBarContent config = barConfigs.get(uuid);
                    if (config != null) {
                        bossRadii.put(uuid, config.getRadius());
                    }

                    double maxHealth = entity.getAttribute(Attribute.MAX_HEALTH) != null ? entity.getAttribute(Attribute.MAX_HEALTH).getValue() : 1.0;
                    bossHealthPct.put(uuid, entity.getHealth() / maxHealth);
                }

                if (bossLocations.isEmpty()) return;

                Map<UUID, Location> playerLocations = new HashMap<>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    playerLocations.put(p.getUniqueId(), p.getLocation());
                }

                Bukkit.getScheduler().runTaskAsynchronously(BeastsPlus.getInstance(), () -> {
                    Map<UUID, Set<UUID>> calculatedViewers = new HashMap<>();

                    for (Map.Entry<UUID, Location> bossEntry : bossLocations.entrySet()) {
                        UUID bossId = bossEntry.getKey();
                        Location bLoc = bossEntry.getValue();
                        double radius = bossRadii.getOrDefault(bossId, 32.0);
                        double radiusSq = radius * radius;
                        Set<UUID> viewers = new HashSet<>();

                        for (Map.Entry<UUID, Location> playerEntry : playerLocations.entrySet()) {
                            Location pLoc = playerEntry.getValue();
                            if (bLoc.getWorld().equals(pLoc.getWorld()) && bLoc.distanceSquared(pLoc) <= radiusSq) {
                                viewers.add(playerEntry.getKey());
                            }
                        }
                        calculatedViewers.put(bossId, viewers);
                    }

                    Bukkit.getScheduler().runTask(BeastsPlus.getInstance(), () -> {
                        for (Map.Entry<UUID, Set<UUID>> calcEntry : calculatedViewers.entrySet()) {
                            UUID bossId = calcEntry.getKey();
                            ServerBossEvent bar = activeBars.get(bossId);
                            Set<UUID> currentViewers = barViewers.get(bossId);
                            Double healthPct = bossHealthPct.get(bossId);

                            if (bar != null && currentViewers != null && healthPct != null) {
                                bar.setProgress((float) Math.max(0.0, Math.min(1.0, healthPct)));
                                Set<UUID> newViewers = calcEntry.getValue();

                                for (UUID playerUuid : newViewers) {
                                    if (currentViewers.add(playerUuid)) {
                                        Player p = Bukkit.getPlayer(playerUuid);
                                        if (p != null) bar.addPlayer(((CraftPlayer) p).getHandle());
                                    }
                                }

                                Iterator<UUID> viewerIt = currentViewers.iterator();
                                while (viewerIt.hasNext()) {
                                    UUID oldViewer = viewerIt.next();
                                    if (!newViewers.contains(oldViewer)) {
                                        Player p = Bukkit.getPlayer(oldViewer);
                                        if (p != null) bar.removePlayer(((CraftPlayer) p).getHandle());
                                        viewerIt.remove();
                                    }
                                }
                            }
                        }
                    });
                });
            }
        }.runTaskTimer(BeastsPlus.getInstance(), 10L, 10L);
    }
}