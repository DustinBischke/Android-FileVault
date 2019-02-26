package ca.bischke.apps.filevault;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class GridMarginDecoration extends RecyclerView.ItemDecoration
{
    private int margin;

    public GridMarginDecoration(int margin)
    {
        this.margin = margin;
    }

    public GridMarginDecoration(@NonNull Context context, @DimenRes int dimenMargin)
    {
        this(context.getResources().getDimensionPixelSize(dimenMargin));
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
    {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.set(margin, margin, margin, margin);
    }
}
