package com.tunjid.projects.avantphotouploader.helpers;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.nineoldandroids.view.ViewHelper;
import com.tunjid.projects.avantphotouploader.designsupportcopies.AnimationUtils;

/**
 * Subclass of {@link android.support.design.widget.FloatingActionButton} that has a translate hide and show.
 * with snackbar behavior.
 */

@CoordinatorLayout.DefaultBehavior(FloatingActionButton.Behavior.class)

public class FloatingActionButton extends android.support.design.widget.FloatingActionButton {
    private boolean mVisible;

    public FloatingActionButton(Context context) {
        this(context, null);
    }

    public FloatingActionButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void show() {
        this.toggle(true, true, false, false);
    }

    @Override
    public void hide() {
        this.toggle(false, true, false, false);
    }

    public void hideThenShow(final int resourceID) {
        FloatingActionButton.this.hide();
        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                FloatingActionButton.this.setImageResource(resourceID);
                FloatingActionButton.this.show();
            }
        }, 240L);
    }

    public void animateOutWithColor(final int colorNormal, final int colorPressed) {

        this.hide();

        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                FloatingActionButton.this.setBackgroundTintList(ColorStateList.valueOf(colorNormal));
                FloatingActionButton.this.setRippleColor(colorPressed);
                FloatingActionButton.this.show();
            }
        }, 240L);
    }

    public void showTranslate() {
        this.showTranslate(true);
    }

    public void hideTranslate() {
        this.hideTranslate(true);
    }

    public void showTranslate(boolean animate) {
        this.toggle(true, animate, false, true);
    }

    public void hideTranslate(boolean animate) {
        this.toggle(false, animate, false, true);
    }

    private void toggle(final boolean visible, final boolean animate, boolean force, final boolean translate) {
        if (this.mVisible != visible || force) {
            this.mVisible = visible;
            int height = this.getHeight();
            if (height == 0 && !force) {
                ViewTreeObserver translationY = this.getViewTreeObserver();
                if (translationY.isAlive()) {
                    translationY.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        public boolean onPreDraw() {
                            ViewTreeObserver currentVto = FloatingActionButton.this.getViewTreeObserver();
                            if (currentVto.isAlive()) {
                                currentVto.removeOnPreDrawListener(this);
                            }

                            FloatingActionButton.this.toggle(visible, animate, true, translate);
                            return true;
                        }
                    });
                    return;
                }
            }

            int hiddenHeight = height + this.getMarginBottom();
            int translationY1 = visible ? 0 : hiddenHeight;
            float currentPosition = this.getTranslationY();

            if (animate) {
                if (translate) {
                    // The Fab hasn't had it's visibility changed to visible.
                    if (this.getVisibility() == GONE && visible) {
                        this.setTranslationY(hiddenHeight);
                        this.setVisibility(VISIBLE);
                        this.animate().translationY(0F).setDuration(200L)
                                .setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
                    }
                    else {
                        this.animate().translationY((float) translationY1).setDuration(200L)
                                .setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
                    }
                }
                else {
                    if ((int) currentPosition == hiddenHeight) {
                        this.animate().translationY(0F).setDuration(200L)
                                .setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
                    }
                    else {
                        if (visible) {
                            this.animate().scaleX(1.0F).scaleY(1.0F).alpha(1.0F).setDuration(200L)
                                    .setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
                        }
                        else {
                            this.animate().scaleX(0.0F).scaleY(0.0F).alpha(0.0F).setDuration(200L)
                                    .setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
                        }
                    }
                }
            }
            else {
                ViewHelper.setTranslationY(this, (float) translationY1);
            }

            if (!this.hasHoneycombApi()) {
                this.setClickable(visible);
            }
        }

    }

    private boolean hasHoneycombApi() {
        return Build.VERSION.SDK_INT >= 11;
    }

    private int getMarginBottom() {
        int marginBottom = 0;
        ViewGroup.LayoutParams layoutParams = this.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            marginBottom = ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin;
        }

        return marginBottom;
    }
}
