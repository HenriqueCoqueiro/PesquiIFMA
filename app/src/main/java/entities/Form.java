package entities;

import java.util.List;

public class Form {
    private String title;
    private String description;
    private List<String> questions;

    public Form() {
    }

    public Form(String title, String description, List<String> questions) {
        this.title = title;
        this.description = description;
        this.questions = questions;
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
}
