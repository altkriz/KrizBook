package com.kriztech.book.Fragment;

import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.kriztech.book.R;
import com.kriztech.book.Utility.Constant;
import com.kriztech.book.Utility.PrefManager;

public class AuthorInfo extends Fragment {

    private static final String TAG = AuthorInfo.class.getSimpleName();
    private PrefManager prefManager;
    private TextView txtAboutAuthor;

    public AuthorInfo() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.subfragment_authorinfo, container, false);
        PrefManager.forceRTLIfSupported(getActivity().getWindow(), getActivity());
        prefManager = new PrefManager(getActivity());

        Log.e(TAG, "authorInfo ==>>> " + Constant.authorInfo);

        txtAboutAuthor = root.findViewById(R.id.txtAboutAuthor);
        txtAboutAuthor.setText(Html.fromHtml(Constant.authorInfo));

        return root;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}