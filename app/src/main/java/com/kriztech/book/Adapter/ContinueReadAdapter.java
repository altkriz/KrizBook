package com.kriztech.book.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
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

public class ContinueReadAdapter extends RecyclerView.Adapter<ContinueReadAdapter.MyViewHolder> {

    private List<Result> bookList;
    private Context mcontext;
    private String from;

    public ContinueReadAdapter(Context context, List<Result> bookList, String from) {
        this.mcontext = context;
        this.bookList = bookList;
        this.from = from;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txt_bookname, txtSellCount, txt_description, txt_category;
        ImageView ivThumb;
        LinearLayout lyBook, lySellCount;

        public MyViewHolder(View view) {
            super(view);
            lyBook = view.findViewById(R.id.lyBook);
            txt_bookname = view.findViewById(R.id.txt_bookname);
            txt_description = itemView.findViewById(R.id.txt_description);
            txt_category = itemView.findViewById(R.id.txt_category);
            ivThumb = view.findViewById(R.id.ivThumb);
            lySellCount = itemView.findViewById(R.id.lySellCount);
            txtSellCount = itemView.findViewById(R.id.txtSellCount);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (from.equalsIgnoreCase("Home")) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.continue_read_item, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.continue_read_item2, parent, false);
        }
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        if (from.equalsIgnoreCase("Home")) {
            holder.txt_bookname.setText("" + bookList.get(position + 1).getTitle());
            holder.txt_description.setText(Html.fromHtml(bookList.get(position + 1).getDescription()));
            holder.txt_category.setText("" + bookList.get(position + 1).getCategoryName());

            if (bookList.get(position + 1).getIsPaid().equalsIgnoreCase("1")) {
                holder.lySellCount.setVisibility(View.VISIBLE);
                holder.txtSellCount.setText("" + Utils.changeToK(Long.parseLong("" + bookList.get(position + 1).getTotalSell())));
            } else {
                holder.lySellCount.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(bookList.get(position + 1).getImage()))
                Picasso.get().load(bookList.get(position + 1).getImage()).into(holder.ivThumb);

            holder.lyBook.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e("click", "call");
                    Intent intent = new Intent(mcontext, BookDetails.class);
                    intent.putExtra("docID", "" + bookList.get(position + 1).getId());
                    intent.putExtra("authorID", "" + bookList.get(position + 1).getAuthorId());
                    mcontext.startActivity(intent);
                }
            });

        } else {
            holder.txt_bookname.setText("" + bookList.get(position).getTitle());
            holder.txt_description.setText(Html.fromHtml(bookList.get(position).getDescription()));
            holder.txt_category.setText("" + bookList.get(position).getCategoryName());

            if (bookList.get(position).getIsPaid().equalsIgnoreCase("1")) {
                holder.lySellCount.setVisibility(View.VISIBLE);
                holder.txtSellCount.setText("" + Utils.changeToK(Long.parseLong("" + bookList.get(position).getTotalSell())));
            } else {
                holder.lySellCount.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(bookList.get(position).getImage()))
                Picasso.get().load(bookList.get(position).getImage()).into(holder.ivThumb);

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

    }

    public void addBook(List<Result> items) {
        this.bookList.addAll(items);
        Log.e("bookList", "" + bookList.size());
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (from.equalsIgnoreCase("Home")) {
            return (bookList.size()) - 1;
        } else {
            return bookList.size();
        }
    }

}