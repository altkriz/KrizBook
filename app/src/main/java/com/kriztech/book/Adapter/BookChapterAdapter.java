package com.kriztech.book.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.kriztech.book.Model.BookModel.BookChapter;
import com.kriztech.book.R;
import com.kriztech.book.Interface.ItemClickListener;

import java.util.List;

public class BookChapterAdapter extends RecyclerView.Adapter<BookChapterAdapter.MyViewHolder> {

    private List<BookChapter> BookChapterList;
    Context mcontext;
    private String currencySymbol;
    ItemClickListener itemClick;

    public BookChapterAdapter(Context context, List<BookChapter> BookChapterList, String currencySymbol, ItemClickListener itemClick) {
        this.mcontext = context;
        this.BookChapterList = BookChapterList;
        this.currencySymbol = currencySymbol;
        this.itemClick = itemClick;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txtChapterTitle, txtPrice;
        LinearLayout lyChapter, lyUnlock, lyPriceLock, lyReadNow;

        public MyViewHolder(View view) {
            super(view);
            txtChapterTitle = view.findViewById(R.id.txtChapterTitle);
            txtPrice = view.findViewById(R.id.txtPrice);
            lyChapter = view.findViewById(R.id.lyChapter);
            lyUnlock = view.findViewById(R.id.lyUnlock);
            lyPriceLock = view.findViewById(R.id.lyPriceLock);
            lyReadNow = view.findViewById(R.id.lyReadNow);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chapter_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        holder.txtChapterTitle.setText("" + BookChapterList.get(position).getTitle());

        if (BookChapterList.get(position).getIsBuy() == 0 && Integer.parseInt(BookChapterList.get(position).getPrice()) > 0) {
            holder.lyReadNow.setVisibility(View.GONE);
            holder.lyPriceLock.setVisibility(View.VISIBLE);
            holder.lyUnlock.setVisibility(View.VISIBLE);
            holder.txtPrice.setVisibility(View.VISIBLE);
            holder.txtPrice.setText(currencySymbol + "" + BookChapterList.get(position).getPrice());
        } else {
            holder.lyReadNow.setVisibility(View.VISIBLE);
            holder.lyPriceLock.setVisibility(View.GONE);
            holder.lyUnlock.setVisibility(View.INVISIBLE);
            holder.txtPrice.setVisibility(View.INVISIBLE);
        }

        holder.lyChapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("click", "" + position);
                itemClick.onItemClick(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return BookChapterList.size();
    }

}