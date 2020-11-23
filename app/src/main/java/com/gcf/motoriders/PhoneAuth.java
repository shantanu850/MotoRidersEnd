package com.gcf.motoriders;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class PhoneAuth extends AppCompatActivity {
    Button btnGenerateOTP, btnSignIn;
    EditText etPhoneNumber, etOTP;
    private FirebaseAuth mAuth;
    private Spinner spinner;
    private String verificationid;
    PhoneAuthProvider.ForceResendingToken token;
    TextView resend;
    String ph_number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);
        btnGenerateOTP = findViewById(R.id.btn_generate_otp);
        btnSignIn = findViewById(R.id.btn_sign_in);
        etPhoneNumber = findViewById(R.id.et_phone_number);
        etOTP = findViewById(R.id.et_otp);
        resend=(TextView)findViewById(R.id.resend);
        mAuth = FirebaseAuth.getInstance();
        mAuth.getAccessToken(true);
        mAuth.getFirebaseAuthSettings().forceRecaptchaFlowForTesting(true);
        mAuth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);
        //set country code spinner
        spinner = findViewById(R.id.spinnerCountries);
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, CountryData.countryNames));
        btnGenerateOTP.setOnClickListener(view -> {
            ph_number="+"+CountryData.countryAreaCodes[spinner.getSelectedItemPosition()]+etPhoneNumber.getText().toString();
            sendVerificationCode(ph_number);
        });
        resend.setOnClickListener(view ->{
                sendVerificationCode(ph_number);
        }
                );
        btnSignIn.setOnClickListener(view -> {
            String code = etOTP.getText().toString().trim();
            if ((code.isEmpty() || code.length() < 6)){
                etOTP.setError("Enter code...");
                etOTP.requestFocus();
                return;
            }
            verifyCode(code);
        });
    }
    private void verifyCode(String code){
        try {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationid, code);
            signInWithCredential(credential);
        }catch (Exception e){
            Toast toast = Toast.makeText(this, "Verification Code is wrong, try again", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener((Executor) PhoneAuth.this, task -> {
                    if (task.isSuccessful()) {
                        userLogin();
                    } else {
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast toast = Toast.makeText(PhoneAuth.this, "Verification Code is wrong, try again", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                });
    }
    private void sendVerificationCode(String number){
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(number)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onCodeSent(@NotNull String verificationId, @NotNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        verificationid = verificationId;
                        token=forceResendingToken;
                    }

                    @Override
                    public void onVerificationCompleted(@NotNull PhoneAuthCredential phoneAuthCredential) {
                        String code = phoneAuthCredential.getSmsCode();
                        if (code != null){
                            verifyCode(code);
                        }
                    }

                    @Override
                    public void onVerificationFailed(@NotNull FirebaseException e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }
    private void userLogin() {
        class UserLogin extends AsyncTask<Void,Void,String> {
            public static final String REQUEST_METHOD = "GET";
            public static final int READ_TIMEOUT = 15000;
            public static final int CONNECTION_TIMEOUT = 15000;
            @Override
            protected String doInBackground(Void... voids) {
                String stringUrl = URLs.URL_LOGIN+"?phone="+ph_number;
                String result;
                String inputLine;
                try {
                    //Create a URL object holding our url
                    URL myUrl = new URL(stringUrl);
                    //Create a connection
                    HttpURLConnection connection =(HttpURLConnection)
                            myUrl.openConnection();
                    //Set methods and timeouts
                    connection.setRequestMethod(REQUEST_METHOD);
                    connection.setReadTimeout(READ_TIMEOUT);
                    connection.setConnectTimeout(CONNECTION_TIMEOUT);

                    //Connect to our url
                    connection.connect();
                    //Create a new InputStreamReader
                    InputStreamReader streamReader = new
                            InputStreamReader(connection.getInputStream());
                    //Create a new buffered reader and String Builder
                    BufferedReader reader = new BufferedReader(streamReader);
                    StringBuilder stringBuilder = new StringBuilder();
                    //Check if the line we are reading is not null
                    while((inputLine = reader.readLine()) != null){
                        stringBuilder.append(inputLine);
                    }
                    //Close our InputStream and Buffered reader
                    reader.close();
                    streamReader.close();
                    //Set our result equal to our stringBuilder
                    result = stringBuilder.toString();
                }
                catch(IOException e){
                    e.printStackTrace();
                    result = null;
                }
               // Log.d(TAG, "doInBackground: "+result);
                return result;
            }
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                //  progressBar = (ProgressBar) findViewById(R.id.progressBar);
                // progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                //progressBar.setVisibility(View.GONE);
                try {
                    JSONObject obj = new JSONObject(s);
                    //Log.i(TAG, "onPostExecute:"+obj.toString());
                    if (obj.getBoolean("status")) {
                        Toast.makeText(PhoneAuth.this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                        JSONArray userJson = obj.getJSONArray("data");
                        JSONObject ussr = userJson.getJSONObject(0);
                        User user = new User(
                                ussr.getString("phone"),
                                ussr.getString("username"),
                                ussr.getString("email"),
                                ussr.getString("gender")
                        );
                        SharedPrefManager.getInstance(PhoneAuth.this).userLogin(user);
                        finish();
                        startActivity(new Intent(PhoneAuth.this, MainActivity.class));
                    } else {
                        Toast.makeText(PhoneAuth.this, "User Not found : Please Register", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(PhoneAuth.this, RegisterAcivity.class);
                        intent.putExtra("data",ph_number);
                        intent.putExtra("isEmail",false);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        UserLogin ul = new UserLogin();
        ul.execute();
    }

}

