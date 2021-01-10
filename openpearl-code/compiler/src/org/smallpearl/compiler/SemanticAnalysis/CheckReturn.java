package org.smallpearl.compiler.SemanticAnalysis;



    import java.util.LinkedList;
    import java.util.Vector;

    import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.smallpearl.compiler.*;
import org.smallpearl.compiler.SmallPearlParser.ExpressionContext;import org.smallpearl.compiler.SmallPearlParser.FormatPositionContext;
import org.smallpearl.compiler.SmallPearlParser.ProcedureBodyContext;
import org.smallpearl.compiler.SmallPearlParser.ProcedureDeclarationContext;
import org.smallpearl.compiler.SmallPearlParser.ResultAttributeContext;
import org.smallpearl.compiler.SmallPearlParser.TaskDeclarationContext;
import org.smallpearl.compiler.Exception.*;
import org.smallpearl.compiler.SymbolTable.FormalParameter;
import org.smallpearl.compiler.SymbolTable.ModuleEntry;
import org.smallpearl.compiler.SymbolTable.ProcedureEntry;
import org.smallpearl.compiler.SymbolTable.SymbolTable;
import org.smallpearl.compiler.SymbolTable.SymbolTableEntry;
import org.smallpearl.compiler.SymbolTable.VariableEntry;


public class CheckReturn extends SmallPearlBaseVisitor<Void> implements SmallPearlVisitor<Void> {
//
//        private int m_verbose;
//        private boolean m_debug;
//        private String m_sourceFileName;
//        private ExpressionTypeVisitor m_expressionTypeVisitor;
//        private SymbolTableVisitor m_symbolTableVisitor;
//        private SymbolTable m_symboltable;
//        private SymbolTable m_currentSymbolTable;
//        private ModuleEntry m_module;
//        private AST m_ast = null;
//        private TypeDefinition m_typeOfReturns;
//        //private TypeDefinition m_typeOfReturnExpression;
//
//        private ParseTreeProperty<SymbolTable> m_symboltablePerContext = null;
//
//
//
//
//        public Void visitProcedureDeclaration(SmallPearlParser.ProcedureDeclarationContext ctx) {
//            String globalId = null;
//            LinkedList<FormalParameter> formalParameters = null;
//            ASTAttribute resultType = null;
//
//            if (m_verbose > 0) {
//                System.out.println("SymbolTableVisitor: visitProcedureDeclaration");
//            }
//            ErrorStack.enter(ctx,"PROC");
//
//            this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
//
//            SymbolTableEntry entry = this.m_currentSymbolTable.lookup(ctx.ID().toString());
//            if (entry == null) {
//                throw new InternalCompilerErrorException("PROC "+ctx.ID().toString()+" not found", ctx.start.getLine(), ctx.start.getCharPositionInLine());
//            }
//
//            ProcedureEntry procedureEntry = (ProcedureEntry)entry;
//
//            //TODO: MS This caanot be corrected, because semancti check should not alter the symboltable
//            //this.m_currentSymbolTable = this.m_currentSymbolTable.newLevel(procedureEntry);
//
//            if ( procedureEntry.getFormalParameters() != null && procedureEntry.getFormalParameters().size() > 0) {
//                /* check formal parameters of this procedure */
//
//
//                for (FormalParameter formalParameter : procedureEntry.getFormalParameters()) {
//                    checkFormalParameter(formalParameter);
//                }
//            }
//
//            // reset the attribute before visitChildren()
//            // m_typeOfReturnExpression contains the type of the last RETURN statement
//            // in the procedure body
//            m_typeOfReturns = procedureEntry.getResultType();
////    m_typeOfReturnExpression = null;
//
//            visitChildren(ctx);
//
//            if (m_typeOfReturns != null) {
//                // check last statement of function to be RETURN
//                // this is easier to implement as to enshure that all paths of control
//                // meet a RETURN(..) statemen
//                ProcedureBodyContext b = ctx.procedureBody();
//                int last = b.statement().size();
//                if (last == 0) {
//                    ErrorStack.add("must end with RETURN ("+m_typeOfReturns.toString()+")");
//                } else {
//                    SmallPearlParser.StatementContext lastStmnt = b.statement(last-1);
//                    if (lastStmnt.unlabeled_statement() != null) {
//                        if (lastStmnt.unlabeled_statement().returnStatement() == null) {
//                            ErrorStack.add("must end with RETURN ("+m_typeOfReturns.toString()+")");
//
//                        }
//                    }
//                }
//            }
//            this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
//            ErrorStack.leave();
//            return null;
//        }
//
//
//        int currentlevel = 0;
//
//
//        public int m_currentLevel(){
//            return currentlevel;
//        }
//
//
//        public int m_deeperLevel(){
//
//            currentlevel = currentlevel +1;
//            return currentlevel;
//        }
//
//
//        public int m_lowerLevel(){
//            currentlevel =currentlevel -1;
//            return currentlevel;
//        }
//
//        // myStack.get(m_currentLevel()).add(true);
//
//
//        public void visitProcedureDeclaration(SmallPearlParser.ProcedureDeclarationContext ctx) {
//            Vector<Vector<Integer>> myStack= new Vector<Vector<Integer>>();
//            boolean thenHasReturn = false;
//            boolean elseHasReturn = false;
//            visitChildren(ctx);
//            ProcedureBodyContext b = ctx.procedureBody();
//
//            if (m_typeOfReturns != null) {
//                if (SmallPearlParser.StatementContext.unlabeled_statement().ifStatement() != null) {
//
//                    m_deeperLevel();
//
//                    if (nextStmnt.unlabeled_statement().retrunStatement != null) {
//                        thenHasReturn = true;
//
//                        SmallPearlParser.StatementContext nextStmnt = b.statement(first + 1);
//                    } else {
//                        SmallPearlParser.StatementContext nextStmnt = b.statement(first + 1);
//                    }
//
//                }
//                if (SmallPearlParser.StatementContext.unlabeled_statement().elseStatement() != null) {
//                    m_deeperLevel();
//                    if (nextStmnt.unlabeled_statement().retrunStatement != null) {
//                        elseHasReturn = true;
//                        if (m_lowerLevel() && m_deeperLevel() == 1) {
//                            myStack.get(m_currentLevel()).add(true);
//                        } else {
//                            myStack.get(m_currentLevel()).add(false);
//                        }
//                        m_lowerLevel();
//                        SmallPearlParser.StatementContext nextStmnt = b.statement(first + 1);
//                    } else {
//                        SmallPearlParser.StatementContext nextStmnt = b.statement(first + 1);
//                    }
//
//                }
//                if (m_lowerLevel() && m_deeperLevel() == true) {
//                    myStack.get(m_currentLevel()).add(1);
//                } else {
//                    myStack.get(m_currentLevel()).add(0);
//                }
//            }
//        }
//
//
//
//
//    public void visitProcedureDeclaration(SmallPearlParser.ProcedureDeclarationContext ctx) {
//        String globalId = null;
//        LinkedList<FormalParameter> formalParameters = null;
//        ASTAttribute resultType = null;
//        visitChildren(ctx);
//
//        ProcedureBodyContext b = ctx.procedureBody();
//        int nextStmnt = b.statement().size();
//        int last = b.statement().size();
//        for (int first = 0; first < last; first++) {
//            if (nextStmnt.unlabeled_statement().retrunStatement != null) {
//                System.out.println("Found return as Statement number " + nextStmnt);
//                SmallPearlParser.StatementContext nextStmnt = b.statement(first + 1);
//            }
//        }
//
//
////    obsolete - resultType is already in the symbol table
////    @Override
////    public Void visitResultAttribute (SmallPearlParser.ResultAttributeContext ctx) {
////         ErrorStack.enter(ctx.resultType(),"RETURNS");
////     m_typeOfReturns = null;
////     if (ctx.resultType().simpleType() != null) {
////         m_typeOfReturns = CommonUtils.getTypeDefinitionForSimpleType(ctx.resultType().simpleType());
////     } else if (ctx.resultType().typeReference() != null) {
////       TypeDefinition baseType = CommonUtils.getBaseTypeForReferenceType(ctx.resultType().typeReference());
////       if (ctx.resultType().typeReference().assignmentProtection()!= null) {
////         baseType.setHasAssignmentProtection(true);
////         m_typeOfReturns = baseType;
////       }
////       if (ctx.resultType().typeReference().virtualDimensionList() != null) {
////         int dimensions =  ctx.resultType().typeReference().virtualDimensionList().commas().getChildCount();
////         TypeArraySpecification array = new TypeArraySpecification(baseType,dimensions);
////         m_typeOfReturns = array;
////       }
////     } else if (ctx.resultType().ID() != null) {
////        ErrorStack.add("TYPE not supported");
////     }
////     ErrorStack.leave();
////     return null;
////    }
//
//
//        public Void visitReturnStatement (SmallPearlParser.ReturnStatementContext ctx){
//            RuleContext parent = ctx;
//            ErrorStack.enter(ctx, "RETURN");
//
//
//            if (m_typeOfReturns == null && ctx.expression() != null) {
//                ErrorStack.add("illegal without RETURNS in declaration");
//            } else if (ctx.expression() != null) {
//                // we have an expression at RETURN
//                TypeDefinition exprType = m_ast.lookupType(ctx.expression());
//
//                // can be removed if all possible types are detected
//                Boolean typeIsCompatible = true;
//
//                TypeDefinition tmpTypeOfResult = m_typeOfReturns;
//                TypeDefinition tmpExprType = exprType;
//
//                if (m_typeOfReturns != null) {
//                    // check for implicit dereference /reference possibilities
//                    // --> base types must be compatible
//                    if (tmpTypeOfResult instanceof TypeReference) {
//                        tmpTypeOfResult = ((TypeReference) tmpTypeOfResult).getBaseType();
//                    }
//                    if (tmpExprType instanceof TypeReference) {
//                        tmpExprType = ((TypeReference) tmpExprType).getBaseType();
//                    }
//
//                    // check compatibility of baseTypes
//                    if (tmpTypeOfResult instanceof TypeFixed && tmpExprType instanceof TypeFixed) {
//                        if (((TypeFixed) tmpTypeOfResult).getPrecision() < ((TypeFixed) tmpExprType).getPrecision()) {
//                            typeIsCompatible = false;
//                        }
//                    } else if (tmpTypeOfResult instanceof TypeFloat && tmpExprType instanceof TypeFloat) {
//                        if (((TypeFloat) tmpTypeOfResult).getPrecision() < ((TypeFloat) tmpExprType).getPrecision()) {
//                            typeIsCompatible = false;
//                        }
//                    } else if (tmpTypeOfResult instanceof TypeBit && tmpExprType instanceof TypeBit) {
//                        if (((TypeBit) tmpTypeOfResult).getPrecision() < ((TypeBit) tmpExprType).getPrecision()) {
//                            typeIsCompatible = false;
//                        }
//                    } else if (tmpTypeOfResult instanceof TypeChar && tmpExprType instanceof TypeChar) {
//                        if (((TypeChar) tmpTypeOfResult).getSize() < ((TypeChar) tmpExprType).getSize()) {
//                            typeIsCompatible = false;
//                        }
//                    } else if (tmpTypeOfResult instanceof TypeChar && tmpExprType instanceof TypeVariableChar) {
//                        // this must be checked during runtime
//                    } else {
//                        if (!tmpTypeOfResult.equals(tmpExprType)) {
//                            typeIsCompatible = false;
//                        }
//                    }
//
//                    if (!typeIsCompatible) {
//                        ErrorStack.add("expression does not fit to RETURN type: expected " + m_typeOfReturns.toString() + " -- got " + exprType.toString());
//                    }
//
//                    if (m_typeOfReturns instanceof TypeReference) {
//                        // check lifeCyle required
//                        ASTAttribute attrRhs = m_ast.lookup(ctx.expression());
//                        if (attrRhs.getVariable() != null) {
//                            int level = attrRhs.getVariable().getLevel();
//                            if (level > 1) {
//                                ErrorStack.add("life cycle of '" + attrRhs.getVariable().getName() + "' is too short");
//                            }
//                        } else if (attrRhs.getType() instanceof TypeProcedure) {
//                            // ok - we have a procedure name
//                        }
//                    }
//
//                }
//            }
//
//            ErrorStack.leave();
//            return null;
//        }
//
//
//    }

}