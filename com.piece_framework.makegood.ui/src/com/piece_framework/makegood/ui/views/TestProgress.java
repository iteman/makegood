package com.piece_framework.makegood.ui.views;

import com.piece_framework.makegood.launch.elements.ProblemType;

public class TestProgress {
    private int allTestCount;
    private int endTestCount;
    private boolean initialized;
    private int passCount;
    private int failureCount;
    private int errorCount;
    private long startTime;
    private double totalTime;
    private long startTimeForTestCase;
    private double testCaseTime;

    public void initialize(int allTestCount) {
        this.allTestCount = allTestCount;
        endTestCount = 0;
        passCount = 0;
        failureCount = 0;
        errorCount = 0;

        startTime = System.currentTimeMillis();
        totalTime = 0.0d;

        startTimeForTestCase = System.currentTimeMillis();

        initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void finalize() {
        updateTime();
    }

    public int getAllTestCount() {
        return allTestCount;
    }

    public int getEndTestCount() {
        return endTestCount;
    }

    public int getPassCount() {
        return passCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public double getTotalTime() {
        return totalTime;
    }

    public int getRate() {
        int rate = (int) (((double) endTestCount / (double) allTestCount) * 100d);
        return rate <= 100 ? rate : 100;
    }

    public double getAverage() {
        return totalTime / (double) endTestCount;
    }

    public void incrementEndTestCount() {
        ++endTestCount;
        updateTime();
    }

    public void incrementResultCount(ProblemType problemType) {
        if (problemType == ProblemType.Pass) {
            ++passCount;
        } else if (problemType == ProblemType.Failure) {
            ++failureCount;
        } else if (problemType == ProblemType.Error) {
            ++errorCount;
        }
        updateTime();
    }

    public void startTestCase() {
        startTimeForTestCase = System.currentTimeMillis();
    }

    public void endTestCase() {
        testCaseTime = (System.currentTimeMillis() - startTimeForTestCase) / 1000d;
    }

    public double getTestCaseTime() {
        return testCaseTime;
    }

    private void updateTime() {
        totalTime = (System.currentTimeMillis() - startTime) / 1000d;
    }
}
