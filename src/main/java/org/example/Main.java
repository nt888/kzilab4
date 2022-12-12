package org.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    public static void main(String[] args) {
        interactive();
    }

    private static void split(BigInteger secret, BigInteger k, BigInteger n) {
        try (var outWriter = new BufferedWriter(new FileWriter("shares.txt"))) {

            var sharesAndModulo = SSSS.splitSecret(secret, k, n);
            var shares = sharesAndModulo.getKey();
            var modulus = sharesAndModulo.getValue();

            System.out.printf("\nModulus = %d\n", modulus);
            outWriter.write(modulus.toString());
            outWriter.newLine();

            System.out.println("Shares:");
            for (var share : shares) {
                System.out.format("(%d, %d)\n", share.x(), share.y());
                outWriter.write(share.x().toString());
                outWriter.write(" ");
                outWriter.write(share.y().toString());
                outWriter.newLine();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void combine(int k, BigInteger modulus, List<SSSS.Share> shares) {
        var secret = SSSS.combineShares(shares, k, modulus);

        var sb = new StringBuilder();

        sb.append("\nChosen shares:\n");
        for (var share : shares) {
            sb.append(String.format("(%d, %d)\n", share.x(), share.y()));
        }

        sb.append(String.format("\nSecret: %d\n", secret));

        System.out.println(sb);
    }

    private static void interactive() {
        try (var inScanner = new Scanner(System.in)) {
            System.out.print("""
                    [0] Split secret
                    [1] Combine shares
                    """);
            System.out.print("Enter choice: ");

            int mode = inScanner.nextInt();

            switch (mode) {
                case 0 -> {
                    System.out.print("Enter secret: ");
                    var secret = new BigInteger(inScanner.next());

                    System.out.print("Enter k (threshold): ");
                    var k = new BigInteger(inScanner.next());

                    System.out.print("Enter n (number of shares): ");
                    var n = new BigInteger(inScanner.next());

                    split(secret, k, n);
                }
                case 1 -> {
                    System.out.print("Enter k (threshold): ");
                    int k = inScanner.nextInt();

                    System.out.print("File with modulus and shares: ");
                    var modAndSharesFile = inScanner.next();

                    try (var inReader = new BufferedReader(new FileReader(modAndSharesFile))) {
                        List<String> lines = new ArrayList<>(inReader.lines().toList());

                        var mod = new BigInteger(lines.remove(0));

                        List<SSSS.Share> shares = new ArrayList<>();
                        var randomIndexes = ThreadLocalRandom.current().ints(0, lines.size()).distinct().iterator();

                        for (int i = 0; i < k; i++) {
                            String[] shareStrings = lines.get(randomIndexes.nextInt()).split(" ");
                            BigInteger x = new BigInteger(shareStrings[0]);
                            BigInteger y = new BigInteger(shareStrings[1]);

                            shares.add(new SSSS.Share(x, y));
                        }

                        combine(k, mod, shares);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

}