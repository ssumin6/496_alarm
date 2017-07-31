package kaist.alarm;

/**
 * Created by q on 2017-07-29.
 */

public class Alarm {
    String time_text;
    boolean open;
    int pending_list_index;

    public Alarm(int requestCode, String TimeText) {
        time_text = TimeText;
        open = true;
        pending_list_index = requestCode;
    }
    public void setOpen(boolean tx){
        open = tx;
    }
}
