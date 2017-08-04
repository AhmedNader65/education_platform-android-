package com.mrerror.tm;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mrerror.tm.connection.NetworkConnection;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SignupActivity extends AppCompatActivity  {
    private static final String TAG = "SignupActivity";

    @InjectView(R.id.input_name)
    EditText _nameText;
    @InjectView(R.id.input_email) EditText _emailText;
    @InjectView(R.id.input_password) EditText _passwordText;
    @InjectView(R.id.btn_signup)
    Button _signupButton;
    @InjectView(R.id.link_login)
    TextView _loginLink;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.inject(this);

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });
    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        String name = _nameText.getText().toString();
        final String email = _emailText.getText().toString();
        final String password = _passwordText.getText().toString();

        // TODO: Implement your own signup logic here.
        String url = "http://educationplatform.pythonanywhere.com/api/users/register/";
        new NetworkConnection(new NetworkConnection.OnCompleteFetchingData() {
            @Override
            public void onCompleted(String result) throws JSONException {
                String url = "http://educationplatform.pythonanywhere.com/api/users/login/";
                new NetworkConnection(new NetworkConnection.OnCompleteFetchingData() {
                    @Override
                    public void onCompleted(String result) throws JSONException {
                        onSignupSuccess();
                        progressDialog.dismiss();
                        JSONObject obj = new JSONObject(result);
                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(SignupActivity.this);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putInt("id",obj.getInt("id"));
                        editor.putString("username",obj.getString("username"));
                        editor.putString("group",obj.getString("group"));
                        editor.putBoolean("logged",true);
                        editor.commit();
                        startActivity(new Intent(SignupActivity.this,MainActivity.class));
                    }

                    @Override
                    public void onError(String error) {

                    }
                }).postData(SignupActivity.this,url,new String[]{"email","password"},
                        new String[]{email,password});
            }

            @Override
            public void onError(String error) {

            }
        }).postData(this,url,new String[]{"username","email","email2","password"}
                ,new String[]{name,email,email,password});
        
    }

        
    public void onSignupSuccess() {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("at least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

}