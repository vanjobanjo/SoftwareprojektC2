package de.fhwedel.klausps_examples.controller;

/**
 * project structure
 */
public class Main {
    
    public static void main(String[] args) {
        System.out.println("this is the project!");
    }

    public int doStuff(ABC abc) {
        return abc.getStuff("lol");
    }

    public class ABC {

        public int getStuff(String s) {
            return 0;
        }

    }

}
