package com.example.chat;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends ArrayAdapter<Message> {

    private List<Message> messages;
    private Context context;

    public MessageAdapter(Context context, int resource, ArrayList<Message> messages) {
        super(context, resource, messages);

        this.messages = messages;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        int layoutResource = 0;
        int viewType = getItemViewType(position);

        if (viewType == 0) {
            layoutResource = R.layout.my_message_item;
        } else {
            layoutResource = R.layout.you_message_item;
        }

        if (convertView != null) {
            viewHolder = (ViewHolder) convertView.getTag();
        } else {
            convertView = layoutInflater.inflate(layoutResource, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }

        Message message = getItem(position);
        Log.d("avatar", "1: " + message.getRecipientAvatar());

        boolean isText = message.getImageUrl() == null;

        if (viewType == 1 && message.getText().equals("*")) {
            Vibrator vibe = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            vibe.vibrate(500);
        }
        if (viewType == 1 && message.getText().equals("🚜")) {
            Vibrator vibe = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                int i = 0;
                @Override
                public void run() {
                    if (i == 16) {
                        return;
                    }
                    vibe.vibrate(100);
                    handler.postDelayed(this, 200);
                    i++;
                }
            };
            handler.post(runnable);
        }

        viewHolder.userName.setText(message.getName());
        if (viewType == 1 && message.getRecipientAvatar() != null) {
            Glide.with(viewHolder.avatar.getContext()).load(message.getRecipientAvatar()).into(viewHolder.avatar);
        }

        if (isText) {
            viewHolder.photoImageView.setVisibility(View.GONE);
            viewHolder.messageTextView.setVisibility(View.VISIBLE);
            viewHolder.messageTextView.setText(message.getText());
        } else {
            viewHolder.messageTextView.setVisibility(View.GONE);
            viewHolder.photoImageView.setVisibility(View.VISIBLE);
            Glide.with(viewHolder.photoImageView.getContext()).load(message.getImageUrl()).into(viewHolder.photoImageView);
        }
        return convertView;
    }


    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);

        if (message.isMine()) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    private class ViewHolder {

        private TextView userName;
        private TextView messageTextView;
        private ImageView photoImageView;
        private ImageView avatar;

        public ViewHolder(View view) {
            photoImageView = view.findViewById(R.id.photoImageView);
            messageTextView = view.findViewById(R.id.messageTextView);
            userName = view.findViewById(R.id.messageUserName);
            avatar = view.findViewById(R.id.chatAvatarImageView);
        }
    }

}
