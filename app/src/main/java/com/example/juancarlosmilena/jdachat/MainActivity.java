package com.example.juancarlosmilena.jdachat;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements LogInFragment.LogInListener, UsersFragment.OnUserFragmentListener, SendMessageFragment.OnSendListener, ChatFragment.OnChatListener {


    private FirebaseAuth mAuth;
    List<User> userList = new ArrayList<>();

    ChildEventListener childEventListenerChat;
    DatabaseReference lastChatReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = new LogInFragment();

        fm.beginTransaction().replace(R.id.fragment_container, fragment , "LOGIN").commit();


    }

    @Override
    public void login(Boolean isLogin , final String email , final String password) {
        if(isLogin){

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.i("Firebase 00", "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                Toast.makeText(getApplicationContext(), "Authentication succed.",
                                        Toast.LENGTH_SHORT).show();

                                //Escribir en Firebase la información de usuario
                                //en esta version simplificada también guardamos
                                //un identificador para el único chat que tendrà cada usuario
                                Map<String,String> usermap = new HashMap<>();
                                usermap.put("email", email);
                                usermap.put("password", password);
                                usermap.put("chat","chat"+user.getUid());
                                FirebaseDatabase.getInstance().getReference().child("users")
                                        .child(user.getUid()).setValue(usermap);


                                cargar_usersFragment();


                            } else {
                                // If sign in fails, display a message to the user.
                                Log.i("Firebase 00", "signInWithEmail:failure", task.getException());
                                Toast.makeText(getApplicationContext(), "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();

                            }

                            // ...
                        }
                    });

        }else{

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.i("Firebase 00", "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                Toast.makeText(getApplicationContext(), "Authentication succed.",
                                        Toast.LENGTH_SHORT).show();



                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("Firebase 00", "createUserWithEmail:failure", task.getException());
                                Toast.makeText(getApplicationContext(), "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();

                            }

                            // ...
                        }
                    });


        }
    }

    private void cargar_usersFragment() {

        //Proceso habitual de cargar un fragment, pero añadiendo un TAG al fragment
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = new UsersFragment();
        fm.beginTransaction().replace(R.id.fragment_container, fragment , "USERS").addToBackStack("USERS").commit();

    }

    @Override
    public void getFirebaseUsers() {


        //Añadimos un Listener en el Firebase Database para leer
        //los usuarios registrados en la aplicacion y dibujarlos mediante Recyclerview
        FirebaseDatabase.getInstance().getReference()
                .child("users").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {


                User item = dataSnapshot.getValue(User.class);
                Log.i("FIREBASE USERS", item.getEmail());

                //Aqui buscamos al Fragment de Usuarios y llamamos a la función
                //addUserToList que añade un nuevo usuario al ArrayList
                //y actualiza el Adapter del RecyclerView
                UsersFragment usersFragment = (UsersFragment) getSupportFragmentManager().findFragmentByTag("USERS");
                usersFragment.addUserToList(item);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

    }

    @Override
    public void sendMessage(String chat) {

        //Proceso habitual de dibujar un fragment añadiendo un TAG
        //y utilizando newInstance() para poder comunicarle en que chat
        //deberá escribirse el mensaje
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = SendMessageFragment.newInstance(chat);
        fm.beginTransaction().replace(R.id.fragment_container, fragment, "SEND").commit();

    }

    @Override
    public void writeMessage(String msg , String chat) {

        //Generamos una Key automàtica en Firebase Database mediante push()
        //y escribimos en ese nuevo nodo del chat escogido el mensaje escrito
        FirebaseDatabase.getInstance().getReference().child("chat").child(chat).push().setValue(msg);

        //Carga de Fragment con TAG y con info inicial mediante newInstance
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = ChatFragment.newInstance(chat);
        fm.beginTransaction().replace(R.id.fragment_container, fragment, "CHAT").commit();
    }

    @Override
    public void getFirebaseChat(String chat) {


        //Declaramos por separado el ChildEventListener que añadiremos
        //más tarde a un nodo de Firebase Database
        //De este modo podremos más tarde eliminar el ChildEventListener
        //y que no se duplique información
        childEventListenerChat = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                String msg = dataSnapshot.getValue(String.class);

                //usersFragment.addUserToList(item);
                ChatFragment chatFragment = (ChatFragment) getSupportFragmentManager().findFragmentByTag("CHAT");
                chatFragment.addMessageToList(msg);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };

        //Guardamos en una variable la referencia a un nodo concreto de
        //Firebase Database para utilizarla para añadir el ChildEventListener
        //y para eliminar más tarde el Listener y evitar duplicar.
        lastChatReference = FirebaseDatabase.getInstance().getReference()
                .child("chat").child(chat);

        lastChatReference.addChildEventListener(childEventListenerChat);

    }

    @Override
    public void removeChatListener() {
        //Este metodo elimina el ChildEventListener que se utilizó para
        //mostrar en el RecyclerView los mensajes de un chat concreto
        lastChatReference.removeEventListener(childEventListenerChat);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Debo inflar el recurso de tipo Menu que debo tener generado
        //en res/menu
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    //Este método será invocado cada vez que alguien pulse
    //alguno de los items del menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //Escojo que hacer en función del botón pulsado
        switch (item.getItemId()){
            case R.id.usersMenu:
                cargar_usersFragment();
                break;

            //Este botón de momento no tiene utilidad ;-)
            case R.id.msgMenu:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
