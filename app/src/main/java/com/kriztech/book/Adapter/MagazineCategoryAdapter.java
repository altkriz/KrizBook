package com.kriztech.book.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.kriztech.book.Activity.MagazineByCategory;
import com.kriztech.book.Model.CategoryModel.Result;
import com.kriztech.book.R;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MagazineCategoryAdapter extends RecyclerView.Adapter<MagazineCategoryAdapter.MyViewHolder> {

    Context mcontext;
    private List<Result> categoryList;
    String type;

    public MagazineCategoryAdapter(Context context, List<Result> categoryList, String type) {
        this.mcontext = context;
        this.categoryList = categoryList;
        this.type = type;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txtCategory;
        RoundedImageView ivThumb;

        public MyViewHolder(View view) {
            super(view);
            txtCategory = view.findViewById(R.id.txtCategory);
            ivThumb = view.findViewById(R.id.ivThumb);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (type.equalsIgnoreCase("Home")) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.magazine_category_frg, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.magazine_category_item, parent, false);
        }
        return new MagazineCategoryAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        holder.txtCategory.setText("" + categoryList.get(position).getName());

        if (!TextUtils.isEmpty(categoryList.get(position).getImage()))
            Picasso.get().load(categoryList.get(position).getImage()).into(holder.ivThumb);

        holder.ivThumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("click", "" + position);
                Intent intent = new Intent(mcontext, MagazineByCategory.class);
                intent.putExtra("ID", "" + categoryList.get(position).getId());
                intent.putExtra("Name", "" + categoryList.get(position).getName());
                mcontext.startActivity(intent);
            }
        });
    }

    public void addBook(List<Result> items) {
        this.categoryList.addAll(items);
        Log.e("categoryList", "" + categoryList.size());
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

}