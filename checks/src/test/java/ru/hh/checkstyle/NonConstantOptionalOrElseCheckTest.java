package ru.hh.checkstyle;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import org.junit.Test;
import static ru.hh.checkstyle.NonConstantOptionalOrElseCheck.MSG_KEY;

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
      "8:77: " + getCheckMessage(MSG_KEY, "orElse()", "staticService.getValue()"),
      "9:77: " + getCheckMessage(MSG_KEY, "orElse()", "createStatic()"),
      "16:55: " + getCheckMessage(MSG_KEY, "orElse()", "staticService.getValue()"),
      "17:55: " + getCheckMessage(MSG_KEY, "orElse()", "createStatic()"),
      "27:56: " + getCheckMessage(MSG_KEY, "orElse()", "staticService.getValue()"),
      "28:56: " + getCheckMessage(MSG_KEY, "orElse()", "createStatic()"),
    };
    verify(checkConfig, getPath("NonConstantOptionalOrElseCheckTestInput.java"), expected);
  }
}
