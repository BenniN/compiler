package org.smallpearl.compiler.SemanticAnalysis;

import org.smallpearl.compiler.Exception.UnderflowException;

public class Stackspeicher {

    private Knoten obersterKnoten = null;
    private int aktuelleHoehe = 0;

    public void push(String daten){
        Knoten knoten =new Knoten(daten, obersterKnoten);
        obersterKnoten =knoten;
        aktuelleHoehe++;
    }

    public String  pop () throws UnderflowException{
        if (aktuelleHoehe == 0){
            throw new UnderflowException();
        }

        String daten= obersterKnoten.getData();
        obersterKnoten = obersterKnoten.getNext();
        aktuelleHoehe--;
        return daten;
    }
}
