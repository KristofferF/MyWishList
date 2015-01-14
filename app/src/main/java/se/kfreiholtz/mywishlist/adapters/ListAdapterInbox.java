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

public class ListAdapterInbox extends ArrayAdapter<ParseObject> {

    protected Context mContext;
    protected List<ParseObject> mLists;

    public ListAdapterInbox(Context context, List<ParseObject> lists) {
        super(context, R.layout.list_item_inbox, lists);
        mContext = context;
        mLists = lists;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if(convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_inbox, parent, false);
            holder = new ViewHolder();
            holder.iconImageView = (ImageView) convertView.findViewById(R.id.listIcon);
            holder.nameLabel = (TextView) convertView.findViewById(R.id.nameLabel);
            holder.senderLabel = (TextView) convertView.findViewById(R.id.senderLabel);
            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder)convertView.getTag();
        }

        ParseObject list = mLists.get(position);
        String[] listTypes = getContext().getResources().getStringArray(R.array.list_types);
        String listType = list.getString(ParseConstants.KEY_LIST_TYPE);
        if(listType.equals(listTypes[0])){
            holder.iconImageView.setImageResource(R.drawable.ic_birthday_cake);
        }
        else if (listType.equals(listTypes[1])) {
            holder.iconImageView.setImageResource(R.drawable.ic_christmas_tree);
        }
        else if (listType.equals(listTypes[2])) {
            holder.iconImageView.setImageResource(R.drawable.ic_wedding_rings);
        }
        else {
            holder.iconImageView.setImageResource(R.drawable.ic_gift);
        }
        holder.nameLabel.setText(list.getString(ParseConstants.KEY_LIST_NAME));
        holder.senderLabel.setText(list.getString(ParseConstants.KEY_SENDER_NAME));

        return convertView;
    }

    /**
     * The viewholder class for the listadapter
     */
    private static class ViewHolder {
        ImageView iconImageView;
        TextView nameLabel;
        TextView senderLabel;
    }

    /**
     * Refresh the list
     */
    public void refill(List<ParseObject> lists){
        mLists.clear();
        mLists.addAll(lists);
        notifyDataSetChanged();
    }
}
