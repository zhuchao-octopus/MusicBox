package com.tp.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;


import com.musicbox.databinding.ActivityMainBinding;
import com.tp.adapter.KeyboardAdapter;
import com.tp.adapter.SearchAdapter;
import com.tp.adapter.SelectedAdapter;
import com.tp.bridge.SelEffectBridge;
import com.tp.data.json.regoem.CheckMacBean;
import com.tp.data.json.regoem.IpBean;
import com.tp.data.json.regoem.Recommend3Bean;
import com.tp.data.json.regoem.RecommendBean;
import com.tp.data.json.regoem.RecommendbgBean;
import com.tp.data.json.regoem.RecommendlogoBean;
import com.tp.data.json.regoem.RecommendmarqueeBean;
import com.tp.data.json.regoem.RecommendversionBean;
import com.tp.data.json.regoem.RemoveAppBean;
import com.tp.listener.FilterListener;
import com.tp.utils.AppHandler;
import com.tp.utils.AppManager;
import com.tp.utils.HttpUtils;
import com.tp.utils.NetTool;
import com.tp.utils.TimeHandler;
import com.tp.utils.Utils;
import com.tp.views.dialogs.BottomAppDialog;
import com.tp.views.dialogs.HomeAppDialog;
import com.tp.views.dialogs.HomeAppsDialog;
import com.google.gson.Gson;
import com.musicbox.BuildConfig;
import com.musicbox.R;

import org.jsoup.helper.StringUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.vov.vitamio.MediaPlayer;
import me.jessyan.progressmanager.ProgressListener;
import me.jessyan.progressmanager.ProgressManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;



/**
 */
public class MainActivity extends AppCompatActivity implements View.OnFocusChangeListener,
        View.OnClickListener, View.OnKeyListener {

    private static final String TAG = "musicbox/Main";

    private ActivityMainBinding binding;
    private SelEffectBridge selEffectBridge;
    public AppHandler appHandler;

    private Context mContext;
    private String language = null;
    private String newKey = null;

    //所有的视频文件的集合
    private List<String> vList = new ArrayList<>();
    //选中的视频文件的集合
    private List<String> sList = new ArrayList<>();
    //优先播放的集合
//    List<String> list = new ArrayList<>();
    //U盘的路径
    private String pathsss = null;

    private boolean isPause = true;
    private boolean isVisibility = false;
    private boolean isVisibilitys = false;

    private SearchAdapter sAdapter;
    private SelectedAdapter seAdapter;
    private String folderNameS = "/pick/";
    private List<String> inputList = new ArrayList<>();
    private int seleIndex = -2;
    private boolean isClicked = false;
    private boolean isRepetition;
    private boolean isAccompany = false;
    private int key;
    private boolean isDown = false;
    private boolean isDowns = false;
    private int order = 0;
    private boolean isFailVerification = false;
    private Thread mThread = null;

    //加载推荐app成功
    private boolean isCheckVersion = false;

    private KeyboardAdapter kba;
    List<Drawable> lists;
    List<Drawable> slist;

    private String deviceId = "750";//TVBOX 天谱
    private String host = "http://www.gztpapp.cn:8976/";    //天谱  （节流）8976
    private String appName = "TPMUSIC";      //天谱
    private String lunchname = "TPMUSIC";
//    int cid = -1 ;    //客户号  (国内)

    String netMac;   //设备的网络mac地址
    String cidIP ;   //设备的IP
    String region ; //设备所在地区


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //检查vitamio框架是否可用
        if (!io.vov.vitamio.LibsChecker.checkVitamioLibs(this))
            return;

        mContext = this;
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        dialog();
        registerUDiskReceiver();
        try {
            scan();
        } catch (IOException e) {
            e.printStackTrace();
        }

        binding.flSetting.setOnKeyListener(this);
        binding.flSetting.setOnClickListener(this);
        binding.flSetting.setOnFocusChangeListener(this);
        binding.flSearch.setOnKeyListener(this);
        binding.flSearch.setOnClickListener(this);
        binding.flSearch.setOnFocusChangeListener(this);
        binding.flSelected.setOnKeyListener(this);
        binding.flSelected.setOnClickListener(this);
        binding.flSelected.setOnFocusChangeListener(this);
        binding.flPause.setOnKeyListener(this);
        binding.flPause.setOnClickListener(this);
        binding.flPause.setOnFocusChangeListener(this);
        binding.flNexts.setOnKeyListener(this);
        binding.flNexts.setOnClickListener(this);
        binding.flNexts.setOnFocusChangeListener(this);
        binding.flRepeat.setOnKeyListener(this);
        binding.flRepeat.setOnClickListener(this);
        binding.flRepeat.setOnFocusChangeListener(this);
        binding.flTrack.setOnKeyListener(this);
        binding.flTrack.setOnClickListener(this);
        binding.flTrack.setOnFocusChangeListener(this);


        selEffectBridge = (SelEffectBridge) binding.mainUpView.getEffectBridge();
        selEffectBridge.setUpRectResource(R.drawable.home_sel_btn);
        binding.llTop.getViewTreeObserver().addOnGlobalFocusChangeListener(
                new ViewTreeObserver.OnGlobalFocusChangeListener() {
                    @Override
                    public void onGlobalFocusChanged(View oldFocus, final View newFocus) {
//                        Log.e(TAG, "onGlobalFocusChanged " + newFocus + " " + oldFocus);

                        if (newFocus == null){
                            return;
                        }
                        int focusVId = newFocus.getId();
                        switch (focusVId) {
                            case R.id.fl_search:
                            case R.id.fl_selected:
                            case R.id.fl_pause:
                            case R.id.fl_nexts:
                            case R.id.fl_repeat:
                            case R.id.fl_track:
                            case R.id.fl_setting:
                                selEffectBridge.setVisibleWidget(false);
                                binding.mainUpView.setFocusView(newFocus, oldFocus, 0f);
                                newFocus.bringToFront();
                                break;
                        }
                    }
                });
        kba = new KeyboardAdapter(this);


        String mac = Utils.getDevID().toUpperCase();
        NetTool.setMac(mac);

        initView();

        //add start apk  com.example.show/.MainActivity
        //开机自启动指定app（"com.example.show"）
//        statMActivity(MainActivity.this, "com.h3launcher", "com.globalhome.activities.LanguageActivity");

        startJump(mContext,LanguageActivity.class,false);
        Log.d(TAG,"onCreate");
//        String n = getEthernetMacAddress();
//        String w = getWiFiMacAddress(mContext);
//        if (!"".equals(n) && null != n) {
//            netMac = n;
//        }else if (!"".equals(w) && null != w) {
//            netMac = w;
//        }
//        getRecommendIP();
    }

    private void startJump(Context context,Class cls,boolean b) {
        Intent intent = new Intent(context,cls);
        if (b) {
            intent.putExtra("again", "again");
        }else {
            intent.putExtra("again", "first");
        }
        startActivityForResult(intent,888);
    }


    private void fiveSecondGone(){
        mThread = new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    sleep(5000);
                    //无移动5秒后隐藏
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //这里执行要隐藏的代码
                            DView();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    /**
     * 视频广告播放器
     */

    private void advertisementVideo(final String u, final int p) {
        /**
         * 网络播放
         */
        binding.vv.setVideoURI(Uri.parse(u));
        binding.vv.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                switch (what){
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                        String m = binding.vv.getDuration()+"";
                        binding.progressBar.setMax(Integer.parseInt(m));
                        binding.vv.pause();
                        break;
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                        binding.vv.start();
                        break;
                    case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String c = binding.vv.getCurrentPosition()+"";
                                binding.progressBar.setProgress(Integer.parseInt(c));
                            }
                        });
                        break;
                }
                return true;
            }
        });
//        binding.vv.start();
        if (u.endsWith(".mp3")){
            binding.ivMp3.setVisibility(View.VISIBLE);
        }else {
            binding.ivMp3.setVisibility(View.GONE);
        }

            //显示正在播放的歌名
        String s = u.replace(".mp4","").replace(".mp3","").replace(".avi","")
                .replace(".mpg","").replace(".dat","").replace(".mkv","")
                .replace(".vob","").replace(".wmv","").replace(".mov","");
        s = s.substring(s.lastIndexOf("/")+1);
        binding.tvSingingname.setText(s);

            //正在播放的，移出播放列表
            if (sList.size() > 0) {
                if (p == -1) {
                    sList.remove(0);
                } else if (p == -2) {

                } else {
                    sList.remove(p);
                }
                if (seAdapter != null) {
                    seAdapter.notifyDataSetInvalidated();
                }
                binding.tvCount.setText(sList.size() + "");
            }
            //显示下一首
            if (sList.size() != 0) {
                String next = sList.get(0).replace(".mp4","").replace(".mp3","").replace(".avi","")
                        .replace(".mpg","").replace(".dat","").replace(".mkv","")
                        .replace(".vob","").replace(".wmv","").replace(".mov","");
                next = next.substring(next.lastIndexOf("/")+1);
                binding.tvNextname.setText(next);
            } else {
                binding.tvNextname.setText("");
            }
            binding.vv.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (sList.size() == 0) {
                        if (p == -2) {
                            /**
                             * 刚启动APP时播放的list的歌曲
                             */
                            //  list有歌曲时，顺序循环播放
//                            if (list.size() != 0) {
//                                order++;
//                                if (order >= list.size()) {
//                                    order = 0;
//                                }
//                                advertisementVideo(list.get(order), -2);
//                            }
                        }else if (p == -1){
                            /**
                             *  刚启动APP时播放的vList的歌曲、播放的是sList的歌曲
                             */
                            //  sList没有歌曲时，循环播放当前歌曲
                            advertisementVideo(u, -1);
                        }
                    }else {
                        //sList有歌曲时，按顺序播放
                        advertisementVideo(sList.get(0), -1);
                    }
                }
            });
    }

      //开机自启动
//    private void  statMActivity(Context ctx,String packname,String classname) {
//         Intent i = new Intent();
//         i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//         ComponentName cn = new ComponentName(packname,classname);
//         i.setComponent(cn);
//         ctx.startActivity(i);
//    }

    /**
     * 初始化布局
     */
    //隐藏全部布局
    private void initView() {
         binding.llTop.setVisibility(View.GONE);
         binding.rlAll.setVisibility(View.GONE);
         binding.llBottom.setVisibility(View.GONE);
         binding.flSetting.setVisibility(View.GONE);
         binding.flPause.setFocusable(false);
         binding.flSearch.setFocusable(false);
    }
    //显示列表布局
    private void vView(){
        binding.rlAll.setVisibility(View.VISIBLE);
        binding.rlAll.getBackground().setAlpha(200);
        searchList();
    }
    //隐藏列表布局
    private void VView(){
        binding.rlAll.setVisibility(View.GONE);
        binding.flSearch.setFocusable(false);
        if (binding.llBottom.getVisibility() == View.VISIBLE){
            binding.flPause.requestFocus();
        }
    }

    //显示控制栏布局
    private void dView(){
        binding.llTop.setVisibility(View.VISIBLE);
        binding.llBottom.setVisibility(View.VISIBLE);
        binding.flSetting.setVisibility(View.VISIBLE);
    }

    //隐藏控制栏布局
    private void DView(){
        binding.llTop.setVisibility(View.GONE);
        binding.llBottom.setVisibility(View.GONE);
        binding.flSetting.setVisibility(View.GONE);
        binding.flPause.setFocusable(false);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 888 && resultCode == 777) {
            String languages = data.getStringExtra("777");
            if (!"".equals(languages) && null != languages) {
                language = languages;
                newKey = language;
                if (languages.equals("burmese")) {
                    binding.tvSinging.setText("လက်ရှိသီချင်း:");
                    binding.tvNext.setText("ကိုသီချင်း:");
                    binding.tvSearch.setText("ရှာရန်စာရင်း");
                    binding.tvSelected.setText("သီချင်းများ");
                    binding.tvPause.setText("ခေတ္တရပ်");
                    binding.tvNexts.setText("ကိုသီချင်း");
                    binding.tvRepeat.setText("ပြန်ဖွင့်");
                    binding.tvTrack.setText("အသံပူးတှဲ");
                } else if (languages.equals("cn")) {
                    binding.tvSinging.setText("当前歌曲：");
                    binding.tvNext.setText("下一曲：");
                    binding.tvSearch.setText("搜索列表");
                    binding.tvSelected.setText("已选歌曲");
                    binding.tvPause.setText("暂停");
                    binding.tvNexts.setText("下一曲");
                    binding.tvRepeat.setText("重唱");
                    binding.tvTrack.setText("伴唱");
                } else if (languages.equals("en")) {
                    binding.tvSinging.setText("Singing:");
                    binding.tvNext.setText("Next:");
                    binding.tvSearch.setText("Search List");
                    binding.tvSelected.setText("Selected List");
                    binding.tvPause.setText("Pause");
                    binding.tvNexts.setText("Next");
                    binding.tvRepeat.setText("Repeat");
                    binding.tvTrack.setText("Track");
                }
                inputList.clear();
                binding.tvInput.setText("");
                settings(language);
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.vv.pause();
        unregisterReceiver(mOtgReceiver);
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mOtgReceiver, usbDeviceStateFilter);
        Log.d(TAG, "onResume");
        Log.d(TAG,"launcher is resume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        Log.d(TAG,"launcher was killed");

    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
//        Log.e(TAG, "onFocusChange " + v);
        switch (v.getId()) {
            case R.id.fl_setting: {
                kba.setSelectedIndex(-2);//字母表格无焦点
                gone();
                binding.ivSelected.setImageResource(R.drawable.u51);
                binding.ivSearch.setImageResource(R.drawable.u51);

                selEffectBridge.setVisibleWidget(true);
                float scale = hasFocus ? 1.0f : 1.0f;
                binding.ivSetting.setImageResource(
                        hasFocus ? R.drawable.u24_mouseover: R.drawable.u24);
                binding.ivSetting.animate().scaleX(scale).scaleY(scale).start();
                return;
            }
            case R.id.fl_pause: {
                kba.setSelectedIndex(-2);
                if (sAdapter != null) {
                    sAdapter.setSelectedIndex(-2, false);
                }
                if (seAdapter != null) {
                    seAdapter.setSelectedIndex(-2, -2);
                }
                binding.ivSelected.setImageResource(R.drawable.u51);
                binding.ivSearch.setImageResource(R.drawable.u51);

                selEffectBridge.setVisibleWidget(true);
                float scale = hasFocus ? 1.0f : 1.0f;
                if (!isPause){
                    binding.ivPause.setImageResource(
                            hasFocus ? R.drawable.play1 : R.drawable.play2);
                }else {
                    binding.ivPause.setImageResource(
                            hasFocus ? R.drawable.u31_mouseover : R.drawable.u31);
                }
                binding.tvPause.setTextColor(hasFocus ? Color.parseColor("#F08516") : Color.parseColor("#FFFFFF"));
                binding.ivSetting.animate().scaleX(scale).scaleY(scale).start();
                return;
            }
            case R.id.fl_nexts: {
                kba.setSelectedIndex(-2);
                if (sAdapter != null) {
                    sAdapter.setSelectedIndex(-2, false);
                }
                if (seAdapter != null) {
                    seAdapter.setSelectedIndex(-2, -2);
                }
                binding.ivSelected.setImageResource(R.drawable.u51);
                binding.ivSearch.setImageResource(R.drawable.u51);

                selEffectBridge.setVisibleWidget(true);
                float scale = hasFocus ? 1.0f : 1.0f;
                binding.ivNexts.setImageResource(
                        hasFocus ? R.drawable.u28_mouseover: R.drawable.u28);
                binding.tvNexts.setTextColor(hasFocus ? Color.parseColor("#F08516") : Color.parseColor("#FFFFFF"));
                binding.ivSetting.animate().scaleX(scale).scaleY(scale).start();
                return;
            }
            case R.id.fl_repeat: {
                kba.setSelectedIndex(-2);
                if (sAdapter != null) {
                    sAdapter.setSelectedIndex(-2, false);
                }
                if (seAdapter != null) {
                    seAdapter.setSelectedIndex(-2, -2);
                }
                binding.ivSelected.setImageResource(R.drawable.u51);
                binding.ivSearch.setImageResource(R.drawable.u51);

                selEffectBridge.setVisibleWidget(true);
                float scale = hasFocus ? 1.0f : 1.0f;
                binding.ivRepeat.setImageResource(
                        hasFocus ? R.drawable.u29_mouseover: R.drawable.u29);
                binding.tvRepeat.setTextColor(hasFocus ? Color.parseColor("#F08516") : Color.parseColor("#FFFFFF"));
                binding.ivSetting.animate().scaleX(scale).scaleY(scale).start();
                return;
            }
            case R.id.fl_track: {
                kba.setSelectedIndex(-2);
                if (sAdapter != null) {
                    sAdapter.setSelectedIndex(-2, false);
                }
                if (seAdapter != null) {
                    seAdapter.setSelectedIndex(-2, -2);
                }
                binding.ivSelected.setImageResource(R.drawable.u51);
                binding.ivSearch.setImageResource(R.drawable.u51);

                selEffectBridge.setVisibleWidget(true);
                float scale = hasFocus ? 1.0f : 1.0f;
                binding.ivTrack.setImageResource(
                        hasFocus ? R.drawable.u33_mouseover: R.drawable.u33);
                binding.tvTrack.setTextColor(hasFocus ? Color.parseColor("#F08516") : Color.parseColor("#FFFFFF"));
                binding.ivSetting.animate().scaleX(scale).scaleY(scale).start();
                return;
            }
            case R.id.fl_search: {
                kba.setSelectedIndex(-2);
                binding.ivSelected.setImageResource(R.drawable.u51);

                if (!isDown) {
                    selEffectBridge.setVisibleWidget(true);
                    float scale = hasFocus ? 1.0f : 1.0f;
                    binding.ivSearch.setImageResource(
                            hasFocus ? R.drawable.u51_mouseover : R.drawable.u51);
                    binding.ivSetting.animate().scaleX(scale).scaleY(scale).start();
                }
                searchList();
                return;
            }
            case R.id.fl_selected: {
                kba.setSelectedIndex(-2);
                binding.ivSearch.setImageResource(R.drawable.u51);

                if (!isDowns) {
                    selEffectBridge.setVisibleWidget(true);
                    float scale = hasFocus ? 1.0f : 1.0f;
                    binding.ivSelected.setImageResource(
                            hasFocus ? R.drawable.u51_mouseover : R.drawable.u51);
                    binding.ivSetting.animate().scaleX(scale).scaleY(scale).start();
                }
                selectedList();
                return;
            }
        }
    }
    @Override
    public void onClick(View v) {
        handleViewKey(v, -1, true);
    }

    private void gone(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    sleep(180);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (sAdapter != null) {
                                sAdapter.setSelectedIndex(-2, false);
                            }
                            if (seAdapter != null) {
                                seAdapter.setSelectedIndex(-2, -2);
                            }
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void launchApp(String packageName) {
        appHandler.launchApp(packageName);
    }

    /**
     * 处理点击事件、菜单键
     *
     * @param v
     * @param keyCode
     * @param isClick true:点击 false:菜单
     */
    private void handleViewKey(View v, int keyCode, boolean isClick) {
        int id = v.getId();
        switch (id) {
            case R.id.fl_setting: {
                startJump(mContext,LanguageActivity.class,true);
                return;
            }
            case R.id.fl_pause: {
                pause();
                return;
            }
            case R.id.fl_nexts: {
                //下一首
                next();
                return;
            }
            case R.id.fl_repeat: {
                //重唱
                repeat();
                return;
            }
            case R.id.fl_track: {
                //伴唱
                if (isAccompany){
                    key = 1;
                }else {
                    key = 2;
                }
                if (binding.vv != null) {
                    switchTrack();
                }
                return;
            }
        }
    }

    /**
     * 菜单
     */
    private void menu(){
        if (binding.llTop.getVisibility() == View.GONE) {
            dView();
            binding.flPause.setFocusable(true);
            binding.flPause.requestFocus();

        }else if (binding.llTop.getVisibility() == View.VISIBLE){
            DView();
        }
    }

    /**
     * 搜歌
     */
    private void souge(){
        int ppp =  binding.llTop.getVisibility();
        int pp = binding.rlAll.getVisibility();
        binding.tvInput.setText("");
        inputList.clear();
        if (binding.rlAll.getVisibility() == View.GONE) {
            vView();
            binding.flSearch.setFocusable(true);
            if (ppp == 0 && pp == 0){

            }else {
                binding.flSearch.requestFocus();
            }
        }else if (binding.rlAll.getVisibility() == View.VISIBLE){
            VView();
        }
    }

    /**
     * 下一首
     */
    private void next(){
        isPause = true;
        binding.flPause.setFocusable(true);
        binding.ivTrack.setImageResource(R.drawable.u33);
        binding.tvTrack.setTextColor(Color.parseColor("#FFFFFF"));
        if (sList.size() != 0){
            advertisementVideo(sList.get(0),-1);
        }else {
            if (language != null) {
                if (language.equals("cn")) {
                    Toast.makeText(mContext, "播放列表为空，请添加！", Toast.LENGTH_LONG).show();
                } else if (language.equals("en")) {
                    Toast.makeText(mContext, "The playlist is empty, please add!", Toast.LENGTH_LONG).show();
                } else if (language.equals("burmese")) {
                    Toast.makeText(mContext, "playlist ဗလာဖြစ်ပါသည်, add ကျေးဇူးပြုပြီး!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * 重唱
     */
    private void repeat(){
        isPause = true;
        binding.flPause.setFocusable(true);
        binding.ivTrack.setImageResource(R.drawable.u33);
        binding.tvTrack.setTextColor(Color.parseColor("#FFFFFF"));
        String current = (String) binding.tvSingingname.getText();
        boolean b = false;
        String path = null;
        if (!current.equals("") && current != null){
//            for (int i = 0; i < list.size(); i++) {
//                if (list.get(i).contains(current)){
//                    b = true;
//                    path = list.get(i);
//                }
//            }

//            if (b && path != null){
//                advertisementVideo(path, -2);
//            }else {
                for (int i = 0; i < vList.size(); i++) {
                     if (vList.get(i).contains(current)){
                        path = vList.get(i);
                     }
                }
                advertisementVideo(path, -1);
//            }
        }
    }

    /**
     * 暂停、播放
     */
    private void pause(){
        Log.e("Tag","isPause="+isPause);
        if (isPause) {
            //暂停
            binding.ivPause.setImageResource(R.drawable.play1);
            isPause = false;
            if (null != binding.vv && binding.vv.isPlaying()) {
                binding.vv.pause();
            }
            if (language != null) {
                if (language.equals("cn")) {
                    binding.tvPause.setText("播放");
                } else if (language.equals("en")) {
                    binding.tvPause.setText("Play");
                } else if (language.equals("burmese")) {
                    binding.tvPause.setText("အသံလွှင်း");
                }
            }
        }else {
            //播放
            binding.ivPause.setImageResource(R.drawable.u31_mouseover);
            binding.vv.start();
            isPause = true;
            if (language != null) {
                if (language.equals("cn")) {
                    binding.tvPause.setText("暂停");
                } else if (language.equals("en")) {
                    binding.tvPause.setText("Pause");
                } else if (language.equals("burmese")) {
                    binding.tvPause.setText("ခေတ္တရပ်");
                }
            }
        }
    }

    /**
     * 伴唱功能
     */
    public void switchTrack() {
        binding.flPause.setFocusable(true);
        isPause = true;
        SparseArray<io.vov.vitamio.MediaFormat> audioTrackMap = binding.vv.getAudioTrackMap("utf-8");
        if (null != audioTrackMap && audioTrackMap.size() != 2) {
            //如果是双音轨size应该等于2
            if (language.equals("cn")) {
                Toast.makeText(mContext,"该视频不支持此功能！",Toast.LENGTH_SHORT).show();
            } else if (language.equals("en")) {
                Toast.makeText(mContext,"This feature is not supported on this video!",Toast.LENGTH_SHORT).show();
            } else if (language.equals("burmese")) {
                Toast.makeText(mContext,"အဆိုပါဗီဒီယိုသည်ဤအင်္ဂါရပ်ကိုထောက်ပံ့မထားဘူး",Toast.LENGTH_SHORT).show();
            }
            key = -1;
            return;
        }else {
            //key= 2伴奏 1 取消伴奏
            if (!isAccompany && key == 2) {//isAccompany 标记当前是否是伴奏状态
                binding.vv.setAudioTrack(key);
                binding.vv.setSubTrack(key);
                isAccompany = true;
                if (language.equals("cn")) {
                    binding.tvTrack.setText("原唱");
                } else if (language.equals("en")) {
                    binding.tvTrack.setText("Track");
                } else if (language.equals("burmese")) {
                    binding.tvTrack.setText("မူရင်းသီချင်း");
                }
            } else if (isAccompany && key == 1) {
                binding.vv.setAudioTrack(key);
                binding.vv.setSubTrack(key);
                isAccompany = false;
                if (language.equals("cn")) {
                    binding.tvTrack.setText("伴唱");
                } else if (language.equals("en")) {
                    binding.tvTrack.setText("Accompany");
                } else if (language.equals("burmese")) {
                    binding.tvTrack.setText("အသံပူးတှဲ");
                }
            }
        }
    }



    /**
     * 添加到播放列表
     */
    private void selectedList() {
       binding.rvList.setVisibility(View.GONE);
       binding.sList.setVisibility(View.VISIBLE);
       if (sList.size() != 0){
           seAdapter = new SelectedAdapter(mContext);
           seAdapter.setDatas(sList);
           binding.sList.setAdapter(seAdapter);

           binding.sList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
               @Override
               public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                   kba.setSelectedIndex(-2);

                   seleIndex = position;
                   parent.setOnKeyListener(new View.OnKeyListener() {
                       @Override
                       public boolean onKey(View v, int keyCode, KeyEvent event) {
                               if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
                                   seAdapter.setSelectedIndex(-2,seleIndex);
                               }else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
                                   seAdapter.setSelectedIndex(seleIndex,-2);
                               }else if (keyCode == KeyEvent.KEYCODE_DPAD_UP){
                                   seAdapter.setSelectedIndex(seleIndex,-2);
                               }else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
                                   seAdapter.setSelectedIndex(seleIndex,-2);
                               }else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER){
                                   if (!isClicked) {
                                       if (seAdapter.isPlay() == 0) {
                                           Log.e("tag","seAdapter="+seAdapter.getItem(seleIndex).toString()+"    seleIndex="+seleIndex);
                                           advertisementVideo(seAdapter.getItem(seleIndex).toString(), seleIndex);
                                           isClicked = true;
                                       } else if (seAdapter.isPlay() == 1) {
                                           sList.remove(seleIndex);
                                           seAdapter.notifyDataSetInvalidated();
                                           binding.tvCount.setText(sList.size() + "");
                                           isClicked = true;
                                           if (sList.size() == 0){
                                               binding.tvNextname.setText("");
                                           }
                                       }
                                       new Thread(){
                                           @Override
                                           public void run() {
                                               super.run();
                                               try {
                                                   sleep(1000);
                                                   isClicked = false;
                                               } catch (InterruptedException e) {
                                                   e.printStackTrace();
                                               }
                                           }
                                       }.start();
                                   }
                               }
                           return false;
                       }
                   });

               }

               @Override
               public void onNothingSelected(AdapterView<?> parent) {
               }
           });

       }
    }

    /**
     * 扫描到U盘的所有视频文件的列表
     */
    private void searchList() {
        binding.sList.setVisibility(View.GONE);
        binding.rvList.setVisibility(View.VISIBLE);
        if (vList.size() != 0) {
            sAdapter = new SearchAdapter(mContext,vList, new FilterListener() {
                @Override
                public void getFilterData(List<String> list) {
                }
            });
            binding.rvList.setAdapter(sAdapter);
            binding.tvAll.setText(vList.size()+"");
            binding.rvList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.e("Tag","sAdapter.getItem(position)="+sAdapter.getItem(position));
                    isRepetition = true;
                    if (sList.size() != 0) {
                        for (int i = 0; i < sList.size(); i++) {
                            if (sList.get(i).equals(sAdapter.getItem(position))) {
                                isRepetition = false;
                            }
                        }
                    }else {
                        isRepetition = true;
                    }
                    if (isRepetition) {
                        sList.add(sAdapter.getItem(position));
                        binding.tvCount.setText(sList.size() + "");
                        //显示下一首
                        if (sList.size() != 0) {
                            String next = sList.get(0).replace(".mp4","").replace(".mp3","").replace(".avi","")
                                    .replace(".mpg","").replace(".dat","").replace(".mkv","")
                                    .replace(".vob","").replace(".wmv","").replace(".mov","");
                            next = next.substring(next.lastIndexOf("/")+1);
                            binding.tvNextname.setText(next);
                        } else {
                            binding.tvNextname.setText("");
                        }
                        sAdapter.setSelectedIndex(position, true);
                    }else {
                        if (language.equals("cn")) {
                            Toast.makeText(mContext,"该歌曲已添加！",Toast.LENGTH_SHORT).show();
                        } else if (language.equals("en")) {
                            Toast.makeText(mContext,"This song has been added!",Toast.LENGTH_SHORT).show();
                        } else if (language.equals("burmese")) {
                            Toast.makeText(mContext,"သီချင်းကဆက်ပြောသည်ပြီးပါပြီ",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

            binding.rvList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    kba.setSelectedIndex(-2);
                    sAdapter.setSelectedIndex(position,false);
                    parent.setOnKeyListener(new View.OnKeyListener() {
                        @Override
                        public boolean onKey(View v, int keyCode, KeyEvent event) {
                            final int no = kba.TheNumberOfLetters();
                            if (event.getAction() == KeyEvent.ACTION_UP){

                            }else {
                                if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                                    binding.flSelected.requestFocus();
                                    selectedList();
                                } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                                    binding.gvKeyboard.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            binding.gvKeyboard.requestFocus();
                                            if (no != -2) {
                                                binding.gvKeyboard.setSelection(no);
                                            }
                                            if (sAdapter != null) {
                                                sAdapter.setSelectedIndex(-2, false);
                                            }
                                            if (seAdapter != null) {
                                                seAdapter.setSelectedIndex(-2, -2);
                                            }

                                        }
                                    });
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            super.run();
                                            try {
                                                sleep(200);
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        kba.setSelectedIndex(no);
                                                    }
                                                });
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }.start();
                                }
                            }
                            return false;
                        }
                    });
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
    }


    // 从系统文件中获取以太网MAC地址
    public static String getEthernetMacAddress() {
        try {
            return loadFileAsString("/sys/class/net/eth0/address").toUpperCase().substring(0, 17);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 读取系统文件
    private static String loadFileAsString(String filePath) throws IOException {
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }

    // 从系统文件中获取WIFI MAC地址
    public static String getWiFiMacAddress(Context context) {
        WifiManager my_wifiManager = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE));
        WifiInfo wifiInfo = my_wifiManager.getConnectionInfo();
        return wifiInfo.getMacAddress();
    }


    /**
     * 获取外网IP
     * @return
     */
    private void getRecommendIP() {
        String url = "http://ip-api.com/json";
        HttpUtils.getInstance().getOkHttpClient().newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("tag","访问失败！");
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null && response.isSuccessful()) {
                    final String res = response.body().string();
                    Log.d(TAG, "onResponse:ip= " + res);
                    if (res != null && !res.equals("")) {
                        final IpBean rBean = new Gson().fromJson(res, IpBean.class);
                        cidIP = rBean.getQuery();
                        String province = rBean.getRegionName();
                        ModifyTheLanguageOfTheRegion(province);
                    }
                    if (cidIP != null && !cidIP.equals("")){
                        //获取到IP等信息后检查该设备是否可用
                        checkMac(Utils.getDevID().toUpperCase());
                        Log.d("Tag","netMac="+netMac+"     cidIP="+cidIP+"    region="+region);
                    }
                }
            }
        });
    }


    /**
     * 获取外网IP
     * @return
     */
    private void GetNetIp() {
        URL infoUrl = null;
        InputStream inStream = null;
        try {
            infoUrl = new URL("http://ip-api.com/json/");
            URLConnection connection = infoUrl.openConnection();
            HttpURLConnection httpConnection = (HttpURLConnection)connection;
            int responseCode = httpConnection.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK) {
                inStream = httpConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream,"utf-8"));
                StringBuilder strber = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    strber.append(line + "\n");
                }
                inStream.close();
                //从反馈的结果中提取出IP地址
                line = strber.toString();
                final IpBean rBean = new Gson().fromJson(line, IpBean.class);
                cidIP = rBean.getQuery();
                String province = rBean.getRegionName();
                ModifyTheLanguageOfTheRegion(province);
            }
            //获取到IP等信息后检查该设备是否可用
            checkMac(Utils.getDevID().toUpperCase());
            Log.e("Tag","netMac="+netMac+"     cidIP="+cidIP+"    region="+region);
        } catch(MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //把拼音的省份改成中文
    private void ModifyTheLanguageOfTheRegion(String province) {
        if (province.equals("Guangdong")){
            region = "广东省";
        }else if (province.equals("Guangxi")){
            region = "广西壮族自治区";
        } else if (province.equals("Hainan")){
            region = "海南省";
        }else if (province.equals("Beijing")){
            region = "北京市";
        }else if (province.equals("Tianjin")){
            region = "天津市";
        }else if (province.equals("Shanghai")){
            region = "上海市";
        }else if (province.equals("Chongqing")){
            region = "重庆市";
        }else if (province.equals("Hebei")){
            region = "河北省";
        }else if (province.equals("Henan")){
            region = "河南省";
        }else if (province.equals("Yunan")){
            region = "云南省";
        }else if (province.equals("Liaoning")){
            region = "辽宁省";
        }else if (province.equals("Heilongjiang")){
            region = "黑龙江省";
        }else if (province.equals("Hunan")){
            region = "湖南省";
        }else if (province.equals("Anhui")){
            region = "安徽省";
        }else if (province.equals("Shandong")){
            region = "山东省";
        }else if (province.equals("Xinjiang")){
            region = "新疆维吾尔族自治区";
        }else if (province.equals("Jiangsu")){
            region = "江苏省";
        }else if (province.equals("Zhejiang")){
            region = "浙江省";
        }else if (province.equals("Jiangxi")){
            region = "江西省";
        }else if (province.equals("Hubei")){
            region = "湖北省";
        }else if (province.equals("Gansu")){
            region = "甘肃省";
        }else if (province.equals("Shanxi")){
            region = "山西省";
        }else if (province.equals("Shanxi")){
            region = "陕西省";
        }else if (province.equals("Neimenggu")){
            region = "内蒙古蒙古族自治区";
        }else if (province.equals("Jilin")){
            region = "吉林省";
        }else if (province.equals("Fujian")){
            region = "福建省";
        }else if (province.equals("Guizhou")){
            region = "贵州省";
        }else if (province.equals("Qinghai")){
            region = "青海省";
        }else if (province.equals("Sichuan")){
            region = "四川省";
        } else if (province.equals("Xizang")){
            region = "西藏藏族自治区";
        }else if (province.equals("Ningxia")){
            region = "宁夏回族自治区";
        }else if (province.equals("Taiwan")){
            region = "台湾省";
        } else if (province.equals("Hong Kong")){
            region = "香港特别行政区";
        }else if (province.equals("Macao")){
            region = "澳门特别行政区";
        }
    }


    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
         if (mThread != null){
             mThread.interrupt();
             mThread = null;
         }
         fiveSecondGone();
         mThread.start();


        if (event.getAction() == KeyEvent.ACTION_UP) {
            return false;
        }else{
            switch (v.getId()){
                case R.id.fl_pause:
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        if (binding.rlAll.getVisibility() == View.VISIBLE ) {
                            binding.gvKeyboard.post(new Runnable() {
                                @Override
                                public void run() {
                                    binding.gvKeyboard.requestFocus();
                                    binding.gvKeyboard.setSelection(lists.size() - 1);
                                    gone();
                                    new Thread(){
                                        @Override
                                        public void run() {
                                            super.run();
                                            try {
                                                sleep(200);
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        kba.setSelectedIndex(lists.size() - 1);
                                                    }
                                                });
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }.start();
                                }
                            });
                        }
                    }
                    break;
                case R.id.fl_nexts:
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP){
                        if (binding.rlAll.getVisibility() == View.VISIBLE) {
                            if (binding.sList.getVisibility() == View.VISIBLE) {
                                Log.e("tag", "sList");
                                if (sList.size() != 0) {
                                    binding.flNexts.setNextFocusUpId(R.id.s_list);
                                } else {
                                    isDowns = false;
                                    binding.flNexts.setNextFocusUpId(R.id.fl_selected);
                                }
                            } else if (binding.rvList.getVisibility() == View.VISIBLE) {
                                Log.e("tag", "rvList");
                                binding.flNexts.setNextFocusUpId(R.id.rv_list);
                            }
                        }else {
                            binding.flNexts.setNextFocusUpId(R.id.fl_setting);
                        }
                    }
                    break;
                case R.id.fl_repeat:
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP){
                        if (binding.rlAll.getVisibility() == View.VISIBLE) {
                            if (binding.sList.getVisibility() == View.VISIBLE) {
                                Log.e("tag", "sList");
                                if (sList.size() != 0) {
                                    binding.flRepeat.setNextFocusUpId(R.id.s_list);
                                } else {
                                    isDowns = false;
                                    binding.flRepeat.setNextFocusUpId(R.id.fl_selected);
                                }
                            } else if (binding.rvList.getVisibility() == View.VISIBLE) {
                                Log.e("tag", "rvList");
                                binding.flRepeat.setNextFocusUpId(R.id.rv_list);
                            }
                        }else {
                            binding.flRepeat.setNextFocusUpId(R.id.fl_setting);
                        }
                    }
                    break;
                case R.id.fl_track:
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP){
                        if (binding.rlAll.getVisibility() == View.VISIBLE) {
                            if (binding.sList.getVisibility() == View.VISIBLE) {
                                Log.e("tag", "sList");
                                if (sList.size() != 0) {
                                    binding.flTrack.setNextFocusUpId(R.id.s_list);
                                } else {
                                    isDowns = false;
                                    binding.flTrack.setNextFocusUpId(R.id.fl_selected);
                                }
                            } else if (binding.rvList.getVisibility() == View.VISIBLE) {
                                Log.e("tag", "rvList");
                                binding.flTrack.setNextFocusUpId(R.id.rv_list);
                            }
                        }else {
                            binding.flTrack.setNextFocusUpId(R.id.fl_setting);
                        }
                    }
                    break;
                case R.id.fl_search:
                    isDown = false;
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                            gone();
                            binding.gvKeyboard.post(new Runnable() {
                                @Override
                                public void run() {
                                    binding.gvKeyboard.requestFocus();
                                    binding.gvKeyboard.setSelection(0);
                                }
                            });
                            new Thread(){
                                @Override
                                public void run() {
                                    super.run();
                                    try {
                                        sleep(200);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                kba.setSelectedIndex(0);
                                            }
                                        });
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }.start();
                    }else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
                        binding.ivSearch.setImageResource(R.drawable.u51_mouseover);
                        isDown = true;
                    }
                    break;
                case R.id.fl_selected:
                    isDowns = false;
                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        binding.ivSelected.setImageResource(R.drawable.u51_mouseover);
                        isDowns = true;
                        if (sList.size() == 0){
                            binding.flTrack.requestFocus();
                        }
                    }
                    break;
            }

        }
        return super.onKeyDown(keyCode,event);
    }

    private void settings(final String yuyan) {
        lists = TheLetterForm(yuyan);
        slist = changeLetter(yuyan);

        kba.setApps(lists);
        kba.setSeList(slist);
        binding.gvKeyboard.setAdapter(kba);
        binding.gvKeyboard.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                binding.ivSearch.setImageResource(R.drawable.u51);
                binding.ivSelected.setImageResource(R.drawable.u51);
                kba.setSelectedIndex(position);
                gone();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        binding.gvKeyboard.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                int shu = -1;
                int cr = -1;
                Log.e("TAG","yuyan="+yuyan);
                if (yuyan.equals("burmese")){
                     shu = 43;
                     if (position >= 43){
                         cr = position;
                     }
                }else if (yuyan.equals("en") || yuyan.equals("cn")){
                    shu = 36;
                    if (position >= 36){
                        cr = position;
                    }
                }

                //切换键盘
                if (cr == 45){
                    settings("cn");
                }else if (cr == 38){
                    settings("burmese");
                }
                //添加输入的字母
                if (position < shu) {
                    Log.e("tag","position="+position+"      yuyan="+yuyan);
                    String ss = output(position,yuyan);
                    inputList.add(ss);
                }

                if (inputList.size() != 0) {
                    //清除全部输入的字母
                    if (cr == 43 || cr == 36) {
                        inputList.clear();
                    }
                    //移除最后一个输入的字母
                    if (cr == 44 || cr == 37) {
                        inputList.remove(inputList.size() - 1);
                    }
                }

                lister();
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        try {
                            sleep(180);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    kba.setSelectedIndex(position);
                                }
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });


    }

    /**
     * 对输入框进行监听，如果有文字，就过滤；没有就不处理
     */
   private void lister(){
        String str[] = new String[inputList.size()];
        str = inputList.toArray(str);
        String s1 = StringUtil.join(str,"");

        binding.tvInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (sAdapter != null){
                    sAdapter.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

       binding.tvInput.setText(s1);
    }


    private String output(int position,String language){
        String s = null;
        List<String> ls = new ArrayList<>();
        List<String> ls1 = new ArrayList<>();
        String[] strings = {"A","B","C","D","E","F","G","H","I","J","K","L", "M","N","O","P","Q","R","S","T","U",
                    "V","W","X","Y","Z","1","2","3","4","5","6","7","8","9","0"};
        String[] strings1 = {"က","ခ","ဂ","ဃ","င","စ","ဆ","ဇ","ဈ","ည","ဋ","ဌ", "ဍ","ဎ","ဏ","ာ","ထ","ဒ","ဓ","န","ပ",
                    "ဖ","ဗ","ဘ","မ","ယ","ရ","လ","၀","သ","ဟ","ဠ","အ","1","2","3","4","5","6","7","8","9","0"};

        for (int i = 0; i < strings.length; i++) {
             ls.add(strings[i]);
        }
        for (int i = 0; i < strings1.length; i++){
            ls1.add(strings1[i]);
        }
        if (language.equals("burmese")){
            s = ls1.get(position);
        }else if (language.equals("en") || language.equals("cn")){
            s = ls.get(position);
        }

        return s;
    }

    private List<Drawable> TheLetterForm(String language){
        List<Drawable> bd = new ArrayList<>();
        List<Drawable> cd = new ArrayList<>();
        int[] a = {R.drawable.a1,R.drawable.a2,R.drawable.a3,R.drawable.a4,R.drawable.a5,R.drawable.a6,R.drawable.a7,R.drawable.a8
                    ,R.drawable.a9,R.drawable.a10,R.drawable.a11,R.drawable.a12,R.drawable.a13,R.drawable.a14,R.drawable.a15,R.drawable.a16
                    ,R.drawable.a17,R.drawable.a18,R.drawable.a19,R.drawable.a20,R.drawable.a21,R.drawable.a22,R.drawable.a23,R.drawable.a24
                    ,R.drawable.a25,R.drawable.a26,R.drawable.a28,R.drawable.a29,R.drawable.a30,R.drawable.a31,R.drawable.a32
                    ,R.drawable.a33,R.drawable.a34,R.drawable.a35,R.drawable.a36,R.drawable.a27,R.drawable.u208,R.drawable.u205,R.drawable.u210};
        int[] c = {R.drawable.c1,R.drawable.c2,R.drawable.c3,R.drawable.c4,R.drawable.c5,R.drawable.c6,R.drawable.c7,R.drawable.c8
                ,R.drawable.c9,R.drawable.c10,R.drawable.c11,R.drawable.c12,R.drawable.c13,R.drawable.c14,R.drawable.c15,R.drawable.c16
                ,R.drawable.c17,R.drawable.c18,R.drawable.c19,R.drawable.c20,R.drawable.c21,R.drawable.c22,R.drawable.c23,R.drawable.c24
                ,R.drawable.c25,R.drawable.c26,R.drawable.c27,R.drawable.c28,R.drawable.c29,R.drawable.c30,R.drawable.c31,R.drawable.c32
                ,R.drawable.c33,R.drawable.c34,R.drawable.c35,R.drawable.c36,R.drawable.c37,R.drawable.c38,R.drawable.c39,R.drawable.c40
                ,R.drawable.c41,R.drawable.c42,R.drawable.c43,R.drawable.u208,R.drawable.u205,R.drawable.u210};
        for (int i = 0; i < a.length; i++) {
             Drawable cb = getResources().getDrawable(a[i]);
             cd.add(cb);
        }
        for (int i = 0; i < c.length; i++) {
            Drawable bb = getResources().getDrawable(c[i]);
            bd.add(bb);
        }

            if (language.equals("burmese")){
                return bd;
            }else if (language.equals("en") || language.equals("cn")){
                return cd;
            }
          return null;
    }

    private List<Drawable> changeLetter(String language){
        List<Drawable> bd = new ArrayList<>();
        List<Drawable> cd = new ArrayList<>();
        int[] b = {R.drawable.b1,R.drawable.b2,R.drawable.b3,R.drawable.b4,R.drawable.b5,R.drawable.b6,R.drawable.b7,R.drawable.b8
                ,R.drawable.b9,R.drawable.b10,R.drawable.b11,R.drawable.b12,R.drawable.b13,R.drawable.b14,R.drawable.b15,R.drawable.b16
                ,R.drawable.b17,R.drawable.b18,R.drawable.b19,R.drawable.b20,R.drawable.b21,R.drawable.b22,R.drawable.b23,R.drawable.b24
                ,R.drawable.b25,R.drawable.b26,R.drawable.b28,R.drawable.b29,R.drawable.b30,R.drawable.b31,R.drawable.b32
                ,R.drawable.b33,R.drawable.b34,R.drawable.b35,R.drawable.b36,R.drawable.b27,R.drawable.u208_mouseover,R.drawable.u205_mouseover,R.drawable.u210_mouseover};
        int[] d = {R.drawable.d1,R.drawable.d2,R.drawable.d3,R.drawable.d4,R.drawable.d5,R.drawable.d6,R.drawable.d7,R.drawable.d8
                ,R.drawable.d9,R.drawable.d10,R.drawable.d11,R.drawable.d12,R.drawable.d13,R.drawable.d14,R.drawable.d15,R.drawable.d16
                ,R.drawable.d17,R.drawable.d18,R.drawable.d19,R.drawable.d20,R.drawable.d21,R.drawable.d22,R.drawable.d23,R.drawable.d24
                ,R.drawable.d25,R.drawable.d26,R.drawable.d27,R.drawable.d28,R.drawable.d29,R.drawable.d30,R.drawable.d31,R.drawable.d32
                ,R.drawable.d33,R.drawable.d34,R.drawable.d35,R.drawable.d36,R.drawable.d37,R.drawable.d38,R.drawable.d39,R.drawable.d40
                ,R.drawable.d41,R.drawable.d42,R.drawable.d43,R.drawable.u208_mouseover,R.drawable.u205_mouseover,R.drawable.u210_mouseover};
        for (int i = 0; i < b.length; i++) {
            Drawable cb = getResources().getDrawable(b[i]);
            cd.add(cb);
        }
        for (int i = 0; i < d.length; i++) {
            Drawable bb = getResources().getDrawable(d[i]);
            bd.add(bb);
        }
        if (language.equals("cn") || language.equals("en")){
            return cd;
        }else if (language.equals("burmese")) {
            return bd;
        }
        return null;
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d(TAG, "onLowMemory");
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int ppp =  binding.llTop.getVisibility();
        int pp = binding.rlAll.getVisibility();
//        Toast.makeText(mContext,"event="+event,Toast.LENGTH_LONG).show();
        if (keyCode == KeyEvent.KEYCODE_BACK){
            inputNumber("B");
            initView();
//            if (isFirst){
                if (language.equals("cn")){
                    Toast.makeText(mContext, "三秒内再次点击“返回键”退出应用！", Toast.LENGTH_SHORT).show();
                }else if (language.equals("en")){
                    Toast.makeText(mContext, "Click the \"Back button\" again within three seconds to exit the app!", Toast.LENGTH_SHORT).show();
                }else if (language.equals("burmese")){
                    Toast.makeText(mContext, "လျှောက်လွှာမှထွက်ရန်သုံးစက္ကန့် \"နောက်ကျော button ကို\" အတွင်းကိုထပ်ကလစ်နှိပ်ပါ!", Toast.LENGTH_SHORT).show();
                }
//                isFirst = false;
//            }
            return true;
        } else {
            switch (keyCode) {
                case KeyEvent.KEYCODE_MENU:
                    //菜单
                    menu();
                    break;
                case KeyEvent.KEYCODE_F9:
                    //搜歌
                    souge();
                    break;
                case KeyEvent.KEYCODE_F10:
                    //重唱
                    dView();
                    binding.flRepeat.requestFocus();
                    repeat();
                    break;
                case KeyEvent.KEYCODE_F1:
                    //切歌
                    dView();
                    binding.flNexts.requestFocus();
                    next();
                    break;
                case KeyEvent.KEYCODE_F5:
                    //暂停
                    dView();
                    binding.flPause.setFocusable(true);
                    binding.flPause.requestFocus();
                    pause();
                    break;
                case KeyEvent.KEYCODE_F2:
                    //伴唱
                    dView();
                    binding.flTrack.requestFocus();
                    if (isAccompany) {
                        key = 1;
                    } else {
                        key = 2;
                    }
                    if (binding.vv != null) {
                        switchTrack();
                    }
                    break;
                case KeyEvent.KEYCODE_F11:
                    //设置
                    startJump(mContext,LanguageActivity.class,true);
                    break;



                    
                case KeyEvent.KEYCODE_0:// 0
                    inputNumber("0");
                    break;
                case KeyEvent.KEYCODE_1:// 1
                    inputNumber("1");
                    break;
                case KeyEvent.KEYCODE_2:// 2
                    inputNumber("2");
                    break;
                case KeyEvent.KEYCODE_3:// 3
                    inputNumber("3");
                    break;
                case KeyEvent.KEYCODE_4:// 4
                    inputNumber("4");
                    break;
                case KeyEvent.KEYCODE_5:// 5
                    inputNumber("5");
                    break;
                case KeyEvent.KEYCODE_6:// 6
                    inputNumber("6");
                    break;
                case KeyEvent.KEYCODE_7:// 7
                    inputNumber("7");
                    break;
                case KeyEvent.KEYCODE_8:// 8
                    inputNumber("8");
                    break;
                case KeyEvent.KEYCODE_9:// 9
                    inputNumber("9");
                    break;
//                case KeyEvent.KEYCODE_F1: //F1
//                    inputNumber("F1");
//                    break;
//                case KeyEvent.KEYCODE_F2:    //F2
//                    inputNumber("F2");
//                    break;
                case KeyEvent.KEYCODE_F3:     //F3
                    inputNumber("F3");
                    break;
            }
              //方向键弹出控制栏
//            if (ppp == 8 && pp == 8) {
//                switch (keyCode) {
//                    case KeyEvent.KEYCODE_DPAD_DOWN:
//                    case KeyEvent.KEYCODE_DPAD_UP:
//                    case KeyEvent.KEYCODE_DPAD_LEFT:
//                    case KeyEvent.KEYCODE_DPAD_RIGHT:
//                        dView();
//                        binding.flPause.setFocusable(true);
//                        binding.flPause.requestFocus();
//                        break;
//                }
//            }
            return super.onKeyDown(keyCode, event);
        }
    }

    private static final String StartDragonTest = "1379";//测试
    private static final String StartDragonAging = "2379";//老化
    private static final String versionInfo = "3379";//版本信息

    private static final String zhibo = "F1";  //直播
    private static final String dianbo = "F2";  //点播
    private static final String app = "F3";    //我的应用

    long oldTime = 0;
    String num = "";


    private void inputNumber(String i) {
        long inputTime = System.currentTimeMillis();
        if (inputTime - oldTime < 3000){
            //3s内输入有效
            num += i;
        } else {
            //如果输入时间超过1s,num统计的值重置为输入值
            num = i;
        }
        oldTime = inputTime;


        switch (num) {
            case "BB":
                num = "";
                oldTime = 0;
                finish();
                break;
            case StartDragonTest:
                //重置输入
                num = "";
                oldTime = 0;
//                Toast.makeText(this, "启动测试:", Toast.LENGTH_SHORT).show();
                if (AppManager.isInstallApp(mContext, "com.wxs.scanner")) {
//                    startActivity(new Intent().setClassName("com.kong.apptesttools", "com.kong.apptesttools.MainActivity"));
                    startActivity(new Intent().setClassName("com.wxs.scanner", "com.wxs.scanner.activity.workstation.CheckActivity"));
                } else {
//                    Toast.makeText(mContext, "未安装测试App", Toast.LENGTH_SHORT).show();
                    Toast.makeText(mContext, R.string.no_install_app, Toast.LENGTH_SHORT).show();
                }
                break;
            case StartDragonAging:
                //重置输入
                num = "";
                oldTime = 0;
//                Toast.makeText(this, "启动老化测试:", Toast.LENGTH_SHORT).show();
                if (AppManager.isInstallApp(mContext, "com.softwinner.agingdragonbox")) {
                    AppManager.startAgingApk(mContext);
                } else {
//                    Toast.makeText(mContext, "未安装老化App", Toast.LENGTH_SHORT).show();
                    Toast.makeText(mContext, R.string.no_install_old_app, Toast.LENGTH_SHORT).show();
                }
                break;
            case versionInfo:
                num = "";
                oldTime = 0;
                String deviceName;
                if ("702".equals(deviceId)) {
                    deviceName = "柴喜";
                } else if ("701".equals(deviceId)) {
//                    deviceName = "拓普赛特";
                    deviceName = "Mở rộng khu vực";
                }else if("704".equals(deviceId)){
                    deviceName = "老凤祥";
                } else if("696".equals(deviceId)){
                    deviceName = "精合智";
                }  else {
                    deviceName = "其它";
                }
                new AlertDialog.Builder(mContext)
//                        .setTitle("版本信息")
//                        .setMessage(appName + "-" + BuildConfig.VERSION_NAME +
//                                "\n服务范围：" + (host.startsWith("http://192.168.") ? "内网" : "外网") +
//                                "\n品牌商：" + deviceName)
                        .setTitle("Phiên bản thông tin")
                        .setMessage(appName + "-" + BuildConfig.VERSION_NAME +
                                "\nPhạm vi dịch vụ：" + (host.startsWith("http://192.168.") ? "Mạng nội bộ" : "Mạng bên ngoài") +
                                "\nThương hiệu：" + deviceName)
                        .show();
                break;

            case "8888":
                num = "";
                oldTime = 0;
                View view1 = LayoutInflater.from(mContext).inflate(R.layout.test_cache, null);
                ((TextView) view1.findViewById(R.id.tv_content)).setText("");
                new AlertDialog.Builder(mContext)
                        .setTitle("remove result")
                        .setView(view1)
                        .show();

                break;
        }
    }

    /**
     * 检查mac是否可用
     */
    private void checkMac(final String mac) {
        String url = host + "jhzBox/box/loadBox.do?cy_brand_id=" + deviceId+ "&mac=" + mac+"&netCardMac="+netMac+"&codeIp="+cidIP+"&region="+region;

        HttpUtils.getInstance().getOkHttpClient().newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("tag","访问失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null && response.isSuccessful()) {
                    String res = response.body().string();
                    final String no_mac = getResources().getString(R.string.no_mac);
                    Log.d("Tag","res music="+res);
                    final CheckMacBean checkMacBean = new Gson().fromJson(res, CheckMacBean.class);
                    if (checkMacBean.getStatus() != 0) {
                        //设备不可用
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle(R.string.hint)
                                        .setMessage(no_mac+"\nMAC: "+mac)
                                        .setCancelable(false)
                                        .show();
                            }
                        });
                        isFailVerification = true;
                        if (isFailVerification){
                            isFailVerification = false;
                            if (binding.vv.isPlaying()){
                                binding.vv.pause();
                            }
                            Log.e(TAG,"false     "+binding.vv.isPlaying());
                        }

                    }
                    if (!isCheckVersion) {
                        checkVersion();
                    }
                }

            }
        });
    }

    //开机播放
    private void bofang(){
//        Log.e("tag","pathsss="+pathsss);
//        if (list != null && list.size() != 0) {
//            advertisementVideo(list.get(0), -2);
//        }
//        if (vList.size() > 0 && null != vList) {
//            if (list != null && list.size() == 0) {
//                advertisementVideo(vList.get(0), -1);
//            }
//        }

        if (sList.size() > 0 && null != sList){
            //默认播放选中列表
            advertisementVideo(sList.get(0),-1);
        }else if (vList.size() > 0 && null != vList){
            //默认播放搜索列表
            advertisementVideo(vList.get(0), -1);
        }
    }
    /**
     * 普通下载apk安装
     *
     * @param url
     */
    private void downloadApk(final String url) {
        HttpUtils.getInstance().getOkHttpClient().newCall(new Request.Builder()
                .url(url).build())
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                Toast.makeText(mContext, "下载失败!", Toast.LENGTH_SHORT).show();
                                Toast.makeText(mContext, R.string.download_failed, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response != null && response.isSuccessful()) {
                            InputStream inputStream = response.body().byteStream();

                            final String filePath = AppManager.getAppDir() + url.substring(url.lastIndexOf("/") + 1);
                            FileOutputStream fos = new FileOutputStream(filePath);
                            int len = 0;
                            byte[] buffer = new byte[1024 * 10];
                            while ((len = inputStream.read(buffer)) != -1) {
                                fos.write(buffer, 0, len);
                            }
                            fos.flush();
                            fos.close();
                            inputStream.close();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //安装
                                    AppManager.install(mContext, filePath);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
//                                    Toast.makeText(mContext, "下载失败!", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(mContext, R.string.download_failed, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
    }

    /**
     * 检查更新版本
     */
    private void checkVersion() {
        isCheckVersion = true;
        String url = host + "jhzBox/box/appOnlineVersion.do?versionNum=" + BuildConfig.VERSION_NAME + "&cy_brand_id=" + deviceId
                + "&cy_versions_name=" + appName+"&lunchname="+lunchname+"&mac="+Utils.getDevID().toUpperCase()+"&netCardMac="+netMac;
        HttpUtils.getInstance().getOkHttpClient().newCall(new Request.Builder().url(url).build())
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response != null && response.isSuccessful()) {
                            String res = response.body().string();
                            Log.d(TAG, "onResponse:version= " + res);
                            final RecommendversionBean versionBean = new Gson().fromJson(res, RecommendversionBean.class);
                            if (versionBean.getStatus() == 4) {
                                final RecommendversionBean.DataBean data = versionBean.getData();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        new AlertDialog.Builder(mContext)
                                                .setTitle(R.string.version_updating)
                                                .setMessage(data.getCy_versions_info())
                                                .setNegativeButton(R.string.cancles, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        dialogInterface.dismiss();
                                                    }
                                                })
                                                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        Toast.makeText(mContext, R.string.background_download, Toast.LENGTH_SHORT).show();
                                                        downloadApk(data.getCy_versions_path());
                                                    }
                                                }).show();
                                    }
                                });
                            }
                        }
                    }
                });
    }


    /**
     * @description OTG广播注册
     * @author ldm
     * @time 2017/9/1 17:19
     */
    IntentFilter usbDeviceStateFilter = new IntentFilter();
    private void registerUDiskReceiver() {
        //监听otg插入 拔出
        usbDeviceStateFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        usbDeviceStateFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        usbDeviceStateFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        usbDeviceStateFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        usbDeviceStateFilter.addDataScheme("file");
        //注册监听自定义广播
        scanMusic();
    }

    private void scanMusic(){
        String usb = getSharedPreferences("usb",MODE_PRIVATE).getString("usb_path",null);
        Log.e("tag","usb="+usb);
        if (usb != null && !"".equals(usb)){
            sus(usb);
        }
    }
    AlertDialog dialog;
    private boolean isShow = true;
    private void dialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder =  new AlertDialog.Builder(mContext);
                builder.setMessage(R.string.insert_and_extract);
                builder.setCancelable(false);
                dialog = builder.create();
            }
        });
    }
    

    private void sus (String path){
        UsbManager manager =(UsbManager)getSystemService(Context.USB_SERVICE);

            HashMap<String,UsbDevice> deviceList = manager.getDeviceList();
            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
            if (!deviceIterator.hasNext()){
                 dialog.show();
            }
            while (deviceIterator.hasNext()){
                UsbDevice usbDevice = deviceIterator.next();
                int deviceClass = usbDevice.getDeviceClass();
                Log.e("tag","deviceClass="+deviceClass);
                if(deviceClass==0){
                    UsbInterface anInterface = usbDevice.getInterface(0);
                    //通过下面的InterfaceClass 来判断到底是哪一种的，例如7就是打印机，8就是usb的U盘 
                     Log.e("tag","anInterface="+anInterface);
                    if(anInterface.getInterfaceClass()==8){
                        Log.e("tag","此设备是U盘       >"+path);
                        hasMusic(path);
                    }
                }
            }
        if (isShow){
           dialog.show();
        }

    }
    //判断是否有指定文件夹
    private void hasMusic(String path){
        //遍历查找TPsong文件夹
        String tpsong = getPath(path);
        Log.e("tag","tpsong="+tpsong);
        if (tpsong != null){
            isShow = false;
            //判断pick文件夹是否存在
            File filess = new File(tpsong+"/pick");
            if (filess.exists()) {
                sList = GetVideoFileName(String.valueOf(filess));
            }
            vList = GetVideoFileName(tpsong);
        }
        if (vList.size() > 0 && null != vList){
            dialog.dismiss();
            //是U盘就保存
            getSharedPreferences("usb", MODE_PRIVATE).edit().putString("usb_path", path).commit();
            pathsss = tpsong+"/";
            searchList();
            bofang();
        }
    }

    // 获取当前目录下所有的mp4文件
    public static List<String> GetVideoFileName(String fileAbsolutePath) {
        List<String> vecFile = new ArrayList<>();
        File file = new File(fileAbsolutePath);
        if (file.length() != 0) {
            vecFile = scanFile(file,vecFile);
        }
        return vecFile;
    }
    private static List<String> scanFile (File file,List<String> mList){
            File[] subFile = file.listFiles();
            for (int iFileLength = 0; iFileLength < subFile.length; iFileLength++) {
                // 判断是否为文件夹
                if (!subFile[iFileLength].isDirectory()) {
                    String filename = subFile[iFileLength].getName();
                    // 判断是否为MP4结尾
                    String f = filename.trim().toLowerCase();
                    if (f.endsWith(".mp4") || f.endsWith(".avi") || f.endsWith(".mpg") || f.endsWith(".dat") || f.endsWith(".mp3")
                            || f.endsWith(".mkv") || f.endsWith(".vob") || f.endsWith(".wmv") || f.endsWith(".mov")) {
                        mList.add(subFile[iFileLength].toString());
                    }
                }else {
                    scanFile(subFile[iFileLength],mList);
                }
            }
        return mList;
    }


    private String s = null;
    //找到所有音乐（实则为音乐所在地址） 并存入集合中
    public String getPath(String path) {
        //获得外部存储的根目录
        File dir = new File(path);
        //调用遍历所有文件的方法
        s = recursionFile(dir);
        //返回文件路径集合
        return s;
    }
    //遍历所有文件 并将路径名存入集合中 参数需要 路径和集合
    private String folder = null;
    public String recursionFile(File dir) {
        //得到某个文件夹下所有的文件
        File[] files = dir.listFiles();
        //文件为空
        if (files == null) {
            return null;
        }
        //遍历当前文件下的所有文件
        for (File file : files) {
            //如果是文件夹
            if (file.isDirectory()) {
                if (file.getName().equals("TPsong")) {
                    folder = file.getAbsolutePath();
                }else {
                    //则递归(方法自己调用自己)继续遍历该文件夹
                    recursionFile(file);
                }
            }
        }
        return folder;
    }


    /**
     * @description OTG广播，监听U盘的插入及拔出
     * @author ldm
     * @time 2017/9/1 17:20
     * @param
     */
    private BroadcastReceiver mOtgReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, final Intent intent) {
            final String action = intent.getAction();
//            Log.e("TAG","action="+action);
            
            if (action.equals(Intent.ACTION_MEDIA_EJECT)
                    || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
//                Log.e("tag","U盘已拔出");
                binding.vv.stopPlayback();
                dialog.show();
            }else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                String path = intent.getDataString();
//                Log.e("TAG","PATH="+path);
                final String pathString = path.split("file://")[1];
                //判断是否是U盘
                sus(pathString);
            }
        }
    };


    private void scan () throws IOException {
        Runtime runtime = Runtime.getRuntime();
        Process proc = runtime.exec("mount");
        InputStream is = proc.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        String line = "";
        BufferedReader br = new BufferedReader(isr);
        while ((line = br.readLine()) != null) {
        //此处保存至路径的buffer中供使用
            // 将常见的linux分区过滤掉
            if (line.contains("proc") || line.contains("tmpfs") || line.contains("media") || line.contains("asec") || line.contains("secure") || line.contains("system") || line.contains("cache")
                    || line.contains("sys") || line.contains("data") || line.contains("shell") || line.contains("root") || line.contains("acct") || line.contains("misc") || line.contains("obb")) {
                continue;
            }

            // 下面这些分区是我们需要的
            if (line.contains("fat") || line.contains("fuse") || (line.contains("ntfs")) || (line.contains("mnt"))){
                // 将mount命令获取的列表分割，items[0]为设备名，items[1]为挂载路径
                String items[] = line.replace(" on","").split(" ");
                if (items != null && items.length > 1){
                    String path = items[1];
//                    Log.e("tag","path==="+path);
                    sus(path);
                }
            }
      }
    }
}
