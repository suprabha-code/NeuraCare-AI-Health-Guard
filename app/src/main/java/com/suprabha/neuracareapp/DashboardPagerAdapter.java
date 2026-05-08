package com.suprabha.neuracareapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class DashboardPagerAdapter extends PagerAdapter {

    private Context context;

    public DashboardPagerAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return 3; // Number of pages
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = null;

        switch (position) {
            case 0:
                view = inflater.inflate(R.layout.activity_edit_history, container, false);
                break;
            case 1:
                view = inflater.inflate(R.layout.activity_view_history, container, false);
                break;
            case 2:
                view = inflater.inflate(R.layout.activity_sos, container, false);
                break;
        }

        if (view != null) {
            container.addView(view);
        }

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}