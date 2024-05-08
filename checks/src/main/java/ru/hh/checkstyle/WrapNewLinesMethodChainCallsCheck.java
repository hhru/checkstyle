package ru.hh.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

public class WrapNewLinesMethodChainCallsCheck extends AbstractCheck {

  public static final String WRAPPED_CHAIN_MSG_KEY = "ru.hh.checkstyle.wrapped.chain";
  private static final int[] METHOD_CHAIN_CONTINUATION_NODES = {
      TokenTypes.METHOD_CALL,
      TokenTypes.DOT,
      TokenTypes.INDEX_OP
  };

  private static final int[] PARENTS_OF_METHOD_CALL = {
      TokenTypes.EXPR,
      TokenTypes.METHOD_CALL,
      TokenTypes.LITERAL_CASE,
  };

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
    if (isTopMostMethodCall(methodCall) && !isSingleLineMethodChain(methodCall)) {
        checkMethodChainingMultiLine(methodCall);
    }
  }

  private void checkMethodChainingMultiLine(DetailAST methodCallAst) {
    DetailAST childAst = methodCallAst.getFirstChild();
    DetailAST currentLineOuterMostMethodAst = methodCallAst;
    int methodCallCount = 1;
    while (TokenUtil.isOfType(childAst, METHOD_CHAIN_CONTINUATION_NODES) || TokenUtil.isOfType(childAst, TokenTypes.IDENT)) {
      if (TokenUtil.isOfType(childAst, TokenTypes.METHOD_CALL) || isVariableReference(childAst)) {
        if (currentLineOuterMostMethodAst.getLineNo() == childAst.getLineNo()) {
          methodCallCount++;
          if (methodCallCount > 1) {
            logViolation(currentLineOuterMostMethodAst);
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

  private void logViolation(DetailAST currentLineOuterMostMethodAst) {
    DetailAST chainStep = currentLineOuterMostMethodAst.getFirstChild();
    log(chainStep, WRAPPED_CHAIN_MSG_KEY);
  }

  private static boolean isSingleLineMethodChain(DetailAST methodCallAst) {
    DetailAST childAst = methodCallAst;
    while (TokenUtil.isOfType(childAst, METHOD_CHAIN_CONTINUATION_NODES)) {
      childAst = getMethodCallDescendantAst(childAst);
    }
    return TokenUtil.areOnSameLine(childAst, methodCallAst);
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

  private static boolean isTopMostMethodCall(DetailAST methodCallAst) {
    boolean result = false;
    DetailAST ancestorAst = methodCallAst.getParent();
    while (!TokenUtil.isOfType(ancestorAst, PARENTS_OF_METHOD_CALL)) {
      final DetailAST ancestorAstPrevSibling = ancestorAst.getPreviousSibling();
      if (ancestorAstPrevSibling != null
          && !TokenUtil.isOfType(ancestorAst, TokenTypes.TYPECAST, TokenTypes.INDEX_OP)) {
        result = true;
        break;
      }
      ancestorAst = ancestorAst.getParent();
    }
    return result || TokenUtil.isOfType(ancestorAst, TokenTypes.EXPR, TokenTypes.LITERAL_CASE);
  }

  private static boolean isVariableReference(DetailAST childAst) {
    return TokenUtil.isOfType(childAst, TokenTypes.IDENT) && TokenUtil.isOfType(childAst.getParent(), TokenTypes.DOT);
  }
}
