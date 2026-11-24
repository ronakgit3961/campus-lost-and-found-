package com.intellirecover.model;

/**
 * Model representing a confidential verification question and its secret answer.
 * Answers are strictly protected and never displayed in public listings or toString().
 */
public class VerificationQuestion {
    private String question;
    private String secretAnswer;

    public VerificationQuestion(String question, String secretAnswer) {
        this.question = question != null ? question.trim() : "";
        this.secretAnswer = secretAnswer != null ? secretAnswer.trim() : "";
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    /**
     * Internal accessor used only by secure verification services.
     */
    public String getSecretAnswer() {
        return secretAnswer;
    }

    public void setSecretAnswer(String secretAnswer) {
        this.secretAnswer = secretAnswer;
    }

    /**
     * Verifies a candidate answer with case-insensitive normalization
     * and minor whitespace/punctuation tolerance.
     */
    public boolean verifyAnswer(String candidate) {
        if (candidate == null || secretAnswer == null) {
            return false;
        }
        String normalizedExpected = normalize(secretAnswer);
        String normalizedCandidate = normalize(candidate);
        return normalizedExpected.equals(normalizedCandidate);
    }

    private String normalize(String input) {
        return input.trim().toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    @Override
    public String toString() {
        // Redacts secret answer to protect private data
        return "VerificationQuestion{question='" + question + "', answer=[PROTECTED]}";
    }
}
