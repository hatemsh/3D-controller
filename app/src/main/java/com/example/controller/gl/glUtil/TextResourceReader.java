package com.example.controller.gl.glUtil;

import android.content.Context;
import android.content.res.Resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Hatem on 07-Mar-16.
 */
public class TextResourceReader {

    private static final TextResourceReader INSTANCE = new TextResourceReader();
    private TextResourceReader(){}
    private Context context;

    public static TextResourceReader getReader(){
        return INSTANCE;
    }

    public String readTextFileFromResource(int resourceId){


        StringBuilder body = new StringBuilder();

        try {
            InputStream inputStream = context.getResources().openRawResource(resourceId);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String nextLine;

            while ((nextLine = bufferedReader.readLine()) != null) {

                body.append(nextLine);
                body.append('\n');
            }
        } catch (IOException e){


            throw new RuntimeException("Could not open resource: " + resourceId, e);

        } catch (Resources.NotFoundException nfe) {

            throw new RuntimeException("resource not found: " + resourceId, nfe);
        }

        return body.toString();
    }

    public void setContext(Context context){
        this.context = context.getApplicationContext();
    }
}