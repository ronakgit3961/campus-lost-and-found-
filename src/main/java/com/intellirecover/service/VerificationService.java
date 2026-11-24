package com.intellirecover.service;

import com.intellirecover.model.FoundItem;
import com.intellirecover.model.VerificationQuestion;

import java.util.List;

/**
 * FEATURE 2: Secret Ownership Verification.
 * Evaluates claimant knowledge of private identifying traits without ever disclosing answers.
 * Separates Match Confidence from Ownership Verification.
 */
public class VerificationService {
    private double passingThreshold = 75.0; // Configurable verification threshold percentage

    public VerificationService() {}

    public VerificationService(double passingThreshold) {
        this.passingThreshold = passingThreshold;
    }

    public double getPassingThreshold() {
        return passingThreshold;
    }

    public void setPassingThreshold(double passingThreshold) {
        this.passingThreshold = passingThreshold;
    }

    /**
     * Evaluates submitted answers against stored secret questions for a found item.
     * Returns a verification score percentage from 0.0 to 100.0.
     */
    public double evaluateAnswers(FoundItem foundItem, List<String> submittedAnswers) {
        if (foundItem == null || !foundItem.hasSecretQuestions()) {
            // If the finder provided no secret questions, baseline verification passes to admin review
            return 100.0;
        }

        List<VerificationQuestion> questions = foundItem.getSecretQuestions();
        if (submittedAnswers == null || submittedAnswers.isEmpty()) {
            return 0.0;
        }

        int correct = 0;
        int total = questions.size();

        for (int i = 0; i < total; i++) {
            VerificationQuestion q = questions.get(i);
            String candidateAnswer = (i < submittedAnswers.size()) ? submittedAnswers.get(i) : "";
            if (q.verifyAnswer(candidateAnswer)) {
                correct++;
            }
        }

        return Math.round(((double) correct / total) * 100.0 * 10.0) / 10.0;
    }

    /**
     * Checks if the verification score meets or exceeds the required passing threshold.
     */
    public boolean isPassing(double verificationScore) {
        return verificationScore >= passingThreshold;
    }
}
