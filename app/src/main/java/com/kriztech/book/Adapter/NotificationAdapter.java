package com.kriztech.book.Adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kriztech.book.Model.NotificationModel.Result;
import com.kriztech.book.R;
import com.kriztech.book.Utility.Utils;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.MyViewHolder> {

    Context context;
    List<Result> notificationList;

    public NotificationAdapter(Context context, List<Result> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout lyNotification, lyImage;
        TextView txtTitle, txtDescription, txtDate;
        RoundedImageView ivThumb;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            lyNotification = itemView.findViewById(R.id.lyNotification);
            lyImage = itemView.findViewById(R.id.lyImage);
            ivThumb = itemView.findViewById(R.id.ivThumb);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtDate = itemView.findViewById(R.id.txtDate);
        }
    }

    @NonNull
    @Override
    public NotificationAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_items, parent, false);

        NotificationAdapter.MyViewHolder viewHolder = new NotificationAdapter.MyViewHolder(itemLayoutView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.MyViewHolder holder, int position) {
        holder.txtTitle.setText("" + notificationList.get(position).getHeadings());
        holder.txtDescription.setText("" + notificationList.get(position).getContents());
        holder.txtDate.setText("" + Utils.DateFormat2(notificationList.get(position).getCreatedAt()));

        if (!TextUtils.isEmpty(notificationList.get(position).getBigPicture())) {
            holder.lyImage.setVisibility(View.VISIBLE);
            Picasso.get().load(notificationList.get(position).getBigPicture()).into(holder.ivThumb);
        } else {
            holder.lyImage.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

}