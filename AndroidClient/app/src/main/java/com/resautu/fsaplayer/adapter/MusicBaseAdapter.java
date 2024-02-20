package com.resautu.fsaplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.resautu.fsaplayer.R;
import com.resautu.fsaplayer.data.ItemData;

import java.util.List;

public class MusicBaseAdapter extends BaseAdapter {
    private Context context;
    private List<ItemData> itemList;

    public MusicBaseAdapter(Context context, List<ItemData> itemList) {
        this.context = context;
        this.itemList = itemList;
    }
    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.audio_item, null);
        ImageView imageView = view.findViewById(R.id.iv_icon);
        TextView audio_name = view.findViewById(R.id.tv_audio_name);
        TextView audio_desc = view.findViewById(R.id.tv_audio_desc);
        audio_name.setText(itemList.get(position).getItemName());
        return view;
    }
}
