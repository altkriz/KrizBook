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
import com.iarcuschin.simpleratingbar.SimpleRatingBar;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FeatureAdapter extends RecyclerView.Adapter<FeatureAdapter.MyViewHolder> {

    private List<Result> bookList;
    private Context mcontext;
    private String from, currency_symbol;

    public FeatureAdapter(Context context, List<Result> bookList, String from, String currency_symbol) {
        this.mcontext = context;
        this.bookList = bookList;
        this.from = from;
        this.currency_symbol = currency_symbol;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txt_bookname, txtSellCount, txt_view, txt_book_price, txt_description, txt_category;
        ImageView ivThumb;
        SimpleRatingBar simpleRatingBar;
        LinearLayout lyBook, lySellCount;

        public MyViewHolder(View view) {
            super(view);
            lyBook = view.findViewById(R.id.lyBook);
            txt_bookname = view.findViewById(R.id.txt_bookname);
            ivThumb = view.findViewById(R.id.ivThumb);
            txt_view = view.findViewById(R.id.txt_view);
            txt_book_price = view.findViewById(R.id.txt_book_price);
            txt_description = view.findViewById(R.id.txt_description);
            txt_category = view.findViewById(R.id.txt_category);
            simpleRatingBar = view.findViewById(R.id.ratingbar);
            lySellCount = view.findViewById(R.id.lySellCount);
            txtSellCount = view.findViewById(R.id.txtSellCount);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (from.equalsIgnoreCase("Home")) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.feature_item, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.feature_item2, parent, false);
        }
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

        if (!from.equalsIgnoreCase("Home")) {
            holder.txt_description.setText(Html.fromHtml(bookList.get(position).getDescription()));
            holder.txt_category.setText("" + bookList.get(position).getCategoryName());
        }

        if (bookList.get(position).getIsPaid().equalsIgnoreCase("1")) {
            holder.txt_book_price.setText(currency_symbol + " " + bookList.get(position).getPrice());
        } else {
            holder.txt_book_price.setText("" + mcontext.getResources().getString(R.string.free));
        }

        if (!TextUtils.isEmpty(bookList.get(position).getImage()))
            Picasso.get().load(bookList.get(position).getImage()).into(holder.ivThumb);

        holder.simpleRatingBar.setRating(Float.parseFloat(bookList.get(position).getAvgRating()));

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