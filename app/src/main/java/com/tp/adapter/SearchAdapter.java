package com.tp.adapter;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.tp.listener.FilterListener;
import com.tp.utils.AppMain;
import com.musicbox.R;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

import org.jsoup.helper.StringUtil;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class SearchAdapter extends BaseAdapter implements Filterable {


    private List<String> beans;
    private Context mContext;
    private static int selectedIndex = -2;        //记录当前选中的条目索引
    private List<Integer> ls = new ArrayList<>();
    private MyFilter filter = null;// 创建MyFilter对象
    private FilterListener listener = null;// 接口对象
    private boolean isClicked = false;

    private static StringBuffer sb = new StringBuffer();


    public SearchAdapter(Context context,List<String> list, FilterListener filterListener) {
        this.mContext = context;
        this.listener = filterListener;
        this.beans = list;
    }


    public void setSelectedIndex(int position,boolean b) {
        this.selectedIndex = position;
        this.isClicked = b;
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return this.beans != null ? this.beans.size() : 0;
    }

    @Override
    public String getItem(int position) {
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
            convertView = LayoutInflater.from(AppMain.ctx()).inflate(R.layout.rv_item, null);
            viewHolder.iv = convertView.findViewById(R.id.iv_bg);
            viewHolder.imageView = convertView.findViewById(R.id.iv_se);
            viewHolder.tv = convertView.findViewById(R.id.tv_songname);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        String songname = beans.get(position).replace(".mp4","").replace(".mp3","").replace(".avi","")
                .replace(".mpg","").replace(".dat","").replace(".mkv","")
                .replace(".vob","").replace(".wmv","").replace(".mov","");
        songname = songname.substring(songname.lastIndexOf("/")+1);
        viewHolder.tv.setText(songname);
        Log.e("tag","selectedIndex="+selectedIndex+"     position="+position);
        if (selectedIndex == position){
            //选中状态
            viewHolder.imageView.setImageResource(R.drawable.notick);
            viewHolder.tv.setTextColor(Color.parseColor("#F08516"));
            if (isClicked) {
                ls.add(selectedIndex);
            }
        } else {
            //非选中状态
            viewHolder.imageView.setImageResource(R.drawable.notick);
            viewHolder.tv.setTextColor(Color.parseColor("#FFFFFF"));
        }

        for (int i = 0; i < ls.size(); i++) {
            if (ls.get(i) == position){
                viewHolder.imageView.setImageResource(R.drawable.tick);
                viewHolder.tv.setTextColor(Color.parseColor("#F08516"));
            }
        }
        return convertView;
    }



    @Override
    public Filter getFilter() {
        // 如果MyFilter对象为空，那么重写创建一个
        if (filter == null) {
            filter = new MyFilter(beans);
        }
        return filter;

    }

    class MyFilter extends Filter {
        // 创建集合保存原始数据
        private List<String> original = new ArrayList<String>();

        public MyFilter(List<String> list) {
            this.original = list;
        }
        /**
         * 该方法返回搜索过滤后的数据
         */
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            // 创建FilterResults对象
            FilterResults results = new FilterResults();

            /**
             * 没有搜索内容的话就还是给results赋值原始数据的值和大小
             * 执行了搜索的话，根据搜索的规则过滤即可，最后把过滤后的数据的值和大小赋值给results
             *
             */
            if(TextUtils.isEmpty(constraint)){
                results.values = original;
                results.count = original.size();
            }else {
                // 创建集合保存过滤后的数据
                List<String> mList = new ArrayList<String>();
                // 遍历原始数据集合，根据搜索的规则过滤数据
                for(String s: original){
                    // 这里就是过滤规则的具体实现【规则有很多，大家可以自己决定怎么实现】
                    String s1 = s.replace(".mp4","").replace(".mp3","").replace(".avi","")
                            .replace(".mpg","").replace(".dat","").replace(".mkv","")
                            .replace(".vob","").replace(".wmv","").replace(".mov","");
                    s1 = s1.substring(s1.lastIndexOf("/")+1);
//                    Log.e("Tag","s1="+s1);
                    //判断是否含有中文
                    if (isChinese(s1)) {
                        //是中文就是转换为拼音首字母
                        String ss = getPinYinHeadChar(s1);
                        if (ss.trim().toUpperCase().startsWith(constraint.toString().trim().toUpperCase())) {
                            // 规则匹配的话就往集合中添加该数据
                            mList.add(s);
                        }
                    }else {
                        if (s1.trim().toUpperCase().startsWith(constraint.toString().trim().toUpperCase())) {
                            // 规则匹配的话就往集合中添加该数据
                            mList.add(s);
                        }
                    }
                }
                results.values = mList;
                results.count = mList.size();
            }

            // 返回FilterResults对象
            return results;

        }


        // 判断一个字符是否是中文
        public boolean isChinese(char c) {
            return c >= 0x4E00 && c <= 0x9FA5;// 根据字节码判断
        }

        // 判断一个字符串是否含有中文
        public boolean isChinese(String str) {
            if (str == null) {
                return false;
            }
            for (char c : str.toCharArray()) {
                if (isChinese(c))
                    return true;// 有一个中文字符就返回
            }
            return false;
        }


        /**
         * 该方法用来刷新用户界面，根据过滤后的数据重新展示列表
         */

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // 获取过滤后的数据
            beans = (List<String>) results.values;
            // 如果接口对象不为空，那么调用接口中的方法获取过滤后的数据，具体的实现在new这个接口的时候重写的方法里执行
            if(listener != null){
                listener.getFilterData(beans);
            }
            // 刷新数据源显示
            notifyDataSetChanged();

        }


        /**
         * 获取汉字字符串的首字母，英文字符不变
         * 例如：阿飞→af
         */
        public String getPinYinHeadChar(String chines) {
            sb.setLength(0);
            char[] chars = chines.toCharArray();
            HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
            defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
            defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
            for (int i = 0; i < chars.length; i++) {
                if (chars[i] > 128) {
                    try {
                        sb.append(PinyinHelper.toHanyuPinyinStringArray(chars[i], defaultFormat)[0].charAt(0));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    sb.append(chars[i]);
                }
            }
            return sb.toString();
        }


        /**
         * 获取汉字字符串的汉语拼音，英文字符不变
         */
        public String getPinYin(String chines) {
            sb.setLength(0);
            char[] nameChar = chines.toCharArray();
            HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
            defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
            defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
            for (int i = 0; i < nameChar.length; i++) {
                if (nameChar[i] > 128) {
                    try {
                        sb.append(PinyinHelper.toHanyuPinyinStringArray(nameChar[i], defaultFormat)[0]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    sb.append(nameChar[i]);
                }
            }
            return sb.toString();
        }
    }
}
