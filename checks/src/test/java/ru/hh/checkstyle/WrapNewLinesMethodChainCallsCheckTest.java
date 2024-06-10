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
        "18:16: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "18:50: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "18:86: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "18:107: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "19:16: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "33:18: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "33:52: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "33:88: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "33:109: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "34:18: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "50:18: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "50:52: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "50:88: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "50:109: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "51:18: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "63:9: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "64:18: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "74:29: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "76:18: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "76:27: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "90:50: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "90:59: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "91:69: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "120:20: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "120:34: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "120:54: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "120:66: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "120:75: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "122:7: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "122:17: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "122:40: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "148:18: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "176:51: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "176:60: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "177:38: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "177:60: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "178:39: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "179:39: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "187:33: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "187:42: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "187:74: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "187:95: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "188:33: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "188:53: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "188:74: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "188:109: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "188:118: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "189:29: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "189:50: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "189:70: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "189:91: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "189:111: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "190:29: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "190:50: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
    };
    verify(checkConfig, getPath("WrapNewLinesMethodChainCallCheckTestInput.java"), expected);
  }

  @Test
  public void testCheckWithAllowUnwrappedFirstCall() throws Exception {
    final DefaultConfiguration checkConfig = createModuleConfig(WrapNewLinesMethodChainCallsCheck.class);
    checkConfig.addProperty("allowUnwrappedFirstCall", "true");
    String[] expected = {
        "18:16: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "18:50: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "18:86: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "18:107: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "19:16: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "33:18: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "33:52: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "33:88: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "33:109: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "34:18: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "50:18: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "50:52: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "50:88: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "50:109: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "51:18: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "64:18: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "76:18: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "76:27: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "90:59: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "91:69: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "120:34: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "120:66: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "120:75: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "122:7: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "122:17: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "122:40: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "176:51: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "176:60: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "177:38: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "177:60: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "178:39: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "179:39: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "187:42: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "187:74: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "187:95: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "188:33: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "188:53: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "188:74: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "188:118: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "189:29: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "189:50: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "189:70: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "189:91: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "189:111: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "190:29: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
        "190:50: " + getCheckMessage(WRAPPED_CHAIN_MSG_KEY),
    };
    verify(checkConfig, getPath("WrapNewLinesMethodChainCallCheckTestInput.java"), expected);
  }
}
