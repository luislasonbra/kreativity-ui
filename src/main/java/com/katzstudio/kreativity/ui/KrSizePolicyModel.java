package com.katzstudio.kreativity.ui;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Model for a list of size policies.
 */
public class KrSizePolicyModel {

    private final List<KrUnifiedSize> sizePolicies = Lists.newArrayList();

    public KrSizePolicyModel(List<KrUnifiedSize> sizePolicies) {
        this.sizePolicies.addAll(sizePolicies);
    }

    public KrSizePolicyModel(KrUnifiedSize... policies) {
        this.sizePolicies.addAll(Lists.newArrayList(policies));
    }

    public Stream<KrUnifiedSize> stream() {
        return sizePolicies.stream();
    }

    public List<Float> getSizes(float availableSpace) {
        float absoluteRequested = stream()
                .map(KrUnifiedSize::getAbsolute)
                .map(KrSizePolicyModel::nonNegative)
                .reduce(0.0f, (a, b) -> a + b);

        float relativeRequested = stream()
                .map(KrUnifiedSize::getRelative)
                .map(KrSizePolicyModel::nonNegative)
                .reduce(0.0f, Float::sum);

        final float totalSpaceAvailable = (availableSpace < absoluteRequested) ? (relativeRequested > 0 ? absoluteRequested * 2 : absoluteRequested) : availableSpace;
        float totalRelativeAvailable = totalSpaceAvailable - absoluteRequested;
        final float relativeUnit = relativeRequested != 0 ? totalRelativeAvailable / relativeRequested : 0;

        return stream()
                .map(size -> (nonNegative(size.getAbsolute()) + nonNegative(size.getRelative()) * relativeUnit) * availableSpace / totalSpaceAvailable)
                .collect(Collectors.toList());
    }

    private static float nonNegative(float value) {
        return Math.max(value, 0);
    }
}