package com.katzstudio.kreativity.ui;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static com.katzstudio.kreativity.ui.KrAlignment.Horizontal.CENTER;
import static com.katzstudio.kreativity.ui.KrAlignment.Horizontal.LEFT;
import static com.katzstudio.kreativity.ui.KrAlignment.Horizontal.RIGHT;
import static com.katzstudio.kreativity.ui.KrAlignment.Vertical.BOTTOM;
import static com.katzstudio.kreativity.ui.KrAlignment.Vertical.MIDDLE;
import static com.katzstudio.kreativity.ui.KrAlignment.Vertical.TOP;

/**
 * Alignment anchors
 */
@RequiredArgsConstructor
public enum KrAlignment {

    TOP_LEFT(TOP, LEFT),

    TOP_CENTER(TOP, CENTER),

    TOP_RIGHT(TOP, RIGHT),

    MIDDLE_LEFT(MIDDLE, LEFT),

    MIDDLE_CENTER(MIDDLE, CENTER),

    MIDDLE_RIGHT(MIDDLE, RIGHT),

    BOTTOM_LEFT(BOTTOM, LEFT),

    BOTTOM_CENTER(BOTTOM, CENTER),

    BOTTOM_RIGHT(BOTTOM, RIGHT);

    public enum Vertical {
        TOP, MIDDLE, BOTTOM
    }

    public enum Horizontal {
        LEFT, CENTER, RIGHT
    }

    @Getter private final Vertical vertical;

    @Getter private final Horizontal horizontal;
}
