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
        NormalBasis a = NormalBasis.fromBinary("10000010111110000111101001100001000110001111011101010011111111110100001011000101101100000000000100010001001100010100001011110001110001010010110001011001111110011010100011010000011011010111100101010101110101110001000011011110010001111010101");
        NormalBasis b = NormalBasis.fromBinary("01010111000001010100100111001111000011010101011011000000110011011000101111000100100001100110111011110000000110110101111001000000010110100101011011001011101110011011111001110000011110010001011110110011001111011010100001101010001111010110001");
        NormalBasis n = NormalBasis.fromBinary("00100101010010110100001110010111001001101011011011110110110001111101110000000100011011100011011101001100110110001001111011100110101001110000101011100011011000101101110001011011111000111011100010111010001110001011110010111000011110000110010");

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
