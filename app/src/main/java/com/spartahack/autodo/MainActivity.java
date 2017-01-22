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
import com.evernote.edam.type.Resource;
import com.evernote.edam.type.User;
import com.evernote.thrift.TException;

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

    private static final String CONSUMER_KEY = "rohansapre";
    private static final String CONSUMER_SECRET = "ba4973576cbffdfe";
    private static final EvernoteSession.EvernoteService EVERNOTE_SERVICE = EvernoteSession.EvernoteService.SANDBOX;
    private static final boolean SUPPORT_APP_LINKED_NOTEBOOKS = true;
    private static String[] TASKS;

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

    public void onClick(View v){
        Toast.makeText(MainActivity.this, "Hello", Toast.LENGTH_SHORT).show();
    }
}