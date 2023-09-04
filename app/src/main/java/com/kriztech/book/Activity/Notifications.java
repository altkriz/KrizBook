package com.kriztech.book.Activity;

import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.kriztech.book.Adapter.NotificationAdapter;
import com.kriztech.book.Model.NotificationModel.NotificationModel;
import com.kriztech.book.Model.NotificationModel.Result;
import com.kriztech.book.Model.SuccessModel.SuccessModel;
import com.kriztech.book.R;
import com.kriztech.book.Utility.PrefManager;
import com.kriztech.book.Utility.Utils;
import com.kriztech.book.Webservice.BaseURL;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Notifications extends AppCompatActivity {

    private PrefManager prefManager;

    private ShimmerFrameLayout shimmer;
    private RelativeLayout rlAdView;
    private LinearLayout lyBack, lyToolbar, lyRefresh, lyNoData, lyRecycler, lyFbAdView;
    private TextView txtToolbarTitle;

    private RecyclerView rvNotification;
    private List<Result> notificationList;
    private NotificationAdapter notificationAdapter;

    private com.facebook.ads.AdView fbAdView = null;
    private AdView mAdView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setTheme(Notifications.this);
        setContentView(R.layout.activity_notification);
        PrefManager.forceRTLIfSupported(getWindow(), Notifications.this);

        init();
        txtToolbarTitle.setText("" + getString(R.string.notifications));

        lyBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        lyRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!prefManager.getLoginId().equalsIgnoreCase("0")) {
                    GetNotification();
                } else {
                    lyToolbar.setVisibility(View.VISIBLE);
                    lyRecycler.setVisibility(View.GONE);
                    lyNoData.setVisibility(View.VISIBLE);
                    Utils.shimmerHide(shimmer);
                }
            }
        });

    }

    private void init() {
        try {
            prefManager = new PrefManager(Notifications.this);

            shimmer = findViewById(R.id.shimmer);
            lyToolbar = findViewById(R.id.lyToolbar);
            txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
            lyBack = findViewById(R.id.lyBack);
            lyRefresh = findViewById(R.id.lyRefresh);

            rlAdView = findViewById(R.id.rlAdView);
            lyFbAdView = findViewById(R.id.lyFbAdView);

            lyNoData = findViewById(R.id.lyNoData);
            lyRecycler = findViewById(R.id.lyRecycler);
            rvNotification = findViewById(R.id.rvNotification);
        } catch (Exception e) {
            Log.e("init", "Exception => " + e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!prefManager.getLoginId().equalsIgnoreCase("0")) {
            GetNotification();
        } else {
            lyToolbar.setVisibility(View.VISIBLE);
            lyRecycler.setVisibility(View.GONE);
            lyNoData.setVisibility(View.VISIBLE);
            Utils.shimmerHide(shimmer);
        }

        AdInit();
    }

    private void AdInit() {
        Log.e("banner_ad", "" + prefManager.getValue("banner_ad"));
        if (prefManager.getValue("banner_ad").equalsIgnoreCase("yes")) {
            rlAdView.setVisibility(View.VISIBLE);
            Utils.Admob(Notifications.this, mAdView, prefManager.getValue("banner_adid"), rlAdView);
        } else {
            rlAdView.setVisibility(View.GONE);
        }

        Log.e("fb_banner_status", "" + prefManager.getValue("fb_banner_status"));
        if (prefManager.getValue("fb_banner_status").equalsIgnoreCase("on")) {
            lyFbAdView.setVisibility(View.VISIBLE);
            Utils.FacebookBannerAd(Notifications.this, fbAdView, "" + prefManager.getValue("fb_banner_id"), lyFbAdView);
        } else {
            lyFbAdView.setVisibility(View.GONE);
        }
    }

    //get_notification API
    private void GetNotification() {
        Utils.shimmerShow(shimmer);

        Call<NotificationModel> call = BaseURL.getVideoAPI().get_notification("" + prefManager.getLoginId());
        call.enqueue(new Callback<NotificationModel>() {
            @Override
            public void onResponse(@NonNull Call<NotificationModel> call, @NonNull Response<NotificationModel> response) {
                try {
                    Log.e("get_notification", "status => " + response.body().getStatus());
                    if (response.code() == 200 && response.body().getStatus() == 200) {

                        if (response.body().getResult().size() > 0) {
                            notificationList = new ArrayList<>();
                            notificationList = response.body().getResult();
                            Log.e("notificationList", "size => " + notificationList.size());

                            SetNotification();

                            lyRecycler.setVisibility(View.VISIBLE);
                            lyNoData.setVisibility(View.GONE);
                        } else {
                            lyRecycler.setVisibility(View.GONE);
                            lyNoData.setVisibility(View.VISIBLE);
                        }

                    } else {
                        Log.e("get_notification", "message => " + response.body().getStatus());
                        lyRecycler.setVisibility(View.GONE);
                        lyNoData.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    Log.e("get_notification", "Exception => " + e);
                }
                Utils.shimmerHide(shimmer);
            }

            @Override
            public void onFailure(@NonNull Call<NotificationModel> call, @NonNull Throwable t) {
                Utils.shimmerHide(shimmer);
                lyRecycler.setVisibility(View.GONE);
                lyNoData.setVisibility(View.VISIBLE);
                Log.e("get_notification", "That didn't work!!! => " + t.getMessage());
            }
        });
    }

    private void SetNotification() {

        notificationAdapter = new NotificationAdapter(Notifications.this, notificationList);
        rvNotification.setLayoutManager(new GridLayoutManager(Notifications.this, 1));
        rvNotification.setAdapter(notificationAdapter);
        notificationAdapter.notifyDataSetChanged();

        // Create and add a callback
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                Log.e("=>pos", "" + viewHolder.getAdapterPosition());
                ReadNotification(notificationList.get(viewHolder.getAdapterPosition()).getId());
            }

            // You must use @RecyclerViewSwipeDecorator inside the onChildDraw method
            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(Notifications.this, R.color.greenColor))
                        .addSwipeLeftActionIcon(R.drawable.ic_checked)
                        .addSwipeRightBackgroundColor(ContextCompat.getColor(Notifications.this, R.color.greenColor))
                        .addSwipeRightActionIcon(R.drawable.ic_checked)
                        .addSwipeRightLabel(getResources().getString(R.string.marked_as_read))
                        .setSwipeRightLabelColor(getResources().getColor(R.color.text_white))
                        .addSwipeLeftLabel(getResources().getString(R.string.marked_as_read))
                        .setSwipeLeftLabelColor(getResources().getColor(R.color.text_white))
                        .create()
                        .decorate();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(rvNotification);
    }

    //read_notification API
    private void ReadNotification(String ID) {

        Call<SuccessModel> call = BaseURL.getVideoAPI().read_notification("" + prefManager.getLoginId(), "" + ID);
        call.enqueue(new Callback<SuccessModel>() {
            @Override
            public void onResponse(@NonNull Call<SuccessModel> call, @NonNull Response<SuccessModel> response) {
                try {
                    if (response.code() == 200 && response.body().getStatus() == 200) {
                        Log.e("read_notification", "status => " + response.body().getStatus());

                        GetNotification();

                    } else {
                        Log.e("read_notification", "message => " + response.body().getMessage());
                    }
                } catch (Exception e) {
                    Log.e("read_notification", "Exception => " + e);
                }
            }

            @Override
            public void onFailure(@NonNull Call<SuccessModel> call, @NonNull Throwable t) {
                Log.e("read_notification", "That didn't work!!! => " + t.getMessage());
            }
        });
    }

    @Override
    public void onPause() {
        Utils.shimmerHide(shimmer);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Utils.shimmerHide(shimmer);
        if (mAdView != null) {
            mAdView.destroy();
        }
        if (fbAdView != null) {
            fbAdView.destroy();
        }
        super.onDestroy();
    }

}