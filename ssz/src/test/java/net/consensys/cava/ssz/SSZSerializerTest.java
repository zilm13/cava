package net.consensys.cava.ssz;

import net.consensys.cava.crypto.Hash;
import net.consensys.cava.ssz.fixtures.AttestationRecord;
import net.consensys.cava.ssz.fixtures.Bitfield;
import net.consensys.cava.ssz.fixtures.Sign;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Collections;

import static com.sun.org.apache.xerces.internal.impl.dv.util.HexBin.decode;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SSZSerializerTest {

    @Test
    void simpleTest() {
        Sign.Signature sig = new Sign.Signature();
        SecureRandom random = new SecureRandom();
        byte[] r = new byte[20];
        random.nextBytes(r);
        sig.r = new BigInteger(1, r);
        byte[] s = new byte[20];
        random.nextBytes(s);
        sig.s = new BigInteger(1, s);

        AttestationRecord expected = new AttestationRecord(
                12412L,
                123,
                Collections.emptyList(),
                Hash.sha2_256("aa".getBytes()),
                new Bitfield(decode("abcdef45")),
                12400L,
                Hash.sha2_256("bb".getBytes()),
                sig
        );

        byte[] encoded = SSZSerializer.encode(expected);
        AttestationRecord constructed = (AttestationRecord) SSZSerializer.decode(encoded, AttestationRecord.class);

        assertEquals(expected, constructed);
    }

    @Test
    void bitfieldTest() {
        Bitfield expected = new Bitfield(
                decode("abcd")
        );

        byte[] encoded = SSZSerializer.encode(expected);
        Bitfield constructed = (Bitfield) SSZSerializer.decode(encoded, Bitfield.class);

        assertEquals(expected, constructed);
    }

    @Test
    void SignatureTest() {
        Sign.Signature signature = new Sign.Signature();
        signature.r = new BigInteger("23452342342342342342342315643768758756967967");
        signature.s = new BigInteger("8713785871");

        byte[] encoded = SSZSerializer.encode(signature);
        Sign.Signature constructed = (Sign.Signature) SSZSerializer.decode(encoded, Sign.Signature.class);

        assertEquals(signature, constructed);
    }
}
