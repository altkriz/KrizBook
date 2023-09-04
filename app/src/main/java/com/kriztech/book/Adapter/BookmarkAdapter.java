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
import com.kriztech.book.Activity.MagazineDetails;
import com.kriztech.book.Interface.ItemClick;
import com.kriztech.book.Model.BookmarkModel.Result;
import com.kriztech.book.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.MyViewHolder> {

    private Context mcontext;
    private List<Result> bookMarkList;
    private String from;
    private ItemClick itemClick;

    public BookmarkAdapter(Context context, List<Result> bookMarkList, String from, ItemClick itemClick) {
        this.mcontext = context;
        this.bookMarkList = bookMarkList;
        this.from = from;
        this.itemClick = itemClick;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txt_bookname, txt_view;
        ImageView ivThumb;
        LinearLayout lyDelete;

        public MyViewHolder(View view) {
            super(view);
            txt_bookname = view.findViewById(R.id.txt_bookname);
            ivThumb = view.findViewById(R.id.ivThumb);
            txt_view = view.findViewById(R.id.txt_view);
            lyDelete = view.findViewById(R.id.lyDelete);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmark_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        holder.txt_view.setText("" + bookMarkList.get(position).getReadcnt());
        holder.txt_bookname.setText("" + bookMarkList.get(position).getTitle());

        if (!TextUtils.isEmpty(bookMarkList.get(position).getImage()))
            Picasso.get().load(bookMarkList.get(position).getImage()).into(holder.ivThumb);

        holder.ivThumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("click", "pos => " + position);
                Intent intent;
                if (from.equalsIgnoreCase("Magazine")) {
                    intent = new Intent(mcontext, MagazineDetails.class);
                } else {
                    intent = new Intent(mcontext, BookDetails.class);
                }
                intent.putExtra("docID", "" + bookMarkList.get(position).getId());
                intent.putExtra("authorID", "" + bookMarkList.get(position).getAuthorId());
                mcontext.startActivity(intent);
            }
        });

        holder.lyDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("click", "" + position);
                itemClick.OnClick(bookMarkList.get(position).getId(), position);
            }
        });

    }

    public void addBook(List<Result> items) {
        this.bookMarkList.addAll(items);
        Log.e("bookMarkList", "" + bookMarkList.size());
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return bookMarkList.size();
    }

}