package com.henrique.pesquiifma;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class RespostasActivity extends AppCompatActivity {
    private List<String> respostasList;
    private TextView respostasTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_respostas);

        respostasTextView = findViewById(R.id.respostasTextView);

        // Pega o formId passado da activity anterior
        String formId = getIntent().getStringExtra("formId");

        // Log para verificar o valor do formId
        Log.d("RespostasActivity", "formId recebido: " + formId);

        if (formId != null) {
            carregarRespostas(formId);
        } else {
            Toast.makeText(this, "Erro: Formulário não encontrado!", Toast.LENGTH_SHORT).show();
        }
    }

    private void carregarRespostas(String formId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Buscar as respostas na subcoleção "respostas" usando o formId
        db.collection("respostas")  // A coleção onde as respostas estão armazenadas
                .document(formId)    // Usando o formId como documento para as respostas desse formulário
                .collection("respostas") // Subcoleção onde as respostas individuais estão
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    respostasList = new ArrayList<>();

                    // Processa as respostas
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Acessando as respostas no documento da subcoleção
                        List<String> respostas = (List<String>) document.get("respostas");
                        if (respostas != null) {
                            respostasList.addAll(respostas); // Adiciona todas as respostas na lista
                        }
                    }

                    // Exibe as respostas
                    if (!respostasList.isEmpty()) {
                        StringBuilder respostasText = new StringBuilder();
                        for (String resposta : respostasList) {
                            respostasText.append(resposta).append("\n");
                        }
                        respostasTextView.setText(respostasText.toString());
                    } else {
                        respostasTextView.setText("Nenhuma resposta encontrada.");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao carregar as respostas!", Toast.LENGTH_SHORT).show();
                });
    }
}
