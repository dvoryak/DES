package com.company.des;

import java.math.BigInteger;
import java.util.Arrays;

public class BitArray {

    private boolean[] data;
    private final int size;

    public BitArray(int size) {
        this.size = size;
        this.data = new boolean[size];
    }

    public BitArray(BitArray bitArray) {
        this.data = bitArray.data.clone();
        this.size = bitArray.size;
    }

    public BitArray(String binary) {
        size = binary.length();
        data = new boolean[binary.length()];
        String[] split = binary.split("");
        for (String s : split) {
            if (!s.equals("1") && !s.equals("0")) throw new IllegalArgumentException("not a binary string");
        }
        for (int i = 0; i < data.length; i++) {
            data[i] = getBoolFromBit(Integer.parseInt(split[i]));
        }
    }

    public byte getBit(int position) {
        if (position >= size || position < 0) throw new IllegalArgumentException("index out of bound array : " + position);
        return getBitFromBoolean(data[position]);
    }

    public void setBit(int position, int value) {
        if (position >= size)
            throw new IllegalArgumentException("index out of bound array");
        data[position] = getBoolFromBit(value);
    }

    /**
     * The method does not change the state of instance
     *
     * @param another BitArray instance
     * @return new instance that represent a result of xor between this and another instance
     * @throws IllegalArgumentException
     */
    public BitArray XOR(BitArray another) {

        BitArray xor = new BitArray(this);

        if (xor.size != another.size)
            throw new IllegalArgumentException("can't Xor bcz these instance have different size");

        for (int i = 0; i < xor.size; i++) {
            if ((!xor.data[i] && !another.data[i]) || (xor.data[i] && another.data[i]))
                xor.data[i] = false;
            else {
                xor.data[i] = true;
            }
        }
        return xor;
    }

    public void leftCycleShift(int step) {
        for (int i = 0; i < step; i++) {
            int temp = getBit(0);
            for (int j = 0; j < this.size - 1; j++) {
                setBit(j,getBit(j + 1));
                if(j == size - 2) setBit(size - 1,temp);
            }
        }
    }

    public void rightCycleShift(int step) {
        for (int i = 0; i < step; i++) {
            int tmp = getBit(size() - 1);
            for (int j = size - 1; j > 0; j--) {
                setBit(j,getBit(j - 1));
                if (j == 1) setBit(0,tmp);
            }
        }
    }

    public int size() {
        return size;
    }

    private byte getBitFromBoolean(boolean bool) {
        return !bool ? ((byte) 0) : ((byte) 1);
    }

    private boolean getBoolFromBit(int b) {
        if(b != 1 && b != 0) throw new IllegalArgumentException("Expected 1 or 0 values");
        return b == 1;
    }

    public String getNumber() {
        return new BigInteger(this.toString(), 2).toString(10);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(size);
        for (boolean b : data) {
            sb.append(getBitFromBoolean(b));
        }
        return sb.toString();
    }

}
