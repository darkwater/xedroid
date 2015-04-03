package com.novaember.xedroid;

import android.content.res.Resources;
import android.util.TypedValue;

public class Util
{
    public static float getPx(float x, Resources resources)
    {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, x, resources.getDisplayMetrics());
    }
}
