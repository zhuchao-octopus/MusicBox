package com.tp.activities;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;

import com.musicbox.R;
import com.tp.adapter.AppAdapter;
import com.tp.data.App;
import com.musicbox.databinding.ActivityMyApplicationBinding;
import com.tp.utils.AppHandler;
import com.tp.utils.PageType;
import com.tp.utils.Utils;

/**
 * Created by Oracle on 2017/12/1.
 */

public class AppsActivity extends AppCompatActivity {

    private static final String TAG = AppsActivity.class.getSimpleName();
    private ActivityMyApplicationBinding binding;
    private AppHandler appHandler;
    private AppAdapter appAdapter;
    private PageType pageType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_application);
        String pageTypeStr = getIntent().getStringExtra("type");
        pageType = TextUtils.isEmpty(pageTypeStr) ?
                PageType.MY_APP_TYPE : PageType.valueOf(pageTypeStr);
        appHandler = new AppHandler(AppsActivity.this, pageType);
        appAdapter = new AppAdapter(this);
        appHandler.setOnScanListener(new AppHandler.OnScanListener() {
            @Override
            public void onResponse(SparseArray<App> apps) {
                appAdapter.setApps(apps);
                binding.allapps.setAdapter(appAdapter);
            }
        });
        binding.allapps.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                App app = (App) appAdapter.getItem(position);
                if (app != null) {
                    String packageName = app.getPackageName();
                    if (!TextUtils.isEmpty(packageName)) {
                        appHandler.launchApp(packageName);
                    }
                }
            }
        });
        binding.allapps.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                App app = (App) appAdapter.getItem(position);
                if (app != null) {
                    String packageName = app.getPackageName();
                    if (!TextUtils.isEmpty(packageName)) {
                        Utils.uninstallApp(AppsActivity.this, packageName);
                    }
                }
                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        switch (pageType) {
            case RECENT_TYPE:
                appHandler.scanRecent();
                break;
            case MY_APP_TYPE:
                appHandler.scan();
                break;
        }
        appHandler.regAppReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        appHandler.unRegAppReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        appHandler.release();
        appHandler.setOnScanListener(null);
        appAdapter.release();
        appHandler = null;
        appAdapter = null;
        binding = null;
    }

    public static void lunchAppsActivity(Context context, PageType pageType) {
        Intent intent = new Intent(context, AppsActivity.class);
        intent.putExtra("type", pageType.name());
        context.startActivity(intent);
    }
}
