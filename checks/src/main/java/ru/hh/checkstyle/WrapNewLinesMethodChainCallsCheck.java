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
      checkMethodChainingMultiLine(methodCall);
    }
  }

  private void checkMethodChainingMultiLine(DetailAST methodCallAst) {
    DetailAST childAst = methodCallAst.getFirstChild();
    DetailAST currentLineOuterMostMethodAst = methodCallAst;
    int methodCallCount = 1;
    while (TokenUtil.isOfType(childAst, METHOD_CHAIN_CONTINUATION_NODES) || TokenUtil.isOfType(childAst, TokenTypes.IDENT)) {
      if (isSimpleMethodCall(childAst) || isVariableReference(childAst) || isCallAfterMultilineCall(childAst)) {
        if (TokenUtil.areOnSameLine(currentLineOuterMostMethodAst, childAst)) {
          methodCallCount++;
          if (methodCallCount > 1) {
            log(currentLineOuterMostMethodAst.getFirstChild(), WRAPPED_CHAIN_MSG_KEY);
            currentLineOuterMostMethodAst = childAst;
          }
        }
        else {
          currentLineOuterMostMethodAst = childAst;
          methodCallCount = 1;
        }
      }
      childAst = getMethodCallDescendantAst(childAst);
    }
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

  private static DetailAST getMethodCallDescendantAst(DetailAST ast) {
    DetailAST childAst = ast.getFirstChild();
    while (childAst != null && childAst.getType() == TokenTypes.LPAREN) {
      childAst = childAst.getNextSibling();
      if (!TokenUtil.isOfType(childAst, METHOD_CHAIN_CONTINUATION_NODES)) {
        childAst = getMethodCallDescendantAfterLParen(childAst);
      }
    }
    return childAst;
  }

  private static DetailAST getMethodCallDescendantAfterLParen(DetailAST ast) {
    if (TokenUtil.isOfType(ast, TokenTypes.QUESTION)) {
      return ast;
    }
    DetailAST result = ast;
    DetailAST childAst = result.getFirstChild();
    while (childAst != null) {
      result = childAst;
      if (TokenUtil.isOfType(childAst, METHOD_CHAIN_CONTINUATION_NODES)) {
        break;
      }
      childAst = childAst.getNextSibling();
    }
    return result;
  }

  private static boolean isVariableReference(DetailAST childAst) {
    return TokenUtil.isOfType(childAst, TokenTypes.IDENT) && TokenUtil.isOfType(childAst.getParent(), TokenTypes.DOT);
  }

  private static boolean isCallAfterMultilineCall(DetailAST childAst) {
    if (!TokenUtil.isOfType(childAst, TokenTypes.DOT)) {
      return false;
    }
    DetailAST methodCallDescendantAst = getMethodCallDescendantAst(childAst);
    return TokenUtil.isOfType(methodCallDescendantAst, TokenTypes.METHOD_CALL)
        && !TokenUtil.areOnSameLine(methodCallDescendantAst, childAst)
        && TokenUtil.areOnSameLine(methodCallDescendantAst.findFirstToken(TokenTypes.RPAREN), childAst);
  }

  private static boolean isSimpleMethodCall(DetailAST childAst) {
    return TokenUtil.isOfType(childAst, TokenTypes.METHOD_CALL) && !TokenUtil.isOfType(childAst.getParent(), TokenTypes.TYPECAST);
  }

  private static class MethodCallInfo {
    private final boolean allowUnwrappedFirstCall;
    private int chainDepth;
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
