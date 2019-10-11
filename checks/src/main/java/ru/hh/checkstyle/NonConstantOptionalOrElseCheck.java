package ru.hh.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;
import java.util.regex.Pattern;

public class NonConstantOptionalOrElseCheck extends AbstractCheck {

  public static final String METHOD_MSG_KEY = "ru.hh.checkstyle.optional.non.constant.or.else";
  public static final String NEW_MSG_KEY = "ru.hh.checkstyle.optional.constructor.or.else";

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
                log(ast, METHOD_MSG_KEY, methodLiteral, getMethodLiteral(argExpr.findFirstToken(TokenTypes.METHOD_CALL)));
              } else if (argExpr.getChildCount(TokenTypes.LITERAL_NEW) > 0) {
                String newInstanceLiteral = orElseCandidate + ast.getText() + ast.getLastChild().getText();
                log(ast, NEW_MSG_KEY, newInstanceLiteral, getNewLiteral(argExpr.findFirstToken(TokenTypes.LITERAL_NEW)));
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

  private static String getNewLiteral(DetailAST newCallRoot) {
    final DetailAST className = newCallRoot.getFirstChild();
    // Class that looks like: Class()
    final String classNameInCode;
    if (className.getType() == TokenTypes.IDENT) {
      classNameInCode = className.getText();
    }
    // Class that looks like: ru.hh.Class()
    else {
      classNameInCode =  className.getLastChild().getText();
    }

    return newCallRoot.getText() + " " + classNameInCode + "...";
  }
}
