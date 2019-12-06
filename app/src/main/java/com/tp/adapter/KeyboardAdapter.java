package com.tp.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.tp.utils.AppMain;
import com.tp.utils.GlideMgr;
import com.musicbox.R;

import java.util.ArrayList;
import java.util.List;

public class KeyboardAdapter extends BaseAdapter {


    private List<Drawable> list = new ArrayList<>();
    private List<Drawable> seList = new ArrayList<>();
    private static int selectedIndex = -1;        //记录当前选中的条目索引

    public Context mContext;

    public KeyboardAdapter(Context context) {
        mContext = context;
    }

    public void setApps(List<Drawable> list) {

        if (this.list != null) {
            this.list.clear();
        }
        if (list != null) {
            this.list = list;
        }
    }

    public int TheNumberOfLetters(){
        if (list.size() != 0){
            return list.size()-1;
        }
        return -2;
    }

    public void setSeList(List<Drawable> slist) {

        if (this.seList != null) {
            this.seList.clear();
        }
        if (slist != null) {
            this.seList = slist;
        }
    }

    public void setSelectedIndex(int position) {
        this.selectedIndex = position;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.list != null ? this.list.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return this.list.get(position);
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

            convertView = LayoutInflater.from(AppMain.ctx()).inflate(R.layout.keyboard_item, null);
            viewHolder.iv = convertView.findViewById(R.id.keyboard);
            viewHolder.bg = convertView.findViewById(R.id.app_bg);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

//        Log.e("Tag","selectedIndex="+selectedIndex+"     position="+position);
        if (selectedIndex == position){
            //选中
            try {
                Drawable drawable = seList.get(position);
                viewHolder.iv.setImageDrawable(drawable);
                viewHolder.bg.setImageResource(R.drawable.appbg);
//                GlideMgr.loadNormalDrawableImg(mContext, drawable, viewHolder.iv);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            //未选中
            try {
                Drawable drawables = list.get(position);
                viewHolder.iv.setImageDrawable(drawables);
                viewHolder.bg.setImageResource(R.drawable.appbg_no);
//                GlideMgr.loadNormalDrawableImg(mContext, drawables, viewHolder.iv);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return convertView;
    }

}
