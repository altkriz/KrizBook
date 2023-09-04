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

public class NewArrivalAdapter extends RecyclerView.Adapter<NewArrivalAdapter.MyViewHolder> {

    private List<Result> bookList;
    Context mcontext;
    String from;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txt_bookname, txtSellCount;
        ImageView ivThumb;
        LinearLayout lyBook, lySellCount;

        public MyViewHolder(View view) {
            super(view);
            lyBook = view.findViewById(R.id.lyBook);
            lySellCount = view.findViewById(R.id.lySellCount);
            txt_bookname = view.findViewById(R.id.txt_bookname);
            txtSellCount = view.findViewById(R.id.txtSellCount);
            ivThumb = view.findViewById(R.id.ivThumb);
        }
    }

    public NewArrivalAdapter(Context context, List<Result> bookList, String from) {
        this.bookList = bookList;
        this.mcontext = context;
        this.from = from;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (from.equalsIgnoreCase("Home")) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.newarrival_item, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.newarrival_item2, parent, false);
        }
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        holder.txt_bookname.setText("" + bookList.get(position).getTitle());

        if (bookList.get(position).getIsPaid().equalsIgnoreCase("1")) {
            holder.lySellCount.setVisibility(View.VISIBLE);
            holder.txtSellCount.setText("" + Utils.changeToK(Long.parseLong("" + bookList.get(position).getTotalSell())));
        } else {
            holder.lySellCount.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(bookList.get(position).getImage()))
            Picasso.get().load(bookList.get(position).getImage()).placeholder(R.drawable.no_image_potr).into(holder.ivThumb);

        holder.lyBook.setOnClickListener(new View.OnClickListener() {
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