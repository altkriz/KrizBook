package com.kriztech.book.Adapter;

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
import com.kriztech.book.Activity.MagazineDetails;
import com.kriztech.book.Model.DownloadModel.Result;
import com.kriztech.book.R;
import com.kriztech.book.Interface.ItemClick;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MyPurchaseAdapter extends RecyclerView.Adapter<MyPurchaseAdapter.MyViewHolder> {

    private List<Result> downloadList;
    private Context mcontext;
    private String from, where, currency_symbol;
    private ItemClick itemClick;

    public MyPurchaseAdapter(Context context, List<Result> downloadList, String from, String where, ItemClick itemClick, String currency_symbol) {
        this.downloadList = downloadList;
        this.mcontext = context;
        this.from = from;
        this.where = where;
        this.itemClick = itemClick;
        this.currency_symbol = currency_symbol;
        Log.e("=>where", "" + where);
        Log.e("=>from", "" + from);
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
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_purchase_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        holder.txt_book.setText("" + downloadList.get(position).getTitle());
        holder.txt_book_author.setText(mcontext.getResources().getString(R.string.by) + " " + downloadList.get(position).getAuthorName());
        holder.txt_category.setText("" + downloadList.get(position).getCategoryName());
        holder.txt_price.setText(currency_symbol + " " + downloadList.get(position).getPrice());
        holder.simpleRatingBar.setRating(Float.parseFloat(downloadList.get(position).getAvgRating()));
        holder.txt_status.setText("" + mcontext.getResources().getString(R.string.brought));
        holder.txt_description.setText(Html.fromHtml(downloadList.get(position).getDescription()));
        holder.txt_description.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(downloadList.get(position).getImage()))
            Picasso.get().load(downloadList.get(position).getImage()).into(holder.ivThumb);

        holder.ivThumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                if (from.equalsIgnoreCase("Books")) {
                    intent = new Intent(mcontext, BookDetails.class);
                } else {
                    intent = new Intent(mcontext, MagazineDetails.class);
                }
                intent.putExtra("docID", "" + downloadList.get(position).getId());
                intent.putExtra("authorID", "" + downloadList.get(position).getAuthorId());
                mcontext.startActivity(intent);
            }
        });

        holder.layout_read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemClick.OnClick("" + downloadList.get(position).getId(), position);
            }
        });

    }

    public void addBook(List<Result> items) {
        this.downloadList.addAll(items);
        Log.e("downloadList", "" + downloadList.size());
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return downloadList.size();
    }

}