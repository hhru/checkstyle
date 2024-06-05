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
  private static final int[] METHOD_CHAIN_CONTINUATION_NODES = {
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
      if (!tryLogViolationDetails(methodCall)) {
        logViolation(methodCallInfo.getCaller());
      }
    }
  }

  private boolean tryLogViolationDetails(DetailAST methodCallAst) {
    boolean atLeastOneViolationIsLogged = false;
    DetailAST child = getMethodCallDescendant(methodCallAst);
    DetailAST currentLineOuterMostMethodCaller = methodCallAst;
    int methodCallCount = 1;
    while (TokenUtil.isOfType(child, METHOD_CHAIN_CONTINUATION_NODES)) {
      if (TokenUtil.isOfType(child, TokenTypes.METHOD_CALL)
          || isProhibitedReference(child, allowUnwrappedFirstCall)
          || isCallAfterMultilineCall(child)
      ) {
        if (TokenUtil.areOnSameLine(currentLineOuterMostMethodCaller, child)) {
          methodCallCount++;
          if (methodCallCount > 1) {
            logViolation(
                currentLineOuterMostMethodCaller.getFirstChild() == null
                    ? currentLineOuterMostMethodCaller : currentLineOuterMostMethodCaller.getFirstChild()
            );
            atLeastOneViolationIsLogged = true;
            currentLineOuterMostMethodCaller = child;
          }
        } else {
          currentLineOuterMostMethodCaller = child;
          methodCallCount = 1;
        }
      }
      child = getMethodCallDescendant(child);
    }
    return atLeastOneViolationIsLogged;
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

  private static DetailAST getMethodCallDescendant(DetailAST ast) {
    DetailAST child = ast.getFirstChild();
    while (child != null && !TokenUtil.isOfType(child, METHOD_CHAIN_CONTINUATION_NODES)) {
      child = child.getFirstChild();
    }
    return child;
  }

  private static boolean isProhibitedReference(DetailAST child, boolean allowUnwrappedFirstCall) {
    return isOfIdentifier(child) && !allowUnwrappedFirstCall && getMethodCallDescendant(child) == null || isObjectCreation(child);
  }

  private static boolean isOfIdentifier(DetailAST child) {
    return TokenUtil.isOfType(child, TokenTypes.IDENT)
        && TokenUtil.isOfType(child.getParent(), TokenTypes.DOT);
  }

  private static boolean isObjectCreation(DetailAST child) {
    return TokenUtil.isOfType(child.getParent(), TokenTypes.LITERAL_NEW);
  }

  private static boolean isCallAfterMultilineCall(DetailAST child) {
    if (!TokenUtil.isOfType(child, TokenTypes.DOT) && !TokenUtil.isOfType(child.getParent(), TokenTypes.METHOD_CALL)) {
      return false;
    }
    DetailAST nextCallInChain = child.getParent();
    DetailAST previousCallInChain = getMethodCallDescendant(child);
    if (previousCallInChain == null) {
      return false;
    }
    DetailAST rParenOfPreviousCallInChain = previousCallInChain.findFirstToken(TokenTypes.RPAREN);
    return TokenUtil.isOfType(previousCallInChain, TokenTypes.METHOD_CALL)
        && !TokenUtil.areOnSameLine(previousCallInChain, rParenOfPreviousCallInChain)
        && TokenUtil.areOnSameLine(rParenOfPreviousCallInChain, nextCallInChain);
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
