package kaist.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
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
        try{
            String tem = item.time_text;
            int hour = Integer.parseInt(tem.substring(3,5));
            int min = Integer.parseInt(tem.substring(6,8));
            boolean am= tem.substring(0,2).equals("오전");
            GregorianCalendar cal = new GregorianCalendar();
            cal.set(GregorianCalendar.SECOND,0);
            cal.set(GregorianCalendar.HOUR, hour);
            cal.set(GregorianCalendar.MINUTE, min);

            if (am){
                cal.set(GregorianCalendar.AM_PM,GregorianCalendar.AM);
            }else{
                cal.set(GregorianCalendar.AM_PM,GregorianCalendar.PM);
            }
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
                Log.d("WHOAREYOUd", (new Time(info.getTriggerTime())).toString());
                Toast.makeText(mContext,"SET ALARM+"+item.time_text, Toast.LENGTH_SHORT).show();
            }else{
                mManager.cancel(pi);
                pi.cancel();
                Toast.makeText(mContext,"DESTORY ALARM+"+item.time_text, Toast.LENGTH_SHORT).show();
            }
        }catch(Exception e){
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
        boolean notOkay= deleteItem.isGroup;//group 알람이면 true
        if (!notOkay) {
            int code = deleteItem.pending_list_index;
            boolean do_ = deleteItem.open;
            Log.d("DeleteALARM", mItems.get(position).time_text);
            Log.d("Delete", "occurred");
            mItems.remove(position);
            notifyItemRemoved(position);
            notifyDataSetChanged();

            AlarmManager mManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

            if (do_) {
                //알람 deletion
                Intent temp = new Intent(mContext, BasicAlarm.class);
                PendingIntent pi = PendingIntent.getActivity(mContext, code, temp, 0);
                mManager.cancel(pi);
                pi.cancel();
            }

            last_position -= 1;
        }else{
            Toast.makeText(mContext, "그룹 알람은 삭제할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
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
