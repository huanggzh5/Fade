package com.sysu.pro.fade.message;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.sysu.pro.fade.Const;
import com.sysu.pro.fade.R;
import com.sysu.pro.fade.beans.AddMessage;
import com.sysu.pro.fade.beans.CommentMessage;
import com.sysu.pro.fade.beans.Note;
import com.sysu.pro.fade.beans.SimpleResponse;
import com.sysu.pro.fade.beans.User;
import com.sysu.pro.fade.message.Activity.CommentActivity;
import com.sysu.pro.fade.message.Activity.ContributionActivity;
import com.sysu.pro.fade.message.Activity.FansActivity;
import com.sysu.pro.fade.message.Adapter.ChatAdapter;
import com.sysu.pro.fade.message.Adapter.ConversationListAdapterEx;
import com.sysu.pro.fade.message.Class.NotificationUser;
import com.sysu.pro.fade.service.MessageService;
import com.sysu.pro.fade.utils.RetrofitUtil;
import com.sysu.pro.fade.utils.UserUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.RongContext;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imlib.model.Conversation;
import retrofit2.Retrofit;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * Created by road on 2017/7/14.
 */
public class ContentMessage {
    private FragmentActivity activity;
    private Context context;
    private View rootview;

    private RecyclerView notification_Rv;
    private ChatAdapter adapter;
    private List<NotificationUser> userList = new ArrayList<NotificationUser>();

    private int contributionCount;
    private int newFanCount;
    private int commentCount;

    private TextView processCountTv;
    private TextView newFanCountTv;
    private TextView commentCountTv;

    private User user;
    private Retrofit retrofit;
    private MessageService messageService;
    private Integer start;

    public static ViewPager mViewPager;
    private List<Fragment> mFragment = new ArrayList<>();
    private ImageView moreImage, mImageChats, mImageContact, mImageFind, mImageMe, mMineRed;
    private TextView mTextChats, mTextContact, mTextFind, mTextMe;
    private ImageView mSearchImageView;
    /**
     * 会话列表的fragment
     */
    private ConversationListFragment mConversationListFragment = null;
    private boolean isDebug;
    private Context mContext;
    private Conversation.ConversationType[] mConversationsTypes = null;

    public ContentMessage(FragmentActivity activity, Context context, View rootview){
        this.activity = activity;
        this.context = context;
        this.rootview = rootview;
        EventBus.getDefault().register(this);
        initNotification();
        initLayout();
        initListener();
        setNotification();  //设置消息数量
        initMsgViewPager();
    }

    private void initMsgViewPager() {
        Fragment conversationList = initConversationList();
        mViewPager = (ViewPager) rootview.findViewById(R.id.msg_viewpager);


        mFragment.add(conversationList);
        FragmentPagerAdapter fragmentPagerAdapter = new FragmentPagerAdapter(activity.getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFragment.get(position);
            }

            @Override
            public int getCount() {
                return mFragment.size();
            }
        };
        mViewPager.setAdapter(fragmentPagerAdapter);
        mViewPager.setOffscreenPageLimit(1);
    }


    private Fragment initConversationList() {
        if (mConversationListFragment == null) {
            ConversationListFragment listFragment = new ConversationListFragment();
            listFragment.setAdapter(new ConversationListAdapterEx(RongContext.getInstance()));
            Uri uri;
            if (isDebug) {
                uri = Uri.parse("rong://" + activity.getApplicationInfo().packageName).buildUpon()
                        .appendPath("conversationlist")
                        .appendQueryParameter(Conversation.ConversationType.PRIVATE.getName(), "true") //设置私聊会话是否聚合显示
                        .appendQueryParameter(Conversation.ConversationType.GROUP.getName(), "true")//群组
                        .appendQueryParameter(Conversation.ConversationType.PUBLIC_SERVICE.getName(), "false")//公共服务号
                        .appendQueryParameter(Conversation.ConversationType.APP_PUBLIC_SERVICE.getName(), "false")//订阅号
                        .appendQueryParameter(Conversation.ConversationType.SYSTEM.getName(), "true")//系统
                        .appendQueryParameter(Conversation.ConversationType.DISCUSSION.getName(), "true")
                        .build();
                mConversationsTypes = new Conversation.ConversationType[]{Conversation.ConversationType.PRIVATE,
                        Conversation.ConversationType.GROUP,
                        Conversation.ConversationType.PUBLIC_SERVICE,
                        Conversation.ConversationType.APP_PUBLIC_SERVICE,
                        Conversation.ConversationType.SYSTEM,
                        Conversation.ConversationType.DISCUSSION
                };

            } else {
                uri = Uri.parse("rong://" + activity.getApplicationInfo().packageName).buildUpon()
                        .appendPath("conversationlist")
                        .appendQueryParameter(Conversation.ConversationType.PRIVATE.getName(), "false") //设置私聊会话是否聚合显示
                        .appendQueryParameter(Conversation.ConversationType.GROUP.getName(), "false")//群组
                        .appendQueryParameter(Conversation.ConversationType.PUBLIC_SERVICE.getName(), "false")//公共服务号
                        .appendQueryParameter(Conversation.ConversationType.APP_PUBLIC_SERVICE.getName(), "false")//订阅号
                        .appendQueryParameter(Conversation.ConversationType.SYSTEM.getName(), "true")//系统
                        .build();
                mConversationsTypes = new Conversation.ConversationType[]{Conversation.ConversationType.PRIVATE,
                        Conversation.ConversationType.GROUP,
                        Conversation.ConversationType.PUBLIC_SERVICE,
                        Conversation.ConversationType.APP_PUBLIC_SERVICE,
                        Conversation.ConversationType.SYSTEM
                };
            }
            listFragment.setUri(uri);
            mConversationListFragment = listFragment;
            return listFragment;
        } else {
            return mConversationListFragment;
        }
    }

    private void initNotification() {
        user = new UserUtil(activity).getUer();
        retrofit = RetrofitUtil.createRetrofit(Const.BASE_IP,user.getTokenModel());
        messageService = retrofit.create(MessageService.class);
        messageService.getAddMessage(user.getUser_id().toString())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<AddMessage>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(AddMessage addMessage) {
                        contributionCount = addMessage.getAddContributeNum();
                        newFanCount = addMessage.getAddFansNum();
                        commentCount = addMessage.getAddCommentNum();
                        Log.d("yellow", "contribution: " + contributionCount);
                        if (contributionCount > 0) {
                            processCountTv.setVisibility(View.VISIBLE);
                            processCountTv.setText(String.valueOf(contributionCount));
                        }
                        if (newFanCount > 0) {
                            newFanCountTv.setVisibility(View.VISIBLE);
                            newFanCountTv.setText(String.valueOf(newFanCount));
                        }
                        if (commentCount > 0) {
                            commentCountTv.setVisibility(View.VISIBLE);
                            commentCountTv.setText(String.valueOf(commentCount));
                        }
                    }
                });
    }


    private void initLayout() {
        processCountTv = (TextView) rootview.findViewById(R.id.notification_process_count);
        newFanCountTv = (TextView) rootview.findViewById(R.id.notification_new_fan_count);
        commentCountTv = (TextView) rootview.findViewById(R.id.notification_comment_count);
        //notification_Rv = (RecyclerView) rootview.findViewById(R.id.notification_recyclerView);
    }

    private void setNotification() {
        Uri uri = Uri.parse("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1514632966065&di=c3b195646" +
                "3daaa6431d4cc26c04083a3&imgtype=0&src=http%3A%2F%2Fwww.ghost64.com%2" +
                "Fqqtupian%2FzixunImg%2Flocal%2F2017%2F03%2F09%2F14890377211618.jpg");
        int user_count = 22;
        String user_id = "黄路";
        String user_content =  "美国的小雷，比你们不知道高到哪里去了，你们还是太年轻";
        String user_time = "16:22";
//        contributionCount = 33;
//        newFanCount = 29;
//        commentCount = 99;
        userList.add(new NotificationUser(uri, user_count, user_id, user_content, user_time));
        Uri uri2 = Uri.parse("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1514632966065&di=c3b195646" +
                "3daaa6431d4cc26c04083a3&imgtype=0&src=http%3A%2F%2Fwww.ghost64.com%2" +
                "Fqqtupian%2FzixunImg%2Flocal%2F2017%2F03%2F09%2F14890377211618.jpg");
        int user_count2 = 0;
        String user_id2 = "胡文浩";
        String user_content2 =  "中国的小云，比你们不知道高到哪里去了，你们还是太年轻";
        String user_time2 = "2017-2-28";
        userList.add(new NotificationUser(uri2, user_count2, user_id2, user_content2, user_time2));
        adapter = new ChatAdapter(userList);
        //notification_Rv.setLayoutManager(new LinearLayoutManager(context));
        //notification_Rv.setAdapter(adapter);
    }

    private void initListener() {
        rootview.findViewById(R.id.notification_process_contribution).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //进度贡献查看
                Intent intent = new Intent(context, ContributionActivity.class);
                activity.startActivity(intent);
                contributionCount = 0;
                //消除贡献队列
                processCountTv.setVisibility(View.GONE);
            }
        });

        rootview.findViewById(R.id.notification_new_fan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //新的粉丝查看
                Intent intent = new Intent(context, FansActivity.class);
                activity.startActivity(intent);
                newFanCount = 0;
                //消除粉丝
                newFanCountTv.setVisibility(View.GONE);
            }
        });

        rootview.findViewById(R.id.notification_comment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //评论查看
                Intent intent = new Intent(context, CommentActivity.class);
                activity.startActivity(intent);
                commentCount = 0;
                //消除评论
                commentCountTv.setVisibility(View.GONE);
            }
        });
    }

    //根据msg消除通知
    @Subscribe(threadMode = ThreadMode.MAIN)
    public  void onGetSimpleResponse(SimpleResponse response){
        switch (response.getSuccess()){
            case "00":
                //贡献队列数量加一
                contributionCount++;
                processCountTv.setVisibility(View.VISIBLE);
                processCountTv.setText(String.valueOf(contributionCount));
                //续减秒产生的转发帖信息
                Note note = ((JSONObject) response.getExtra().get("note")).toJavaObject(Note.class);
                break;
            case "01":
                //粉丝队列数量加一
                newFanCount++;
                newFanCountTv.setVisibility(View.VISIBLE);
                newFanCountTv.setText(String.valueOf(newFanCount));
                //粉丝信息
                User user = ((JSONObject) response.getExtra().get("user")).toJavaObject(User.class);
                break;
            case "02":
                //评论数量加一
                commentCount++;
                commentCountTv.setVisibility(View.VISIBLE);
                commentCountTv.setText(String.valueOf(commentCount));
                //评论信息
                CommentMessage message = ((JSONObject) response.getExtra().get("message")).toJavaObject(CommentMessage.class);
                break;
        }
    }
}
