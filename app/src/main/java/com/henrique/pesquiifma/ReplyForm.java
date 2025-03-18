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

                                // Verifica se a pergunta é do tipo "Sim/Não"
                                if (questionTextStr.contains("Sim/Não")) {
                                    createYesNoQuestion(questionTextStr);
                                }
                                // Verifica se a pergunta é do tipo "Múltipla Escolha"
                                else if (questionTextStr.contains("Múltipla Escolha")) {
                                    // Extração da parte da string que contém as opções
                                    String optionsString = questionTextStr.split(":")[1].trim();  // Obtém a parte após ":"
                                    optionsString = optionsString.substring(1, optionsString.length() - 1);  // Remove os colchetes []

                                    // Divida a string com base nas vírgulas para obter a lista de opções
                                    List<String> optionsList = Arrays.asList(optionsString.split(",\\s*"));  // Divide e remove os espaços

                                    // Agora passamos a lista de opções para o método de criação da pergunta
                                    createMultipleChoiceQuestion(questionTextStr, optionsList);
                                }
                                else {
                                    createTextQuestion(questionTextStr);
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

    private void createYesNoQuestion(String questionText) {
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

    private void createMultipleChoiceQuestion(String questionText, List<String> options) {
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

    private void createTextQuestion(String questionText) {
        TextView questionTextView = new TextView(this);
        questionTextView.setText(questionText);
        questionsContainer.addView(questionTextView);

        EditText answerField = new EditText(this);
        questionsContainer.addView(answerField);
        answerFields.add(answerField); // Adiciona o EditText à lista
    }

    private void submitResponses() {
        List<String> answers = new ArrayList<>();

        // Coleta as respostas dos EditTexts
        for (EditText field : answerFields) {
            String answer = field.getText().toString();
            answers.add(answer);
            Log.d("Resposta", "Resposta de EditText: " + answer);
        }

        // Coleta as respostas dos RadioGroups
        for (RadioGroup group : radioGroups) {
            int selectedId = group.getCheckedRadioButtonId();
            if (selectedId != -1) {
                RadioButton selectedRadioButton = findViewById(selectedId);
                String answer = selectedRadioButton.getText().toString();
                answers.add(answer);
                Log.d("Resposta", "Resposta de RadioGroup: " + answer);
            } else {
                answers.add("");  // Ou algum valor default caso nenhum botão seja selecionado
                Log.d("Resposta", "Nenhuma resposta selecionada para RadioGroup.");
            }
        }

        // Verifique se as respostas foram corretamente coletadas
        Log.d("Resposta", "Respostas coletadas: " + answers.toString());

        // Salvar as respostas no Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("respostas")
                .document(formId) // Usando o formId para salvar as respostas associadas ao formulário
                .collection("respostas") // Adicionando uma subcoleção para respostas individuais
                .add(new ResponseWrapper(answers, formId, uid)) // Criar um novo documento de resposta
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Respostas enviadas com sucesso!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.d("Resposta", "Respostas coletadas: " + answers.toString());
                    Toast.makeText(this, "Erro ao enviar as respostas!", Toast.LENGTH_SHORT).show();
                });
    }


    // Classe auxiliar para salvar as respostas no Firestore
    public static class ResponseWrapper {
        public List<String> respostas;
        public String formId; // ID do formulário
        public String uid; // UID do formulário no Firestore

        public ResponseWrapper() {}

        public ResponseWrapper(List<String> respostas, String formId, String uid) {
            this.respostas = respostas;
            this.formId = formId; // Inicializando com o formId
            this.uid = uid; // Inicializando com o UID do Firestore
        }
    }
}