package com.kriztech.book.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.kriztech.book.Model.BannerModel.Result;
import com.kriztech.book.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class BannerAdapter extends PagerAdapter {

    private Context context;
    private List<Result> bannerList;

    public BannerAdapter(Context context, List<Result> bannerList) {
        this.context = context;
        this.bannerList = bannerList;
    }

    @Override
    public int getCount() {
        return bannerList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        View imageLayout = LayoutInflater.from(container.getContext()).inflate(R.layout.banner_item_row, container, false);

        ImageView imageView = imageLayout.findViewById(R.id.image);

        if (!TextUtils.isEmpty(bannerList.get(position).getImage()))
            Picasso.get().load(bannerList.get(position).getImage()).placeholder(R.drawable.no_image_land).into(imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("position", "" + position);
                Log.e("url", "" + bannerList.get(position).getUrl());

                if (!TextUtils.isEmpty(bannerList.get(position).getUrl())) {
                    Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("" + bannerList.get(position).getUrl()));
                    context.startActivity(viewIntent);
                }
            }
        });

        container.addView(imageLayout, 0);
        return imageLayout;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        (container).removeView((View) object);
    }

}