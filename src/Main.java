import java.util.Random;

public class Main {
    static final double ghz = 3.2;

    static NormalBasis randomGF() {
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(NormalBasis.m);
        for (int i = 0; i < NormalBasis.m; i++) {
            sb.append(rnd.nextBoolean() ? '1' : '0');
        }
        return NormalBasis.fromBinary(sb.toString());
    }

    static long cycles(double avgNs) {
        return (long) (avgNs * ghz);
    }

    public static void main(String[] args) throws Exception {
        NormalBasis a = NormalBasis.fromBinary("01101010011010001010001011111100100011000101010101111001011111110111010101110110001000010010000100111000101001110000010100111011100110110001110010110000000111000111011000010111100001010111000000011101011000010001101011100000111011000000000");
        NormalBasis b = NormalBasis.fromBinary("10010010011000010101011100101001111001110101000001001001001111001001000110010110111100010010010111101100110001111110100001100000010110001111000110001100001101001110100001111010111110110100101011001011010010010001011011110001101100001110100");
        NormalBasis n = NormalBasis.fromBinary("11101011000010100110001111011101110101101011101000100001000001111001000100011111110111101110101100011010001100110000111011110100000101011010101010101110100110000011101001000011011110111100111110110010101010111110001110011010110001110111110");

        System.out.println("A      = " + NormalBasis.toBinary(a));
        System.out.println("B      = " + NormalBasis.toBinary(b));
        System.out.println("N      = " + NormalBasis.toBinary(n));

        System.out.println("A + B  = " + NormalBasis.toBinary(a.add(b)));
        System.out.println("A * B  = " + NormalBasis.toBinary(a.mul(b)));
        System.out.println("A^2    = " + NormalBasis.toBinary(a.sq()));
        System.out.println("A^-1   = " + NormalBasis.toBinary(a.inverse()));
        System.out.println("A^N    = " + NormalBasis.toBinary(a.pow(n)));
        System.out.println("Tr(A)  = " + a.trace());


        int launchOp = 500;
        NormalBasis[] As = new NormalBasis[launchOp];
        NormalBasis[] Bs = new NormalBasis[launchOp];
        for (int i = 0; i < launchOp; i++) {
            As[i] = randomGF();
            Bs[i] = randomGF();
        }

        int warm = Math.min(2000, launchOp);
        for (int i = 0; i < warm; i++) {
            As[i].add(Bs[i]);
            As[i].mul(Bs[i]);
            As[i].sq();
            As[i].inverse();
            As[i].pow(n);
            As[i].trace();
        }
        System.gc();
        Thread.sleep(20);

        long t0, t1;
        t0 = System.nanoTime();
        for (int i = 0; i < launchOp; i++) As[i].add(Bs[i]);
        t1 = System.nanoTime();
        double addAvg = (t1 - t0) / (double) launchOp;

        t0 = System.nanoTime();
        for (int i = 0; i < launchOp; i++) As[i].mul(Bs[i]);
        t1 = System.nanoTime();
        double mulAvg = (t1 - t0) / (double) launchOp;

        t0 = System.nanoTime();
        for (int i = 0; i < launchOp; i++) As[i].sq();
        t1 = System.nanoTime();
        double sqAvg = (t1 - t0) / (double) launchOp;

        int heavy = Math.max(1, launchOp / 10);

        t0 = System.nanoTime();
        for (int i = 0; i < heavy; i++) As[i].inverse();
        t1 = System.nanoTime();
        double invAvg = (t1 - t0) / (double) heavy;

        t0 = System.nanoTime();
        for (int i = 0; i < heavy; i++) As[i].pow(n);
        t1 = System.nanoTime();
        double powAvg = (t1 - t0) / (double) heavy;

        t0 = System.nanoTime();
        for (int i = 0; i < launchOp; i++) As[i].trace();
        t1 = System.nanoTime();
        double trAvg = (t1 - t0) / (double) launchOp;

        System.out.println("\ntime measurements (avg ns):");
        System.out.printf("Add:     %.2f ns%n", addAvg);
        System.out.printf("Mul:     %.2f ns%n", mulAvg);
        System.out.printf("Square:  %.2f ns%n", sqAvg);
        System.out.printf("Inverse: %.2f ns%n", invAvg);
        System.out.printf("Power:   %.2f ns%n", powAvg);
        System.out.printf("Trace:   %.2f ns%n", trAvg);

        System.out.println("\ncycles per operation:");
        System.out.printf("Add:     %d cycles%n", cycles(addAvg));
        System.out.printf("Mul:     %d cycles%n", cycles(mulAvg));
        System.out.printf("Square:  %d cycles%n", cycles(sqAvg));
        System.out.printf("Inverse: %d cycles%n", cycles(invAvg));
        System.out.printf("Power:   %d cycles%n", cycles(powAvg));
        System.out.printf("Trace:   %d cycles%n", cycles(trAvg));
    }
}
