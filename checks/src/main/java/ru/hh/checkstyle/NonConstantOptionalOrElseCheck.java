package ru.hh.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;
import java.util.regex.Pattern;

public class NonConstantOptionalOrElseCheck extends AbstractCheck {

  public static final String MSG_KEY = "ru.hh.checkstyle.optional.non.constant.or.else";

  private Pattern methodNamePattern = CommonUtil.createPattern("orElse");

  public void setMethodNamePattern(String methodNamePattern) {
    this.methodNamePattern = CommonUtil.createPattern(methodNamePattern);
  }

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
  public void visitToken(DetailAST ast) {
    switch (ast.getType()) {
      case TokenTypes.METHOD_CALL:
        final DetailAST dot = ast.getFirstChild();
        //only called on object
        if (dot.getType() != TokenTypes.IDENT) {
          final String orElseCandidate = dot.getLastChild().getText();
          //matches orElse
          if (methodNamePattern.matcher(orElseCandidate).matches()) {
            DetailAST argLineList = dot.getNextSibling();
            int argsCount = argLineList.getChildCount(TokenTypes.EXPR);
            if (argsCount == 1) {
              DetailAST argExpr = argLineList.getFirstChild();
              if (argExpr.getChildCount(TokenTypes.METHOD_CALL) > 0) {
                String methodLiteral = orElseCandidate + ast.getText() + ast.getLastChild().getText();
                log(ast, MSG_KEY, methodLiteral, getMethodLiteral(argExpr.findFirstToken(TokenTypes.METHOD_CALL)));
              }
            }
          }
        }
        break;
      default:
    }
  }

  private static String getMethodLiteral(DetailAST methodCallRoot) {
    final DetailAST dot = methodCallRoot.getFirstChild();
    // method that looks like: method()
    final String methodNameInCode;
    if (dot.getType() == TokenTypes.IDENT) {
      methodNameInCode = dot.getText();
    }
    // method that looks like: obj.method()
    else {
      methodNameInCode = dot.getFirstChild().getText() + dot.getText() + dot.getLastChild().getText();
    }
    return methodNameInCode + methodCallRoot.getText() + methodCallRoot.getLastChild().getText();
  }

}
