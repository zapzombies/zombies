package io.github.zap.zombies.game.equipment2;

import io.github.zap.zombies.game.equipment2.feature.Feature;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record EquipmentLevel(@NotNull List<Feature> features, int visualFeatureIndex) {

    public EquipmentLevel(@NotNull List<Feature> features, int visualFeatureIndex) {
        if (visualFeatureIndex < features.size()) {
            this.features = features;
            this.visualFeatureIndex = visualFeatureIndex;
        } else throw new IndexOutOfBoundsException("visualFeatureIndex " + visualFeatureIndex + " is not less than " +
                "the feature list size " + features.size());
    }

}
