package kaist.alarm;

import android.media.MediaPlayer;

import java.io.Serializable;

/**
 * Created by q on 2017-08-01.
 */

public class SaveAlarm implements Serializable {
    MediaPlayer music;

    public SaveAlarm(MediaPlayer music) {
        this.music = music;
    }

    public MediaPlayer getMusic(){
        return music;
    }

    public void setMusic(){
        this.music = music;
    }
}
