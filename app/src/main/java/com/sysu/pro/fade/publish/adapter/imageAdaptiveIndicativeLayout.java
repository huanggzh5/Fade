package com.sysu.pro.fade.publish.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.sysu.pro.fade.R;
import com.sysu.pro.fade.publish.PublishActivity;
import com.sysu.pro.fade.publish.imageselector.ClickToPreviewActivity;

import java.util.ArrayList;
import java.util.List;


import static com.sysu.pro.fade.publish.PublishActivity.publishActivity;

/**
 * Created by LaiXiancheng on 2017/8/14.
 *
 * FrameLayout，含有用于显示图片的viewPager，以及滑动之后会响应的图片下方点，
 * 可以通过高宽比调整自身高度（宽度为屏幕宽度），
 * 但是有最大高度限制，默认最大高度为400dp，可设置最大高度值
 * **************************************
 * 使用示例：
 * 1.在xml布局文件中使用imageAdaptiveIndicativeLayout（注意！请将高度设置为wrap_content）
 * 		<imageAdaptiveIndicativeLayout
 android:id="@+id/image_layout"
 android:layout_width="match_parent"
 android:layout_height="wrap_content"/>

 * 2.在java中（注意！setViewPagerMaxHeight()应该在setHeightByRatio()之前）
 *		imageLayout.setViewPagerMaxHeight(200);
 imageLayout.setHeightByRatio(ratio);
 imageLayout.setPaths(ImagePathList());
 * **************************************
 * @author LaiXiancheng/(lxc.sysu@qq.com)/
 * @version 1.1 修复布局上顶问题
 */
public class imageAdaptiveIndicativeLayout extends FrameLayout {
	private int viewPagerMaxHeight = 400;
	private mImageItemPagerAdapter imgAdapter;
	private static ViewPager pager;
	public static MyCallBack myCallBack;
	private LinearLayout dotLinearLayout;
	public static ArrayList<String> images;
	public static int newCount;

	public imageAdaptiveIndicativeLayout(@NonNull Context context) {
		super(context);
		init();
	}

	public imageAdaptiveIndicativeLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	/**
	 * 设置最大高度值
	 * @param newMaxHeight 新的最大高度
	 */
	public void setViewPagerMaxHeight(int newMaxHeight){
		viewPagerMaxHeight = newMaxHeight;
	}

	private void init() {
		inflate(getContext(), R.layout.image_pager_layout, this);
		this.pager = (ViewPager)findViewById(R.id.pager);
		this.dotLinearLayout = (LinearLayout) findViewById(R.id.linear_layout_dots);
	}

	public int getPosition() {
		return pager.getCurrentItem();
	}

	/**
	 * 根据通过高宽比调整自身高度（宽度为屏幕宽度）
	 * @param heightToWidthRatio 高宽比，注意是“高/宽”。建议传入所有图片高宽比中最大的那个高宽比
	 */
	public void setHeightByRatio(float heightToWidthRatio){
		ViewGroup.LayoutParams layoutParams = pager.getLayoutParams();
		int screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
		int fitHeight = (int)(heightToWidthRatio*screenWidth);
		int maxHeight = Dp2Px(viewPagerMaxHeight, getContext());
		int realHeight = maxHeight < fitHeight ? maxHeight :fitHeight;
		layoutParams.height = realHeight;
		layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
		pager.setLayoutParams(layoutParams);
	}

	/**
	 * 设置图片的路径，可以是本地路径，也可以是网络路径
	 * @param imagePathList
	 */
	public void setPaths(List<String> imagePathList,int position){
		imgAdapter = new mImageItemPagerAdapter(
				((AppCompatActivity)this.getContext()).getSupportFragmentManager(),imagePathList);
//		imgAdapter.getItem(position);
		pager.setAdapter(imgAdapter);
		pager.setCurrentItem(position);
		addDots(getContext(), dotLinearLayout,imagePathList.size());
		setDot(getContext(),
				 dotLinearLayout,position >= imagePathList.size()-1 ? imagePathList.size()-1 : position);
		//viewPager滑动的时候，设置下方点的变化
		pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}
			@Override
			public void onPageScrollStateChanged(int state) {
			}
			@Override
			public void onPageSelected(int position) {
				setDot(getContext(),dotLinearLayout,position);
			}


		});
	}


	/**
	 * 单位转化，dp转px
	 * @param padding_in_dp
	 * @param context
	 * @return
	 */
	int Dp2Px(int padding_in_dp, Context context){
		final float scale = context.getResources().getDisplayMetrics().density;
		return  (int) (padding_in_dp * scale + 0.5f);
	}


	public void setCallBack(MyCallBack callBack) {
		this.myCallBack = callBack;
	}

	public void notifyChanged() {
		imgAdapter.notifyDataSetChanged();
	}

	public void setImages(ArrayList<String> images, int newCount) {
		this.images = images;
		this.newCount = newCount;
	}

	/**
	 * 该pager的adapter，使用与fragment结合的方式
	 */
	private class mImageItemPagerAdapter extends FragmentStatePagerAdapter {

		public List<String> imagePathList;

		public mImageItemPagerAdapter(FragmentManager fm, List<String> imagePathList) {
			super(fm);
			this.imagePathList = imagePathList;
		}

		@Override
		public int getCount() {
			return imagePathList == null ? 0 : imagePathList.size();
		}

		@Override
		public Fragment getItem(int position) {
			String nextUrl = "";
			if (position+1 < imagePathList.size())
				nextUrl = imagePathList.get(position+1);
			return mImageItemFragment.newInstance(imagePathList, position, nextUrl);
		}

	}

	public static class mImageItemFragment extends Fragment {
		private String mImageUrl;
		private String mNextUrl;
		private ImageView mImageView;
		private int fragmentIndex;
		List<String> urlList;

		public static mImageItemFragment newInstance(List<String> urlList, int position,
													 String nextUrl) {
			final mImageItemFragment f = new mImageItemFragment();
			String imageUrl = urlList.get(position);
			final Bundle args = new Bundle();
			args.putString("url", imageUrl);
			args.putString("nextUrl", nextUrl);
			f.urlList = urlList;
			f.setArguments(args);

			return f;
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			mImageUrl = getArguments() != null ? getArguments().getString("url") : null;
			mNextUrl = getArguments() != null ? getArguments().getString("nextUrl") : null;

		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			final View v = inflater.inflate(R.layout.image_item_fragment, container, false);
			mImageView = (ImageView) v.findViewById(R.id.full_image);
			mImageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ClickToPreviewActivity.openActivity(publishActivity, images,
							newCount, pager.getCurrentItem());
					Toast.makeText(publishActivity,"click",Toast.LENGTH_LONG).show();
				}
			});
			return v;
		}

		/**
		 * 图片在这里加载进入imageView，修改或添加下面的Glide方法有更多自定义效果
		 * @param savedInstanceState
		 */
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

			Glide.with(this).load(mImageUrl).fitCenter().into(mImageView);
		}


	}


	/**
	 * 根据图片的数量来添加对应的点
	 * @param dotsLinearLayout 用于放“点”的linearLayout
	 * @param imgCnt 图片的数量
	 */
	public void addDots(Context context, LinearLayout dotsLinearLayout, int imgCnt){
		dotsLinearLayout.removeAllViews();
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Dp2Px(10,context),Dp2Px(10,context));

		if (imgCnt > 1){
			ImageView dotView = new ImageView(context);
			dotView.setImageResource(R.drawable.point_selected);
			dotView.setLayoutParams(lp);
			dotView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			dotsLinearLayout.addView(dotView);
			for(int i = 1; i < imgCnt; i++){
				dotView = new ImageView(context);
				dotView.setImageResource(R.drawable.point_unselected);
				dotView.setLayoutParams(lp);
				dotView.setScaleType(ImageView.ScaleType.FIT_CENTER);
				dotsLinearLayout.addView(dotView);
			}
		}
	}


	/**
	 * 设置图片滑动之后下方“点”的变化
	 * @param dotsLinearLayout 用于放“点”的linearLayout
	 * @param index 当前正在查看的图的索引号
	 */
	public void setDot(final Context context, LinearLayout dotsLinearLayout, int index){
		ImageView dotView;
		for(int i = 0; i < dotsLinearLayout.getChildCount(); i++){
			dotView = (ImageView) dotsLinearLayout.getChildAt(i);
			dotView.setImageResource(R.drawable.point_unselected);
		}

		dotView = (ImageView) dotsLinearLayout.getChildAt(index);
		if (index < dotsLinearLayout.getChildCount())
			dotView.setImageResource(R.drawable.point_selected);
		else
			Log.d("setDot","index out of range");
	}
}
