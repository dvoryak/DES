package com.company.des;

import java.math.BigInteger;

/**
 * @author paveldvoryak
 *
 */

public class DES {
    private BitArray key = new BitArray("0110011010100101000011111101001010000101011110010100111111101011");

    public DES() {
    }

    private BitArray[] keys = updateKey();


    public BitArray encrypt(BitArray data) {
        if (data.size() != 64) throw new IllegalArgumentException("block should be 64 bits");

        data = initialPermutation(data);

        BitArray result = feistel(data, false);

        return finalPermutation(result);
    }

    public BitArray decrypt(BitArray data) {
        if (data.size() != 64) throw new IllegalArgumentException("block should be 64 bits");

        data = initialPermutation(data);

        BitArray result = feistel(data, true);

        return finalPermutation(result);
    }

    public BitArray getKey() {
        return new BitArray(key);
    }

    public void setKey(BitArray key) {
        if(key.size() != 64) throw new IllegalArgumentException("size of key should be 64");
        this.key = key;
        keys = updateKey();
    }

    private BitArray[] updateKey() {
        BitArray[] keys;
        keys = new BitArray[16];
        BitArray key1 = new BitArray(key);
        BitArray[] blockOfKeys = getBlockOfKeys(key1);
        for(int i = 0; i < 16; i++) {
            BitArray roundKey = getRoundKey(blockOfKeys, i + 1);
            keys[i] = roundKey;
        }
        return keys;
    }

    private BitArray feistel(BitArray data, boolean reverse) {

        int rounds = 16;
        int round = reverse ? 16 : 1;

        BitArray left = new BitArray(data.size() / 2);
        BitArray right = new BitArray(data.size() / 2);

        for (int i = 0; i < data.size(); i++)
            if (i < 32) {
                left.setBit(i, data.getBit(i));
            } else {
                right.setBit(i - 32, data.getBit(i));
            }

        if (!reverse) {
            for (int i = 0; i < rounds; i++) {
                BitArray temp = new BitArray(left);
                left = new BitArray(right);
                right = temp.XOR(func_feistel(new BitArray(right), keys[i]));
                round++;
            }
        } else  {
            BitArray tmp = new BitArray(left);
            left = right;
            right = tmp;
            round = 15;
            for (int i = 0; i < 16; i++) {
                BitArray temp = new BitArray(left);
                left = new BitArray(right);
                right = temp.XOR(func_feistel(new BitArray(right), keys[round]));
                round--;
            }
            tmp = new BitArray(left);
            left = new BitArray(right);
            right = tmp;
        }

        return merge(64,left,right);

    }

    /**
     * 1. Expansion R block to 48 bits +
     * 2. XOR with key +
     * 3. Transformation S
     * 3.1 Merge 8x4 array to 1x32
     * 4. Permutation P
     * 5. Return block of 32 bits
     *
     * @param r   block of 32 bits
     * @param key block of 48 bits
     * @return block of 32 bits
     */
    public BitArray func_feistel(BitArray r, BitArray key) {
        if (r.size() != 32 && key.size() != 48) throw new IllegalArgumentException("data = 32 and key = 48");
        //Expansion block to 48 bits
        BitArray rR = expansionBlock(r);
        // Xor
        BitArray xor = rR.XOR(key);
        // divide on 8 parts
        BitArray[] arrays = getPart(xor);
        // S transformation
        BitArray merged32bit = merge(32,transformationS(arrays));
        // Permutation
        return permutation(merged32bit, Table.PERMUTATION_P, 32);
    }

    /**
     * @param bits array that represent 8 BitArray 6 bits each
     * @return array[8] of BitArray 4 bit each
     */
    public BitArray[] transformationS(BitArray[] bits) {
        BitArray[] out = new BitArray[8];
        StringBuilder sb = null;
        for (int i = 0; i < 8; i++) {
            sb = new StringBuilder(5);
            sb.append(bits[i].getBit(0)).append(bits[i].getBit(5));
            int m = new BigInteger(sb.toString(), 2).intValue();
            sb = new StringBuilder();
            sb.append(bits[i].getBit(1)).append(bits[i].getBit(2)).append(bits[i].getBit(3))
                    .append(bits[i].getBit(4));
            int l = new BigInteger(sb.toString(), 2).intValue();
            sb = new StringBuilder();
            sb.append(new BigInteger(Table.sBox[i][m][l] + "", 10).toString(2));
            while (sb.toString().length() < 4) {
                sb.reverse().append(0).reverse();
            }
            /*if(i + 1 == 3) {
                System.out.println(i + ":" + sb.toString());
                System.out.println(i + ":" + "m :" + m + " l :" + l);
            }*/
            out[i] = new BitArray(sb.toString());
        }
        return out;
    }

    /**
     * @param data  block of bits
     * @param table permutations table
     * @return table block of bits same size with initial block
     */
    private BitArray permutation(BitArray data, int[] table, int size) {
        BitArray out = new BitArray(size);
        for (int i = 0; i < size; i++) {
            out.setBit(i, data.getBit(table[i] - 1));
        }

        return out;
    }

    /**
     * Initial permutation for 64 bits block
     *
     * @param data block of 64 bits
     * @return block of 64 bits
     */
    private BitArray initialPermutation(BitArray data) {
        if (data.size() != 64) throw new IllegalArgumentException("expected block of 64 bits :" + data.size());
        return permutation(data, Table.FIRST_PERMUTATION, 64);
    }

    /**
     * Final permutation for 64 bits block
     *
     * @param data block of 64 bits
     * @return block of 64 bits
     */
    private BitArray finalPermutation(BitArray data) {
        if (data.size() != 64) throw new IllegalArgumentException("expected block of 64 bits");
        return permutation(data, Table.FINAL_PERMUTATION, 64);
    }

    /**
     * @param data block of 32 bits
     * @return expansion block of 48 bits
     * @throws IllegalArgumentException if size of block is not equal 32 bits
     */
    private BitArray expansionBlock(BitArray data) {
        if (data.size() != 32) throw new IllegalArgumentException("Block's size is not equal 32 bits");
        return permutation(data,Table.EXPANSION_PERMUTATION,48);
    }

    /**
     * Permutation with key and delete 8,16,24...64 bit
     * @param key block of 64 bit
     * @return block of 56 bit
     */
    private BitArray permutationKey(BitArray key) {
        if(key.size() != 64) throw new IllegalArgumentException("expected block of 64 bits");
        return permutation(key,Table.PERMUTATION_FOR_EXTENDED_KEY,56);
    }

    /**
     * @param bitArray block of 48 bits
     * @return 8 arrays 6 bits each
     */
    private BitArray[] getPart(BitArray bitArray) {
        if (bitArray.size() != 48) throw new IllegalArgumentException("expected 48 bits block");
        BitArray[] out = new BitArray[8];
        for (int i = 0; i < 8; i++) {
            out[i] = new BitArray(6);
            for (int j = 0; j < 6; j++) {
                out[i].setBit(j, bitArray.getBit((i * 6) + j));
            }
        }
        return out;
    }

    /**
     * @param key block of 64 bits
     * @return block[] C and D blocks
     */
    private BitArray[] getBlockOfKeys(BitArray key) {
        if (key.size() != 64) throw new IllegalArgumentException("expected size of key 64");
        key = permutationKey(key);

        BitArray blockC = new BitArray(28);
        BitArray blockD = new BitArray(28);

        for (int i = 0; i < key.size(); i++) {
            if (i < 28) {
                blockC.setBit(i, key.getBit(i));
            } else {
                blockD.setBit(i - 28, key.getBit(i));
            }
        }
        return new BitArray[]{blockC, blockD};
    }

    /**
     * @param cd    array of C and D blocks 28 bits each
     * @param round expected a values from 1 to 16
     * @return key of round
     * @throws IllegalArgumentException
     */
    private BitArray getRoundKey(BitArray[] cd, int round) {
        if (round > 16 || round <= 0) throw new IllegalArgumentException("Expected round <= 16");
        BitArray C = cd[0];
        BitArray D = cd[1];

        C.leftCycleShift(Table.COUNTS_OF_SHIFTS[round - 1]);
        D.leftCycleShift(Table.COUNTS_OF_SHIFTS[round - 1]);

        BitArray out = merge(56,C,D);

        return permutation(out, Table.PERMUTATION_FOR_Ki, 48);
    }

    /**
     *
     * @param newSize new size output array
     * @param arrays array of BitArray
     * @return
     */
    private BitArray merge(int newSize, BitArray... arrays) {
        BitArray out = new BitArray(newSize);
        int index = 0;
        for (BitArray array : arrays) {
            for(int i = 0; i < array.size(); i++) {
                out.setBit(index++,array.getBit(i));
            }
        }
        return out;
    }


}
