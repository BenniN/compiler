/*
 * [The "BSD license"]
 *  Copyright (c) 2012-2017 Marcel Schaible
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.smallpearl.compiler.SemanticAnalysis;

import org.smallpearl.compiler.*;
import org.smallpearl.compiler.Exception.InternalCompilerErrorException;
import org.smallpearl.compiler.Exception.TypeMismatchException;
import org.smallpearl.compiler.SymbolTable.ModuleEntry;
import org.smallpearl.compiler.SymbolTable.SymbolTable;
import org.smallpearl.compiler.SymbolTable.SymbolTableEntry;
import org.smallpearl.compiler.SymbolTable.VariableEntry;

public class CheckAssignment extends SmallPearlBaseVisitor<Void> implements SmallPearlVisitor<Void> {

  private int m_verbose;
  private boolean m_debug;
  private String m_sourceFileName;
  private ExpressionTypeVisitor m_expressionTypeVisitor;
  private SymbolTableVisitor m_symbolTableVisitor;
  private SymbolTable m_symboltable;
  private SymbolTable m_currentSymbolTable;
  private ModuleEntry m_module;
  private AST m_ast = null;

  public CheckAssignment(String sourceFileName,
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

    Log.info("Semantic Check: Check assignment statements");    }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // The type of the variable given to the left of the assignment sign has to match the type of the  value of the
  // expression, with the following exceptions:
  //  (1) The value of a FIXED variable or an integer, resp., may be assigned to a FLOAT variable.
  //  (2) The precision of a numeric variable to the left of an assignment sign may be greater than the precision of
  //      the value of the expression.
  //  (3) A bit or character string, resp., to the left may have a greater length than the value to be assigned; if
  //      needed, the latter is extended by zeros or spaces, resp., on the right.
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  public Void visitAssignment_statement(SmallPearlParser.Assignment_statementContext ctx) {
    Log.debug("CheckAssignment:visitAssignment_statement:ctx" + CommonUtils.printContext(ctx));

    String id = null;
    ErrorStack.enter(ctx,"assignment");
    ASTAttribute attrLhs = null; 
    ConstantSelection selection = null;
    
    SmallPearlParser.NameContext ctxName = null;
    if ( ctx.stringSelection() != null ) {
      attrLhs = m_ast.lookup(ctx.stringSelection()); 
      selection = attrLhs.getConstantSelection();
      if ( ctx.stringSelection().charSelection() != null ) {
        ctxName = ctx.stringSelection().charSelection().name();
   
      }
      else  if (ctx.stringSelection().bitSelection() != null) {
        ctxName = ctx.stringSelection().bitSelection().name();
      } else {
        throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
      }
// deprecated --> covered by 'name'      
//    } else if (ctx.selector() != null ) {
//      Log.debug("ExpressionTypeVisitor:visitAssignment_statement:selector:ctx" + CommonUtils.printContext(ctx.selector()));
//      visitSelector(ctx.selector());
//      id = ctx.selector().ID().getText();
    } else {
      attrLhs = m_ast.lookup(ctx.name()); 
      ctxName = ctx.name();
    }
    
    id = ctxName.ID().getText();
    
    SymbolTableEntry lhs = m_currentSymbolTable.lookup(id);

    Log.debug("CheckAssignment:visitAssignment_statement:ctx.expression" + CommonUtils.printContext(ctx.expression()));

    SmallPearlParser.ExpressionContext expr = ctx.expression();

    TypeDefinition rhs = m_ast.lookupType(ctx.expression());
    ASTAttribute rhs1 = m_ast.lookup(ctx.expression());

    if ( lhs instanceof VariableEntry) {
      VariableEntry variable = (VariableEntry) lhs;
      if (variable.getAssigmentProtection()) {
        ErrorStack.add("left hand side is INV");
      }
      if ( lhs == null ) {
        throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
      }

      if ( variable.getLoopControlVariable()) {
        ErrorStack.add("left hand side is loop variable");
      }

      if ( rhs == null ) {
        throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
      }

      if ( variable.getType() instanceof TypeFloat ) {
        TypeFloat lhs_type = (TypeFloat) variable.getType();

        if ( !(rhs instanceof  TypeFloat || rhs instanceof TypeFixed) ) {
          ErrorStack.add("type mismatch: "+variable.getType().toString()+":="+rhs1.getType().toString());
          //  throw new TypeMismatchException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
        }

        if ( rhs instanceof TypeFloat ) {
          TypeFloat rhs_type = (TypeFloat)rhs;

          if ( rhs_type.getPrecision() >  lhs_type.getPrecision() ) {
            ErrorStack.add("type mismatch: "+variable.getType().toString()+":="+rhs1.getType().toString());
          }
        }
        else if ( rhs instanceof TypeFixed ) {
          TypeFixed rhs_type = (TypeFixed) rhs;
        }
      }
      else if ( variable.getType() instanceof TypeFixed ) {
        TypeFixed lhs_type = (TypeFixed) variable.getType();

        if ( rhs instanceof TypeReference &&
            ((TypeReference)rhs).getBaseType() instanceof TypeFixed) {
          ErrorStack.add("type mismatch: "+variable.getType().toString()+":="+rhs1.getType().toString());
        } else if ( !(rhs instanceof TypeFixed) ) {
          ErrorStack.add("type mismatch: "+variable.getType().toString()+":="+rhs1.getType().toString());
        } else { 

          int rhs_precision = 0;
          if (rhs instanceof TypeReference) {
            TypeFixed typ = (TypeFixed) ((TypeReference) rhs).getBaseType();
            rhs_precision = typ.getPrecision();
          }
          else {
            rhs_precision = ((TypeFixed) rhs).getPrecision();
          }

          if ( ((TypeFixed) variable.getType()).getPrecision() < rhs_precision ) {
            ErrorStack.add("type mismatch: "+variable.getType().toString()+":="+rhs1.getType().toString());
          }
        }
      }
      else if ( variable.getType() instanceof TypeClock ) {
        if ( !(rhs instanceof TypeClock) ) {
          ErrorStack.add("type mismatch: "+variable.getType().toString()+":="+rhs1.getType().toString());
        }
      }
      else if ( variable.getType() instanceof TypeDuration ) {
        if ( !(rhs instanceof TypeDuration) ) {
          ErrorStack.add("type mismatch: "+variable.getType().toString()+":="+rhs1.getType().toString());
        }
      }
      else if ( variable.getType() instanceof TypeBit ) {
        TypeBit lhs_type = (TypeBit) variable.getType();

        if (!(rhs instanceof TypeBit)) {
          ErrorStack.add("type mismatch: "+variable.getType().toString()+":="+rhs1.getType().toString());
        }

        TypeBit rhs_type = (TypeBit) rhs;

        if (rhs_type.getPrecision() > lhs_type.getPrecision()) {
          ErrorStack.add("type mismatch: "+variable.getType().toString()+":="+rhs1.getType().toString());
        }
      }
      else if (variable.getType() instanceof TypeChar) {
        if (!(rhs instanceof TypeChar || rhs instanceof TypeVariableChar)) {
          ErrorStack.add("type mismatch: "+variable.getType().toString()+":="+rhs1.getType().getName());
        }
        TypeChar lhs_type = (TypeChar) variable.getType();
        if (rhs instanceof TypeChar) {
          TypeChar rhs_type = (TypeChar)rhs;
          if (selection == null) {
            if (rhs_type.getPrecision() > lhs_type.getPrecision()) {
              ErrorStack.add("type mismatch: "+variable.getType().toString()+":="+rhs1.getType().toString());
            }
          } else {
            if (rhs_type.getPrecision() > selection.getNoOfElements()) {
              ErrorStack.add("type mismatch: CHAR("+selection.getNoOfElements()+") := "+rhs1.getType().toString());
            }
          }
        }
      }
      else if ( variable.getType() instanceof TypeReference ) {
        TypeReference lhs_type = (TypeReference) variable.getType();
        TypeDefinition rhs_type;

        if ( ctx.dereference() == null ) {
          if ( (rhs1.getVariable() == null) && ( !(rhs1.getType() instanceof TypeTask))) {
            ErrorStack.add("type mismatch: "+variable.getType().toString()+":="+rhs1.getType().toString());
          }

          TypeDefinition lt = lhs_type.getBaseType();

          if ( rhs instanceof TypeReference) {
            rhs_type = ((TypeReference) rhs).getBaseType();
          }
          else {
            rhs_type = rhs;
          }

          if ( !(lt.equals(rhs_type))) {
            ErrorStack.add("type mismatch: "+variable.getType().toString()+":="+rhs1.getType().toString());
          }
        }
        else {
          TypeDefinition lt = lhs_type.getBaseType();
          if ( !(lt.equals(rhs))) {
            ErrorStack.add("type mismatch: "+variable.getType().toString()+":="+rhs1.getType().toString());
          }
        }
      }
      else if ( variable.getType() instanceof TypeTask ) {
        System.out.println("Semantic: visitAssignment_statement: TASK");
      }
    }
    else {
      throw new InternalCompilerErrorException(ctx.getText(), ctx.start.getLine(), ctx.start.getCharPositionInLine());
    }
    ErrorStack.leave();

    return null;
  }

  @Override
  public Void visitModule(SmallPearlParser.ModuleContext ctx) {
    org.smallpearl.compiler.SymbolTable.SymbolTableEntry symbolTableEntry = m_currentSymbolTable.lookupLocal(ctx.ID().getText());
    m_currentSymbolTable = ((org.smallpearl.compiler.SymbolTable.ModuleEntry)symbolTableEntry).scope;
    visitChildren(ctx);
    m_currentSymbolTable = m_currentSymbolTable.ascend();
    return null;
  }

  @Override
  public Void visitProcedureDeclaration(SmallPearlParser.ProcedureDeclarationContext ctx) {
    this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
    visitChildren(ctx);
    this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
    return null;
  }

  @Override
  public Void visitTaskDeclaration(SmallPearlParser.TaskDeclarationContext ctx) {
    this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
    visitChildren(ctx);
    m_currentSymbolTable = m_currentSymbolTable.ascend();
    return null;
  }

  @Override
  public Void visitBlock_statement(SmallPearlParser.Block_statementContext ctx) {
    this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
    visitChildren(ctx);
    this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
    return null;
  }

  @Override
  public Void visitLoopStatement(SmallPearlParser.LoopStatementContext ctx) {
    this.m_currentSymbolTable = m_symbolTableVisitor.getSymbolTablePerContext(ctx);
    visitChildren(ctx);
    this.m_currentSymbolTable = this.m_currentSymbolTable.ascend();
    return null;
  }
}