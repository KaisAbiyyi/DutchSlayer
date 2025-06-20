package io.DutchSlayer.defend.entities.traps;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import io.DutchSlayer.defend.ui.ImageLoader;

public class Trap {
    public final Rectangle bounds;
    public boolean occupied = false;
    private final float centerX, centerY;
    private final float[] verts;

    private final float w, h;
    private Texture tex;
    private final TrapType type;

    private float cooldown = 0f;
    private static final boolean SINGLE_USE = true;

    public Trap(float[] verts, float scale, TrapType type) {
        this.verts = verts.clone();
        this.type = type;

        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float maxY = Float.MIN_VALUE;

        for (int i = 0; i < verts.length; i += 2) {
            minX = Math.min(minX, verts[i]);
            maxX = Math.max(maxX, verts[i]);
            minY = Math.min(minY, verts[i+1]);
            maxY = Math.max(maxY, verts[i+1]);
        }

        this.bounds = new Rectangle(minX, minY, maxX-minX, maxY-minY);
        this.centerX = minX + (maxX-minX)/2f;
        this.centerY = minY + (maxY-minY)/2f;

        switch(type) {
            case ATTACK:
                this.tex = ImageLoader.trapAttackTex;
                break;
            case SLOW:
                this.tex = ImageLoader.trapSlowTex;
                break;
            case EXPLOSION:
                this.tex = ImageLoader.trapBombTex;
                break;
            default:
                this.tex = ImageLoader.trapTex;
                break;
        }

        if (tex == null) {
            tex = ImageLoader.trapTex;
        }

        this.w = tex.getWidth()  * scale;
        this.h = tex.getHeight() * scale;
    }

    public Trap(float[] verts, float scale) {
        this(verts, scale, TrapType.ATTACK);
    }

    public void update(float delta) {
        if (cooldown > 0) {
            cooldown -= delta;
        }
    }

    public void drawBatch(SpriteBatch batch) {
        boolean isUsed = false;
        if (occupied && !(SINGLE_USE && isUsed)) {
            if (isOnCooldown()) {
                batch.setColor(0.5f, 0.5f, 0.5f, 0.8f);
            } else {

                batch.setColor(1f, 1f, 1f, 1f);
            }

            float spriteX = centerX - w/2f;
            float spriteY = getTowerAlignedY();
            batch.draw(tex, spriteX, spriteY, w, h);
            batch.setColor(1f, 1f, 1f, 1f);
        }
    }

    private float getTowerAlignedY() {
        float y0 = verts[1];
        float y1 = verts[3];
        float y2 = verts[5];
        float y3 = verts[7];

        return (y0 + y1 + y2 + y3) / 4.4f;
    }

    public boolean isOnCooldown() {
        return cooldown > 0;
    }

    public boolean isUsed() {
        return false;
    }

    public TrapType getType() {
        return type;
    }
    public float getCenterX() {
        return centerX;
    }
    public float getCenterY() {
        return centerY;
    }
}
