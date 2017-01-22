package com.spartahack.autodo;

import android.app.DownloadManager;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;
import android.provider.ContactsContract;

import java.io.ByteArrayOutputStream;
import java.util.Properties;


import com.creativityapps.gmailbackgroundlibrary.BackgroundMail;
import com.evernote.client.android.EvernoteSession;

public class MainActivity extends AppCompatActivity {

    private static final String CONSUMER_KEY = "rohansapre";
    private static final String CONSUMER_SECRET = "ba4973576cbffdfe";
    private static final EvernoteSession.EvernoteService EVERNOTE_SERVICE = EvernoteSession.EvernoteService.SANDBOX;
    private static final boolean SUPPORT_APP_LINKED_NOTEBOOKS = true;
    private static String name, phoneNumber,email;
    String[] removeList = {"thank", "to", "you", "a", "send", "an", "email", "the"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EvernoteSession mEvernoteSession = new EvernoteSession.Builder(this).setEvernoteService(EVERNOTE_SERVICE).setSupportAppLinkedNotebooks(SUPPORT_APP_LINKED_NOTEBOOKS).build(CONSUMER_KEY, CONSUMER_SECRET).asSingleton();
        try {
            if (!mEvernoteSession.isLoggedIn()) {
                mEvernoteSession.authenticate(this);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public void onClick(View v) {
        String[] projection = new String[] { ContactsContract.CommonDataKinds.Email.DATA };
        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " = ?";
        String[] selectionArguments = { "John Johnson" };
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, projection ,selection,selectionArguments, null);
        if(phones != null)
        {
            while(phones.moveToNext()) {
                email = phones.getString(0);
            }

        }
        phones.close();
        sendThanks(email);
    }

    public void sendThanks(String emailID)
    {
        BackgroundMail.newBuilder(this)
                .withUsername("jane83231@gmail.com")
                .withPassword("janesmith12")
                .withMailto(emailID)
                .withType(BackgroundMail.TYPE_PLAIN)
                .withSubject("Thanks!")
                .withBody("Hey, Thank you so much")
                .withOnSuccessCallback(new BackgroundMail.OnSuccessCallback() {
                    @Override
                    public void onSuccess() {
                        //do some magic
                        Toast.makeText(MainActivity.this, "Email Sent", Toast.LENGTH_SHORT).show();
                    }
                })
                .withOnFailCallback(new BackgroundMail.OnFailCallback() {
                    @Override
                    public void onFail() {
                        //do some magic
                        Toast.makeText(MainActivity.this, "Email Not Sent!", Toast.LENGTH_SHORT).show();
                    }
                })
                .send();
    }


    public void convertStringsToTasks(String[] arr) {
        for(String s : arr){
            s = s.trim();
            s = s.toLowerCase();
            if(s.contains("thank")) {
                s = convertStringToTask(s);
                String emailID = getEmailID(s.trim().split(" "));
                sendThanks(emailID);
            }
        }
    }


    public String getEmailID(String[] arr) {
        //Access android contacts
        // Run match queries on the db with arr

        for (int i = 0; i < arr.length - 1; i++) {
            String s1 = arr[i];
            String s2 = arr[i + 1];

        }
        return "";
    }


    public String convertStringToTask(String s){

        for(String str : removeList)
            s = removeString(s, str);

        if(s.charAt(s.length()-1) == '.')
            s.substring(0,s.length()-1);

        if(s.contains(" for ")){
            s = s.substring(0, s.indexOf(" for "));
        }
        return s;
    }


    public String removeString(String s1, String s2){
        if(s1.contains(" "+s2+" "))
            return s1.replace(" "+s2+" ", " ");
        if(s1.startsWith(s2+" "))
            return s1.substring(s2.length()+1);
        if(s1.endsWith(" "+s2))
            return s1.substring(0, s1.length()-s2.length()-1);

        return s1;
    }


}