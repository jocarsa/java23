package j034.unnucleo;

import java.math.BigInteger;

public class J034UnNucleo {
    public static void main(String[] args) {
        double numero = 1.0000000002;
        
        // Adjusting the iteration limit for demonstration purposes
        long iterations = 10000000000L;  // Use long here for larger values
        
        for(long i = 0; i < iterations; i++){
            numero *= 1.00000000534;
        }
        
        System.out.println(numero);
    }
}

