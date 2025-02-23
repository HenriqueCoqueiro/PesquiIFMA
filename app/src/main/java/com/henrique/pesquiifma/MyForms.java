package com.henrique.pesquiifma;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import entities.Form;


import adapters.FormAdapter;

public class MyForms extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FormAdapter adapter;
    private List<Form> formList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_forms);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        formList = new ArrayList<>();
        adapter = new FormAdapter(formList);
        recyclerView.setAdapter(adapter);

        carregarFormulariosDoUsuario();
    }

    private void carregarFormulariosDoUsuario() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); // Pega o usuário logado
        if (user == null) {
            Toast.makeText(this, "Usuário não logado", Toast.LENGTH_SHORT).show();
            return; // Se não houver usuário logado, retorna
        }

        String userId = user.getUid(); // Pega o ID do usuário logado

        // Inicializando a lista de formulários
        if (formList == null) {
            formList = new ArrayList<>();
        }

        // Inicializando o adapter se ainda não foi feito
        if (adapter == null) {
            adapter = new FormAdapter(formList);
            recyclerView.setAdapter(adapter);
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("formularios")
                .whereEqualTo("uid", userId) // Filtra pelos formulários do usuário
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    formList.clear(); // Limpa a lista antes de adicionar novos formulários

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String titulo = document.getString("titulo");
                        String descricao = document.getString("descricao");
                        formList.add(new Form(titulo, descricao));
                    }

                    // Notifica o adapter que a lista foi atualizada
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao carregar os formulários!", Toast.LENGTH_SHORT).show();
                });
    }

}
