package com.app.toado.activity.chat;

import android.Manifest;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.app.toado.R;
import com.app.toado.adapter.NewChatAdapter;
import com.app.toado.model.PhoneContacts;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NewChatActivity extends AppCompatActivity {
    private static final String[] CONTACTS_PERMISSION = {
            Manifest.permission.READ_CONTACTS };


    HashMap<String, String> fireContacts;
    EditText etSearch;
    Cursor cursor;
    String name, phonenumber;
    ArrayList<String> phnlistIds;
    ArrayList<PhoneContacts> phnlist;
    private RecyclerView recyclerView;
    private NewChatAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newchat);

        fireContacts = new HashMap<>();

        phnlist = new ArrayList<>();
        phnlistIds = new ArrayList<>();

        etSearch=(EditText) findViewById(R.id.etSearchContacts);

        recyclerView = (RecyclerView) findViewById(R.id.rvnewchat);
        mAdapter = new NewChatAdapter(phnlist, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        getContactsIntoArrayList();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {

                // filter your list from your input
                filter(s.toString());
                //you can use runnable postDelayed like 500 ms to delay search text
            }
        });
    }

    public void getContactsIntoArrayList() {


        cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        while (cursor.moveToNext()) {

            name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

            phonenumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            PhoneContacts ph = new PhoneContacts(phonenumber, name, "nil", "no");
            if (!phnlistIds.contains(phonenumber)) {
                phnlist.add(ph);
                phnlistIds.add(phonenumber);
            }
        }

        cursor.close();

    }


    void filter(String text){
        List<PhoneContacts> temp = new ArrayList();
        for(PhoneContacts d: phnlist){
            //or use .contains(text)
            if(d.getContactname().toLowerCase().contains(text.toLowerCase())){
                temp.add(d);
            }
        }
        //update recyclerview
        mAdapter.updateList(temp);
    }

    public void backPressed(View view) {
        finish();
    }

}
