package com.app.toado.activity.chat.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Created by ghanendra on 12/08/2017.
 */

public class ResizableImageView extends android.support.v7.widget.AppCompatImageView
{

    private Bitmap mBitmap;

    // Constructor

    public ResizableImageView(Context context)
    {
        super(context);
    }


    // Overriden methods


    @Override
    protected void onMeasure(int widthMeasureSpec,
                             int heightMeasureSpec) {
        if(mBitmap != null)
        {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = width * mBitmap.getHeight() / mBitmap.getWidth();
            setMeasuredDimension(width, height);

        }
        else
        {
            super.onMeasure(widthMeasureSpec,
                    heightMeasureSpec);
        }
    }

    @Override
    public void setImageBitmap(Bitmap bitmap)
    {
        mBitmap = bitmap;
        super.setImageBitmap(bitmap);
    }

}
