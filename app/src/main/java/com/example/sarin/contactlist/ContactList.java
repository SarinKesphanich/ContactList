package com.example.sarin.contactlist;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class ContactList extends Activity {

    ArrayList<Contacts> contactList;
    ContactAdapter adapter;
    int groupid;
    ProgressDialog prgDialog;
    private String[] menuItems = {"Edit", "Delete", "Contact Detail"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        Intent intent = getIntent();
        groupid = intent.getIntExtra("groupid", 0);

        getContact();
        ListView listview = (ListView) findViewById(R.id.contactListView);
        registerForContextMenu(listview);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void getContact(){
        // Show Progress Dialog
        prgDialog.show();
        RequestParams params = new RequestParams();
        params.put("groupid", groupid);
        String url = "http://contact.sarin.me/api/index.php/contact/get";
        AsyncHttpClient client = new AsyncHttpClient();
        SharedPreferences persondata = getSharedPreferences("persondata", MODE_PRIVATE);
        String api_key = persondata.getString("apikey", "");
        client.addHeader("Authorization", api_key);
        client.setConnectTimeout(5000);
        client.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                prgDialog.hide();
                try {
                    if (response.getJSONObject("data").getBoolean("status")) {
                        contactList = new ArrayList<Contacts>();
                        JSONArray groups = response.getJSONObject("data").getJSONArray("contacts");

                        for (int i = 0; i < groups.length(); i++) {
                            JSONObject object = groups.getJSONObject(i);

                            Contacts contact = new Contacts();

                            contact.setId(object.getInt("contactid"));
                            contact.setName(object.getString("contactname"));
                            contact.setImg(object.getString("contactimg"));
                            contactList.add(contact);
                        }

                        ListView listview = (ListView) findViewById(R.id.contactListView);
                        adapter = new ContactAdapter(getApplicationContext(), R.layout.contactlist_adapter, contactList);
                        listview.setAdapter(adapter);
                        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view,
                                                    int position, long id) {
                                nevigateToContactDetail(contactList.get(position).getId());
                            }
                        });
                    }

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                //super.onFailure(statusCode, headers, responseString, throwable);
                prgDialog.hide();
                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void delContact(int contactid){
        // Show Progress Dialog
        prgDialog.show();
        RequestParams params = new RequestParams();
        params.put("contactid", contactid);
        String url = "http://contact.sarin.me/api/index.php/contact/del";
        AsyncHttpClient client = new AsyncHttpClient();
        SharedPreferences persondata = getSharedPreferences("persondata", MODE_PRIVATE);
        String api_key = persondata.getString("apikey", "");
        client.addHeader("Authorization", api_key);
        client.setConnectTimeout(5000);
        client.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                prgDialog.hide();
                try {
                    if(response.getJSONObject("data").getBoolean("status")){
                        Toast.makeText(getApplicationContext(), "Deleting Contact Complete", Toast.LENGTH_LONG).show();
                        getContact();
                    } else {
                        Toast.makeText(getApplicationContext(), response.getJSONObject("data").getString("errormsg"), Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                //super.onFailure(statusCode, headers, responseString, throwable);
                prgDialog.hide();
                // When Http response code is '404'
                if(statusCode == 404){
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if(statusCode == 500){
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else{
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void nevigateToContactDetail(int id) {

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.contactListView) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.setHeaderTitle(contactList.get(info.position).getName());
            // String[] menuItems = getResources().getStringArray(R.array.menu);
            for (int i = 0; i < menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        int contactid = contactList.get(info.position).getId();
        String contactname = contactList.get(info.position).getName();
        manage(menuItemIndex, contactid, contactname);
        return true;
    }

    private void manage(int menuItemIndex, int contactid, String contactname) {
        switch (menuItemIndex) {
            case 0:
//                ggroupid = groupid;
//                editInput.setText(gname);
//                editInput.setSelectAllOnFocus(true);
//                editalert.show();
                break;
            case 1:
                delContact(contactid);
                break;
            case 2:
                nevigateToContactDetail(contactid);
                break;
        }
    }
}
