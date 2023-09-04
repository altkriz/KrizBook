package com.kriztech.book.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.kriztech.book.Activity.BookByCategory;
import com.kriztech.book.Model.CategoryModel.Result;
import com.kriztech.book.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.MyViewHolder> {

    private List<Result> categoryList;
    private Context mcontext;
    private String from;

    public CategoryAdapter(Context context, List<Result> categoryList, String from) {
        this.mcontext = context;
        this.categoryList = categoryList;
        this.from = from;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txt_bookname, txt_tag;
        ImageView ivThumb;
        LinearLayout ly_category;

        public MyViewHolder(View view) {
            super(view);
            txt_bookname = view.findViewById(R.id.txt_bookname);
            ivThumb = view.findViewById(R.id.ivThumb);
            txt_tag = view.findViewById(R.id.txt_tag);
            ly_category = view.findViewById(R.id.ly_category);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        holder.txt_bookname.setText("" + categoryList.get(position).getName());
        holder.txt_tag.setText("" + categoryList.get(position).getName().charAt(0));

        if (!TextUtils.isEmpty(categoryList.get(position).getImage()))
            Picasso.get().load(categoryList.get(position).getImage()).placeholder(R.drawable.no_image_land).into(holder.ivThumb);

        holder.ly_category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("click", "call");
                Intent intent = new Intent(mcontext, BookByCategory.class);
                intent.putExtra("cat_id", categoryList.get(position).getId());
                intent.putExtra("cat_name", categoryList.get(position).getName());
                intent.putExtra("cat_image", categoryList.get(position).getImage());
                mcontext.startActivity(intent);
            }
        });

    }

    public void addCategory(List<Result> items) {
        this.categoryList.addAll(items);
        Log.e("categoryList", "" + categoryList.size());
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

}