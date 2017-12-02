package com.company;

import com.company.des.BitArray;
import com.company.des.DES;

public class Main {

    public static void main(String[] args) {
	// write your code here
        DES des = new DES();
        String message = "0110100101110100001000000111011101101111011100100110101101110011";
        des.setKey(new BitArray(64));
        BitArray encrypt = des.encrypt(new BitArray(message));
        BitArray decrypt = des.decrypt(encrypt);
        System.out.println("message : " + message );
        System.out.println("encrypt : " + encrypt.toString() );
        System.out.println("decrypt : " + decrypt.toString() );
        if(decrypt.toString().equals(message))
            System.out.println(true);
        else System.out.println(false);
    }
}
