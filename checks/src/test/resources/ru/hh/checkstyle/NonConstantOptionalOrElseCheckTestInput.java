package ru.hh.checkstyle;

import java.util.Optional;

public class NonConstantOptionalOrElseCheckTestInput {

  private static final Service staticService = new Service(2);
  private static final Integer staticFieldObjectCall = Optional.of(1).orElse(staticService.getValue());
  private static final Integer staticFieldMethodCall = Optional.of(1).orElse(createStatic());
  private static final Integer staticFieldConstant = Optional.of(1).orElse(2);

  private static Integer staticBlockFieldObjectCall;
  private static Integer staticBlockFieldMethodCall;
  private static Integer staticBlockFieldConstant;
  static {
    staticBlockFieldObjectCall = Optional.of(1).orElse(staticService.getValue());
    staticBlockFieldMethodCall = Optional.of(1).orElse(createStatic());
    staticBlockFieldConstant = Optional.of(1).orElse(2);
  }


  private static Integer createStatic() {
    return staticService.getValue();
  }

  public static void method() {
    int valueObjectCall = Optional.ofNullable(1).orElse(staticService.getValue());
    int valueMethodCall = Optional.ofNullable(1).orElse(createStatic());
    int valueMethodConstantInline = Optional.ofNullable(1).orElse(2);
    int val = 2;
    int valueMethodConstant = Optional.ofNullable(1).orElse(val);
  }

  private static class Service {
    private final Integer value;

    private Service(Integer value) {
      this.value = value;
    }

    public Integer getValue() {
      return value;
    }
  }
}
