package com.kirosc.mathisfun;

import java.util.Random;

/**
 * Created by Kiros on 07-Mar-18.
 */

public final class QuestionGenerator {
    public static final int ADDITION = 0;
    public static final int SUBTRACTION = 1;
    public static final int MULTIPLICATION = 2;
    public static final int DIVISION = 3;

    // Since it is a static class for generation question only,
    // make the constructor private so no one can instantiate it.
    private QuestionGenerator() {
    }

    /**
     * Generate a random question
     *
     * @return String[] which first index is the question
     * and the second index is the answer
     */
    public static String[] getQuestion() {
        int x, y;
        int operation = getRandomNumber(0, 3);
        String[] question = new String[2];

        switch (operation) {
            case ADDITION:
                x = getRandomNumber(1, 99);
                y = getRandomNumber(1, 99);

                question[0] = x + " + " + y;
                question[1] = String.valueOf(x + y);
                break;
            case SUBTRACTION:
                x = getRandomNumber(1, 99);
                do {
                    y = getRandomNumber(1, 99);
                } while (x >= y);

                question[0] = y + " − " + x;
                question[1] = String.valueOf(y - x);
                break;
            case MULTIPLICATION:
                x = getRandomNumber(1, 20);
                y = getRandomNumber(1, 20);

                question[0] = x + " × " + y;
                question[1] = String.valueOf(x * y);
                break;
            case DIVISION:
                do {
                    x = getRandomNumber(1, 99);
                    y = getRandomNumber(1, 99);
                } while (x % y != 0);

                question[0] = x + " ÷ " + y;
                question[1] = String.valueOf(x / y);
                break;
            default:
                throw new UnsupportedOperationException("Operation is not supported.");
        }
        return question;
    }

    /**
     * Return an integer number between x and y inclusively
     *
     * @param x
     * @param y
     * @return Random integer number
     */
    public static int getRandomNumber(int x, int y) {
        Random random = new Random();
        return random.nextInt(++y - x) + x;
    }
}
