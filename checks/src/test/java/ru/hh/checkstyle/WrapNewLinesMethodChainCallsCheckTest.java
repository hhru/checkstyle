package ru.hh.checkstyle;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import org.junit.jupiter.api.Test;
import static ru.hh.checkstyle.WrapNewLinesMethodChainCallsCheck.WRAPPED_CHAIN_MSG_KEY;

public class WrapNewLinesMethodChainCallsCheckTest extends AbstractModuleTestSupport {

  @Override
  protected String getPackageLocation() {
    return "ru/hh/checkstyle";
  }

  @Test
  public void testCheck() throws Exception {
    final DefaultConfiguration checkConfig = createModuleConfig(WrapNewLinesMethodChainCallsCheck.class);
    String[] expected = {
        "17:79: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "32:54: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "49:5: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "63:5: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "74:5: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "90:14: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "120:12: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "120:50: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "148:12: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "175:13: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "186:29: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "187:105: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
    };
    verify(checkConfig, getPath("WrapNewLinesMethodChainCallCheckTestInput.java"), expected);
  }
}
