package com.example.todolist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String emailUsuario;
    ListView listViewTareas;
    List<String> listaTareas = new ArrayList<>();
    List<String> listaIdTareas = new ArrayList<>();
    ArrayAdapter<String> mAdapterTareas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        emailUsuario = mAuth.getCurrentUser().getEmail();
        listViewTareas = findViewById(R.id.ListView);

        //Actualizar la UI con las tareas del usuario logeado
        actualizarUI();


    }

    private void actualizarUI() {
        db.collection("Tareas")
                .whereEqualTo("emailUsuario", emailUsuario)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }

                        listaTareas.clear();
                        listaIdTareas.clear();

                        for (QueryDocumentSnapshot doc : value) {
                            listaIdTareas.add(doc.getId());
                            listaTareas.add(doc.getString("nombreTarea"));
                            }
                        if (listaTareas.size() == 0) {
                            listViewTareas.setAdapter(null);
                        }else {
                            mAdapterTareas = new ArrayAdapter<String>(MainActivity.this, R.layout.item_tarea,R.id.nombreTarea,listaTareas);
                            listViewTareas.setAdapter(mAdapterTareas);
                        }

                        }
                    });
                }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mas:
                //activar el cuadro de diálogo para añadir tarea
                final EditText taskEditText = new EditText(this);
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("Nueva tarea")
                        .setMessage("¿Que quieres hacer a continuación?")
                        .setView(taskEditText)
                        .setPositiveButton("Añadir", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //añadir tarea a la DB

                                String miTarea = taskEditText.getText().toString();

                                Map<String, Object> tarea = new HashMap<>();
                                tarea.put("nombreTarea", miTarea);
                                tarea.put("emailUsuario", emailUsuario);

                                db.collection("Tareas").add(tarea);

                                Toast addTarea = new Toast(getApplicationContext());
                                LayoutInflater inflater = getLayoutInflater();
                                View layout = inflater.inflate(R.layout.toast_layout,
                                        (ViewGroup) findViewById(R.id.lytLayout));
                                TextView txtMsg = (TextView)layout.findViewById(R.id.txtMensaje);
                                txtMsg.setText("Tarea añadida");
                                addTarea.setDuration(Toast.LENGTH_SHORT);
                                addTarea.setView(layout);
                                addTarea.show();




                            }
                        })
                        .setNegativeButton("Cancelar", null)
                        .create();
                dialog.show();
                return true;

            case R.id.logout:
                //cerrar sesion con Firebase y volver a pantalla de login
                mAuth.signOut();
                onBackPressed();
                finish();
                Toast.makeText(MainActivity.this, "Sesion cerrada", Toast.LENGTH_LONG).show();
                return true;
            default:return super.onOptionsItemSelected(item);


        }

    }

    public void borrarTarea(View view) {
        View parent = (View) view.getParent();
        TextView tareaTextView = parent.findViewById(R.id.nombreTarea);
        String tarea = tareaTextView.getText().toString();
        int posicion = listaTareas.indexOf(tarea);
        db.collection("Tareas").document(listaIdTareas.get(posicion)).delete();
    }

    public void editarTarea(View view) {
        View parent = (View) view.getParent();
        TextView tareaTextView = parent.findViewById(R.id.nombreTarea);
        String tarea = tareaTextView.getText().toString();
        int posicion = listaTareas.indexOf(tarea);

        // Activar cuadro de diálogo para editar tarea
        final EditText taskEditText = new EditText(this);
        taskEditText.setText(tarea);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Editar tarea")
                .setMessage("Ingrese el nuevo nombre de la tarea")
                .setView(taskEditText)
                .setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Editar tarea en la base de datos
                        String nuevoNombre = taskEditText.getText().toString();
                        db.collection("Tareas").document(listaIdTareas.get(posicion)).update("nombreTarea", nuevoNombre);

                        Toast addTarea = new Toast(getApplicationContext());
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate(R.layout.toast_layout,
                                (ViewGroup) findViewById(R.id.lytLayout));
                        TextView txtMsg = (TextView)layout.findViewById(R.id.txtMensaje);
                        txtMsg.setText("Tarea editada");
                        addTarea.setDuration(Toast.LENGTH_SHORT);
                        addTarea.setView(layout);
                        addTarea.show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .create();
        dialog.show();
    }

}