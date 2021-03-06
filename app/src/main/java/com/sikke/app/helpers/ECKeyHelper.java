package com.sikke.app.helpers;

import android.support.annotation.Nullable;
import android.util.Log;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;


import org.spongycastle.asn1.sec.SECNamedCurves;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.asn1.x9.X9IntegerConverter;
import org.spongycastle.crypto.AsymmetricCipherKeyPair;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.generators.ECKeyPairGenerator;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECKeyGenerationParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.crypto.signers.ECDSASigner;
import org.spongycastle.crypto.signers.HMacDSAKCalculator;
import org.spongycastle.jce.ECNamedCurveTable;
import org.spongycastle.jce.spec.ECParameterSpec;
import org.spongycastle.math.ec.ECAlgorithms;
import org.spongycastle.math.ec.ECCurve;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.math.ec.FixedPointCombMultiplier;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;


public class ECKeyHelper implements Serializable {

    public static final ECDomainParameters CURVE ;
    public static final BigInteger HALF_CURVE_ORDER;
    private static final SecureRandom secureRandom;

    public static byte[] bigIntegerToBytes(BigInteger b, int numBytes) {
        if (b == null)
            return null;
        byte[] bytes = new byte[numBytes];
        byte[] biBytes = b.toByteArray();
        int start = (biBytes.length == numBytes + 1) ? 1 : 0;
        int length = Math.min(biBytes.length, numBytes);
        System.arraycopy(biBytes, start, bytes, numBytes - length, length);
        return bytes;
    }

    static {
        // All clients must agree on the curve to use by agreement. Ethereum uses secp256k1.
        X9ECParameters params = SECNamedCurves.getByName("secp256k1");
        CURVE = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
        HALF_CURVE_ORDER = params.getN().shiftRight(1);
        secureRandom = new SecureRandom();
    }

    //    // The two parts of the key. If "priv" is set, "pub" can always be calculated. If "pub" is set but not "priv", we
//    // can only verify signatures not make them.
//    // TODO: Redesign this class to use consistent internals and more efficient serialization.
    private BigInteger priv;
    protected ECPoint pub;

    //
//    // Transient because it's calculated on demand.
//    transient private byte[] pubKeyHash;
//
//    /**
//     * Generates an entirely new keypair. Point compression is used so the resulting public key will be 33 bytes
//     * (32 for the co-ordinate and 1 byte to represent the y bit).
//     */
    public ECKeyHelper() {
        this(secureRandom);
    }
    //
//    /**
//     * Generates an entirely new keypair with the given {@link SecureRandom} object. Point compression is used so the
//     * resulting public key will be 33 bytes (32 for the co-ordinate and 1 byte to represent the y bit).
//     */
    public ECKeyHelper(SecureRandom secureRandom) {
        ECKeyPairGenerator generator = new ECKeyPairGenerator();
        ECKeyGenerationParameters keygenParams = new ECKeyGenerationParameters(CURVE, secureRandom);
        generator.init(keygenParams);
        AsymmetricCipherKeyPair keypair = generator.generateKeyPair();
        ECPrivateKeyParameters privParams = (ECPrivateKeyParameters) keypair.getPrivate();
        ECPublicKeyParameters pubParams = (ECPublicKeyParameters) keypair.getPublic();
        priv = privParams.getD();
        pub = CURVE.getCurve().decodePoint(pubParams.getQ().getEncoded(true));
    }
    //
    protected ECKeyHelper(@Nullable BigInteger priv, ECPoint pub) {
        this.priv = priv;
        if(pub == null)
            throw new IllegalArgumentException("Public key may not be null");
        this.pub = pub;
    }

    public static ECPoint compressPoint(ECPoint uncompressed) {
        return CURVE.getCurve().decodePoint(uncompressed.getEncoded(true));
    }

    public static ECPoint decompressPoint(ECPoint compressed) {
        return CURVE.getCurve().decodePoint(compressed.getEncoded(false));
    }

    public static ECKeyHelper fromPrivate(BigInteger privKey) {
        return new ECKeyHelper(privKey, compressPoint(CURVE.getG().multiply(privKey)));
    }
    //
    public static ECKeyHelper fromPrivate(byte[] privKeyBytes) {
        return fromPrivate(new BigInteger(1, privKeyBytes));
    }

    public static ECKeyHelper fromPrivateAndPrecalculatedPublic(BigInteger priv, ECPoint pub) {
        return new ECKeyHelper(priv, pub);
    }

    public static ECKeyHelper fromPrivateAndPrecalculatedPublic(byte[] priv, byte[] pub) {
        check(priv != null, "Private key must not be null");
        check(pub != null, "Public key must not be null");
        return new ECKeyHelper(new BigInteger(1, priv), CURVE.getCurve().decodePoint(pub));
    }
    //
    public static ECKeyHelper fromPublicOnly(ECPoint pub) {
        return new ECKeyHelper(null, pub);
    }

    public static ECKeyHelper fromPublicOnly(byte[] pub) {
        return new ECKeyHelper(null, CURVE.getCurve().decodePoint(pub));
    }

    public ECKeyHelper decompress() {
        if (!pub.isCompressed())
            return this;
        else
            return new ECKeyHelper(priv, decompressPoint(pub));
    }

    public boolean isPubKeyOnly() {
        return priv == null;
    }

    public boolean hasPrivKey() {
        return priv != null;
    }

    public static byte[] publicKeyFromPrivate(BigInteger privKey, boolean compressed) {
        ECPoint point = CURVE.getG().multiply(privKey);
        return point.getEncoded(compressed);
    }

    public byte[] getPubKey() {
        return pub.getEncoded();
    }

    public ECPoint getPubKeyPoint() {
        return pub;
    }

    /**
     * Gets the private key in the form of an integer field element. The public key is derived by performing EC
     * point addition this number of times (i.e. point multiplying).
     *
     * @throws java.lang.IllegalStateException if the private key bytes are not available.
     */

    public BigInteger getPrivKey() {
        if (priv == null)
            try {
                throw new Exception();
            } catch (Exception e) {
                e.printStackTrace();
            }
        return priv;
    }

    public boolean isCompressed() {
        return pub.isCompressed();
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("pub:").append(Hex.toHexString(pub.getEncoded(false)));
        return b.toString();
    }

    public String toStringWithPrivate() {
        StringBuilder b = new StringBuilder();
        b.append(toString());
        if (priv != null) {
            b.append(" priv:").append(Hex.toHexString(priv.toByteArray()));
        }
        return b.toString();
    }

    public static class ECDSASignature {
        /** The two components of the signature. */
        public final BigInteger r, s;
        public byte v;

        /**
         * Constructs a signature with the given components. Does NOT automatically canonicalise the signature.
         */
        public ECDSASignature(BigInteger r, BigInteger s) {
            this.r = r;
            this.s = s;
        }

        private static ECDSASignature fromComponents(byte[] r, byte[] s) {
            return new ECDSASignature(new BigInteger(1, r), new BigInteger(1, s));
        }

        public static ECDSASignature fromComponents(byte[] r, byte[] s, byte v) {
            ECDSASignature signature = fromComponents(r, s);
            signature.v = v;
            return signature;
        }

        /**
         * Will automatically adjust the S component to be less than or equal to half the curve order, if necessary.
         * This is required because for every signature (r,s) the signature (r, -s (mod N)) is a valid signature of
         * the same message. However, we dislike the ability to modify the bits of a Ethereum transaction after it's
         * been signed, as that violates various assumed invariants. Thus in future only one of those forms will be
         * considered legal and the other will be banned.
         */
        public ECDSASignature toCanonicalised() {
            if (s.compareTo(HALF_CURVE_ORDER) > 0) {
                // The order of the curve is the number of valid points that exist on that curve. If S is in the upper
                // half of the number of valid points, then bring it back to the lower half. Otherwise, imagine that
                //    N = 10
                //    s = 8, so (-8 % 10 == 2) thus both (r, 8) and (r, 2) are valid solutions.
                //    10 - 8 == 2, giving us always the latter solution, which is canonical.
                return new ECDSASignature(r, CURVE.getN().subtract(s));
            } else {
                return this;
            }
        }

        public String toBase64() {
            byte[] sigData = new byte[65];  // 1 header + 32 bytes for R + 32 bytes for S
            sigData[0] = v;
            System.arraycopy(bigIntegerToBytes(this.r, 32), 0, sigData, 1, 32);
            System.arraycopy(bigIntegerToBytes(this.s, 32), 0, sigData, 33, 32);
            return new String(Base64.encode(sigData), Charset.forName("UTF-8"));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ECDSASignature signature = (ECDSASignature) o;

            if (!r.equals(signature.r)) return false;
            if (!s.equals(signature.s)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = r.hashCode();
            result = 31 * result + s.hashCode();
            return result;
        }
    }


    /**
     * Signs the given hash and returns the R and S components as BigIntegers
     * and put them in ECDSASignature
     *
     * @param input to sign
     * @return ECDSASignature signature that contains the R and S components
     */
    public ECDSASignature doSign(byte[] input) {
        // No decryption of private key required.
        if (priv == null)
            try {
                throw new Exception();
            } catch (Exception e) {
                e.printStackTrace();
            }
        check(priv != null, "Private key must not be null");
        ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
        ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(priv, CURVE);
        signer.init(true, privKey);
        BigInteger[] components = signer.generateSignature(input);
        return new ECDSASignature(components[0], components[1]).toCanonicalised();
    }

    /**
     * Takes the sha3 hash (32 bytes) of data and returns the ECDSA signature
     *
     * @throws IllegalStateException if this ECKeyHelper does not have the private part.
     */
    public ECDSASignature sign(byte[] messageHash) throws Exception {
        if (priv == null)
            throw new Exception();
        ECDSASignature sig = doSign(messageHash);
        // Now we have to work backwards to figure out the recId needed to recover the signature.
        int recId = -1;
        for (int i = 0; i < 4; i++) {
            ECKeyHelper k = ECKeyHelper.recoverFromSignature(i, sig, messageHash, isCompressed());
            if (k != null && k.pub.equals(pub)) {
                recId = i;
                break;
            }
        }
        if (recId == -1)
            throw new RuntimeException("Could not construct a recoverable key. This should never happen.");
        sig.v = (byte) (recId + 27 + (isCompressed() ? 4 : 0));
        return sig;
    }

    /**
     * Given a piece of text and a message signature encoded in base64, returns an ECKeyHelper
     * containing the public key that was used to sign it. This can then be compared to the expected public key to
     * determine if the signature was correct.
     *
     * @param messageHash a piece of human readable text that was signed
     * @param signatureBase64 The Ethereum-format message signature in base64
     * @throws SignatureException If the public key could not be recovered or if there was a signature format error.
     */
    public static ECKeyHelper signatureToKey(byte[] messageHash, String signatureBase64) throws SignatureException {
        byte[] signatureEncoded;
        try {
            signatureEncoded = Base64.decode(signatureBase64);
        } catch (RuntimeException e) {
            // This is what you get back from Bouncy Castle if base64 doesn't decode :(
            throw new SignatureException("Could not decode base64", e);
        }
        // Parse the signature bytes into r/s and the selector value.
        if (signatureEncoded.length < 65)
            throw new SignatureException("Signature truncated, expected 65 bytes and got " + signatureEncoded.length);
        int header = signatureEncoded[0] & 0xFF;
        // The header byte: 0x1B = first key with even y, 0x1C = first key with odd y,
        //                  0x1D = second key with even y, 0x1E = second key with odd y
        if (header < 27 || header > 34)
            throw new SignatureException("Header byte out of range: " + header);
        BigInteger r = new BigInteger(1, Arrays.copyOfRange(signatureEncoded, 1, 33));
        BigInteger s = new BigInteger(1, Arrays.copyOfRange(signatureEncoded, 33, 65));
        ECDSASignature sig = new ECDSASignature(r, s);
        boolean compressed = false;
        if (header >= 31) {
            compressed = true;
            header -= 4;
        }
        int recId = header - 27;
        ECKeyHelper key = ECKeyHelper.recoverFromSignature(recId, sig, messageHash, compressed);
        if (key == null)
            throw new SignatureException("Could not recover public key from signature");
        return key;
    }

    /**
     * <p>Verifies the given ECDSA signature against the message bytes using the public key bytes.</p>
     *
     * <p>When using native ECDSA verification, data must be 32 bytes, and no element may be
     * larger than 520 bytes.</p>
     *
     * @param data      Hash of the data to verify.
     * @param signature signature.
     * @param pub       The public key bytes to use.
     */
    public static boolean verify(byte[] data, ECDSASignature signature, byte[] pub) {
        ECDSASigner signer = new ECDSASigner();
        ECPublicKeyParameters params = new ECPublicKeyParameters(CURVE.getCurve().decodePoint(pub), CURVE);
        signer.init(false, params);
        try {
            return signer.verifySignature(data, signature.r, signature.s);
        } catch (NullPointerException npe) {
            // Bouncy Castle contains a bug that can cause NPEs given specially crafted signatures.
            // Those signatures are inherently invalid/attack sigs so we just fail them here rather than crash the thread.
            Log.v("Caught NPE inside bou", String.valueOf(npe));
            return false;
        }
    }

    /**
     * Verifies the given ASN.1 encoded ECDSA signature against a hash using the public key.
     *
     * @param data      Hash of the data to verify.
     * @param signature signature.
     * @param pub       The public key bytes to use.
     */
    public static boolean verify(byte[] data, byte[] signature, byte[] pub) {
        return verify(data, signature, pub);
    }

    /**
     * Verifies the given ASN.1 encoded ECDSA signature against a hash using the public key.
     *
     * @param data      Hash of the data to verify.
     * @param signature signature.
     */
    public boolean verify(byte[] data, byte[] signature) {
        return ECKeyHelper.verify(data, signature, getPubKey());
    }

    /**
     * Verifies the given R/S pair (signature) against a hash using the public key.
     */
    public boolean verify(byte[] sigHash, ECDSASignature signature) {
        return ECKeyHelper.verify(sigHash, signature, getPubKey());
    }

    /**
     * Returns true if this pubkey is canonical, i.e. the correct length taking into account compression.
     */
    public boolean isPubKeyCanonical() {
        return isPubKeyCanonical(pub.getEncoded());
    }

    /**
     * Returns true if the given pubkey is canonical, i.e. the correct length taking into account compression.
     */
    public static boolean isPubKeyCanonical(byte[] pubkey) {
        if (pubkey[0] == 0x04) {
            // Uncompressed pubkey
            if (pubkey.length != 65)
                return false;
        } else if (pubkey[0] == 0x02 || pubkey[0] == 0x03) {
            // Compressed pubkey
            if (pubkey.length != 33)
                return false;
        } else
            return false;
        return true;
    }

    /**
     * <p>Given the components of a signature and a selector value, recover and return the public key
     * that generated the signature according to the algorithm in SEC1v2 section 4.1.6.</p>
     *
     * <p>The recId is an index from 0 to 3 which indicates which of the 4 possible keys is the correct one. Because
     * the key recovery operation yields multiple potential keys, the correct key must either be stored alongside the
     * signature, or you must be willing to try each recId in turn until you find one that outputs the key you are
     * expecting.</p>
     *
     * <p>If this method returns null it means recovery was not possible and recId should be iterated.</p>
     *
     * <p>Given the above two points, a correct usage of this method is inside a for loop from 0 to 3, and if the
     * output is null OR a key that is not the one you expect, you try again with the next recId.</p>
     *
     * @param recId Which possible key to recover.
     * @param sig the R and S components of the signature, wrapped.
     * @param messageHash Hash of the data that was signed.
     * @param compressed Whether or not the original pubkey was compressed.
     * @return An ECKeyHelper containing only the public part, or null if recovery wasn't possible.
     */
    @Nullable
    public static ECKeyHelper recoverFromSignature(int recId, ECDSASignature sig, byte[] messageHash, boolean compressed) {
        check(recId >= 0, "recId must be positive");
        check(sig.r.signum() >= 0, "r must be positive");
        check(sig.s.signum() >= 0, "s must be positive");
        check(messageHash != null, "messageHash must not be null");
        // 1.0 For j from 0 to h   (h == recId here and the loop is outside this function)
        //   1.1 Let x = r + jn
        BigInteger n = CURVE.getN();  // Curve order.
        BigInteger i = BigInteger.valueOf((long) recId / 2);
        BigInteger x = sig.r.add(i.multiply(n));
        //   1.2. Convert the integer x to an octet string X of length mlen using the conversion routine
        //        specified in Section 2.3.7, where mlen = ⌈(log2 p)/8⌉ or mlen = ⌈m/8⌉.
        //   1.3. Convert the octet string (16 set binary digits)||X to an elliptic curve point R using the
        //        conversion routine specified in Section 2.3.4. If this conversion routine outputs “invalid”, then
        //        do another iteration of Step 1.
        //
        // More concisely, what these points mean is to use X as a compressed public key.
        ECCurve.Fp curve = (ECCurve.Fp) CURVE.getCurve();
        BigInteger prime = curve.getQ();  // Bouncy Castle is not consistent about the letter it uses for the prime.
        if (x.compareTo(prime) >= 0) {
            // Cannot have point co-ordinates larger than this as everything takes place modulo Q.
            return null;
        }
        // Compressed keys require you to know an extra bit of data about the y-coord as there are two possibilities.
        // So it's encoded in the recId.
        ECPoint R = decompressKey(x, (recId & 1) == 1);
        //   1.4. If nR != point at infinity, then do another iteration of Step 1 (callers responsibility).
        if (!R.multiply(n).isInfinity())
            return null;
        //   1.5. Compute e from M using Steps 2 and 3 of ECDSA signature verification.
        BigInteger e = new BigInteger(1, messageHash);
        //   1.6. For k from 1 to 2 do the following.   (loop is outside this function via iterating recId)
        //   1.6.1. Compute a candidate public key as:
        //               Q = mi(r) * (sR - eG)
        //
        // Where mi(x) is the modular multiplicative inverse. We transform this into the following:
        //               Q = (mi(r) * s ** R) + (mi(r) * -e ** G)
        // Where -e is the modular additive inverse of e, that is z such that z + e = 0 (mod n). In the above equation
        // ** is point multiplication and + is point addition (the EC group operator).
        //
        // We can find the additive inverse by subtracting e from zero then taking the mod. For example the additive
        // inverse of 3 modulo 11 is 8 because 3 + 8 mod 11 = 0, and -3 mod 11 = 8.
        BigInteger eInv = BigInteger.ZERO.subtract(e).mod(n);
        BigInteger rInv = sig.r.modInverse(n);
        BigInteger srInv = rInv.multiply(sig.s).mod(n);
        BigInteger eInvrInv = rInv.multiply(eInv).mod(n);
        ECPoint.Fp q = (ECPoint.Fp) ECAlgorithms.sumOfTwoMultiplies(CURVE.getG(), eInvrInv, R, srInv);
        return ECKeyHelper.fromPublicOnly(q.getEncoded(compressed));
    }

    /** Decompress a compressed public key (x co-ord and low-bit of y-coord). */
    private static ECPoint decompressKey(BigInteger xBN, boolean yBit) {
        X9IntegerConverter x9 = new X9IntegerConverter();
        byte[] compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(CURVE.getCurve()));
        compEnc[0] = (byte)(yBit ? 0x03 : 0x02);
        return CURVE.getCurve().decodePoint(compEnc);
    }

    /**
     * Returns a 32 byte array containing the private key, or null if the key is encrypted or public only
     */
    @Nullable
    public byte[] getPrivKeyBytes() {
        return bigIntegerToBytes(priv, 32);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof ECKeyHelper)) return false;

        ECKeyHelper ecKeyHelper = (ECKeyHelper) o;

        if (priv != null && !priv.equals(ecKeyHelper.priv)) return false;
        if (pub != null && !pub.equals(ecKeyHelper.pub)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        // Public keys are random already so we can just use a part of them as the hashcode. Read from the start to
        // avoid picking up the type code (compressed vs uncompressed) which is tacked on the end.
        byte[] bits = getPubKey();
        return (bits[0] & 0xFF) | ((bits[1] & 0xFF) << 8) | ((bits[2] & 0xFF) << 16) | ((bits[3] & 0xFF) << 24);
    }

    @SuppressWarnings("serial")
    public static class MissingPrivateKeyException extends RuntimeException {
    }


    private static void check(boolean test, String message) {
        if(!test) throw new IllegalArgumentException(message);
    }

    public static byte[] GenerateSignature(String plaintext, KeyPair keys) throws SignatureException, UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException {
        Signature ecdsaSign = Signature.getInstance("SHA256withECDSA", "BC");
        ecdsaSign.initSign(keys.getPrivate());
        ecdsaSign.update(plaintext.getBytes("UTF-8"));
        byte[] signature = ecdsaSign.sign();
        System.out.println(signature.toString());
        return signature;
    }

    public static boolean ValidateSignature(String plaintext, KeyPair pair, byte[] signature) throws SignatureException, InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchProviderException{
        Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA", "BC");
        ecdsaVerify.initVerify(pair.getPublic());
        ecdsaVerify.update(plaintext.getBytes("UTF-8"));
        return ecdsaVerify.verify(signature);
    }

    public static KeyPair GenerateKeys() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
//	Other named curves can be found in http://www.bouncycastle.org/wiki/display/JA1/Supported+Curves+%28ECDSA+and+ECGOST%29
        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("B-571");

        KeyPairGenerator g = KeyPairGenerator.getInstance("ECDSA", "BC");

        g.initialize(ecSpec, new SecureRandom());

        return g.generateKeyPair();
    }

    private static void writeToStream(byte[] stream, int start, BigInteger value, int size) {
        byte[] data = value.toByteArray();
        int length = Math.min(size, data.length);
        int writeStart = start + size - length;
        int readStart = data.length - length;
        System.arraycopy(data, readStart, stream, writeStart, length);
    }

    private static String checkHas64(String str) {
        String str2 = str;
        if (str2.length() % 2 == 1 && str2.length() < 64)
            str2 = "0" + str2;
        while (str2.length() < 64)
            str2 = "00" + str2;

        return str2;
    }


    private static ECPoint publicPointFromPrivate(BigInteger privKey) {
        /*
         * TODO: FixedPointCombMultiplier currently doesn't support scalars longer than the group
         * order, but that could change in future versions.
         */
        if (privKey.bitLength() > CURVE.getN().bitLength()) {
            privKey = privKey.mod(CURVE.getN());
        }
        return new FixedPointCombMultiplier().multiply(CURVE.getG(), privKey);
    }

    public static BigInteger publicKeyFromPrivate(BigInteger privKey) {
        ECPoint point = publicPointFromPrivate(privKey);

        byte[] encoded = point.getEncoded(false);
        return new BigInteger(1, Arrays.copyOfRange(encoded, 1, encoded.length));  // remove prefix
    }


}