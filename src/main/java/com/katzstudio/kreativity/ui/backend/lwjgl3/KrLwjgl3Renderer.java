package com.katzstudio.kreativity.ui.backend.lwjgl3;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Pools;
import com.katzstudio.kreativity.ui.KrColor;
import com.katzstudio.kreativity.ui.render.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import static com.katzstudio.kreativity.ui.KrToolkit.getDefaultToolkit;

/**
 * {@link KrRenderer} implementation for the libgdx lwjgl3 backend
 */
public class KrLwjgl3Renderer implements KrRenderer {

    private final RenderMode spriteBatchRenderMode;

    private final RenderMode lineShapeRenderMode;

    private final RenderMode filledShapeRenderMode;

    private final RenderMode nullRenderMode;

    private final SpriteBatch spriteBatch;

    private final ShapeRenderer shapeRenderer;

    private RenderMode currentRenderMode;

    @Setter private BitmapFont font;

    private Vector2 translation;

    @Getter private Vector2 viewportSize = new Vector2(0, 0);

    @Getter @Setter private KrBrush brush;

    @Getter @Setter private KrPen pen;

    @Getter private float opacity = 1;

    public KrLwjgl3Renderer() {
        spriteBatch = new SpriteBatch(100);
        shapeRenderer = new ShapeRenderer(100);
        shapeRenderer.setAutoShapeType(true);
        translation = new Vector2(0, 0);

        spriteBatchRenderMode = new SpriteBatchRenderMode();
        lineShapeRenderMode = new ShapeRenderMode(ShapeRenderer.ShapeType.Line);
        filledShapeRenderMode = new ShapeRenderMode(ShapeRenderer.ShapeType.Filled);
        nullRenderMode = new NullRenderMode();

        currentRenderMode = nullRenderMode;

        pen = new KrPen(1.0f, Color.BLACK);
        brush = new KrColorBrush(KrColor.TRANSPARENT);
    }

    @Override
    public void beginFrame() {
        currentRenderMode = nullRenderMode;
    }

    @Override
    public void endFrame() {
        flush();
        translate(-translation.x, -translation.y);
    }

    @Override
    public void drawText(String text, Vector2 position) {
        drawText(text, position.x, position.y);
    }

    @Override
    public void drawText(String text, float x, float y) {
        ensureSpriteBatchOpen();

        Color originalFontColor = font.getColor();

        font.setColor(multiplyAlpha(pen.getColor()));
        font.draw(spriteBatch, text, x, viewportSize.y - y);

        font.setColor(originalFontColor);
    }

    @Override
    public void drawTextWithShadow(String text, Vector2 position, Vector2 shadowOffset, Color shadowColor) {
        if (shadowOffset.equals(Vector2.Zero)) {
            drawText(text, position);
            return;
        }

        Color originalFontColor = font.getColor();

        ensureSpriteBatchOpen();

        // render shadow
        font.setColor(multiplyAlpha(shadowColor));
        font.draw(spriteBatch, text, position.x + shadowOffset.x, viewportSize.y - position.y - shadowOffset.y);

        // render text
        font.setColor(multiplyAlpha(pen.getColor()));
        font.draw(spriteBatch, text, position.x, viewportSize.y - position.y);

        font.setColor(originalFontColor);
    }

    @Override
    public void drawRect(Rectangle rectangle) {
        drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void drawRect(float x, float y, float w, float h) {
        ensureShapeRendererOpen(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(multiplyAlpha(pen.getColor()));

        drawLineInternal(x, y, x + w - 1, y);
        drawLineInternal(x, y, x, y + h - 1);
        drawLineInternal(x + w - 1, y, x + w - 1, y + h - 1);
        drawLineInternal(x, y + h - 1, x + w - 1, y + h - 1);
    }

    @Override
    public void drawLine(float x1, float y1, float x2, float y2) {
        ensureShapeRendererOpen(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(multiplyAlpha(pen.getColor()));
        drawLineInternal(x1, y1, x2, y2);
    }

    private void drawLineInternal(float x1, float y1, float x2, float y2) {
        shapeRenderer.line(x1, viewportSize.y - y1, x2 + 1, viewportSize.y - y2 - 1);
    }

    @Override
    public void fillRect(Rectangle rectangle) {
        fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void fillRect(float x, float y, float w, float h) {
        if (brush instanceof KrDrawableBrush) {
            ensureSpriteBatchOpen();
            KrDrawableBrush drawableBrush = (KrDrawableBrush) brush;
            spriteBatch.setColor(1, 1, 1, ((KrDrawableBrush) brush).getOpacity());
            drawableBrush.getDrawable().draw(spriteBatch, x, viewportSize.y - y - h, w, h);
        }

        if (brush instanceof KrColorBrush) {
            KrColorBrush colorBrush = (KrColorBrush) brush;

            ensureShapeRendererOpen(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(multiplyAlpha(colorBrush.getColor()));
            shapeRenderer.rect(x, viewportSize.y - y - h, w, h);
        }
    }

    @Override
    public void fillRoundedRect(Rectangle geometry, int radius) {
        fillRoundedRect((int) geometry.x, (int) geometry.y, (int) geometry.width, (int) geometry.height, radius);
    }

    @Override
    public void fillRoundedRect(int x, int y, int w, int h, int cornerRadius) {
        Drawable drawable = getRoundedRectDrawable(cornerRadius);

        ensureSpriteBatchOpen();
        if (brush instanceof KrColorBrush) {
            KrColorBrush colorBrush = (KrColorBrush) brush;
            spriteBatch.setColor(multiplyAlpha(colorBrush.getColor()));
        }

        drawable.draw(spriteBatch, x, viewportSize.y - y - h, w, h);

        spriteBatch.setColor(Color.WHITE);
    }

    private Drawable getRoundedRectDrawable(int radius) {
        switch (radius) {
            case 1:
            case 2:
                return getDefaultToolkit().getSkin().getDrawable("rounded_rect_2");
            case 3:
                return getDefaultToolkit().getSkin().getDrawable("rounded_rect_3");
            case 4:
                return getDefaultToolkit().getSkin().getDrawable("rounded_rect_4");
            case 5:
                return getDefaultToolkit().getSkin().getDrawable("rounded_rect_5");
            case 6:
                return getDefaultToolkit().getSkin().getDrawable("rounded_rect_6");
            default:
                return getDefaultToolkit().getSkin().getDrawable("rounded_rect_2");
        }
    }

    @Override
    public void translate(float x, float y) {
        flush();
        translation.add(x, y);
        spriteBatch.getTransformMatrix().translate(x, -y, 0);
        shapeRenderer.translate(x, -y, 0);
    }

    @Override
    public boolean beginClip(Rectangle rectangle) {
        return beginClip(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public boolean beginClip(float x, float y, float width, float height) {
        flush();
        Rectangle clipRectangle = Pools.obtain(Rectangle.class);
        clipRectangle.set(x + translation.x, viewportSize.y - y - height - translation.y, width, height);
        if (ScissorStack.pushScissors(clipRectangle)) {
            return true;
        }
        Pools.free(clipRectangle);
        return false;

    }

    @Override
    public void endClip() {
        flush();
        Pools.free(ScissorStack.popScissors());
    }

    @Override
    public void setViewportSize(float width, float height) {
        viewportSize.set(width, height);
        spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
    }

    @Override
    public float setOpacity(float opacity) {
        float oldOpacity = this.opacity;
        this.opacity = opacity;
        return oldOpacity;
    }

    private Color multiplyAlpha(Color color) {
        Color newColor = new Color(color);
        newColor.a *= this.opacity;
        return newColor;
    }

    @Override
    public void popState() {
        // TODO(alex): implement
    }

    @Override
    public void pushState() {
        // TODO(alex): implement
    }

    private void ensureSpriteBatchOpen() {
        if (currentRenderMode != spriteBatchRenderMode) {
            currentRenderMode.end();
            currentRenderMode = spriteBatchRenderMode;
            currentRenderMode.begin();
        }
    }

    private void ensureShapeRendererOpen(ShapeRenderer.ShapeType shapeType) {
        RenderMode requestedRenderMode = shapeType == ShapeRenderer.ShapeType.Line ? lineShapeRenderMode : filledShapeRenderMode;
        if (currentRenderMode != requestedRenderMode) {
            currentRenderMode.end();
            currentRenderMode = requestedRenderMode;
            currentRenderMode.begin();
        }
    }

    private void flush() {
        currentRenderMode.end();
        currentRenderMode = nullRenderMode;
    }

    private interface RenderMode {
        void begin();

        void end();
    }

    private class SpriteBatchRenderMode implements RenderMode {
        @Override
        public void begin() {
            spriteBatch.begin();
        }

        @Override
        public void end() {
            spriteBatch.end();
        }
    }

    @RequiredArgsConstructor
    private class ShapeRenderMode implements RenderMode {
        public final ShapeRenderer.ShapeType shapeType;

        @Override
        public void begin() {
            shapeRenderer.begin(shapeType);
        }

        @Override
        public void end() {
            shapeRenderer.end();
        }
    }

    private class NullRenderMode implements RenderMode {
        @Override
        public void begin() {
        }

        @Override
        public void end() {
        }
    }
}