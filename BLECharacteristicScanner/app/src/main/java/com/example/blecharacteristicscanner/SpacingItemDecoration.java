package com.example.blecharacteristicscanner;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class SpacingItemDecoration extends RecyclerView.ItemDecoration
{
    private final int spacing;

    public SpacingItemDecoration(int spacing)
    {
        this.spacing = spacing;  // Spacing in px
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
    {
        outRect.bottom = spacing; // Add bottom spacing
    }
}