package entities;

import java.util.List;

public class Form {
    private String title;
    private String description;
    private List<String> questions;
    private String uid; // Novo campo para armazenar o UID

    public Form() {
    }

    public Form(String title, String description, List<String> questions, String uid) {
        this.title = title;
        this.description = description;
        this.questions = questions;
        this.uid = uid;
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
        return uid; // Método getter para o UID
    }

    public void setUid(String uid) {
        this.uid = uid; // Método setter para o UID
    }
}
