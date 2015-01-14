package com.example.lee.simpledragtorefreshdemo.Views;

import android.content.Context;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lee.simpledragtorefreshdemo.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Created by Administrator on 2014/12/5.
 * 使用方法：
 *      实现IRefreshListener，在onRefresh()方法里写数据更新的方法
 *      加载完成后，在onRefresh 方法内调用refreshComplete() 方法
 *      使用setInterface方法传入IRefreshListener对象
 */
public class ReFreshListView extends ListView implements AbsListView.OnScrollListener{

    View header;            //顶部布局文件
    TextView tip;           //提示字符
    ImageView arrow;        //箭头图片
    ProgressBar progress;   //进度条
    int headerHeight;       //顶部布局文件的高度
    int firstVisibleItem;   //当前第一个可见的item的位置
    int scrollState;        //listview当前滚动状态
    boolean isRemark;       //标记当前是否是在listview最顶端按下的
    int startY;             //按下时的Y值

    int state;              //当前的状态
    final int NONE = 0;     //正常状态
    final int PULL = 1;     //下拉状态
    final int RELEASE = 2;   //提示释放
    final int REFRESHING = 3;//正在刷新
    IRefreshListener iRefreshListener;//刷新数据的接口
    android.os.Handler handler = new android.os.Handler(){
        @Override
        public void handleMessage(Message msg) {
            Log.i("LIXU","what"+Integer.toString( msg.what));
            switch (msg.what){
                case 123:
                    Log.i("LIXU","handler:"+Integer.toString(header.getPaddingTop()));
                    topPadding((int)(header.getPaddingTop() * 0.75));    //慢回弹
                    break;
            }
        }
    };
    public ReFreshListView(Context context) {
        super(context);
        initView(context);
    }

    public ReFreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public ReFreshListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);

    }

    /**
     * 添加顶部布局文件
     * */
    private void initView (Context context){
        LayoutInflater inflater = LayoutInflater.from(context);
        header = inflater.inflate(R.layout.header_refresh,null);
        measureView(header);
        headerHeight = header.getMeasuredHeight();
        topPadding(-headerHeight);
        this.addHeaderView(header);     //添加布局文件
        this.setOnScrollListener(this);

        tip = (TextView)header.findViewById(R.id.tip);
        arrow = (ImageView)header.findViewById(R.id.arrow);
        progress = (ProgressBar)header.findViewById(R.id.progress);
    }

    /**
     * 通知父布局占用的宽高
     * @param view
     */
    private  void measureView(View view){
        ViewGroup.LayoutParams p =view.getLayoutParams();
        if (p == null){
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

        }
        int width = ViewGroup.getChildMeasureSpec(0, 0, p.width);
        int height;
        int tempHeight = p.height;
        if  (tempHeight>0) {
            height = MeasureSpec.makeMeasureSpec(tempHeight, MeasureSpec.EXACTLY);
        }else{
            height = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        view.measure(width,height);
    }
/**
 * 设置上边距
 * */
    private void topPadding(int topPadding){
        header.setPadding(header.getPaddingLeft(),topPadding,header.getPaddingRight(),header.getPaddingBottom());
        header.invalidate();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        this.scrollState = scrollState;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        this.firstVisibleItem = firstVisibleItem;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                if (firstVisibleItem==0){
                    isRemark=true;      //在最顶端按下屏幕
                    startY = (int)ev.getY();
                }
                break;
            case MotionEvent.ACTION_UP:
                if(state == RELEASE){
//                    while(header.getPaddingTop()>50)
//                    {
//                        Log.i("LIXU","successtouch"+Integer.toString((int)(header.getPaddingTop() * 0.9)));
//                        topPadding((int)(header.getPaddingTop() * 0.9));

//                        try {
//                            Thread.currentThread().sleep(50);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }

                    //}

                    //topPadding((int) ev.getY() - startY - headerHeight);
//                    new Thread() {
//                        public void run(){
//                            Message msg=handler.obtainMessage();
//                            Log.i("LIXU","successtouch"+Integer.toString(header.getPaddingTop()));
//                        while(header.getPaddingTop()>50)
//                        {
//                            msg.what = 123;
//                            Log.i("LIXU", Integer.toString(header.getPaddingTop()));
//                            handler.sendMessageDelayed(msg,5);
//
//                            Log.i("LIXU", "SB1");
//                            try {
//                                sleep(5);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                    }.start();
                    // handler.post(slowBack);     //将UI更新事件发送给主线程

                    state = REFRESHING;
                    //加载最新数据
                    refreshViewByState(state);
                    //调用onFresh方法
                    iRefreshListener.onRefresh();
                }else if(state==PULL){      //在下拉状态抬起手指的动作
                    state = NONE;
                    isRemark = false;
                    refreshViewByState(state);
            }
                break;
            case MotionEvent.ACTION_MOVE:
                onMove(ev);
                break;
        }
        return super.onTouchEvent(ev);
    }


    /**
     * 判断移动过程中的操作，对当前状态进行转换
     * @param ev
     */
    private void onMove(MotionEvent ev){
        if(!isRemark){
            return;
        }
        int tempY = (int)ev.getY();     //开始移动时的坐标
        int space = tempY - startY;     //移动的距离
        int topPadding = space - headerHeight;  //header顶部坐标
        switch (state){
            case NONE:
                if(space>0){
                    state = PULL;
                    refreshViewByState(state);
                }
                break;
            case PULL:
                topPadding(topPadding);
                if(space>headerHeight+30
                     &&scrollState == SCROLL_STATE_TOUCH_SCROLL){
                    state = RELEASE;
                    refreshViewByState(state);
                }
                break;
            case RELEASE:
                topPadding(topPadding);
                if(topPadding<0){   //顶部坐标小于0时状态转换为“下拉刷新”
                    state = PULL;
                    refreshViewByState(state);
                }else if(topPadding>0){         //当header完全显示时，下拉速度减小
                    topPadding(space/3);
                }
                else if(space<=0){
                    state = NONE;
                    isRemark = false;
                    refreshViewByState(state);
                }
                break;
        }
    }

    /**
     * 根据当前状态刷新界面
     * @param st
     */
    private void refreshViewByState(int st){

        //箭头反转动画
        RotateAnimation animation = new RotateAnimation(0,180,
                RotateAnimation.RELATIVE_TO_SELF,0.5f,
                RotateAnimation.RELATIVE_TO_SELF,0.5f);
        animation.setDuration(200);
        animation.setFillAfter(true);           //动画结束后不回到原位
        RotateAnimation animation1 = new RotateAnimation(180,0,
                RotateAnimation.RELATIVE_TO_SELF,0.5f,
                RotateAnimation.RELATIVE_TO_SELF,0.5f);
        animation1.setDuration(200);

        switch (st){
            case  NONE:
                arrow.clearAnimation();
                topPadding(-headerHeight);
                break;
            case  PULL:
                arrow.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                tip.setText("下拉可以刷新");
                arrow.clearAnimation();
                arrow.setAnimation(animation1);
                break;
            case  RELEASE:
                arrow.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                tip.setText("松开可以刷新");
                arrow.clearAnimation();
                arrow.setAnimation(animation);
                break;
            case  REFRESHING:
                topPadding(50);
                arrow.setVisibility(View.GONE);
                progress.setVisibility(View.VISIBLE);
                tip.setText("正在刷新");
                arrow.clearAnimation();
                break;
        }
    }

    /**
     *表示刷新结束
     */
    public void refreshComplete(){
        state = NONE;
        isRemark = false;
        refreshViewByState(state);
        TextView lastUpadteTime=(TextView)header.findViewById(R.id.lastupdateTime);
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 hh:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String time = format.format(date);
        lastUpadteTime.setText(time);
    }

    public void setInterface(IRefreshListener iRefreshListener){
        this.iRefreshListener = iRefreshListener;
    }

    public interface IRefreshListener{
        //当刷新时调用此回调方法，用于加载刷新后的数据
        public void onRefresh();
    }
}
