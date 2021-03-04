package com.example.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;


public class MainFrag extends Fragment {
//    创造监听器接口以传递参数
    private OnFABClick listener;
//    创造监听器以识别不同的点击事件
    private View.OnClickListener tmpListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(view.getId()==R.id.kbenter)
            {
                listener.onClick(1);
                Log.e("kb",String.valueOf(1));
            }else if(view.getId()==R.id.mic){
                listener.onClick(2);
                Log.e("mic",String.valueOf(2));
            }
            else if (view.getId()==R.id.camera)
            {
                listener.onClick(3);
                Log.e("camera",String.valueOf(3));
            }
        }
    };

    private FloatingActionButton mic,kb,camera;
    private MsgAdapter adapter;
    private RecyclerView msgRecyclerView;
    private SpaceItemDecoration doctor=new SpaceItemDecoration(10);

//    在Frg附加到界面上的时候布置监听器
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            listener=(OnFABClick) context;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException("Activity必须实现OnFABclicked");
        }
    }

    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden){
            //Fragment隐藏时调用
            msgRecyclerView.removeItemDecoration(doctor);
        }else {

            msgRecyclerView.addItemDecoration(doctor);
            try{
                listener=(OnFABClick)getContext();
            }
            catch (ClassCastException e)
            {
                throw new ClassCastException("Activity必须实现OnFABclicked");
            }
            adapter.notifyItemInserted(MainActivity.msgList.size() - 1);
            msgRecyclerView.scrollToPosition(MainActivity.msgList.size() - 1);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.mainfrag,container,false);
//        为三个按钮创建引用

        return view;
    }

    @Override
    public void onViewCreated(View view,Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        kb=view.findViewById(R.id.kbenter);
        mic=view.findViewById(R.id.mic);
        camera=view.findViewById(R.id.camera);

//      为键盘创建鼠标监听事件
        kb.setOnClickListener(tmpListener);

//        为麦克疯创建鼠标监听事件
        mic.setOnClickListener(tmpListener);

//        为照相机创建鼠标监听事件
        camera.setOnClickListener(tmpListener);

//        为消息显示模块附加adapter，注意消息队列全局唯一，使用MainActivity中的msgList队列
        msgRecyclerView=(RecyclerView)view.findViewById(R.id.recycle);//获取中间的轮转
        msgRecyclerView.setLayoutManager( new LinearLayoutManager(getActivity()));

        adapter = new MsgAdapter(MainActivity.msgList);
        msgRecyclerView.setAdapter(adapter);

//        向消息队列数据中添加新的数据的实例，第一行为给定的消息
//        第二行让adapter注意到有新的消息，
//        第三行将消息转到最新的一行
        MainActivity.msgList.add(new Msg("你好，我是彩虹小马机器人，以后请多多指教！",Msg.TYPE_RECEIVED));
        adapter.notifyItemInserted(MainActivity.msgList.size() - 1);
        msgRecyclerView.scrollToPosition(MainActivity.msgList.size() - 1);
    }

    public interface OnFABClick{
     void onClick(int i);
    }
}