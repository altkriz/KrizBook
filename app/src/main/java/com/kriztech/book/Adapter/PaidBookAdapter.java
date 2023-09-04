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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kriztech.book.Activity.BookDetails;
import com.kriztech.book.Model.BookModel.Result;
import com.kriztech.book.R;
import com.kriztech.book.Utility.Utils;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PaidBookAdapter extends RecyclerView.Adapter<PaidBookAdapter.MyViewHolder> {

    List<Result> paidbookList;
    Context mcontext;
    String from, currency_symbol;

    public PaidBookAdapter(Context context, List<Result> paidbookList, String from, String currency_symbol) {
        this.paidbookList = paidbookList;
        this.mcontext = context;
        this.from = from;
        this.currency_symbol = currency_symbol;
    }

    @NonNull
    @Override
    public PaidBookAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        if (from.equalsIgnoreCase("Home")) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.freepaid_book_item, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.freepaid_book_item2, parent, false);
        }
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PaidBookAdapter.MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (from.equalsIgnoreCase("Home")) {
            holder.txt_bookname.setText("" + paidbookList.get(position + 2).getTitle());
            holder.txt_description.setText(Html.fromHtml(paidbookList.get(position + 2).getDescription()));
            holder.txt_category.setText("" + paidbookList.get(position + 2).getCategoryName());
            holder.ratingbar.setRating(Float.parseFloat(paidbookList.get(position + 2).getAvgRating()));
            holder.txt_book_price.setText(currency_symbol + " " + paidbookList.get(position + 2).getPrice());
            if (paidbookList.get(position + 2).getIsPaid().equalsIgnoreCase("1")) {
                holder.lySellCount.setVisibility(View.VISIBLE);
                holder.txtSellCount.setText("" + Utils.changeToK(Long.parseLong("" + paidbookList.get(position + 2).getTotalSell())));
            } else {
                holder.lySellCount.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(paidbookList.get(position + 2).getImage()))
                Picasso.get().load(paidbookList.get(position + 2).getImage()).into(holder.ivThumb);

            holder.lyBook.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e("click", "call");
                    Intent intent = new Intent(mcontext, BookDetails.class);
                    intent.putExtra("docID", "" + paidbookList.get(position + 2).getId());
                    intent.putExtra("authorID", "" + paidbookList.get(position + 2).getAuthorId());
                    mcontext.startActivity(intent);
                }
            });

        } else {
            holder.txt_bookname.setText("" + paidbookList.get(position).getTitle());
            holder.txt_description.setText(Html.fromHtml(paidbookList.get(position).getDescription()));
            holder.txt_category.setText("" + paidbookList.get(position).getCategoryName());
            holder.ratingbar.setRating(Float.parseFloat(paidbookList.get(position).getAvgRating()));
            holder.txt_book_price.setText(currency_symbol + " " + paidbookList.get(position).getPrice());
            if (paidbookList.get(position).getIsPaid().equalsIgnoreCase("1")) {
                holder.lySellCount.setVisibility(View.VISIBLE);
                holder.txtSellCount.setText("" + Utils.changeToK(Long.parseLong("" + paidbookList.get(position).getTotalSell())));
            } else {
                holder.lySellCount.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(paidbookList.get(position).getImage()))
                Picasso.get().load(paidbookList.get(position).getImage()).into(holder.ivThumb);

            holder.lyBook.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e("click", "call");
                    Intent intent = new Intent(mcontext, BookDetails.class);
                    intent.putExtra("docID", paidbookList.get(position).getId());
                    intent.putExtra("authorID", "" + paidbookList.get(position).getAuthorId());
                    mcontext.startActivity(intent);
                }
            });
        }
    }

    public void addBook(List<Result> items) {
        this.paidbookList.addAll(items);
        Log.e("paidbookList", "" + paidbookList.size());
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (from.equalsIgnoreCase("Home")) {
            return (paidbookList.size()) - 2;
        } else {
            return paidbookList.size();
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txt_bookname, txtSellCount, txt_description, txt_category, txt_book_price;
        ImageView ivThumb;
        public SimpleRatingBar ratingbar;
        LinearLayout lyBook, lySellCount;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            lyBook = itemView.findViewById(R.id.lyBook);
            txt_bookname = itemView.findViewById(R.id.txt_bookname);
            txt_description = itemView.findViewById(R.id.txt_description);
            txt_category = itemView.findViewById(R.id.txt_category);
            txt_book_price = itemView.findViewById(R.id.txt_book_price);
            ratingbar = itemView.findViewById(R.id.ratingbar);
            ivThumb = itemView.findViewById(R.id.ivThumb);
            lySellCount = itemView.findViewById(R.id.lySellCount);
            txtSellCount = itemView.findViewById(R.id.txtSellCount);
        }
    }

}