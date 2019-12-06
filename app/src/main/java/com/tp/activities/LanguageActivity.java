package com.tp.activities;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.musicbox.BuildConfig;
import com.musicbox.R;
import com.musicbox.databinding.ActivityLanguageBinding;
import com.tp.data.json.regoem.CheckMacBean;
import com.tp.data.json.regoem.IpBean;
import com.tp.data.json.regoem.RecommendversionBean;
import com.tp.utils.AppManager;
import com.tp.utils.HttpUtils;
import com.tp.utils.NetTool;
import com.tp.utils.Utils;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Oracle on 2017/12/1.
 */

public class LanguageActivity extends AppCompatActivity implements View.OnClickListener,
        View.OnFocusChangeListener,NetTool.OnNetListener{

    private static final String TAG = LanguageActivity.class.getSimpleName();
    private ActivityLanguageBinding binding;

    private String deviceId = "750";//TVBOX 天谱
    private String host = "http://www.gztpapp.cn:8976/";    //天谱  （节流）8976
    private String appName = "TPMUSIC";      //天谱
    private String lunchname = "TPMUSIC";
//    int cid = -1 ;    //客户号  (国内)

    String netMac;   //设备的网络mac地址
    String cidIP ;   //设备的IP
    String region ; //设备所在地区

    private Context context;
    private boolean isCheckVersion = false;
    private boolean isCheckMAC = false;
    private boolean isHasNet = true;
    private NetTool netTool;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_language);
        context = this;
        binding.flBurmese.setOnClickListener(this);
        binding.flBurmese.setOnFocusChangeListener(this);
        binding.flCn.setOnClickListener(this);
        binding.flCn.setOnFocusChangeListener(this);
        binding.flEn.setOnClickListener(this);
        binding.flEn.setOnFocusChangeListener(this);

        binding.flBurmese.requestFocus();

        //监听网络状态
        netTool = new NetTool(this);
        netTool.setOnNetListener(this);
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

    @Override
    protected void onStart() {
        super.onStart();
        netTool.registerNetReceiver();
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
        netTool.unRegisterNetReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onClick(View v) {
//        if (isCheckMAC || !isHasNet){
            switch (v.getId()) {
                case R.id.fl_burmese:
                    selectedLanguage("burmese");
                    break;
                case R.id.fl_cn:
                    selectedLanguage("cn");
                    break;
                case R.id.fl_en:
                    selectedLanguage("en");
                    break;
            }
//        }else{
//            Toast.makeText(context,R.string.checkout_mac,Toast.LENGTH_LONG).show();
//        }
    }

    private void selectedLanguage(String string){
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        intent.putExtra("777",string);
        setResult(777, intent);
        finish();
    }



    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()){
            case R.id.fl_burmese:
                binding.ivBurmese.setImageResource(R.drawable.u8_mouseover);
                binding.tvBurmese.setTextColor(android.graphics.Color.parseColor("#F08516"));
                binding.ivCn.setImageResource(R.drawable.u8);
                binding.ivEn.setImageResource(R.drawable.u8);
                binding.tvCn.setTextColor(android.graphics.Color.parseColor("#FFFFFF"));
                binding.tvEn.setTextColor(android.graphics.Color.parseColor("#FFFFFF"));
                break;
            case R.id.fl_cn:
                binding.ivCn.setImageResource(R.drawable.u8_mouseover);
                binding.tvCn.setTextColor(android.graphics.Color.parseColor("#F08516"));
                binding.ivBurmese.setImageResource(R.drawable.u8);
                binding.ivEn.setImageResource(R.drawable.u8);
                binding.tvBurmese.setTextColor(android.graphics.Color.parseColor("#FFFFFF"));
                binding.tvEn.setTextColor(android.graphics.Color.parseColor("#FFFFFF"));
                break;
            case R.id.fl_en:
                binding.ivEn.setImageResource(R.drawable.u8_mouseover);
                binding.tvEn.setTextColor(android.graphics.Color.parseColor("#F08516"));
                binding.ivCn.setImageResource(R.drawable.u8);
                binding.ivBurmese.setImageResource(R.drawable.u8);
                binding.tvCn.setTextColor(android.graphics.Color.parseColor("#FFFFFF"));
                binding.tvBurmese.setTextColor(android.graphics.Color.parseColor("#FFFFFF"));
                break;
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            return false;
        }
        return super.onKeyDown(keyCode, event);
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
                            Log.d("Tag", "netMac=" + netMac + "     cidIP=" + cidIP + "    region=" + region);
                    }
                }
            }
        });
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
                        isCheckMAC = false;
                        //设备不可用
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new AlertDialog.Builder(context)
                                        .setTitle(R.string.hint)
                                        .setMessage(no_mac+"\nMAC:  "+mac)
                                        .setCancelable(false)
                                        .show();
                            }
                        });
                    }else {
                        isCheckMAC = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context,R.string.mac_pass,Toast.LENGTH_SHORT).show();
                            }
                        }); }
                    if (!isCheckVersion) {
                        checkVersion();
                    }
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
                                        new AlertDialog.Builder(context)
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
                                                        Toast.makeText(context, R.string.background_download, Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(context, R.string.download_failed, Toast.LENGTH_SHORT).show();
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
                                    AppManager.install(context, filePath);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, R.string.download_failed, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
    }

    @Override
    public void onNetState(boolean isConnected, int type) {
        boolean b = NetTool.isNetworkOK();
        if (b){
            isHasNet = true;

            String n = getEthernetMacAddress();
            String w = getWiFiMacAddress(context);
            if (!"".equals(n) && null != n) {
                netMac = n;
            }else if (!"".equals(w) && null != w) {
                netMac = w;
            }
//            Intent intent = getIntent();
//            String s = intent.getStringExtra("again");
//            if (s.equals("first")) {
//                getRecommendIP();
//            }else if (s.equals("again")){
//                isHasNet = false;
//            }
        }else {
            isHasNet = false;
        }
    }

    @Override
    public void wifiLevel(int level) {

    }
}
