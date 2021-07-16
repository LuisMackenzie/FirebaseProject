package com.mackenzie.firebaseproject.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.mackenzie.firebaseproject.R;

import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvUser, tvPass, tvProvider;
    private EditText etAdress, etPhone;
    private Bundle bun;
    private String user, pass, prov;
    private Button btnLogOut, btnError, btnSave, btnDelete, btnLoad;
    private FirebaseAuth out;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private SharedPreferences prefs;
    private static final String TAG = "Fallo Auth";
    private FirebaseFirestore db;
    private Map<String, Object> userMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        objetos();

        // recuperamos la info
        bun = getIntent().getExtras();
        user = bun.getString("email");
        pass = bun.getString("pass");
        prov = bun.getString("provider");

        // aqui le enviamos la informacion al metodo
        setup(user, pass, prov);

        // Toast.makeText(this, "el user es: " + user, Toast.LENGTH_SHORT).show();
        // Toast.makeText(this, "la pass es: " + pass, Toast.LENGTH_SHORT).show();
        // Toast.makeText(this, "el proveedor es " + prov, Toast.LENGTH_SHORT).show();

        // Guardado de datos
        prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putString("email", user);
        prefEditor.putString("pass", pass);
        prefEditor.putString("provider", prov);
        prefEditor.apply();

        // instanciamos firebase
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        // Remote Config
        remoteConfig();

        // Instanciamos Firebase FireStore
        db = FirebaseFirestore.getInstance();
        // Create a new user with a first and last name
        userMap = new HashMap<>();


    }

    private void remoteConfig() {
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            boolean updated = task.getResult();

                            boolean showErrorBtn = mFirebaseRemoteConfig.getBoolean("show_error_button");
                            String errorBtnText = mFirebaseRemoteConfig.getString("error_button_text");
                            if (showErrorBtn) {
                                btnError.setVisibility(View.VISIBLE);
                            }
                            btnError.setText(errorBtnText);
                            Log.d(TAG, "Config params updated: " + updated);
                            // Toast.makeText(HomeActivity.this, "Fetch and activate succeeded",Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(HomeActivity.this, "Fetch failed", Toast.LENGTH_SHORT).show();
                        }
                        // displayWelcomeMessage();
                    }
                });
    }

    private void logOut() {
        Toast.makeText(HomeActivity.this, "Saliendo...", Toast.LENGTH_SHORT).show();
        prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);

        prefs.edit().clear().apply();
        // prefs.edit().commit();
        if (prov.equals("FACEBOOK")) {
            LoginManager.getInstance().logOut();
        }
        out.signOut();
        onBackPressed();
    }

    private void setup(String mail, String pass, String provider) {
        // Log.w("Email", "el mail es " + mail);
        tvUser.setText(mail);
        tvPass.setText(pass);
        tvProvider.setText(provider);

    }

    private void objetos() {
        tvUser = findViewById(R.id.tv_email);
        tvPass = findViewById(R.id.tv_pass);
        tvProvider = findViewById(R.id.tv_provider);
        etAdress = findViewById(R.id.et_adress);
        etPhone = findViewById(R.id.et_phone);
        btnLogOut = findViewById(R.id.btn_out);
        btnSave = findViewById(R.id.btn_save);
        btnLoad = findViewById(R.id.btn_load);
        btnDelete = findViewById(R.id.btn_delete);
        btnError = findViewById(R.id.btn_error);
        btnSave.setOnClickListener(this);
        btnLoad.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
        btnError.setOnClickListener(this);
        btnError.setVisibility(View.INVISIBLE);
        btnLogOut.setOnClickListener(this);
        out = FirebaseAuth.getInstance();
    }

    private void fireSave() {

        userMap.put("email", user);
        userMap.put("provider", prov);
        userMap.put("address", etAdress.getText().toString());
        userMap.put("phone", etPhone.getText().toString());
        db.collection("users").document(user).set(userMap);
        Toast.makeText(HomeActivity.this, "Se Guardo con exito!", Toast.LENGTH_SHORT).show();

    }

    private void fireLoad() {
        db.collection("users").document(user).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String dress = documentSnapshot.get("address").toString();
                        etAdress.setText(dress);
                        String phone = documentSnapshot.get("phone").toString();
                        etPhone.setText(phone);
                        Toast.makeText(HomeActivity.this, "Se han Recuperado los datos guardados", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_out:
                logOut();
                break;
            case R.id.btn_save:
                fireSave();
                break;
            case R.id.btn_load:
                fireLoad();
                break;
            case R.id.btn_delete:
                db.collection("users").document(user).delete();
                Toast.makeText(this, "Se Han Borrado los datos registrados", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_error:

                // Enviar invormacion adicional
                FirebaseCrashlytics.getInstance().setUserId(user);
                FirebaseCrashlytics.getInstance().setCustomKey("provider", prov);
                // enviar log de context
                FirebaseCrashlytics.getInstance().log("Se ha pulsado FORZAR ERROR");

                // Forzando un error
                // Toast.makeText(this, "Error Inminente", Toast.LENGTH_SHORT).show();
                // throw new RuntimeException("Error MAck");
                // break;
        }
    }

}