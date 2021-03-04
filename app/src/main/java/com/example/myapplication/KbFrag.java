package com.example.myapplication;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import static com.example.myapplication.MainActivity.margin;


public class KbFrag extends Fragment {
    private View view;
    private EditText input;
    private Button send;
    private OnMessageClick listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            listener=(OnMessageClick) context;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException("Activity必须实现OnMessageClicked");
        }
    }

    //在每次隐藏或者显示的时候调用用来进行监听器的附加
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden){
            //Fragment隐藏时调用

        }else {
            //Fragment显示时调用
//           删除之前的所有监听器以免误调用
            view.setOnClickListener(null);

//            初始化kbfrag的设置
            this.initEvent();
//            检查并且给监听器赋值以便可以回传参数
            try{
                listener=(OnMessageClick) getContext();
            }
            catch (ClassCastException e)
            {
                throw new ClassCastException("Activity必须实现OnMessageclicked");
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       view=inflater.inflate(R.layout.kbfrag,container,false);
        return view;
    }

    @Override
    public void onViewCreated(View view,Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void initEvent()
    {
//        将输入框input，按钮send的对象找到并且赋值
        input=view.findViewById(R.id.input);
        send=view.findViewById(R.id.sentmsg);
//        强制将焦点放在输入框，同时唤出键盘
        input.setText("");
        input.requestFocus();
        InputMethodManager imm = (InputMethodManager) MainApplication.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(input, InputMethodManager.SHOW_FORCED);

//        按下send以发送信息
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//    传递消息数据到activity中
                listener.onMessageClick(input.getText().toString());
            }
        });}

//        定义回调用的接口
    public interface OnMessageClick{
        void onMessageClick(String str);
    }
}