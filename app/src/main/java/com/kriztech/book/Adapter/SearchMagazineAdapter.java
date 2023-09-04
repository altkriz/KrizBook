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

import com.kriztech.book.Activity.MagazineDetails;
import com.kriztech.book.Model.MagazineModel.Result;
import com.kriztech.book.R;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SearchMagazineAdapter extends RecyclerView.Adapter<SearchMagazineAdapter.MyViewHolder> {

    private List<Result> magazineList;
    Context mcontext;
    String from, currency_symbol;

    public SearchMagazineAdapter(Context context, List<Result> magazineList, String from, String currency_symbol) {
        this.magazineList = magazineList;
        this.mcontext = context;
        this.from = from;
        this.currency_symbol = currency_symbol;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txt_book, txt_description, txt_book_author, txt_category,
                txt_price, txt_status;
        LinearLayout layout_read;
        ImageView ivThumb;
        SimpleRatingBar simpleRatingBar;

        public MyViewHolder(View view) {
            super(view);
            txt_book = view.findViewById(R.id.txt_book);
            txt_description = view.findViewById(R.id.txt_description);
            txt_book_author = view.findViewById(R.id.txt_book_author);
            txt_category = view.findViewById(R.id.txt_category);
            txt_price = view.findViewById(R.id.txt_price);
            txt_status = view.findViewById(R.id.txt_status);
            ivThumb = view.findViewById(R.id.ivThumb);
            simpleRatingBar = view.findViewById(R.id.ratingbar);
            layout_read = view.findViewById(R.id.layout_read);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        holder.txt_book.setText("" + magazineList.get(position).getTitle());
        holder.txt_description.setText(Html.fromHtml(magazineList.get(position).getDescription()));
        holder.txt_book_author.setText(mcontext.getResources().getString(R.string.by) + " " + magazineList.get(position).getAuthorName());
        holder.txt_category.setText("" + magazineList.get(position).getCategoryName());

        if (magazineList.get(position).getIsPaid().equalsIgnoreCase("1")) {
            holder.txt_price.setText(currency_symbol + " " + magazineList.get(position).getPrice());
        } else {
            holder.txt_price.setText("" + mcontext.getResources().getString(R.string.free));
        }

        if (!TextUtils.isEmpty(magazineList.get(position).getImage()))
            Picasso.get().load(magazineList.get(position).getImage()).into(holder.ivThumb);

        holder.ivThumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("click", "call");
                Intent intent = new Intent(mcontext, MagazineDetails.class);
                intent.putExtra("docID", magazineList.get(position).getId());
                intent.putExtra("authorID", "" + magazineList.get(position).getAuthorId());
                mcontext.startActivity(intent);
            }
        });

        holder.layout_read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("=>pos", "" + position);
                //itemClick.OnClick(magazineList.get(position).getId(), position);
            }
        });

    }

    public void addBook(List<Result> items) {
        this.magazineList.addAll(items);
        Log.e("magazineList", "" + magazineList.size());
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return magazineList.size();
    }

}