package com.tunjid.projects.avantphotouploader.designsupportcopies;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

/**
 * Copy of ViewGroupUtilsHoneyComb
 */
public class ViewGroupUtilsHoneycomb {
    private static final ThreadLocal<Matrix> sMatrix = new ThreadLocal();
    private static final ThreadLocal<RectF> sRectF = new ThreadLocal();
    private static final Matrix IDENTITY = new Matrix();

    ViewGroupUtilsHoneycomb() {
    }

    public static void offsetDescendantRect(ViewGroup group, View child, Rect rect) {
        Matrix m = (Matrix)sMatrix.get();
        if(m == null) {
            m = new Matrix();
            sMatrix.set(m);
        } else {
            m.set(IDENTITY);
        }

        offsetDescendantMatrix(group, child, m);
        RectF rectF = (RectF)sRectF.get();
        if(rectF == null) {
            rectF = new RectF();
        }

        rectF.set(rect);
        m.mapRect(rectF);
        rect.set((int)(rectF.left + 0.5F), (int)(rectF.top + 0.5F), (int)(rectF.right + 0.5F), (int)(rectF.bottom + 0.5F));
    }

    static void offsetDescendantMatrix(ViewParent target, View view, Matrix m) {
        ViewParent parent = view.getParent();
        if(parent instanceof View && parent != target) {
            View vp = (View)parent;
            offsetDescendantMatrix(target, vp, m);
            m.preTranslate((float)(-vp.getScrollX()), (float)(-vp.getScrollY()));
        }

        m.preTranslate((float)view.getLeft(), (float)view.getTop());
        if(!view.getMatrix().isIdentity()) {
            m.preConcat(view.getMatrix());
        }

    }
}
