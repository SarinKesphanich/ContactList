package com.example.sarin.contactlist;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;


public class GroupContact extends Activity {

    ArrayList<Groups> groupList;
    GroupAdapter adapter;
    private int usr_id, ggroupid;
    ProgressDialog prgDialog;
    private EditText input, editInput;
    private AlertDialog.Builder alertDlg, editAlertDlg;
    private AlertDialog alert, editalert;
    private String[] menuItems = {"Edit", "Delete", "Contact List"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_contact);

        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);
        SharedPreferences persondata = getSharedPreferences("persondata", Context.MODE_PRIVATE);
        usr_id = persondata.getInt("userid", 0);
        getGroup();

        ListView listview = (ListView) findViewById(R.id.groupListView);
        registerForContextMenu(listview);

        input = new EditText(this);
        input.setId(R.id.gNameAlert);
        editInput = new EditText(this);
        editInput.setId(R.id.gEditNameAlert);
        createAlert();
        createEditAlert();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_group_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {
            case R.id.action_add:
//                Toast.makeText(getApplicationContext(), "Add Click", Toast.LENGTH_LONG).show();
                alert.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void getGroup(){
        // Show Progress Dialog
        prgDialog.show();
        String url = "http://contact.sarin.me/api/index.php/group/get";
        AsyncHttpClient client = new AsyncHttpClient();
        SharedPreferences persondata = getSharedPreferences("persondata", MODE_PRIVATE);
        String api_key = persondata.getString("apikey", "");
        client.addHeader("Authorization", api_key);
        client.setConnectTimeout(5000);
        client.post(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                prgDialog.hide();
                try {
                    if (response.getJSONObject("data").getBoolean("status")) {
                        groupList = new ArrayList<Groups>();
                        JSONArray groups = response.getJSONObject("data").getJSONArray("groups");

                        for (int i = 0; i < groups.length(); i++) {
                            JSONObject object = groups.getJSONObject(i);

                            Groups group = new Groups();

                            group.setId(object.getInt("groupid"));
                            group.setName(object.getString("groupname"));
                            group.setAmount(object.getInt("amount"));

                            groupList.add(group);
                        }

                        ListView listview = (ListView) findViewById(R.id.groupListView);
                        adapter = new GroupAdapter(getApplicationContext(), R.layout.group_adapter, groupList);
                        listview.setAdapter(adapter);
                        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view,
                                                    int position, long id) {
                                nevigateToContactList(groupList.get(position).getId());
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

    public void setGroup(String gname){
        // Show Progress Dialog
        prgDialog.show();
        RequestParams params = new RequestParams();
        params.put("groupname", gname);
        String url = "http://contact.sarin.me/api/index.php/group/set";
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
                        Toast.makeText(getApplicationContext(), "Adding Group Complete", Toast.LENGTH_LONG).show();

                        getGroup();
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

    public void delGroup(int groupid){
        // Show Progress Dialog
        prgDialog.show();
        RequestParams params = new RequestParams();
        params.put("groupid", groupid);
        String url = "http://contact.sarin.me/api/index.php/group/del";
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
                        Toast.makeText(getApplicationContext(), "Deleting Group Complete", Toast.LENGTH_LONG).show();

                        getGroup();
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

    public void editGroup(int groupid, String gname){
        // Show Progress Dialog
        prgDialog.show();
        RequestParams params = new RequestParams();
        params.put("groupid", groupid);
        params.put("groupname", gname);
        String url = "http://contact.sarin.me/api/index.php/group/edit";
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
                        Toast.makeText(getApplicationContext(), "Editing Group Complete", Toast.LENGTH_LONG).show();

                        getGroup();
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

    public void createAlert() {
        alertDlg = null;
        alertDlg = new AlertDialog.Builder(this);
        alertDlg.setMessage("Please Input Group Name")
                .setTitle("Add Group")
                .setCancelable(false);

        input.setText("");
        alertDlg.setView(input);

        alertDlg.setPositiveButton("Connect",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String groupName = input.getText().toString();
                        if(!groupName.equals("")) {
                            setGroup(groupName);
                            input.setText("");
                        }
                    }
                }
        );

        alertDlg.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
        );

        alert = null;
        alert = alertDlg.create();
    }

    public void createEditAlert() {
        editAlertDlg = null;
        editAlertDlg = new AlertDialog.Builder(this);
        editAlertDlg.setMessage("Please Input Group Name")
                .setTitle("Edit Group")
                .setCancelable(false);

        editInput.setText("");
        editAlertDlg.setView(editInput);

        editAlertDlg.setPositiveButton("Connect",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String groupName = editInput.getText().toString();
                        if(!groupName.equals("")) {
                            editGroup(ggroupid, groupName);
                            input.setText("");
                        }
                    }
                }
        );

        editAlertDlg.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
        );

        editalert = null;
        editalert = editAlertDlg.create();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.groupListView) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.setHeaderTitle(groupList.get(info.position).getName());
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
        int groupid = groupList.get(info.position).getId();
        String gname = groupList.get(info.position).getName();
        manage(menuItemIndex, groupid, gname);
        return true;
    }

    private void manage(int menuItemIndex, int groupid, String gname) {
        switch (menuItemIndex) {
            case 0:
                ggroupid = groupid;
                editInput.setText(gname);
                editInput.setSelectAllOnFocus(true);
                editalert.show();
                break;
            case 1:
                delGroup(groupid);
                //getGroup();
                break;
            case 2:
                nevigateToContactList(groupid);
                break;
        }
    }

    private void nevigateToContactList(int groupid) {

    }
}
