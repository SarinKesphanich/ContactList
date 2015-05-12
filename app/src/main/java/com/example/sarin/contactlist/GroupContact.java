package com.example.sarin.contactlist;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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

import java.util.ArrayList;


public class GroupContact extends Activity {

    ArrayList<Groups> groupList;
    GroupAdapter adapter;
    private int usr_id;
    ProgressDialog prgDialog;
    private EditText input;
    private AlertDialog.Builder alertDlg;
    private AlertDialog alert;
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

        input = new EditText(this);
        input.setId(R.id.gNameAlert);
        createAlert();
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
        client.post(url ,new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                prgDialog.hide();
                try {
                    if(response.getJSONObject("data").getBoolean("status")){
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
                        Toast.makeText(getApplicationContext(), "Insert Complete", Toast.LENGTH_LONG).show();
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
        alertDlg.setMessage("Please Group Name")
                .setTitle("Add Group")
                .setCancelable(false);

        input.setText("");
        alertDlg.setView(input);

        alertDlg.setPositiveButton("Connect",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String groupName = input.getText().toString();
                        setGroup(groupName);
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
}
