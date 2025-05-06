package com.henrique.pesquiifma;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReplyForm extends AppCompatActivity {
    private LinearLayout questionsContainer;
    private Button submitButton;
    private FirebaseFirestore db;
    private String formId;
    private String uid;
    private List<RadioGroup> radioGroups = new ArrayList<>();
    private List<EditText> answerFields = new ArrayList<>();
    private List<CheckBox> singleChoiceCheckboxes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply_form);
        questionsContainer = findViewById(R.id.questions_container);
        submitButton = findViewById(R.id.submit_button);
        db = FirebaseFirestore.getInstance();

        Uri data = getIntent().getData();
        if (data != null) {
            formId = data.getLastPathSegment();
            Log.d("ReplyForm", "Link recebido: " + data.toString());
            loadFormQuestions();
        } else {
            Log.d("ReplyForm", "Nenhum link recebido");
        }

        submitButton.setOnClickListener(v -> submitAnswers());
    }

    private void loadFormQuestions() {
        db.collection("formularios")
                .document(formId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> questions = (List<Map<String, Object>>) documentSnapshot.get("perguntas");
                        uid = documentSnapshot.getString("uid");

                        if (questions != null) {
                            for (Map<String, Object> questionMap : questions) {
                                String questionTextStr = (String) questionMap.get("pergunta");
                                String questionId = (String) questionMap.get("id");
                                String tipoResposta = (String) questionMap.get("tipoResposta");

                                if ("Texto".equals(tipoResposta)) {
                                    createTextQuestion(questionTextStr);
                                } else if ("Sim/Não".equals(tipoResposta)) {
                                    createYesNoQuestion(questionTextStr);
                                } else if ("Múltipla Escolha".equals(tipoResposta)) {
                                    // Extração da parte da string que contém as opções
                                    String optionsString = questionTextStr.split(":")[1].trim();  // Obtém a parte após ":"
                                    optionsString = optionsString.substring(1, optionsString.length() - 1);  // Remove os colchetes []

                                    // Divida a string com base nas vírgulas para obter a lista de opções
                                    List<String> opcoes = Arrays.asList(optionsString.split(",\\s*"));  // Divide e remove os espaços

                                    createMultipleChoiceQuestion(questionTextStr, opcoes);
                                } else if ("Escolha Única".equals(tipoResposta)) {
                                    createSingleChoiceQuestion(questionTextStr);
                                }
                            }
                        }
                    } else {
                        Toast.makeText(ReplyForm.this, "Formulário não encontrado!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("ReplyForm", "Erro ao carregar formulário: " + e.getMessage());
                    Toast.makeText(ReplyForm.this, "Erro ao carregar o formulário!", Toast.LENGTH_SHORT).show();
                });
    }

    private void createTextQuestion(String questionText) {
        TextView questionTextView = new TextView(this);
        questionTextView.setText(questionText);
        questionsContainer.addView(questionTextView);

        EditText answerField = new EditText(this);
        questionsContainer.addView(answerField);
        answerFields.add(answerField);
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
        radioGroups.add(radioGroup);
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
        radioGroups.add(radioGroup);
    }

    private void createSingleChoiceQuestion(String questionText) {
        TextView questionTextView = new TextView(this);
        questionTextView.setText(questionText);
        questionsContainer.addView(questionTextView);

        CheckBox checkBox = new CheckBox(this);
        checkBox.setText("Selecionar");
        questionsContainer.addView(checkBox);

        singleChoiceCheckboxes.add(checkBox);
    }

    private void submitAnswers() {
        List<String> answers = new ArrayList<>();

        // Coleta respostas de texto
        for (EditText field : answerFields) {
            answers.add(field.getText().toString());
        }

        // Coleta respostas de radio buttons
        for (RadioGroup group : radioGroups) {
            int selectedId = group.getCheckedRadioButtonId();
            if (selectedId != -1) {
                RadioButton selected = group.findViewById(selectedId);
                answers.add(selected.getText().toString());
            } else {
                answers.add("");
            }
        }

        // Coleta respostas de escolha única
        for (CheckBox checkBox : singleChoiceCheckboxes) {
            answers.add(checkBox.isChecked() ? "Selecionado" : "Não selecionado");
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String, Object> answerMap = new HashMap<>();
        answerMap.put("userId", userId);
        answerMap.put("answers", answers);

        CollectionReference answersRef = db.collection("formularios")
                .document(formId)
                .collection("respostas");

        answersRef.add(answerMap)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(ReplyForm.this, "Respostas enviadas com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ReplyForm.this, "Erro ao enviar respostas.", Toast.LENGTH_SHORT).show();
                });
    }
}