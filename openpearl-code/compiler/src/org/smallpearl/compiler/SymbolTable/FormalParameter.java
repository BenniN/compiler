package org.smallpearl.compiler.SymbolTable;

import org.smallpearl.compiler.SmallPearlParser;
import org.smallpearl.compiler.TypeDefinition;

public class FormalParameter {

    public  String          name;
    public  TypeDefinition  type;
    public  Boolean         assignmentProtection;
    public  org.antlr.v4.runtime.ParserRuleContext m_ctx;

    public FormalParameter() {
    }

    public FormalParameter(String name, TypeDefinition type, Boolean assignmentProtection,  SmallPearlParser.FormalParameterContext ctx) {
        this.name = name;
        this.m_ctx = ctx;
        this.type = type;
        this.assignmentProtection = assignmentProtection;
    }

    public String toString() {
        return (this.assignmentProtection ? " INV " : " " ) + this.type;
    }


}