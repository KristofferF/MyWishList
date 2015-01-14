package se.kfreiholtz.mywishlist.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.List;

import se.kfreiholtz.mywishlist.R;
import se.kfreiholtz.mywishlist.utilities.ParseConstants;

/**
 * Activity for watching lists from the inbox
 *
 * @author Kristoffer Freiholtz
 * @version 1.0 2014-11-18
 */

public class ListAdapterPost extends ArrayAdapter<ParseObject> {

    protected Context mContext;
    protected List<ParseObject> mPosts;

    public ListAdapterPost(Context context, List<ParseObject> lists) {
        super(context, R.layout.list_item_posts, lists);
        mContext = context;
        mPosts = lists;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if(convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_posts, parent, false);
            holder = new ViewHolder();
            holder.nameLabel = (TextView) convertView.findViewById(R.id.senderLabel);
            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder)convertView.getTag();
        }

        ParseObject list = mPosts.get(position);
        holder.nameLabel.setText(list.getString(ParseConstants.KEY_POST_NAME));

        return convertView;
    }

    /**
     * The viewholder class for the listadapter
     */
    private static class ViewHolder {
        TextView nameLabel;
    }

    /**
     * Refresh the list
     */
    public void refill(List<ParseObject> lists){
        mPosts.clear();
        mPosts.addAll(lists);
        notifyDataSetChanged();
    }
}
