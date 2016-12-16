package co.tinode.tindroid;


import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.List;

import co.tinode.tindroid.db.StoredTopic;
import co.tinode.tindroid.db.TopicDb;
import co.tinode.tinodesdk.Topic;

/**
 * Handling contact list.
 */
public class ChatListAdapter extends BaseAdapter {
    private static final String TAG = "ChatListAdapter";

    private Context mContext;
    private List<Topic> mTopics;
    private SparseBooleanArray mSelectedItems;


    public ChatListAdapter(AppCompatActivity context) {
        super();
        mContext = context;
        mSelectedItems = new SparseBooleanArray();
        mTopics = Cache.getTinode().getFilteredTopics(Topic.TopicType.USER, null);
        Log.d(TAG, "Initialized");
    }

    @Override
    public int getCount() {
        return mTopics.size();
    }

    @Override
    public Object getItem(int position) {
        return mTopics.get(position);
    }

    @Override
    public long getItemId(int position) {
        return StoredTopic.getId(mTopics.get(position));
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View item = convertView;
        ViewHolder holder;
        if (item == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE);

            item = inflater.inflate(R.layout.contact, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) item.findViewById(R.id.contactName);
            holder.unreadCount = (TextView) item.findViewById(R.id.unreadCount);
            holder.contactPriv = (TextView) item.findViewById(R.id.contactPriv);
            holder.icon = (AppCompatImageView) item.findViewById(R.id.avatar);
            holder.online = (AppCompatImageView) item.findViewById(R.id.online);

            item.setTag(holder);
        } else {
            holder = (ViewHolder) item.getTag();
        }

        bindView(position, holder);

        return item;
    }

    public void bindView(int position, ViewHolder holder) {
        final Topic<VCard,String,String> topic = mTopics.get(position);

        VCard pub = topic.getPub();

        if (pub != null) {
            holder.name.setText(pub.fn);
        }
        holder.contactPriv.setText(topic.getPriv());

        int unread = topic.getUnreadCount();
        if (unread > 0) {
            holder.unreadCount.setText(unread > 9 ? "9+" : String.valueOf(unread));
            holder.unreadCount.setVisibility(View.VISIBLE);
        } else {
            holder.unreadCount.setVisibility(View.INVISIBLE);
        }

        Bitmap bmp = pub != null ? pub.getBitmap() : null;
        if (bmp != null) {
            holder.icon.setImageDrawable(new RoundedImage(bmp));
        } else {
            Topic.TopicType topicType = topic.getTopicType();
            int res = -1;
            if (topicType == Topic.TopicType.GRP) {
                res = R.drawable.ic_group_circle;
            } else if (topicType == Topic.TopicType.P2P || topicType == Topic.TopicType.ME) {
                res = R.drawable.ic_person_circle;
            }

            Drawable drw;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                drw = mContext.getResources().getDrawable(res, mContext.getTheme());
            } else {
                drw = mContext.getResources().getDrawable(res);
            }
            if (drw != null) {
                holder.icon.setImageDrawable(drw);
            }
        }

        holder.online.setColorFilter(topic.getOnline() ? UiUtils.COLOR_ONLINE : UiUtils.COLOR_OFFLINE);
        // Log.d(TAG, "User " + topic.getName() + " is " + (online ? "online" : "offline"));
    }

    public void toggleSelected(int position) {
        selectView(position, !mSelectedItems.get(position));
    }

    public void removeSelection() {
        mSelectedItems = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public String getTopicNameFromView(View view) {
        final ViewHolder holder = (ViewHolder) view.getTag();
        return holder != null ? holder.topic : null;
    }

    private void selectView(int position, boolean value) {
        if (value)
            mSelectedItems.put(position, true);
        else
            mSelectedItems.delete(position);
        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return mSelectedItems.size();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItems;
    }

    private class ViewHolder {
        String topic;
        TextView name;
        TextView unreadCount;
        TextView contactPriv;
        AppCompatImageView icon;
        AppCompatImageView online;
    }
}