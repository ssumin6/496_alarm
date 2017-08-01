package kaist.alarm;

/**
 * Created by q on 2017-07-29.
 */

public class Alarm {
    String time_text;
    boolean open;
    boolean isGroup;
    int pending_list_index;
    String alarm_type;
    String manager = "";
    String Room_id = "";

    public Alarm(int requestCode, String TimeText) {
        time_text = TimeText;
        open = true;
        isGroup = false;
        pending_list_index = requestCode;
    }
    public void setOpen(boolean tx){
        open = tx;
    }
    public void setAlarm_type(String type){
        alarm_type = type;
    }
    public void setGroup (boolean group){isGroup = group;}
    public void setRoom_id(String room_id){Room_id = room_id;}
    public void setManager(String who){manager = who;}
}