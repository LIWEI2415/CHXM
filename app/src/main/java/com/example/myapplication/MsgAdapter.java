package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.ViewHolder> {
    private List<Msg>mMsgList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout leftLayout;
        LinearLayout rightLayout;
        TextView leftMsg;
        TextView rightMsg;

        public ViewHolder(View view) {//把所有的view都装进程序统一管理
            super(view);

            leftLayout = (LinearLayout) view.findViewById(R.id.leftlayout);
            rightLayout = (LinearLayout) view.findViewById(R.id.rightlayout);

            leftMsg = (TextView) view.findViewById(R.id.leftmsg);
            rightMsg = (TextView) view.findViewById(R.id.rightmsg);
        }
    }
    public MsgAdapter(List<Msg> MsgList)
    {
        super();
        mMsgList=MsgList;
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent,int viewtype)
    {
        View view =LayoutInflater.from(parent.getContext()).
                inflate(R.layout.msg_item,parent,false);

        view.getLayoutParams().height =parent.getHeight()/5;//一个滚动窗可以塞5条信息
        return new ViewHolder(view);
    }

    public void onBindViewHolder(ViewHolder holder,int position)
    {
        Msg msg=mMsgList.get(position);
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        if(msg.getType()==Msg.TYPE_RECEIVED)
        {//收到消息，显示左边的消息布局
            holder.leftLayout.setVisibility(View.VISIBLE);
            holder.rightLayout.setVisibility(View.GONE);
            holder.leftMsg.setText(msg.getContent());
        }
        else
        {
            holder.leftLayout.setVisibility(View.GONE);
            holder.rightLayout.setVisibility(View.VISIBLE);
            holder.rightMsg.setText(msg.getContent());
        }
    }
    public int getItemCount()
    {
        return mMsgList.size();
    }


}

