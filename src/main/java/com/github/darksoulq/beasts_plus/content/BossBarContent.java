package com.github.darksoulq.beasts_plus.content;

import com.github.darksoulq.abyssallib.common.serialization.Codec;
import com.github.darksoulq.abyssallib.common.serialization.Codecs;
import com.github.darksoulq.abyssallib.common.serialization.RecordBuilder;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;

public class BossBarContent {
    private final Component title;
    private final Color color;
    private final Overlay overlay;
    private final double radius;

    public BossBarContent(Component title, Color color, Overlay overlay, double radius) {
        this.title = title;
        this.color = color;
        this.overlay = overlay;
        this.radius = radius;
    }

    public Component getTitle() {
        return title;
    }

    public Color getColor() {
        return color;
    }

    public Overlay getOverlay() {
        return overlay;
    }

    public double getRadius() {
        return radius;
    }

    public static final Codec<BossBarContent> CODEC = RecordBuilder.create(instance -> instance.group(
        Codecs.TEXT_COMPONENT.fieldOf("title").forGetter(BossBarContent::getTitle),
        Codec.enumCodec(Color.class).optionalFieldOf("color", Color.RED).forGetter(BossBarContent::getColor),
        Codec.enumCodec(Overlay.class).optionalFieldOf("style", Overlay.PROGRESS).forGetter(BossBarContent::getOverlay),
        Codecs.DOUBLE.optionalFieldOf("radius", 32.0).forGetter(BossBarContent::getRadius)
    ).apply(instance, BossBarContent::new));
}