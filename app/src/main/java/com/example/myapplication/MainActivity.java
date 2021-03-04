package com.example.myapplication;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.view.inputmethod.InputMethodManager;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
import com.carlos.voiceline.mylibrary.VoiceLineView;
import com.iflytek.cloud.*;
import com.kymjs.rxvolley.RxVolley;
import com.kymjs.rxvolley.client.HttpCallback;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

//版本号：1.6
//已知bug，
// 1.彩蛋没有全部完成
//2.edit界面会闪动

//
public class MainActivity extends AppCompatActivity
        implements MainFrag.OnFABClick , KbFrag.OnMessageClick, MicFrag.OnMediaClick,Runnable{
//    消息显示用队列，全局唯一，动态存储消息
    public static List<Msg> msgList=new ArrayList<>();
    //    碎片实例化
    private FragmentManager mFragmentManager;
    private Fragment currentFragment;
    private FragmentTransaction ft;
    private MainFrag mainFrag=new MainFrag();
    private KbFrag kbFrag=new KbFrag();
    private MicFrag micFrag=new MicFrag();
    private long exitTime;
    private int maxHeight;
    public static int hideHeight,showHeight;
    public static LinearLayout margin;
    private boolean isAlive;
    private static int voiceDB;
    private boolean firstFix=true;
    private  Thread thread = new Thread(this);
    private final static String TAG = MainActivity.class.getSimpleName();
    //语音听写对象
    private SpeechRecognizer mRecognize;
    //语音合成对象
    private SpeechSynthesizer mCompose;
    //默认发音人
    private String voicer = "xiaoyan";
    //用HashMap存储听写结果
    private HashMap<String, String> mRecognizeResults = new LinkedHashMap<String, String>();

    private String question;

    private String answer;

    private boolean firstTime = true;

    private VoiceLineView voiceLineView;
    @SuppressLint("HandlerLeak")
    final private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            return false;
        }
    }) {
        @SuppressLint("HandlerLeak")
        @Override
        public  void handleMessage(Message msg) {//负责改变波形
            super.handleMessage(msg);
            double ratio = (double)voiceDB;
            double db = 0;// 分贝
            //默认的最大音量是100,可以修改，但其实默认的，在测试过程中就有不错的表现
            //你可以传自定义的数字进去，但需要在一定的范围内，比如0-200，就需要在xml文件中配置maxVolume
            //同时，也可以配置灵敏度sensibility
            if (ratio > 1) {
                db = 8 *(double)ratio;
//                db=(int)(1+Math.random()*(70-1+1));
            }
            //只要有一个线程，不断调用这个方法，就可以使波形变化
            //主要，这个方法必须在ui线程中调用
            voiceLineView.setVolume((int) (db));
        }
    };


    public MainActivity() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //关闭状态栏
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_main);//设置初始布局
//      设置进去后的第一块碎片为主碎片mainfrag
        mainFrag=new MainFrag();
        mFragmentManager= getSupportFragmentManager();
        ft=mFragmentManager.beginTransaction();
        ft.add(R.id.mainContainer,mainFrag);
        ft.add(R.id.mainContainer,micFrag);
        ft.add(R.id.mainContainer,kbFrag);
        ft.hide(micFrag);
        ft.hide(kbFrag);
        ft.commitAllowingStateLoss();
        currentFragment=mainFrag;

        Rect rect = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        maxHeight=rect.height();
//监听键盘是否弹出
//        SoftKeyBoardListener.setListener(MainActivity.this, new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {
//
//            @Override
//            public void keyBoardShow(int height) {
////设置空白占位组件的高度来进行屏幕的自适应
//                margin=findViewById(R.id.magin);
//                ViewGroup.LayoutParams lp=margin.getLayoutParams();
////进行屏幕高度的适配
//                if(firstFix) {
//                   showHeight = maxHeight - height
//                            - ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 135, getResources().getDisplayMetrics()));
//                    margin.setLayoutParams(lp);
//                    hideHeight=showHeight+height;
//                    firstFix=false;
////                Toast.makeText(MainActivity.this, "键盘显示 高度" + height, Toast.LENGTH_SHORT).show();
//                }
//                    ValueAnimator va;
//                    va = ValueAnimator.ofInt(lp.height, showHeight);
//
//                    va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                        @Override
//                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                            //获取当前的height值
//                            int h = (Integer) valueAnimator.getAnimatedValue();
//                            //动态更新view的高度
//                            margin.getLayoutParams().height = h;
//                            margin.requestLayout();
//                        }
//                    });
//                    va.setDuration(150);
//                    //开始动画
//                    va.start();
//
//
//            }
//
//
//
//            @Override
//
//            public void keyBoardHide(int height) {
//                margin=findViewById(R.id.magin);
//                ViewGroup.LayoutParams lp=margin.getLayoutParams();
//                ValueAnimator va ;
//                va = ValueAnimator.ofInt(lp.height,hideHeight);
//
//                va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                    @Override
//                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                        //获取当前的height值
//                        int h =(Integer)valueAnimator.getAnimatedValue();
//                        //动态更新view的高度
//                        margin.getLayoutParams().height = h;
//                        margin.requestLayout();
//                    }
//                });
//                va.setDuration(150);
//                //开始动画
//                va.start();
//            }
//
//        });

        //激活图灵机器人
        machine("你好");
        soundPermissions();
        //初始化识别无UI识别对象，使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mRecognize = SpeechRecognizer.createRecognizer(this, mInitListener);
       // 初始化合成对象
       mCompose = SpeechSynthesizer.createSynthesizer(this, mInitListener);
    }

    @Override
    public void onClick(int i) {
        switch(i) {
            case   1:
//获取当前屏幕可见区域的大小


//                设置当前界面为kbFrag

//                获取frag容器的大小

                this.showFragment(kbFrag);


//                设置用于适配的空白占格的大小
                break;
            case   2:this.showFragment(micFrag);
                     int ret = 0;
                     isAlive=true;
                     voiceLineView=findViewById(R.id.voiceLine);
                        //设置参数
                        mRecognizeResults.clear();
                        resetParam();
                        ret = mRecognize.startListening(mRecognizeListener);
                        if (ret != ErrorCode.SUCCESS) {
                            showTip("听写失败，错误码：" + ret);
                        } else {
                            showTip("请开始说话");
                        }
                        thread=new Thread(this);
                thread.start();
                break;
            case   3:

//跑去相机
            default:break;
        }

    }


    private void showFragment(Fragment fg){
        //如果之前没有添加过
        ft=getSupportFragmentManager().beginTransaction();
for(int j=msgList.size()-1;j>-1;j--) {
    if (msgList.get(j).getContent().equals("彩虹小马模式")) {
        ImageView horse = findViewById(R.id.cloud);
        horse.setImageResource(R.drawable.horse);
        MicFrag.horse.setImageResource(R.drawable.kick);
        showTip("切换到彩虹小马模式");
        break;
    }
    else  if(msgList.get(j).getContent().equals("彩虹小马走开"))
    {
        ImageView horse = findViewById(R.id.cloud);
        horse.setImageResource(R.drawable.cloud);
       MicFrag.horse.setImageResource(R.drawable.punch);
        showTip("彩虹小马溜了");
        break;
    }
    if(msgList.get(j).getContent().equals("初始化"))
    {
        ImageView horse = findViewById(R.id.cloud);
        horse.setImageResource(R.drawable.cloud);
        MicFrag.horse.setImageResource(R.drawable.margin);
        showTip("彩虹小马溜了");
        break;
    }
}
        if(!fg.isAdded()){
           ft.hide(currentFragment);
           ft.add(R.id.content,fg);
        }
        else{
           ft.hide(currentFragment);
           ft.show(fg);
        }
        //全局变量，记录当前显示的fragment
        currentFragment = fg;
       ft.commitAllowingStateLoss();
    }

 //在键盘用于传回来消息数据的同时将Frag切换回来
    public void onMessageClick(String str) {
//将消息进行比较分析，同时收起键盘，
        InputMethodManager imm = (InputMethodManager) getBaseContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
//        首先把问的问题插入消息队列中
        if (!"".equals(str)) {
            question = str;
            if (keyWordDetect(question)){
                receiveAnswer();
                //showFragment(mainFrag);
            }
//        此处将回复插入消息队列
        }
        else{
            Msg msg = new Msg("未检测到有效输入", Msg.TYPE_RECEIVED);
           msgList.add(msg);
            showFragment(mainFrag);
        }
    }

//在麦克风界面设置监听将消息传回来
    public void onMediaClick(String str) {
//        此处设置
        Msg msg = new Msg("您已终止语音识别，自动返回主界面", Msg.TYPE_RECEIVED);
        msgList.add(msg);
        this.showFragment(mainFrag);
    }

//    设置用户按两下返回键程序自动退出
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
        {

            if((System.currentTimeMillis()-exitTime) > 2000)  //System.currentTimeMillis()无论何时调用，肯定大于2000
            {
                Toast.makeText(getApplicationContext(), "再按一次退出程序",Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            }
            else
            {
                finish();
                System.exit(0);
            }

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void run() {//runnable的run方法，这一块的录音目前是确定的一段时间，后期可以修改为一段时间没有接受录音就退出，现在线不弄
        while (isAlive) {
            handler.sendEmptyMessage(0);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //初始化监听器
    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code);
            }
        }
    };

    //听写监听器
    private RecognizerListener mRecognizeListener = new RecognizerListener() {
        @Override
        public void onVolumeChanged(int i, byte[] bytes) {//实时音量
            Log.d(TAG,"当前音量大小：" + i);
            voiceDB=i;
            //Toast.makeText(getApplicationContext(), String.valueOf(voiceDB),Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBeginOfSpeech() {
            //showTip("开始说话");
        }

        @Override
        public void onEndOfSpeech() {
            isAlive=false;
            showTip("结束说话");
        }

        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
            Log.d(TAG, recognizerResult.getResultString());
            System.out.println("成功");
            printResult(recognizerResult);
            if (b && keyWordDetect(question)){
                receiveAnswer();
                //showFragment(mainFrag);
            }

        }

        @Override
        public void onError(SpeechError speechError) {

        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };

    //合成监听器
    private SynthesizerListener mComposeListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {

        }

        @Override
        public void onSpeakPaused() {

        }

        @Override
        public void onSpeakResumed() {

        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {

        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {

        }

        @Override
        public void onCompleted(SpeechError error) {

        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {

        }
    };


    //输出语音听写结果
    private void printResult(RecognizerResult results){
        String text = JsonParser.parseIatResult(results.getResultString());
        String sn = null;
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        mRecognizeResults.put(sn, text);
        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mRecognizeResults.keySet()) {
            resultBuffer.append(mRecognizeResults.get(key));
        }
        question = null;
        question = resultBuffer.toString();
    }

    private void showTip(final String str){
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    //听写参数设置
    public void resetParam(){
        //清空参数
        mRecognize.setParameter(SpeechConstant.PARAMS, null);
        //设置听写引擎。TYPE_LOCAL表示本地，TYPE_CLOUD表示云端，TYPE_MIX 表示混合
        mRecognize.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        //设置返回结果格式
        mRecognize.setParameter(SpeechConstant.RESULT_TYPE, "json");
        mRecognize.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        //设置语言区域
        mRecognize.setParameter(SpeechConstant.ACCENT, "mandarin");


        //设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mRecognize.setParameter(SpeechConstant.VAD_BOS, "4000");
        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mRecognize.setParameter(SpeechConstant.VAD_EOS, "1000");
        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mRecognize.setParameter(SpeechConstant.ASR_PTT, "1");
        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        mRecognize.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mRecognize.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/recognize.wav");
    }

    //合成参数设置
    private void setParam() {
        // 清空参数
        mCompose.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        mCompose.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 设置在线合成发音人
        mCompose.setParameter(SpeechConstant.VOICE_NAME, voicer);
        //设置合成语速
        mCompose.setParameter(SpeechConstant.SPEED, "50");
        //设置合成音调
        mCompose.setParameter(SpeechConstant.PITCH, "50");
        //设置合成音量
        mCompose.setParameter(SpeechConstant.VOLUME, "50");
        //设置播放器音频流类型
        mCompose.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 设置播放合成音频打断音乐播放，默认为true
        mCompose.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mCompose.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mCompose.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/recognize.wav");
    }

    //定义录音的动态权限
    private void soundPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.RECORD_AUDIO}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

        }
        else{
            Toast.makeText(this, "用户拒绝了权限", Toast.LENGTH_SHORT).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void machine(String question){
        //定义URL
        //图灵机器人接口地址：http://www.tuling123.com/openapi/api
        //key=后接在图灵官网申请到的apikey
        //info后接输入的内容
        String url = "http://www.tuling123.com/openapi/api?" +
                "key=" + "aa72eb9f48e64efa9afdfda1a55136af" + "&info=" + question;
        //RxVolley将信息发出（添加RxVolley依赖，
        // 在app的build.gradle的ependencies中添加compile 'com.kymjs.rxvolley:rxvolley:1.1.4'）

        RxVolley.get(url, new HttpCallback() {
            @Override
            public void onSuccess(String t) {
                //解析返回的JSON数据
                pasingJson(t);
            }
        });

        Log.e("TESTQOUSTION",question);

    }

    private void pasingJson(String message){
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(message);
            //通过key（text）获取value
            answer= jsonObject.getString("text");
            Log.i("TESTANSWER",answer);
            if (!firstTime){
                showAnswer(answer);
                mCompose.startSpeaking(answer,mComposeListener);
                showFragment(mainFrag);
            }
            else {
                firstTime = false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showQuestion(String str){
        Msg msg= new Msg(str,Msg.TYPE_SENT);
        msgList.add(msg);
    }

    private void showAnswer(String str){
        Msg msg= new Msg(str,Msg.TYPE_RECEIVED);
        msgList.add(msg);

    }

    public void receiveAnswer(){
        showQuestion(question);
        machine(question);
    }

    public boolean keyWordDetect(String str){
        if ((str.indexOf("打开") != -1) || (str.indexOf("启动") != -1)){
            showQuestion(question);
            String content=str.substring(2,str.length()-1);
            String pkgName=null;
            if(getPackageName(content)!=null){
                pkgName=getPackageName(content);
            }
            else {
                pkgName="com.bbk.appstore";
            }
            Intent intent = getPackageManager().getLaunchIntentForPackage(pkgName);
            intent.putExtra("type", "110");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return false;
        }
        else{
            return true;
        }
    }

    final String getPackageName(String appName){
        String packageName=null;
        ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final PackageManager packageManager = getPackageManager();
        List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
        for (int i = 0; i < apps.size(); i++) {
            ResolveInfo info = apps.get(i);
            if(info.activityInfo.loadLabel(packageManager).toString().equals(appName)) {
                packageName= info.activityInfo.applicationInfo.packageName;
            }
        }
        return packageName;
    }
}