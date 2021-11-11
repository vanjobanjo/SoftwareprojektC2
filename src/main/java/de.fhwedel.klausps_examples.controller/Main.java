package de.fhwedel.klausps_examples.controller;

/**
 * project structure
 */
public class Main {
    
    public static void main(String[] args) {

    }

    public static int someMethod(int input) {
        if (input >= 0) {
            return input * input;
        } else {
            return input;
        }
    }

    public static int other(String s) {
        if (s.equals("Hallo Welt")) {
            return -1;
        } else if (s.length() > 5) {
            return 1;
        } else {
            return 0;
        }
    }

}
