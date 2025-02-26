package entities;

import java.util.List;

public class Form {
    private String formId; // Novo campo para armazenar o ID do formulário
    private String title;
    private String description;
    private List<String> questions;
    private String uid; // Novo campo para armazenar o UID

    public Form() {
    }

    public Form(String formId, String title, String description, List<String> questions, String uid) {
        this.formId = formId; // Inicializa o formId
        this.title = title;
        this.description = description;
        this.questions = questions;
        this.uid = uid;
    }

    public Form(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getFormId() {
        return formId; // Método getter para o ID do formulário
    }

    public void setFormId(String formId) {
        this.formId = formId; // Método setter para o ID do formulário
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getQuestions() {
        return questions;
    }

    public void setQuestions(List<String> questions) {
        this.questions = questions;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
