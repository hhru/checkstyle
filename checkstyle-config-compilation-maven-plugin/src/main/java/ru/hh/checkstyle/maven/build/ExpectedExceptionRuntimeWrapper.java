package ru.hh.checkstyle.maven.build;

public class ExpectedExceptionRuntimeWrapper extends RuntimeException {

  public ExpectedExceptionRuntimeWrapper(Throwable cause) {
    super(cause);
  }
}
