package kaist.alarm;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.ContentValues;
import android.icu.util.Output;
import java.util.Map;

/**
 * Created by q on 2017-08-01.
 */


public class RequestHttpGeneral{
    public String request(String _url,String method) throws IOException {
        HttpURLConnection urlconne = null;
        StringBuffer Params = new StringBuffer();
        try{
            URL url = new URL(_url);
            urlconne = (HttpURLConnection)url.openConnection();

            urlconne.setRequestMethod(method);
            urlconne.setRequestProperty("Accept-Charset","UTF-8");
            urlconne.setRequestProperty("Content-Type","application/json");

            if (urlconne.getResponseCode() != HttpURLConnection.HTTP_OK)
                return null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlconne.getInputStream(),"UTF-8"));

            String line;
            String page = "";

            while((line = reader.readLine())!= null){
                page += line;
            }
            return page;

        }catch(MalformedURLException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }finally {
            if(urlconne != null)
                urlconne.disconnect();
        }
        return null;

    }
}

