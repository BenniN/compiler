package org.smallpearl.compiler.Exception;

public class UnderflowException  extends Exception{

    public UnderflowException(){
        System.out.println("Der Stapel ist leer");
    }
}
