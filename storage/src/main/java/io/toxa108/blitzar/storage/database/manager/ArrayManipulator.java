package io.toxa108.blitzar.storage.database.manager;

public class ArrayManipulator {
    public static <T> void insertInArray(T[] array, T value, int pos) {
        if (array.length - 1 - pos >= 0) {
            System.arraycopy(array, pos, array, pos + 1, array.length - 1 - pos);
        }
        array[pos] = value;
    }

    public static void insertInArray(int[] array, int value, int pos) {
        if (array.length - 1 - pos >= 0) {
            System.arraycopy(array, pos, array, pos + 1, array.length - 1 - pos);
        }
        array[pos] = value;
    }

    public static byte[] invert(final byte[] array) {
        for (int i = 0; i < array.length / 2; ++i) {
            byte tmp = array[i];
            array[i] = array[array.length - i - 1];
            array[array.length - i - 1] = tmp;
        }
        return array;
    }

    public static void insertInArray(byte[] array, byte value, int pos) {
        if (array.length - 1 - pos >= 0) {
            System.arraycopy(array, pos, array, pos + 1, array.length - 1 - pos);
        }
        array[pos] = value;
    }

    public static <T> void copyArray(T[] source, T[] destination, int len) {
        if (source.length < len || destination.length < len) {
            throw new IllegalArgumentException();
        }

        if (len >= 0) {
            System.arraycopy(source, 0, destination, 0, len);
        }
    }

    public static void copyArray(int[] source, int[] destination, int len) {
        if (source.length < len || destination.length < len) {
            throw new IllegalArgumentException();
        }

        if (len >= 0) {
            System.arraycopy(source, 0, destination, 0, len);
        }
    }

    public static void copyArray(byte[] source, byte[] destination, int len) {
        if (source.length < len || destination.length < len) {
            throw new IllegalArgumentException();
        }

        if (len >= 0) {
            System.arraycopy(source, 0, destination, 0, len);
        }
    }

    public static <T> void copyArray(T[] source, T[] destination, int pos, int len) {
        if (source.length < len || destination.length < len) {
            throw new IllegalArgumentException();
        }

        if (len >= 0) {
            System.arraycopy(source, pos, destination, 0, len);
        }
    }

    public static void copyArray(int[] source, int[] destination, int pos, int len) {
        if (source.length < len || destination.length < len) {
            throw new IllegalArgumentException();
        }

        if (len >= 0) {
            System.arraycopy(source, pos, destination, 0, len);
        }
    }

    public static void copyArray(byte[] source, byte[] destination, int pos, int len) {
        if (source.length < len || destination.length < len) {
            throw new IllegalArgumentException();
        }

        if (len >= 0) {
            System.arraycopy(source, pos, destination, 0, len);
        }
    }
}
