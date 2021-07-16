package com.mackenzie.firebaseproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.mackenzie.firebaseproject.Activities.DatabaseActivity;
import com.mackenzie.firebaseproject.Activities.HomeActivity;

import java.util.Arrays;
import java.util.List;

import static com.google.android.gms.common.util.CollectionUtils.listOf;
import static com.google.android.gms.common.util.CollectionUtils.mapOf;

public class AuthActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseAuth mAuth;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private GoogleSignInClient mGoogleSignInClient;
    private SignInButton btnGoogle;
    private List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build());
    private Button btnReg, btnLogin, btnTest;
    private EditText etEmail, etPass;
    private String mail, pass;
    private ProgressDialog dialog;
    private String provider = "BASIC", provider2 = "GOOGLE", provider3 = "FACEBOOK";
    private static final String TAG = "Fallo Auth";
    private static final int RC_SIGN_IN = 100;
    private SharedPreferences prefs;
    private LinearLayout authLayout;
    private CallbackManager cManager;
    private LoginButton loginFace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setTheme(R.style.Theme_FirebaseProject);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        objetos();
        mAuth = FirebaseAuth.getInstance();
        // Inicializamos facebook
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        // Creamos el objeto de Remote config
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        // Creamos el objeto de analitycs
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString("Message", "Integracion con firebase completa");
        // le pasamos el bundle al analytics mediante el logevent
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        // Remote Config
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(60)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config);

        notification();
        setup();
        sesion();

    }

    private void notification() {
        // Aqui obtenemos el token de user
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        // Log and toast
                        // String msg = getString(R.string.msg_token_fmt, token);
                        // Log.w(TAG, msg);
                        // Toast.makeText(AuthActivity.this, "Habemus Token", Toast.LENGTH_SHORT).show();
                        // Log.e(TAG, token);
                    }
                });

        Intent intent = new Intent();
        String url = intent.getStringExtra("url");
        boolean isUrlNull = url != null && !url.isEmpty();
        if (isUrlNull) {
            Toast.makeText(this, "Ha llegado informacion:  " + url, Toast.LENGTH_SHORT).show();
        } else {
            // Toast.makeText(this, "Ha llegado nulo el url" + url, Toast.LENGTH_SHORT).show();
        }

        // aqui se suscribe el user un tema
        /*FirebaseMessaging.getInstance().subscribeToTopic("Tutorial")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = getString(R.string.msg_subscribed);
                        if (!task.isSuccessful()) {
                            msg = getString(R.string.msg_subscribe_failed);
                        }
                        Log.d(TAG, msg);
                        // Toast.makeText(AuthActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });*/
    }

    private void sesion() {

        prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);

        String mail2 = prefs.getString("email", null);
        String pass2 = prefs.getString("pass", null);
        String prov2 = prefs.getString("provider", null);
        if (mail2 != null && prov2 != null) {
            authLayout.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Autenticado!", Toast.LENGTH_SHORT).show();
            showHome(mail2, pass2, prov2);
        } else if (prov2 != null){
            authLayout.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Autenticado con campo de mail vacio", Toast.LENGTH_LONG).show();
            showHome(mail2, pass2, prov2);
        }

        // Toast.makeText(this, "algo salio mal 03", Toast.LENGTH_SHORT).show();

    }

    private void objetos() {
        btnReg = findViewById(R.id.btn_sign);
        btnLogin = findViewById(R.id.btn_login);
        btnTest = findViewById(R.id.btn_test);
        loginFace = findViewById(R.id.btn_facebook);
        btnGoogle = findViewById(R.id.btn_google);
        etEmail = findViewById(R.id.et_email);
        etPass = findViewById(R.id.et_pass);
    }

    private void setup() {
        // mail = etEmail.getText().toString();
        // pass = etPass.getText().toString();
        btnReg.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        btnTest.setOnClickListener(this);
        btnGoogle.setOnClickListener(this);
        loginFace.setOnClickListener(this);
        dialog = new ProgressDialog(this);
        authLayout = new LinearLayout(this);
        cManager = CallbackManager.Factory.create();
        loginFace.setReadPermissions("email", "public_profile");

    }

    private void login() {
        mail = etEmail.getText().toString();
        pass = etPass.getText().toString();

        dialog.setMessage("Autenticando...");
        dialog.show();

        mAuth.signInWithEmailAndPassword(mail, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            // Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            showHome(user.getEmail() , pass, provider);
                            // updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG , "createUserWithEmail:failure", task.getException());
                            Toast.makeText(AuthActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            // updateUI(null);
                        }

                    }
                });
    }

    private void faceLogin() {

        // Aqui no se que hace esto TODO Rellenar esto urgente la ultima intruncuion
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));


        LoginManager.getInstance().registerCallback(cManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                if (loginResult != null) {
                    AccessToken accessToken = AccessToken.getCurrentAccessToken();
                    // aqui creamos un objeto que sera true si accestoken no es nulo ni esta expirado
                    boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
                    handleFacebookAccessToken(accessToken);
                }
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(AuthActivity.this, "Error de autenticacion de face", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void googleLogin() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void register() {
        mail = etEmail.getText().toString();
        pass = etPass.getText().toString();

        dialog.setMessage("Realizando el registro en linea...");
        dialog.show();

        mAuth.createUserWithEmailAndPassword(mail, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            // Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            showHome(user.getEmail() , pass, provider);
                            // updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG , "createUserWithEmail:failure", task.getException());
                            Toast.makeText(AuthActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            // updateUI(null);
                        }

                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            String mail = user.getDisplayName();
                            if (user != null) {
                                showHome(mail, null, provider3);
                            } else {
                                Toast.makeText(AuthActivity.this, "El email llega nulo" + user.getEmail(), Toast.LENGTH_SHORT).show();
                            }
                            // updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(AuthActivity.this, "Authentication failed. firebase error", Toast.LENGTH_SHORT).show();
                            // updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void showHome(String mail, String pass, String provider) {
        Intent in = new Intent(AuthActivity.this, HomeActivity.class);
        in.putExtra("email", mail);
        in.putExtra("pass", pass);
        in.putExtra("provider", provider);
        in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(in);
    }

    @Override
    protected void onStart() {
        authLayout.setVisibility(View.VISIBLE);
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // updateUI(currentUser);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Por último, en tu método onActivityResult, llama a callbackManager.onActivityResult para pasar el resultado del
        // inicio de sesión a LoginManager mediante callbackManager.
        cManager.onActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);

                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                // firebaseAuthWithGoogle(account.getIdToken());
                if (account != null) {
                    dialog.setMessage("Autenticando...");
                    dialog.show();
                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                    mAuth.getInstance().signInWithCredential(credential)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        // Log.d(TAG, "createUserWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        showHome(user.getEmail() , pass, provider2);
                                        // updateUI(user);
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG , "createUserWithEmail:failure", task.getException());
                                        Toast.makeText(AuthActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                        // updateUI(null);
                                    }

                                }
                            });
                }

            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_sign:
                if (!TextUtils.isEmpty(etEmail.getText()) && !TextUtils.isEmpty(etPass.getText())) {
                    register();
                } else {
                    Toast.makeText(AuthActivity.this, "Hay algo vacio", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_login:
                if (!TextUtils.isEmpty(etEmail.getText()) && !TextUtils.isEmpty(etPass.getText())) {
                    login();
                } else {
                    Toast.makeText(AuthActivity.this, "Hay algo vacio", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_test:
                // googleLogin();
                Intent in = new Intent(AuthActivity.this, DatabaseActivity.class);
                in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(in);
                break;
            case R.id.btn_facebook:
                Toast.makeText(this, "Boton Feisbuk pulsao!", Toast.LENGTH_SHORT).show();
                faceLogin();
                break;
            case R.id.btn_google:
                Toast.makeText(this, "Este boton de google inicia sesion", Toast.LENGTH_SHORT).show();
                googleLogin();
                break;
        }
    }
}