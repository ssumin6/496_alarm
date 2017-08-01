package kaist.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.channels.AlreadyBoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.sql.Time;
import java.util.GregorianCalendar;

/**
 * Created by q on 2017-07-28.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter implements ItemTouchHelperListener{

    private Context mContext;
    private ArrayList<Alarm> mItems;
    private int last_position;

    public class ViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {

        public TextView textClock;
        public Switch switches;
        public boolean setting;

        public ViewHolder(View v) {
            super(v);
            textClock = (TextView) v.findViewById(R.id.textClock);
            setting = false;
            switches = (Switch) v.findViewById(R.id.switch1);
            switches.setOnCheckedChangeListener(this);
        }

        @Override
        public void onCheckedChanged(CompoundButton button, boolean isChecked){
            if(setting){
                setAlarm(isChecked, getAdapterPosition());
            }
        }
    }

    public RecyclerViewAdapter(Context context, ArrayList items){
        mContext = context;
        mItems = items;
        last_position = items.size()-1;
    }

    public void setAlarm(boolean check, int position){
        Alarm item = mItems.get(position);
        mItems.get(position).setOpen(check);
        AlarmManager mManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        String type = item.alarm_type;
        int request_Code = item.pending_list_index;
        SimpleDateFormat sdf = new SimpleDateFormat("aa hh:mm");
        try {
            Date date = sdf.parse(item.time_text);
            GregorianCalendar cal = new GregorianCalendar();
            cal.set(GregorianCalendar.SECOND,0);
            cal.setTime(date);
            date.toString();
            Log.d("WHOAREYOU",cal.toString());
            Intent i;
            if (type.equals("기본알람")) {
                i= new Intent(mContext, BasicAlarm.class);
            } else if (type.equals("음성알람")) {
                i= new Intent(mContext, AudioAlarm.class);

            } else if (type.equals("수학문제")) {
                i= new Intent(mContext, MathAlarm.class);
            } else {
                i= new Intent(mContext, SensitiveAlarm.class);
            }
            PendingIntent pi = PendingIntent.getActivity(mContext, request_Code, i, 0);
            if (check){
                AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(cal.getTimeInMillis(), pi);
                mManager.setAlarmClock(info,pi);
                Toast.makeText(mContext,"SET ALARM+"+item.time_text, Toast.LENGTH_SHORT).show();
            }else{
                mManager.cancel(pi);
                pi.cancel();
                Toast.makeText(mContext,"DESTORY ALARM+"+item.time_text, Toast.LENGTH_SHORT).show();
            }
        }catch(ParseException e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition){
        if (fromPosition<0 || fromPosition >=mItems.size() ||toPosition <0 || toPosition>= mItems.size())
            return false;

        Alarm temp = mItems.get(fromPosition);
        mItems.set(fromPosition, mItems.get(toPosition));
        mItems.set(toPosition, temp);

        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemRemove(int position) {
        Alarm deleteItem = mItems.get(position);
        int code= deleteItem.pending_list_index;
        boolean do_ = deleteItem.open;
        Log.d("DeleteALARM", mItems.get(position).time_text);
        Log.d("Delete","occurred");
        mItems.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();

        AlarmManager mManager = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);

        if(do_){
            //알람 deletion
            Intent temp = new Intent(mContext, BasicAlarm.class);
            PendingIntent pi = PendingIntent.getActivity(mContext, code, temp, 0);
            mManager.cancel(pi);
            pi.cancel();
        }

        last_position -=1;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(mContext).inflate(R.layout.layout_alarm, parent, false);
        ViewHolder holder = new ViewHolder(v);
        return holder;
    }
    @Override
    //ListView의 getView를 담당하는 method
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position){
        //override 된 method, 밑의 method로 넘겨줌.
        onBindViewHolder((ViewHolder) viewHolder, position);
    }

    //ListView의 getView를 담당하는 method
    public void onBindViewHolder(ViewHolder viewHolder, int position){
        Alarm temp = mItems.get(position);
        viewHolder.textClock.setText(temp.time_text);
        viewHolder.switches.setChecked(temp.open);
        viewHolder.setting = true;
    }

    @Override
    public int getItemCount(){
        if (mItems == null) return 0;
        return mItems.size();
    }

    public void add(Alarm ala){
        mItems.add(ala);
        last_position +=1;
        notifyItemInserted(last_position);
    }

    public ArrayList<Alarm> getAlarmList(){
        return mItems;
    }
}
