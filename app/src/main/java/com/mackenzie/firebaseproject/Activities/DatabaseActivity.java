package com.mackenzie.firebaseproject.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mackenzie.firebaseproject.Models.Clases;
import com.mackenzie.firebaseproject.R;

import java.util.HashMap;
import java.util.Map;

public class DatabaseActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etContent;
    private Spinner spin1, spin2;
    private Button btnSend;
    private FirebaseFirestore db;
    private Map<String, Object> userMap;
    private String [] secciones = {"Sistemas","Programacion","Procesos Matematicos","Extras"};
    private String [] asignaturas = {"Logica Computacional","Tecnologia","Algoritmos","Herramientas", "Lenguaje de Prog II"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);
        objetos();
        // Cambiamos el titulo a la ActionBar
        getSupportActionBar().setTitle("Bases De Datos Input");
        // Iniciamos la BD de fireStore
        db = FirebaseFirestore.getInstance();
        // Iniciamos el objetos que recopilara los datos que le pasaremos a la BD
        userMap = new HashMap<>();
        // Creamos un ArrayAdapter para gestionar el contenido del Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item_seccion, secciones);
        ArrayAdapter <String> adapter2 = new ArrayAdapter<String>(this, R.layout.spinner_item_area, asignaturas);
        // Asignamos el contenido del adapter al Spinner
        spin1.setAdapter(adapter);
        spin2.setAdapter(adapter2);



    }

    public void registrarClase() {
        String seccion = spin1.getSelectedItem().toString();
        String area = spin2.getSelectedItem().toString();
        String tema = etContent.getText().toString();
        if (!TextUtils.isEmpty(tema)) {
            userMap.put("Seccion", seccion);
            userMap.put("Area", area);
            userMap.put("Tema", tema);

            // Forma reducida
            // db.collection("Clases").add(userMap);
            // Forma Extendida con confirmacion de exito o fracaso en la ejecucion
            db.collection("Clases")
                    .add(userMap)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            // Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                            Toast.makeText(DatabaseActivity.this, "Se Guardo con exito!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Log.w(TAG, "Error adding document", e);
                            Toast.makeText(DatabaseActivity.this, "algo salio mal", Toast.LENGTH_SHORT).show();
                        }
                    });

        } else {
            Toast.makeText(this, "Debe Rellenar el Campo de texto", Toast.LENGTH_SHORT).show();
        }
    }

    private void objetos() {
        etContent = findViewById(R.id.et_content);
        spin1 = findViewById(R.id.spinner_seccion);
        spin2 = findViewById(R.id.spinner_area);
        btnSend = findViewById(R.id.btn_registrar);
        btnSend.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        registrarClase();
    }
}