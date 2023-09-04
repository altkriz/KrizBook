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

import com.kriztech.book.Activity.BookDetails;
import com.kriztech.book.Model.BookModel.Result;
import com.kriztech.book.R;
import com.kriztech.book.Utility.Utils;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CategoryBookAdapter extends RecyclerView.Adapter<CategoryBookAdapter.MyViewHolder> {

    private List<Result> bookList;
    Context mcontext;

    public CategoryBookAdapter(Context context, List<Result> bookList) {
        this.bookList = bookList;
        this.mcontext = context;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txt_bookname, txt_view, txtSellCount;
        ImageView ivThumb;
        LinearLayout lySellCount;

        public MyViewHolder(View view) {
            super(view);
            txt_bookname = view.findViewById(R.id.txt_bookname);
            ivThumb = view.findViewById(R.id.ivThumb);
            txt_view = view.findViewById(R.id.txt_view);
            txtSellCount = view.findViewById(R.id.txtSellCount);
            lySellCount = view.findViewById(R.id.lySellCount);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.categorybook_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        holder.txt_view.setText("" + bookList.get(position).getReadcnt());
        holder.txt_bookname.setText("" + bookList.get(position).getTitle());

        if (bookList.get(position).getIsPaid().equalsIgnoreCase("1")) {
            holder.lySellCount.setVisibility(View.VISIBLE);
            holder.txtSellCount.setText("" + Utils.changeToK(Long.parseLong("" + bookList.get(position).getTotalSell())));
        } else {
            holder.lySellCount.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(bookList.get(position).getImage()))
            Picasso.get().load(bookList.get(position).getImage()).placeholder(R.drawable.no_image_potr).into(holder.ivThumb);

        holder.ivThumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("click", "call");
                Intent intent = new Intent(mcontext, BookDetails.class);
                intent.putExtra("docID", "" + bookList.get(position).getId());
                intent.putExtra("authorID", "" + bookList.get(position).getAuthorId());
                mcontext.startActivity(intent);
            }
        });

    }

    public void addBook(List<Result> items) {
        this.bookList.addAll(items);
        Log.e("bookList", "" + bookList.size());
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

}