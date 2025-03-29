package com.henrique.pesquiifma;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ReplyForm extends AppCompatActivity {
    private LinearLayout questionsContainer;
    private Button submitButton;
    private List<EditText> answerFields; // Para campos EditText
    private List<RadioGroup> radioGroups; // Para RadioGroups
    private String formId; // Aqui é o formId passado pela URL
    private String uid; // Este será o UID do documento no Firestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply_form);

        questionsContainer = findViewById(R.id.questions_container);
        submitButton = findViewById(R.id.submit_button);
        answerFields = new ArrayList<>();
        radioGroups = new ArrayList<>();  // Inicializa a lista para RadioGroups

        // Captura o link que abriu o app
        Uri data = getIntent().getData();
        if (data != null) {
            formId = data.getLastPathSegment(); // Pega o formId (ID do formulário) da URL
            Log.d("ReplyForm", "Link recebido: " + data.toString());  // Verificar o link
            carregarFormulario(formId); // Buscar as perguntas no Firestore
        } else {
            Log.d("ReplyForm", "Nenhum link recebido");
        }

        submitButton.setOnClickListener(v -> submitResponses());
    }

    private void carregarFormulario(String formId) {
        // Buscar o formulário do Firestore usando o formId (ID do formulário)
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("formularios")
                .document(formId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> questions = (List<Map<String, Object>>) documentSnapshot.get("perguntas");
                        uid = documentSnapshot.getString("uid"); // Recuperar o UID do formulário

                        if (questions != null) {
                            for (Map<String, Object> questionMap : questions) {
                                String questionTextStr = (String) questionMap.get("pergunta");
                                String questionId = (String) questionMap.get("id"); // Obtém o id da pergunta

                                // Verifica se a pergunta é do tipo "Sim/Não"
                                if (questionTextStr.contains("Sim/Não")) {
                                    createYesNoQuestion(questionTextStr, questionId);
                                }
                                // Verifica se a pergunta é do tipo "Múltipla Escolha"
                                else if (questionTextStr.contains("Múltipla Escolha")) {
                                    // Extração da parte da string que contém as opções
                                    String optionsString = questionTextStr.split(":")[1].trim();  // Obtém a parte após ":"
                                    optionsString = optionsString.substring(1, optionsString.length() - 1);  // Remove os colchetes []

                                    // Divida a string com base nas vírgulas para obter a lista de opções
                                    List<String> optionsList = Arrays.asList(optionsString.split(",\\s*"));  // Divide e remove os espaços

                                    // Agora passamos a lista de opções para o método de criação da pergunta
                                    createMultipleChoiceQuestion(questionTextStr, optionsList, questionId);
                                }
                                else {
                                    createTextQuestion(questionTextStr, questionId);
                                }
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

    private void createYesNoQuestion(String questionText, String questionId) {
        TextView questionTextView = new TextView(this);
        questionTextView.setText(questionText);
        questionsContainer.addView(questionTextView);

        RadioGroup radioGroup = new RadioGroup(this);
        radioGroup.setOrientation(RadioGroup.VERTICAL);

        RadioButton yesButton = new RadioButton(this);
        yesButton.setText("Sim");
        radioGroup.addView(yesButton);

        RadioButton noButton = new RadioButton(this);
        noButton.setText("Não");
        radioGroup.addView(noButton);

        questionsContainer.addView(radioGroup);
        radioGroups.add(radioGroup); // Adiciona o RadioGroup à lista
    }

    private void createMultipleChoiceQuestion(String questionText, List<String> options, String questionId) {
        TextView questionTextView = new TextView(this);
        questionTextView.setText(questionText);
        questionsContainer.addView(questionTextView);

        RadioGroup radioGroup = new RadioGroup(this);
        radioGroup.setOrientation(RadioGroup.VERTICAL);

        for (String option : options) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(option);
            radioGroup.addView(radioButton);
        }

        questionsContainer.addView(radioGroup);
        radioGroups.add(radioGroup); // Adiciona o RadioGroup à lista
    }

    private void createTextQuestion(String questionText, String questionId) {
        TextView questionTextView = new TextView(this);
        questionTextView.setText(questionText);
        questionsContainer.addView(questionTextView);

        EditText answerField = new EditText(this);
        questionsContainer.addView(answerField);
        answerFields.add(answerField); // Adiciona o EditText à lista
    }

    private void submitResponses() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Coleta as respostas dos EditTexts
        for (int i = 0; i < answerFields.size(); i++) {
            EditText field = answerFields.get(i);
            String answer = field.getText().toString();
            String questionId = "pergunta_" + (i + 1); // Gerar ID da pergunta, ou use o que foi recuperado no carregarFormulario
            saveAnswer(questionId, answer);
        }

        // Coleta as respostas dos RadioGroups
        for (int i = 0; i < radioGroups.size(); i++) {
            RadioGroup group = radioGroups.get(i);
            int selectedId = group.getCheckedRadioButtonId();
            String answer = "";
            if (selectedId != -1) {
                RadioButton selectedRadioButton = findViewById(selectedId);
                answer = selectedRadioButton.getText().toString();
            }
            String questionId = "pergunta_" + (i + 1); // Gerar ID da pergunta, ou use o que foi recuperado no carregarFormulario
            saveAnswer(questionId, answer);
        }
    }

    private void saveAnswer(String questionId, String answer) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Salvando as respostas na subcoleção 'respostas'
        db.collection("formularios")  // Coleção 'formularios'
                .document(formId)  // Documento do formulário com formId
                .collection("respostas")  // Subcoleção 'respostas'
                .add(new AnswerWrapper(questionId, answer))  // Adicionar um novo documento de resposta
                .addOnSuccessListener(aVoid -> {
                    Log.d("Resposta", "Resposta salva com sucesso!");
                })
                .addOnFailureListener(e -> {
                    Log.d("Resposta", "Erro ao salvar a resposta: " + e.getMessage());
                });
    }

    // Classe auxiliar para salvar as respostas no Firestore
    public static class AnswerWrapper {
        public String perguntaId; // ID da pergunta
        public String resposta; // Resposta

        public AnswerWrapper() {}

        public AnswerWrapper(String perguntaId, String resposta) {
            this.perguntaId = perguntaId;
            this.resposta = resposta;
        }
    }
}
