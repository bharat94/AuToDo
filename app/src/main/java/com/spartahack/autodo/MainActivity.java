package com.spartahack.autodo;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import com.creativityapps.gmailbackgroundlibrary.BackgroundMail;
import com.evernote.client.android.EvernoteSession;
import com.ibm.watson.developer_cloud.alchemy.v1.AlchemyLanguage;
import com.ibm.watson.developer_cloud.alchemy.v1.model.Keyword;
import com.ibm.watson.developer_cloud.alchemy.v1.model.Keywords;
import com.ibm.watson.developer_cloud.http.ServiceCall;
import com.ibm.watson.developer_cloud.util.CredentialUtils;


public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0;
    private String name,phoneNumber;
    private static final String CONSUMER_KEY = "rohansapre";
    private static final String CONSUMER_SECRET = "ba4973576cbffdfe";
    private static final EvernoteSession.EvernoteService EVERNOTE_SERVICE = EvernoteSession.EvernoteService.SANDBOX;
    private static final boolean SUPPORT_APP_LINKED_NOTEBOOKS = true;
    String[] removeList = {"thank", "to", "you", "a", "send", "an", "email", "the"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            String []permissions = {Manifest.permission.READ_CONTACTS};
            ActivityCompat.requestPermissions(MainActivity.this, permissions, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
        }


    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {


        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    EvernoteSession mEvernoteSession = new EvernoteSession.Builder(this).setEvernoteService(EVERNOTE_SERVICE).setSupportAppLinkedNotebooks(SUPPORT_APP_LINKED_NOTEBOOKS).build(CONSUMER_KEY, CONSUMER_SECRET).asSingleton();
                    try {
                        if (!mEvernoteSession.isLoggedIn()) {
                            mEvernoteSession.authenticate(this);
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }

                    accessContacts();

                    System.out.println("if loop");

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();

                    System.out.println("else loop");
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void onClick(View v) throws IOException {
        //Toast.makeText(MainActivity.this, "Sending Email", Toast.LENGTH_SHORT).show();
        sendThanks("akhila.shankar12@gmail.com");
        getAlchemyKeywords();
    }

    public void getAlchemyKeywords() throws IOException {
        new NetworkOperation().execute( );

    }

    public void sendThanks(String emailID) throws IOException
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


    public void convertStringsToTasks(String[] arr) throws IOException {
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

    private class NetworkOperation extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            AlchemyLanguage service = new AlchemyLanguage();
            service.setApiKey("705d2a04ed9abdb21e451acc8216187ad8621156");

            Map<String, Object> paramsMap = new HashMap<String, Object>();
            paramsMap.put(AlchemyLanguage.TEXT, "Send a thank you message to John Doe for dinner yesterday.");
            Keywords keywords = service.getKeywords(paramsMap).execute();
            //ServiceCall<Keywords> keywords = service.getKeywords(params);
            System.out.println("All Keywords: " + keywords);
            return null;
        }

    }

}