package com.example.sarin.contactlist;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;


public class RegisterActivity extends Activity {

    private EditText mEmailView, mPasswordView, mRePasswordView, mFnameView, mLnameView;
    private String gEmail, gFname, gLname;
    ProgressDialog prgDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mRePasswordView = (EditText) findViewById(R.id.password_2);
        mFnameView = (EditText) findViewById(R.id.fname);
        mLnameView = (EditText) findViewById(R.id.lname);

        Intent intent = getIntent();
        mEmailView.setText(intent.getStringExtra("email"));

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Register();
            }
        });
    }

    private void Register() {
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String password_2 = mRePasswordView.getText().toString();
        String fname = mFnameView.getText().toString();
        String lname = mLnameView.getText().toString();

        gEmail = email;
        gFname = fname;
        gLname = lname;

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(lname)) {
            mLnameView.setError(getString(R.string.error_field_required));
            focusView = mLnameView;
            cancel = true;
        }
        if (TextUtils.isEmpty(fname)) {
            mFnameView.setError(getString(R.string.error_field_required));
            focusView = mFnameView;
            cancel = true;
        }
        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password_2) || !isPasswordValid(password_2)) {
            mRePasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mRePasswordView;
            cancel = true;
        }
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }
        if (!Objects.equals(password, password_2)) {
            mPasswordView.setError("2 Password Not Match");
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            String url = "http://contact.sarin.me/api/index.php/authen/register";
            RequestParams params = new RequestParams();
            params.put("email", email);
            params.put("pass", sha1(password));
            params.put("fname", fname);
            params.put("lname", lname);
            regis(url, params);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
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

    /**
     * Method that performs RESTful webservice invocations
     *
     * @param params
     */
    public void regis(String url, RequestParams params){
        // Show Progress Dialog
        prgDialog.show();
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.setConnectTimeout(5000);
        client.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                prgDialog.hide();
                try {
                    if (response.getJSONObject("data").getBoolean("status")) {
                        Toast.makeText(getApplicationContext(), "You are successfully Registered", Toast.LENGTH_LONG).show();
                        JSONObject obj = response.getJSONObject("data");
                        nevigateToHome(obj.getInt("userid"), obj.getString("apikey"));
                    } else {
                        mEmailView.setError(response.getJSONObject("data").getString("errormsg"));
                        mEmailView.requestFocus();
                        Toast.makeText(getApplicationContext(), response.getJSONObject("data").getString("errormsg"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                //super.onFailure(statusCode, headers, throwable, errorResponse);
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
                    Toast.makeText(getApplicationContext(), "Cannot connect to server. \nPlease make sure IP are correct.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void nevigateToHome(int userid, String apikey) {
        SharedPreferences persondata = getSharedPreferences("persondata", MODE_PRIVATE);
        SharedPreferences.Editor editor = persondata.edit();
        editor.putInt("userid", userid);
        editor.putString("fname", gFname);
        editor.putString("lname", gLname);
        editor.putString("email", gEmail);
        editor.putString("apikey", apikey);
        editor.commit();

        Intent groupContact = new Intent(this, GroupContact.class);
        startActivity(groupContact);
    }

    public String sha1(String s) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        digest.reset();
        byte[] data = digest.digest(s.getBytes());
        return String.format("%0" + (data.length * 2) + "X", new BigInteger(1, data));
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }
}
