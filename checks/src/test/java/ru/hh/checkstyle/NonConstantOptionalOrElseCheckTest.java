package ru.hh.checkstyle;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import org.junit.jupiter.api.Test;
import static ru.hh.checkstyle.NonConstantOptionalOrElseCheck.METHOD_MSG_KEY;
import static ru.hh.checkstyle.NonConstantOptionalOrElseCheck.NEW_MSG_KEY;

public class NonConstantOptionalOrElseCheckTest extends AbstractModuleTestSupport {

  @Override
  protected String getPackageLocation() {
    return "ru/hh/checkstyle";
  }

  @Test
  public void testCheck() throws Exception {
    final DefaultConfiguration checkConfig =
      createModuleConfig(NonConstantOptionalOrElseCheck.class);
    String[] expected = {
        "9:77: " + getCheckMessage(METHOD_MSG_KEY, "orElse()", "staticService.getValue()"),
        "10:77: " + getCheckMessage(METHOD_MSG_KEY, "orElse()", "createStatic()"),
        "17:55: " + getCheckMessage(METHOD_MSG_KEY, "orElse()", "staticService.getValue()"),
        "18:55: " + getCheckMessage(METHOD_MSG_KEY, "orElse()", "createStatic()"),
        "28:56: " + getCheckMessage(METHOD_MSG_KEY, "orElse()", "staticService.getValue()"),
        "29:56: " + getCheckMessage(METHOD_MSG_KEY, "orElse()", "createStatic()"),
        "34:68: " + getCheckMessage(NEW_MSG_KEY, "orElse()", "new Service..."),
        "35:67: " + getCheckMessage(NEW_MSG_KEY, "orElse()", "new Service..."),
        "37:41: " + getCheckMessage(NEW_MSG_KEY, "orElse()", "new Service..."),
        "38:81: " + getCheckMessage(NEW_MSG_KEY, "orElse()", "new HashMap..."),
    };
    verify(checkConfig, getPath("NonConstantOptionalOrElseCheckTestInput.java"), expected);
  }
}
