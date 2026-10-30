package com.github.darksoulq.beasts_plus.content;

import com.github.darksoulq.abyssallib.common.serialization.Codec;
import com.github.darksoulq.abyssallib.common.serialization.Codecs;
import com.github.darksoulq.abyssallib.common.serialization.RecordBuilder;

public class ModelContent {
    private final String modelId;

    public ModelContent(String modelId) {
        this.modelId = modelId;
    }

    public String getModelId() {
        return modelId;
    }

    public static final Codec<ModelContent> CODEC = RecordBuilder.create(instance -> instance.group(
        Codecs.STRING.fieldOf("id").forGetter(ModelContent::getModelId)
    ).apply(instance, ModelContent::new));
}