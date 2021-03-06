package com.sysu.pro.fade.publish.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.sysu.pro.fade.R;

import java.io.File;
import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> implements View.OnClickListener {

    private Context mContext;
    private ArrayList<String> mImages;
    private LayoutInflater mInflater;

    private RecycleViewLisitenter.onItemClickLisitenter onItem;
    private RecycleViewLisitenter.onItemLongClickLisitenter onLongItem;

    public ImageAdapter(Context context) {
        mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.adapter_image, parent, false);
//        View view = View.inflate(mContext, R.layout.adapter_image, null);
//        final ViewHolder holder = new ViewHolder(view);
        view.setOnClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final String image = mImages.get(position);
        holder.itemView.setTag(position);
        //在这里加载
        Glide.with(mContext).load(new File(image)).asBitmap().
                centerCrop().into(holder.ivImage);
    }

    @Override
    public int getItemCount() {
        return mImages == null ? 0 : mImages.size();
    }

    public void onClick(View v) {
        if(onItem!=null){
            onItem.onItemClick(v, (Integer) v.getTag());
        }
    }

//    public void refresh(ArrayList<String> images) {
//        mImages = images;
//        notifyDataSetChanged();
//    }



    //长按事件
    public void setOnItemLongClickLisitenter(RecycleViewLisitenter.onItemLongClickLisitenter onLongItem){
        this.onLongItem = onLongItem;
    }

    //点击事件
    public void setOnItemClickLisitenter(RecycleViewLisitenter.onItemClickLisitenter onItem){
        this.onItem = onItem;
    }



    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivImage;

        public ViewHolder(View itemView) {
            super(itemView);
            ivImage = (ImageView) itemView.findViewById(R.id.iv_image);
        }
    }
}
