package ru.hh.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class WrapNewLinesMethodChainCallsCheck extends AbstractCheck {

  public static final String WRAPPED_CHAIN_MSG_KEY = "ru.hh.checkstyle.wrapped.chain";

  @Override
  public int[] getDefaultTokens() {
    return new int[] { TokenTypes.METHOD_CALL };
  }

  @Override
  public int[] getAcceptableTokens() {
    return getDefaultTokens();
  }

  @Override
  public int[] getRequiredTokens() {
    return getDefaultTokens();
  }

  @Override
  public void visitToken(DetailAST methodCall) {
    if (isMethodPartOfChain(methodCall)) {
      return;
    }

    MethodCallInfo methodCallInfo = analyzeTree(methodCall);

    if (methodCallInfo.violate()) {
      log(methodCallInfo.getCaller(), WRAPPED_CHAIN_MSG_KEY);
    }
  }

  private boolean isMethodPartOfChain(DetailAST methodCall) {
    return Optional.of(methodCall.getParent()).map(DetailAST::getType).filter(type -> type == TokenTypes.DOT).isPresent();
  }

  private MethodCallInfo analyzeTree(DetailAST methodCall) {
    DetailAST dot = methodCall.findFirstToken(TokenTypes.DOT);
    MethodCallInfo methodCallInfo = new MethodCallInfo(methodCall, dot);
    while (dot != null && dot.hasChildren()) {
      DetailAST chainedMethodCall = dot.findFirstToken(TokenTypes.METHOD_CALL);
      if (chainedMethodCall == null) {
        Optional.ofNullable(dot.getFirstChild()).ifPresent(methodCallInfo::setCaller);
        dot = null;
      } else {
        // method(
        // ).next()
        int methodClosingParenthesis = Optional.ofNullable(chainedMethodCall.findFirstToken(TokenTypes.RPAREN)).map(DetailAST::getLineNo).orElse(-1);
        int previousDot = dot.getLineNo();
        methodCallInfo.mustBeOneLineExpression(methodClosingParenthesis == previousDot);

        dot = chainedMethodCall.findFirstToken(TokenTypes.DOT);
        // case someMethod(another.method().chain())
        if (dot == null) {
          Optional.ofNullable(chainedMethodCall.getFirstChild()).ifPresent(methodCallInfo::setCaller);
        }
        methodCallInfo.addLine(chainedMethodCall.getLineNo(), dot);
      }
    }
    return methodCallInfo;
  }

  private static class MethodCallInfo {
    private int chainDepth;
    private DetailAST caller;
    private boolean mustBeOneLineExpression;
    private final Set<Integer> lineNumbers = new HashSet<>();

    MethodCallInfo(DetailAST methodCall, DetailAST dot) {
      addLine(methodCall.getLineNo(), dot);
      if (dot == null) {
        setCaller(methodCall.findFirstToken(TokenTypes.IDENT));
      }
    }

    /**
     * @param dot can be null
     */
    public void addLine(Integer line, DetailAST dot) {
      lineNumbers.add(line);
      if (dot != null) {
        chainDepth++;
      }
    }

    public DetailAST getCaller() {
      return caller;
    }

    public void setCaller(DetailAST caller) {
      addLine(caller.getLineNo(), null);
      this.caller = caller;
    }

    public void mustBeOneLineExpression(boolean mustBeOneLineExpression) {
      this.mustBeOneLineExpression |= mustBeOneLineExpression;
    }

    public boolean violate() {
      return chainDepth > 1 && lineNumbers.size() > 1 && (mustBeOneLineExpression || lineNumbers.size() != chainDepth + 1);
    }
  }
}
