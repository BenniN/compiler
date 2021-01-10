package org.smallpearl.compiler.SemanticAnalysis;

import org.antlr.v4.runtime.RuleContext;
import org.smallpearl.compiler.*;
import org.smallpearl.compiler.SymbolTable.ModuleEntry;
import org.smallpearl.compiler.SymbolTable.SymbolTable;

import java.util.*;

import static org.smallpearl.compiler.SmallPearlParser.*;

public class CheckReturnStatement extends SmallPearlBaseVisitor<Void> implements SmallPearlVisitor<Void> {


    private int m_verbose;
    private boolean m_debug;
    private String m_sourceFileName;
    private ExpressionTypeVisitor m_expressionTypeVisitor;
    private SymbolTableVisitor m_symbolTableVisitor;
    private SymbolTable m_symboltable;
    private SymbolTable m_currentSymbolTable;
    private ModuleEntry m_module;
    private ArrayList<FixedRange> m_listOfAlternatives = null;
    private AST m_ast = null;

    private ResultTypeContext currentResultType;

    private Stack<RuleContext> returns = new Stack<>();

    public CheckReturnStatement(String sourceFileName,
                           int verbose,
                           boolean debug,
                           SymbolTableVisitor symbolTableVisitor,
                           ExpressionTypeVisitor expressionTypeVisitor,
                           AST ast) {

        m_debug = debug;
        m_verbose = verbose;
        m_sourceFileName = sourceFileName;
        m_symbolTableVisitor = symbolTableVisitor;
        m_expressionTypeVisitor = expressionTypeVisitor;
        m_symboltable = symbolTableVisitor.symbolTable;
        m_currentSymbolTable = m_symboltable;
        m_ast = ast;

        m_listOfAlternatives = new ArrayList<FixedRange>();

        Log.debug( "    Check Return Statement");
    }

    @Override
    public Void visitModule(SmallPearlParser.ModuleContext ctx) {
        if (m_debug) {
            System.out.println( "Semantic: Check Return: visitModule");
        }

        ErrorStack.enter(ctx,"check return");

        visitChildren(ctx);

        ErrorStack.leave();
        return null;
    }

    @Override
    public Void visitProcedureDeclaration(SmallPearlParser.ProcedureDeclarationContext ctx) {
        if (m_debug) {
            System.out.println( "Semantic: Check Case: visitProcedureDeclaration");
        }

        // check if this method has a return type

        TypeProcedureContext typeProcedureContext = ctx.typeProcedure();
        if (typeProcedureContext == null) {
            return null;
        }

        ResultAttributeContext resultAttribute = typeProcedureContext.resultAttribute();
        if (resultAttribute == null) {
            // no return type, so skip procedure
            return null;
        }

        // todo: check valid expression later
        ResultTypeContext resultTypeContext = resultAttribute.resultType();
        this.currentResultType = resultTypeContext;

        visitChildren(ctx);

        return null;
    }

    @Override
    public Void visitProcedureBody(ProcedureBodyContext ctx) {

        List<StatementContext> statementList = ctx.statement();

        returns.push(ctx);

        if (statementList.size() == 0) {
            ErrorStack.add("must end with RETURN ("+ currentResultType.toString()+ ctx.getText());
        } else {
            StatementContext last = statementList.get(statementList.size()-1);
            if (last.unlabeled_statement() != null) {
                ReturnStatementContext returnStatementContext = last.unlabeled_statement().returnStatement();
                if (returnStatementContext != null) {
                    visitReturnStatement(returnStatementContext);
                } else {
                    // search in other statements
                    visitChildren(ctx);
                }
            }
        }

        if (returns.isEmpty()) {
            System.out.println("nice");
        } else {
            RuleContext peek = returns.peek();
            printMissingReturn(peek);
        }

        return null;
    }

    private void printMissingReturn(RuleContext context) {
        ErrorStack.add("Missing return statement near: " + context.getText());
    }

    private void check(RuleContext context) {
        if (context == null) return;
        returns.push(context);
        visitChildren(context);
        if (returns.contains(context)) {
            returns.remove(context);
            printMissingReturn(context);
        }
    }

    @Override
    public Void visitReturnStatement(ReturnStatementContext ctx) {
        RuleContext context = returns.pop();
        System.out.println("Return found in " + context.getText());
        if (ctx.expression() == null) {
            ErrorStack.add("missing return value in return statement");
        }
        return null;
    }

    @Override
    public Void visitCase_statement(Case_statementContext ctx) {
        System.out.println("CASE#: " + ctx.getText());

        if (!returns.isEmpty()) {
            returns.pop();
        }

        // statement_1
        Case_statement_selection1Context sel1Ctx = ctx.case_statement_selection1();
        if (sel1Ctx != null) {
            List<Case_statement_selection1_altContext> altList = sel1Ctx.case_statement_selection1_alt();
            altList.forEach(alt -> {
               check(alt);
            });
            Case_statement_selection_outContext case1Out = ctx.case_statement_selection1().case_statement_selection_out();
            if (case1Out != null) {
               check(case1Out);
            }
        }


        // statement_2
        Case_statement_selection2Context case2Ctx = ctx.case_statement_selection2();
        if (case2Ctx != null ) {
            List<Case_statement_selection2_altContext> caseList2 = case2Ctx.case_statement_selection2_alt();
            caseList2.forEach(alt -> {
                check(alt);
            });
            Case_statement_selection_outContext case2Out = ctx.case_statement_selection2().case_statement_selection_out();
            if (case2Out != null) {
                check(case2Out);
            }
        }


        return null;
    }


    @Override
    public Void visitIf_statement(If_statementContext ctx) {
        if (!returns.isEmpty()) returns.pop();

        Then_blockContext then = ctx.then_block();
        check(then);

        Else_blockContext elseBlock = ctx.else_block();
        check(elseBlock);

        return null;
    }


    /*
    @Override
    public Void visitProcedureDeclaration(ProcedureDeclarationContext ctx) {

        ProcedureBodyContext bodyCtx = ctx.procedureBody();

        ResultAttributeContext resultAttributeContext = ctx.typeProcedure().resultAttribute();
        if ( false && resultAttributeContext == null) {
            return null;
        }

        ResultTypeContext resultType = resultAttributeContext.resultType();

        if (resultType == null) {
            return null;
        }



        List<StatementContext> statements = bodyCtx.statement();
        int statementSize = statements.size();

        if (statementSize == 0) {
         //   ErrorStack.add("must end with RETURN ("+resultType.toString()+")");
            ErrorStack.add("must end with RETURN (hello)");
        } else {



            for (StatementContext stmt : statements) {
                System.out.println(stmt.getText() +  " #" + stmt.getChildCount());

                System.out.println("CH: " + stmt.getChild(0).getClass());


                if (stmt.getRuleContext() instanceof If_statementContext) {
                    If_statementContext ifctx = (If_statementContext) stmt.getRuleContext();
                    System.out.println("IF: " + ifctx.toStringTree());
                }

                if (stmt.unlabeled_statement() != null) {
                    if (stmt.unlabeled_statement().returnStatement() != null) {
                        System.out.println("RETURN GEFUNDEN !" +  stmt.getAltNumber());
                    }
                }
            }

        }



        return super.visitProcedureDeclaration(ctx);
    }

     */
}
