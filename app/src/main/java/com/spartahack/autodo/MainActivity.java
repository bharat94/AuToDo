package com.spartahack.autodo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.evernote.client.android.EvernoteSession;
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
import com.evernote.edam.type.User;
import com.evernote.thrift.TException;

import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

public class MainActivity extends AppCompatActivity {

    private static final String CONSUMER_KEY = "rohansapre";
    private static final String CONSUMER_SECRET = "ba4973576cbffdfe";
    private static final EvernoteSession.EvernoteService EVERNOTE_SERVICE = EvernoteSession.EvernoteService.SANDBOX;
    private static final boolean SUPPORT_APP_LINKED_NOTEBOOKS = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EvernoteSession mEvernoteSession = new EvernoteSession.Builder(this).setEvernoteService(EVERNOTE_SERVICE).setSupportAppLinkedNotebooks(SUPPORT_APP_LINKED_NOTEBOOKS).build(CONSUMER_KEY, CONSUMER_SECRET).asSingleton();
        try {
            if (!mEvernoteSession.isLoggedIn())
                mEvernoteSession.authenticate(this);
            getRelevantNodes(mEvernoteSession);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void getRelevantNodes(EvernoteSession evernoteSession) {
        Future<List<Notebook>> notebooks = evernoteSession.getEvernoteClientFactory().getNoteStoreClient().listNotebooksAsync(new EvernoteCallback<List<Notebook>>() {
            @Override
            public void onSuccess(List<Notebook> result) {
                List<String> namesList = new ArrayList<>(result.size());
                System.out.println(result.size());
                for (Notebook notebook : result) {
                    namesList.add(notebook.getName());
                    System.out.println(notebook.getName());
                }
                for (String notebook : namesList) {
                    if (notebook.equalsIgnoreCase("to do list")) {
                        System.out.println("Notebook: " + notebook);
                        Toast.makeText(getApplicationContext(), notebook, Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onException(Exception exception) {
                System.out.println("Error retrieving notebooks");
            }
        });
    }

    public void onClick(View v){
        Toast.makeText(MainActivity.this, "Hello", Toast.LENGTH_SHORT).show();
    }
}