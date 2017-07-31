package kaist.alarm;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * Created by q on 2017-07-31.
 */

public class FriendAdapter extends BaseAdapter {

    private ArrayList<Friend> itemList = new ArrayList<>();
    private Context mContext;

    public FriendAdapter(Context context,ArrayList<Friend> list){
        mContext = context;
        itemList = list;
    }
    public View getView(int position, View convertView, ViewGroup parent){
        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.layout_friend, parent, false);
        }

        ImageView thumbImage = (ImageView)convertView.findViewById(R.id.imageView);
        TextView nameText = (TextView)convertView.findViewById(R.id.textView2);

        Uri image_source = Uri.parse(itemList.get(position).getThumbImage());
        Glide.with(mContext).load(image_source).into(thumbImage);
        nameText.setText(itemList.get(position).getName());

        return convertView;
    }

    public long getItemId(int position){
        return position;
    }

    public Object getItem(int position){
        if (itemList!= null)return itemList.get(position);
        return null;
    }
    public int getCount(){
        if (itemList != null) return itemList.size();
        return 0;
    }

}
