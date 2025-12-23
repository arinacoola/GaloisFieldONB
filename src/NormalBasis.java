import java.util.Arrays;
public class NormalBasis {
    public static final int m = 239;
    public static final int elem = 4;
    public final long[] valEl = new long[elem];

    public NormalBasis() {}

    public static NormalBasis zero() {
        return new NormalBasis();
    }

    public static NormalBasis one() {
        NormalBasis res = new NormalBasis();
        Arrays.fill(res.valEl, ~0L);
        int resbits = m % 64;
        if (resbits != 0) {
            res.valEl[elem - 1] >>>= (64 - resbits);
        }
        return res;
    }

    public static NormalBasis copy(NormalBasis other) {
        NormalBasis res = new NormalBasis();
        System.arraycopy(other.valEl, 0, res.valEl, 0, elem);
        return res;
    }

    public int getBit(int ind) {
        return (int) ((valEl[ind / 64] >>> (ind % 64)) & 1L);
    }

    public void setBit(int ind) {
        valEl[ind / 64] ^= (1L << (ind % 64));
    }

    public NormalBasis add(NormalBasis other) {
        NormalBasis res = new NormalBasis();
        for (int i = 0; i < elem; i++) {
            res.valEl[i] = this.valEl[i] ^ other.valEl[i];
        }
        return res;
    }

    public NormalBasis andBit(NormalBasis other) {
        NormalBasis r = new NormalBasis();
        for (int i = 0; i < elem; i++) {
            r.valEl[i] = this.valEl[i] & other.valEl[i];
        }
        return r;
    }

    public NormalBasis leftCycleShift() {
        NormalBasis res = new NormalBasis();
        int right = m % 64;
        for (int i = 0; i < elem - 1; i++) {
            res.valEl[i] = (this.valEl[i] >>> 1) | (this.valEl[i + 1] << 63);
        }
        long tmp = this.valEl[0] & 1L;
        res.valEl[elem - 1] = (this.valEl[elem - 1] >>> 1) | (tmp << (right - 1));
        return res;
    }

    public NormalBasis rightCycleShift() {
        NormalBasis res = new NormalBasis();
        int right = m % 64;
        long tmp = (this.valEl[elem - 1] >>> (right - 1)) & 1L;
        res.valEl[0] = (this.valEl[0] << 1) | tmp;
        for (int i = 1; i < elem; i++) {
            res.valEl[i] = (this.valEl[i] << 1) | (this.valEl[i - 1] >>> 63);
        }
        res.valEl[elem - 1] &= ~(1L << right);
        return res;
    }

    public NormalBasis sq() {
        return rightCycleShift();
    }

    public int hammingWeight() {
        int weight = 0;
        for (int i = 0; i <= m / 64; i++) {
            weight += Long.bitCount(valEl[i]);
        }
        return weight;
    }

    public int trace() {
        return hammingWeight() & 1;
    }


    private static volatile NormalBasis[] MULT_MATRIX = null;
    public static NormalBasis[] calMatrix() {
        if (MULT_MATRIX != null) return MULT_MATRIX;
        NormalBasis[] mult = new NormalBasis[m];
        int p = m * 2 + 1;
        int[] powers = new int[m];
        boolean[][] tmp = new boolean[m][m];
        powers[0] = 1;
        for (int i = 1; i < m; i++) {
            powers[i] = (2 * powers[i - 1]) % p;
        }
        for (int i = 0; i < m; i++) {
            for (int j = i; j < m; j++) {
                boolean lambda1 = (powers[i] + powers[j]) % p == 1;
                boolean lambda2 = (powers[i] + powers[j]) % p == p - 1;
                boolean lambda3 = (powers[i] - powers[j]) % p == 1;
                boolean lambda4 = (powers[i] - powers[j]) % p == -1;
                if (lambda1 || lambda2 || lambda3 || lambda4) {
                    tmp[i][j] = true;
                    tmp[j][i] = true;
                }
            }
        }
        for (int i = 0; i < m; i++) {
            NormalBasis col = new NormalBasis();
            for (int j = 0; j < m; j++) {
                if (tmp[i][j]) col.setBit(j);
            }
            mult[i] = col;
        }
        MULT_MATRIX = mult;
        return mult;
    }

    public NormalBasis mul(NormalBasis other) {
        NormalBasis result = new NormalBasis();
        NormalBasis left = copy(this);
        NormalBasis right = copy(other);
        NormalBasis[] multMatrix = calMatrix();
        for (int i = 0; i < m; i++) {
            NormalBasis tmp = new NormalBasis();
            for (int j = 0; j < m; j++) {
                int bit = left.andBit(multMatrix[j]).hammingWeight() % 2;
                if (bit == 1) {
                    tmp.setBit(j);
                }
            }
            tmp = tmp.andBit(right);
            if ( (tmp.hammingWeight() & 1) == 1 ) {
                result.setBit(i);
            }
            left = left.leftCycleShift();
            right = right.leftCycleShift();
        }
        return result;
    }

    public NormalBasis pow(NormalBasis exp) {
        NormalBasis result = one();
        for (int i = 0; i < m; i++) {
            if (exp.getBit(i) == 1) {
                result = this.mul(result);
            }
            if (i != m - 1) {
                result = result.sq();
            }
        }
        return result;
    }

    public NormalBasis inverse() {
        NormalBasis a = copy(this);
        NormalBasis result = one();
        for (int i = 0; i < m - 1; i++) {
            a = a.sq();
            result = result.mul(a);
        }
        return result;
    }

    public static String toBinary(NormalBasis e) {
        StringBuilder sb = new StringBuilder(m);
        for (int i = 0; i < m; i++) {
            long bit = (e.valEl[i / 64] >>> (i % 64)) & 1L;
            sb.append(bit == 1 ? '1' : '0');
        }
        return sb.toString();
    }

    public static NormalBasis fromBinary(String bin) {
        NormalBasis res = new NormalBasis();
        for (int i = 0; i < bin.length(); i++) {
            char c = bin.charAt(i);
            if (c == '1') {
                res.valEl[i / 64] |= (1L << (i % 64));
            } else if (c != '0') {
                throw new IllegalArgumentException("bad char: " + c);
            }
        }
        return res;
    }


}
