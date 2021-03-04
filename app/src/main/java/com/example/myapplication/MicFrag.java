package com.example.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.carlos.voiceline.mylibrary.VoiceLineView;
import pl.droidsonroids.gif.GifImageView;


public class MicFrag extends Fragment {
    private View view;
    private OnMediaClick listener;
    public static GifImageView horse;
    private VoiceLineView voice;



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            listener=(OnMediaClick) context;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException("Activity必须实现OnMediaClicked接口");
        }
    }

    //在每次隐藏或者显示的时候调用用来进行监听器的附加
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden){
            //Fragment隐藏时调用

        }else {
            //Fragment显示时调用
//            关闭之前的鼠标点击事件。不然可能重叠
            view.setOnClickListener(null);
            try{
                listener=(OnMediaClick) getContext();
            }
            catch (ClassCastException e)
            {
                throw new ClassCastException("Activity必须实现OnMediaClicked接口");
            }
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
      view = inflater.inflate(R.layout.micfrag, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view,Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        将组件与对应对象适配
        voice=view.findViewById(R.id.voiceLine);
        horse=view.findViewById(R.id.kick);

//        设置点击事件，点击之后程序识别录音并且返回
      voice.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//    传递消息数据到activity
                        listener.onMediaClick("给你马来一记kick");
                    }
                });
//                为消息显示模块附加adapter，注意消息队列全局唯一，使用MainActivity中的msgList队列
        horse.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//    传递消息数据到activity
                        listener.onMediaClick("给你马来一记kick");
                    }
                });
        //        为消息显示模块附加adapter，注意消息队列全局唯一，使用MainActivity中的msgList队列


    }

    public interface OnMediaClick{
        void onMediaClick(String str);
    }

}