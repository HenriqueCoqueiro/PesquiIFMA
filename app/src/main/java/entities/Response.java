package entities;

import java.util.List;

public class Response {
    private String formId;
    private List<String> answers;

    public Response() {
    }

    public Response(String formId, List<String> answers) {
        this.formId = formId;
        this.answers = answers;
    }

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public void setAnswers(List<String> answers) {
        this.answers = answers;
    }
}
