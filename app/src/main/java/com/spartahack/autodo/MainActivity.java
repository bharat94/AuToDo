package com.spartahack.autodo;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.ContactsContract;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLOutput;
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
import com.evernote.client.android.asyncclient.EvernoteCallback;
import com.evernote.client.android.asyncclient.EvernoteNoteStoreClient;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.notestore.NotesMetadataList;
import com.evernote.edam.notestore.NotesMetadataResultSpec;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.Resource;
import com.evernote.edam.type.User;
import com.evernote.thrift.TException;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0;
    private static String email;
    private static final String CONSUMER_KEY = "rohansapre";
    private static final String CONSUMER_SECRET = "ba4973576cbffdfe";
    private static final EvernoteSession.EvernoteService EVERNOTE_SERVICE = EvernoteSession.EvernoteService.SANDBOX;
    private static final boolean SUPPORT_APP_LINKED_NOTEBOOKS = true;
    String[] removeList = {"thank", "to", "you", "a", "send", "an", "email", "the"};
    private static String[] TASKS;

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
                        if (!mEvernoteSession.isLoggedIn())
                            mEvernoteSession.authenticate(this);
                        getRelevantNodes(mEvernoteSession);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }

                    //accessContacts();

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

    public void onClick(View v) {
        String[] projection = new String[] { ContactsContract.CommonDataKinds.Email.DATA };
        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " = ?";
        String[] selectionArguments = { "Akhila"};
            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, projection, selection, selectionArguments, null);
            if (phones != null) {
                while (phones.moveToNext()) {
                    email = phones.getString(0);
                }
                phones.close();
            }
            System.out.println("Mail : " + email);
            sendThanks(email);
            getAlchemyKeywords();
    }

    private void getRelevantNodes(final EvernoteSession evernoteSession) {
        Future<List<Notebook>> notebooks = evernoteSession.getEvernoteClientFactory().getNoteStoreClient().listNotebooksAsync(new EvernoteCallback<List<Notebook>>() {
            @Override
            public void onSuccess(List<Notebook> result) {
                for (Notebook notebook : result) {
                    if (notebook.getName().equalsIgnoreCase("to do list")) {
                        Toast.makeText(getApplicationContext(), notebook.getName(), Toast.LENGTH_LONG).show();
                        NoteFilter filter = new NoteFilter();
                        filter.setNotebookGuid(notebook.getGuid());
                        evernoteSession.getEvernoteClientFactory().getNoteStoreClient().findNotesAsync(filter, 0, 999, new EvernoteCallback<NoteList>() {
                            @Override
                            public void onSuccess(NoteList result) {
                                for (Note note : result.getNotes()) {
                                    Toast.makeText(getApplicationContext(), note.getContent(), Toast.LENGTH_LONG).show();
                                    evernoteSession.getEvernoteClientFactory().getNoteStoreClient().getNoteContentAsync(note.getGuid(), new EvernoteCallback<String>() {
                                        @Override
                                        public void onSuccess(String result) {
                                            TASKS = getToDoList(result);
                                        }

                                        @Override
                                        public void onException(Exception exception) {
                                            System.out.println("Exception: " + exception);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onException(Exception exception) {
                                System.out.println("Exception: " + exception);
                            }
                        });
                    }
                }
            }

            @Override
            public void onException(Exception exception) {
                System.out.println("Error retrieving notebooks");
            }
        });
    }

    public void getAlchemyKeywords() {
        new NetworkOperation().execute();
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
                }).send();
    }

    private String[] getToDoList(String result) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        List<String> data = new ArrayList<>();
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            InputSource inputSource = new InputSource(new StringReader(result));
            Document document = documentBuilder.parse(inputSource);
            NodeList nodeList = document.getElementsByTagName("div");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    Node node1 = element.getChildNodes().item(0);
                    if (node1.hasAttributes()) {
                        NamedNodeMap attributes = node1.getAttributes();
                        if ("true".equalsIgnoreCase(attributes.getNamedItem("checked").getNodeValue())) {
                            data.add(i, element.getChildNodes().item(1).getNodeValue());
                        }
                    }
                }
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] stringData = new String[data.size()];
        return data.toArray(stringData);
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