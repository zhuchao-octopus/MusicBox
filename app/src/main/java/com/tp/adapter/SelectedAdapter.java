package com.tp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.tp.utils.AppMain;
import com.musicbox.R;

import java.util.List;

public class SelectedAdapter extends BaseAdapter {


    private List<String> beans;
    public Context mContext;
    private static int selectedIndex = -2;        //记录当前选中的条目索引
    private int isPlay;
    private static int deletedIndex = -2;

    public SelectedAdapter(Context context) {
        mContext = context;
    }

    public void setDatas(List<String> list) {
            this.beans = list;
    }

    public void setSelectedIndex(int position,int po) {
        this.selectedIndex = position;
        this.deletedIndex = po;
        notifyDataSetChanged();
    }

    public int isPlay(){
        if (isPlay == 0 || isPlay == 1){
            return isPlay;
        }
        return -2;
    }

    @Override
    public int getCount() {
        return this.beans != null ? this.beans.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return this.beans.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(AppMain.ctx()).inflate(R.layout.live_item, null);
            viewHolder.bg = convertView.findViewById(R.id.iv_bg);
            viewHolder.imageView = convertView.findViewById(R.id.iv_playing);
            viewHolder.iv = convertView.findViewById(R.id.iv_deleted);
            viewHolder.tv = convertView.findViewById(R.id.tv_songingname);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        String songname = beans.get(position).replace(".mp4","").replace(".mp3","").replace(".avi","")
                .replace(".mpg","").replace(".dat","").replace(".mkv","")
                .replace(".vob","").replace(".wmv","").replace(".mov","");
        songname = songname.substring(songname.lastIndexOf("/")+1);
        viewHolder.tv.setText(songname);

        if (deletedIndex == position){
            viewHolder.iv.setImageResource(R.drawable.u97_mouseover);
            viewHolder.imageView.setImageResource(R.drawable.u123);
            viewHolder.tv.setTextColor(Color.parseColor("#F08516"));
            isPlay = 1;
        }else {
            viewHolder.iv.setImageResource(R.drawable.u97);
            viewHolder.tv.setTextColor(Color.parseColor("#FFFFFF"));
        }
        if (selectedIndex == position){
            viewHolder.imageView.setImageResource(R.drawable.u123_mouseover);
            viewHolder.iv.setImageResource(R.drawable.u97);
            viewHolder.tv.setTextColor(Color.parseColor("#F08516"));
            isPlay = 0;
        }else {
            viewHolder.imageView.setImageResource(R.drawable.u123);
        }

        return convertView;
    }

    public void release() {

    }
}
