package ru.hh.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class WrapNewLinesMethodChainCallsCheck extends AbstractCheck {

  public static final String WRAPPED_CHAIN_MSG_KEY = "ru.hh.checkstyle.wrapped.chain";
  private static final int[] METHOD_CHAIN_CONTINUATION_TOKENS = {
      TokenTypes.METHOD_CALL,
      TokenTypes.DOT,
      TokenTypes.INDEX_OP,
      TokenTypes.IDENT,
  };

  private boolean allowUnwrappedFirstCall = false;

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

  public void setAllowUnwrappedFirstCall(boolean allowUnwrappedFirstCall) {
    this.allowUnwrappedFirstCall = allowUnwrappedFirstCall;
  }

  public boolean getAllowUnwrappedFirstCall() {
    return allowUnwrappedFirstCall;
  }

  @Override
  public void visitToken(DetailAST methodCall) {
    if (isMethodPartOfChain(methodCall)) {
      return;
    }

    MethodCallInfo methodCallInfo = analyzeTree(methodCall);

    if (methodCallInfo.violate()) {
      logViolationDetails(methodCall);
    }
  }

  private void logViolationDetails(DetailAST methodCallAst) {
    DetailAST chainPreviousCallPoint = getChainPreviousCallPointOrNull(methodCallAst);
    DetailAST chainNextCallPoint = methodCallAst;
    while (TokenUtil.isOfType(chainPreviousCallPoint, METHOD_CHAIN_CONTINUATION_TOKENS)) {
      if (TokenUtil.isOfType(chainPreviousCallPoint, TokenTypes.METHOD_CALL)
          || (!allowUnwrappedFirstCall && isCallChainBeginning(chainPreviousCallPoint))
      ) {
        if (areCallerAndCallOnTheSameLine(chainPreviousCallPoint, chainNextCallPoint)) {
          logViolation(chainNextCallPoint.getFirstChild() == null ? chainNextCallPoint : chainNextCallPoint.getFirstChild());
        }
        chainNextCallPoint = chainPreviousCallPoint;
      }
      chainPreviousCallPoint = getChainPreviousCallPointOrNull(chainPreviousCallPoint);
    }
  }

  private static boolean areCallerAndCallOnTheSameLine(DetailAST chainPreviousCallPoint, DetailAST chainNextCallPoint) {
    return TokenUtil.areOnSameLine(
        findCallerEndToken(chainPreviousCallPoint),
        chainNextCallPoint
    );
  }

  private void logViolation(DetailAST currentLineOuterMostMethodAst) {
    log(currentLineOuterMostMethodAst, WRAPPED_CHAIN_MSG_KEY);
  }

  private boolean isMethodPartOfChain(DetailAST methodCall) {
    return Optional.of(methodCall.getParent()).map(DetailAST::getType).filter(type -> type == TokenTypes.DOT).isPresent();
  }

  private MethodCallInfo analyzeTree(DetailAST methodCall) {
    DetailAST dot = methodCall.findFirstToken(TokenTypes.DOT);
    MethodCallInfo methodCallInfo = new MethodCallInfo(methodCall, dot, allowUnwrappedFirstCall);
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
        dot = chainedMethodCall.findFirstToken(TokenTypes.DOT);
        methodCallInfo.mustBeOneLineExpression(methodClosingParenthesis == previousDot && (!allowUnwrappedFirstCall || dot != null));

        // case someMethod(another.method().chain())
        if (dot == null) {
          Optional.ofNullable(chainedMethodCall.getFirstChild()).ifPresent(methodCallInfo::setCaller);
        }
        methodCallInfo.addLine(chainedMethodCall.getLineNo(), dot);
      }
    }
    return methodCallInfo;
  }

  private static DetailAST getChainPreviousCallPointOrNull(DetailAST ast) {
    DetailAST child = ast.getFirstChild();
    while (child != null && !TokenUtil.isOfType(child, METHOD_CHAIN_CONTINUATION_TOKENS)) {
      child = child.getFirstChild();
    }
    return child;
  }

  private static boolean isCallChainBeginning(DetailAST caller) {
    return  (isCallingIdentifier(caller) && getChainPreviousCallPointOrNull(caller) == null || isObjectCreation(caller));
  }

  private static boolean isCallingIdentifier(DetailAST caller) {
    return TokenUtil.isOfType(caller, TokenTypes.IDENT)
        && TokenUtil.isOfType(caller.getParent(), TokenTypes.DOT);
  }

  private static boolean isObjectCreation(DetailAST child) {
    return TokenUtil.isOfType(child.getParent(), TokenTypes.LITERAL_NEW);
  }

  private static DetailAST findCallerEndToken(DetailAST callPoint) {
    switch (callPoint.getType()) {
      case TokenTypes.METHOD_CALL:
        return callPoint.findFirstToken(TokenTypes.RPAREN);
      default:
        return callPoint;
    }
  }

  private static class MethodCallInfo {
    private final boolean allowUnwrappedFirstCall;
    private int chainDepth;
    private DetailAST caller;
    private boolean mustBeOneLineExpression;
    private final Set<Integer> lineNumbers = new HashSet<>();
    // first from code point of view and last from AST point of view
    private int firstMethodLine;

    MethodCallInfo(DetailAST methodCall, DetailAST dot, boolean allowUnwrappedFirstCall) {
      this.allowUnwrappedFirstCall = allowUnwrappedFirstCall;
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
        firstMethodLine = line;
        chainDepth++;
      }
    }

    public void setCaller(DetailAST caller) {
      addLine(caller.getLineNo(), null);
      this.caller = caller;
    }

    public void mustBeOneLineExpression(boolean mustBeOneLineExpression) {
      this.mustBeOneLineExpression |= mustBeOneLineExpression;
    }

    public boolean violate() {
      if (chainDepth <= 1 || lineNumbers.size() <= 1) {
        return false;
      }
      if (mustBeOneLineExpression) {
        return true;
      }

      if (allowUnwrappedFirstCall && caller.getLineNo() == firstMethodLine) {
        return lineNumbers.size() != chainDepth;
      } else {
        return lineNumbers.size() != chainDepth + 1;
      }
    }
  }
}
