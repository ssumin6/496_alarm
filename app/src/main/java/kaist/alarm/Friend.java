package kaist.alarm;

import java.io.Serializable;

/**
 * Created by q on 2017-07-31.
 */

public class Friend implements Serializable{
    private String thumbImage;
    private String name;
    private String phonenumber;
    private String token;

    public Friend(String image, String na, String number){
        thumbImage = image;
        name = na;
        phonenumber = number;
    }
    public void setToken(String my_token){
        token = my_token;
    }
    public String getThumbImage(){
        return thumbImage;
    }
    public String getName(){
        return name;
    }
    public String getToken(){return token;}
    public String getPhonenumber(){return phonenumber;}
}
