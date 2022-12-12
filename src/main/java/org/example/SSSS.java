package org.example;

import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class SSSS {

    private static final Random random = ThreadLocalRandom.current();

    public static Map.Entry<List<Share>, BigInteger> splitSecret(BigInteger secret, BigInteger k, BigInteger n) {
        var bitLength = secret.max(n).bitLength() * 2;

        var modulus = BigInteger.probablePrime(bitLength, random);

        List<BigInteger> polynomial = new ArrayList<>();

        polynomial.add(secret);
        for (int i = 1; i < k.intValue(); i++) {
            polynomial.add(randomBigInteger(BigInteger.ONE, modulus));
        }

        List<Share> shares = new ArrayList<>(n.intValue());

        for (int i = 0; i < n.intValue(); i++) {
            var x = randomBigInteger(BigInteger.ONE, modulus);
            shares.add(new Share(x, polynomialFunction(polynomial, x, modulus)));
        }

        return new AbstractMap.SimpleImmutableEntry<>(shares, modulus);
    }

    public static BigInteger combineShares(List<Share> shares, int k, BigInteger modulus) {
        var result = BigInteger.ZERO;

        for (int i = 0; i < k; i++) {
            var xi = shares.get(i).x();
            var yi = shares.get(i).y();

            for (int j = 0; j < k; j++) {
                if (i != j) {
                    var xj = shares.get(j).x();
                    var numerator = xj.negate();
                    var denominator = xi.subtract(xj);

                    yi = yi.multiply(numerator.multiply(denominator.modInverse(modulus)));
                }
            }

            result = result.add(yi);
        }

        return result.mod(modulus);
    }

    private static BigInteger randomBigInteger(BigInteger origin, BigInteger bound) {
        var val = new byte[bound.toByteArray().length];
        random.nextBytes(val);

        var result = origin.subtract(BigInteger.ONE);

        while (origin.compareTo(result) > 0) {
            result = new BigInteger(val, 0, val.length).mod(bound);
        }

        return result;
    }

    private static BigInteger polynomialFunction(List<BigInteger> polynomial, BigInteger x, BigInteger modulus) {
        var y = BigInteger.ZERO;

        for (int i = 0; i < polynomial.size(); i++) {
            y = y.add(polynomial.get(i).multiply(x.modPow(BigInteger.valueOf(i), modulus)));
        }

        return y.mod(modulus);
    }

    public record Share(BigInteger x, BigInteger y) {

    }

}
