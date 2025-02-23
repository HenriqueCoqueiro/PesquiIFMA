package com.henrique.pesquiifma;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReplyForm extends AppCompatActivity {
    private LinearLayout questionsContainer;
    private Button submitButton;
    private List<EditText> answerFields;
    private String formId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply_form);

        questionsContainer = findViewById(R.id.questions_container);
        submitButton = findViewById(R.id.submit_button);
        answerFields = new ArrayList<>();

        // Captura o link que abriu o app
        Uri data = getIntent().getData();
        if (data != null) {
            formId = data.getLastPathSegment(); // Pega o ID do formulário da URL
            Log.d("ReplyForm", "Link recebido: " + data.toString());  // Verificar o link
            carregarFormulario(formId); // Buscar as perguntas no Firestore
        } else {
            Log.d("ReplyForm", "Nenhum link recebido");
        }

        submitButton.setOnClickListener(v -> submitResponses());
    }

    private void carregarFormulario(String formId) {
        // Buscar o formulário do Firestore usando o formId
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("formularios")
                .document(formId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> questions = (List<Map<String, Object>>) documentSnapshot.get("perguntas");
                        String uid = documentSnapshot.getString("uid"); // Recuperar o UID do formulário

                        if (questions != null) {
                            for (Map<String, Object> questionMap : questions) {
                                String questionTextStr = (String) questionMap.get("pergunta");

                                TextView questionText = new TextView(this);
                                questionText.setText(questionTextStr);
                                questionsContainer.addView(questionText);

                                EditText answerField = new EditText(this);
                                questionsContainer.addView(answerField);
                                answerFields.add(answerField);
                            }
                        }
                    } else {
                        Toast.makeText(this, "Formulário não encontrado!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("ReplyForm", "Erro ao carregar formulário: " + e.getMessage());
                    Toast.makeText(this, "Erro ao carregar o formulário!", Toast.LENGTH_SHORT).show();
                });
    }

    private void submitResponses() {
        List<String> answers = new ArrayList<>();
        for (EditText field : answerFields) {
            answers.add(field.getText().toString());
        }

        // Salvar as respostas no Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("respostas")
                .document(formId) // Use o formId para salvar as respostas associadas ao formulário
                .set(new ResponseWrapper(answers, formId)) // Adicionando o UID ao wrapper
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Respostas enviadas com sucesso!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao enviar as respostas!", Toast.LENGTH_SHORT).show();
                });
    }

    // Classe auxiliar para salvar as respostas no Firestore
    public static class ResponseWrapper {
        public List<String> respostas;
        public String formId; // Incluindo o formId (UID) nas respostas

        public ResponseWrapper() {}

        public ResponseWrapper(List<String> respostas, String formId) {
            this.respostas = respostas;
            this.formId = formId; // Inicializando o UID no ResponseWrapper
        }
    }

}
