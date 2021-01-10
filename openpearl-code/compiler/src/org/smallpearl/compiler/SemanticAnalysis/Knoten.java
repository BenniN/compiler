package org.smallpearl.compiler.SemanticAnalysis;

public class Knoten {
    private String data;
    private Knoten next;

    public String getData(){
        return data;
    }

    public Knoten(String data,Knoten next){
        this.data =data;
        this.next =next;
    }

    public void setData(String data){
        this.data =data;
    }

    public Knoten getNext(){
        return next;
    }
    public void setNext(Knoten next){
        this.next = next;
    }
}
