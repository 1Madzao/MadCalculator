package com.madstudio.madcalculator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

public class ParticleBackgroundView extends View {

    private static final int PARTICLE_COUNT = 36;
    private final Particle[] particles = new Particle[PARTICLE_COUNT];
    private final Paint particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bgPaint = new Paint();
    private final Random random = new Random();
    private final float[] hsv = new float[]{0f, 0.65f, 0.95f};

    private float globalHue = 215f;
    private boolean isAnimating = false;

    public ParticleBackgroundView(Context context) {
        super(context);
        init();
    }

    public ParticleBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ParticleBackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        particlePaint.setStyle(Paint.Style.FILL);
        glowPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w <= 0 || h <= 0) return;

        bgPaint.setShader(new LinearGradient(
                0f, 0f, w * 0.7f, h,
                Color.parseColor("#07090E"),
                Color.parseColor("#0F1420"),
                Shader.TileMode.CLAMP
        ));

        float density = getResources().getDisplayMetrics().density;
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particles[i] = createParticle(w, h, density);
        }
    }

    private Particle createParticle(int width, int height, float density) {
        Particle p = new Particle();
        p.x = random.nextFloat() * width;
        p.y = random.nextFloat() * height;
        p.radius = (1.5f + random.nextFloat() * 3.2f) * density;
        p.vx = (-0.25f + random.nextFloat() * 0.50f) * density;
        p.vy = (-0.42f - random.nextFloat() * 0.35f) * density;
        p.hueOffset = random.nextFloat() * 65f;
        p.baseAlpha = 28 + random.nextInt(56);
        p.pulsePhase = random.nextFloat() * (float) (Math.PI * 2);
        p.pulseSpeed = 0.012f + random.nextFloat() * 0.018f;
        return p;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return;

        canvas.drawRect(0, 0, width, height, bgPaint);

        globalHue += 0.12f;
        if (globalHue > 360f) globalHue -= 360f;

        for (Particle p : particles) {
            if (p == null) continue;

            p.x += p.vx;
            p.y += p.vy;
            p.pulsePhase += p.pulseSpeed;

            if (p.y + p.radius * 3 < 0) {
                p.y = height + p.radius * 2;
                p.x = random.nextFloat() * width;
            }
            if (p.x < -p.radius * 3) p.x = width + p.radius * 2;
            if (p.x > width + p.radius * 3) p.x = -p.radius * 2;

            hsv[0] = (globalHue + p.hueOffset) % 360f;
            int baseColor = Color.HSVToColor(hsv);

            float pulse = (float) ((Math.sin(p.pulsePhase) + 1f) * 0.5f);
            int currentAlpha = (int) (p.baseAlpha * (0.65f + 0.35f * pulse));

            glowPaint.setColor(baseColor);
            glowPaint.setAlpha(Math.max(8, currentAlpha / 3));
            canvas.drawCircle(p.x, p.y, p.radius * 2.6f, glowPaint);

            particlePaint.setColor(baseColor);
            particlePaint.setAlpha(currentAlpha);
            canvas.drawCircle(p.x, p.y, p.radius, particlePaint);
        }

        if (isAnimating) {
            postInvalidateOnAnimation();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAnimating = true;
        postInvalidateOnAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        isAnimating = false;
        super.onDetachedFromWindow();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        isAnimating = (visibility == VISIBLE);
        if (isAnimating) {
            postInvalidateOnAnimation();
        }
    }

    private static class Particle {
        float x, y, radius, vx, vy;
        float hueOffset, pulsePhase, pulseSpeed;
        int baseAlpha;
    }
}
