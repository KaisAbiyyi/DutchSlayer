package io.DutchSlayer.defend;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class Trap {
    public final Rectangle bounds;
    public boolean occupied = false;
    private final float centerX, centerY;
    private final Texture tex = ImageLoader.trapTex;
    private final float scale;
    private final float w, h;

    // terima verts only untuk hit‐zone, plus scale untuk gambar
    public Trap(float[] verts, float scale) {
        // hit‐zone
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE;
        for (int i = 0; i < verts.length; i += 2) {
            minX = Math.min(minX, verts[i]);
            maxX = Math.max(maxX, verts[i]);
            minY = Math.min(minY, verts[i+1]);
            maxY = Math.max(maxY, verts[i+1]);
        }
        bounds = new Rectangle(minX, minY, maxX-minX, maxY-minY);

        // simpan center untuk draw, dan scale sprite
        this.centerX = minX + (maxX-minX)/2f;
        this.centerY = minY + (maxY-minY)/2f;
        this.scale   = scale;
        this.w       = tex.getWidth()  * scale;
        this.h       = tex.getHeight() * scale;
    }

    public boolean contains(float x, float y) {
        return bounds.contains(x, y);
    }

    public void drawBatch(SpriteBatch batch) {
        if (occupied) {
            // gambar di center, dengan ukuran di‐scale
            batch.draw(tex,
                centerX - w/2f,
                centerY - h/2f,
                w, h);
        }
    }
}
