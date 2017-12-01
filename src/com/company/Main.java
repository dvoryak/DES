package com.company;

import com.company.des.BitArray;
import com.company.des.DES;

public class Main {

    public static void main(String[] args) {
	// write your code here
        DES des = new DES();
        String message = "0110010101100010011000010110000101100001011000010110000101100001";
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
