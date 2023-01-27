package com.capgemini.mrchecker.test.core;

public interface ITestObserver {
    //TestWatcher - after complete test execution (befores + test + afters)
    void onTestDisabled();

    void onTestSuccess();

    void onTestAborted();

    void onTestFailed();

    void onTestFinish();

    //BeforeTestExecutionCallback
    void onSetupFailure();

    ////AfterTestExecutionCallback
    void onTeardownFailure();

    //AfterAllCallback
    void onTestClassFinish();

    //LifecycleMethodExecutionExceptionHandler
    void onBeforeAllException();

    void onBeforeEachException();

    void onAfterAllException();

    void onAfterEachException();

    //TestExecutionExceptionHandler
    void onTestExecutionException();

    void addToTestExecutionObserver();

    ModuleType getModuleType();
}