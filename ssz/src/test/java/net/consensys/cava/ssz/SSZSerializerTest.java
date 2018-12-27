package net.consensys.cava.ssz;

import net.consensys.cava.crypto.Hash;
import net.consensys.cava.ssz.fixtures.AttestationRecord;
import net.consensys.cava.ssz.fixtures.Bitfield;
import net.consensys.cava.ssz.fixtures.Sign;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Collections;

import static com.sun.org.apache.xerces.internal.impl.dv.util.HexBin.decode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SSZSerializerTest {

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
  void nullableTest() {
    Sign.Signature sig = new Sign.Signature();
    SecureRandom random = new SecureRandom();
    byte[] r = new byte[20];
    random.nextBytes(r);
    sig.r = new BigInteger(1, r);
    byte[] s = new byte[20];
    random.nextBytes(s);
    sig.s = new BigInteger(1, s);

    AttestationRecord expected1 = new AttestationRecord(
        12412L,
        123,
        Collections.emptyList(),
        Hash.sha2_256("aa".getBytes()),
        new Bitfield(decode("abcdef45")),
        12400L,
        Hash.sha2_256("aa".getBytes()),
        null
    );
    byte[] encoded1 = SSZSerializer.encode(expected1);
    AttestationRecord actual1 = (AttestationRecord) SSZSerializer.decode(encoded1, AttestationRecord.class);

    assertEquals(expected1, actual1);

    AttestationRecord expected2 = new AttestationRecord(
        12412L,
        123,
        Collections.emptyList(),
        Hash.sha2_256("aa".getBytes()),
        null,
        12400L,
        Hash.sha2_256("aa".getBytes()),
        sig
    );
    byte[] encoded2 = SSZSerializer.encode(expected2);
    AttestationRecord actual2 = (AttestationRecord) SSZSerializer.decode(encoded2, AttestationRecord.class);

    assertEquals(expected2, actual2);

    AttestationRecord expected3 = new AttestationRecord(
        12412L,
        123,
        Collections.emptyList(),
        Hash.sha2_256("aa".getBytes()),
        null,
        12400L,
        Hash.sha2_256("aa".getBytes()),
        null
    );
    byte[] encoded3 = SSZSerializer.encode(expected3);
    AttestationRecord actual3 = (AttestationRecord) SSZSerializer.decode(encoded3, AttestationRecord.class);

    assertEquals(expected3, actual3);
  }

  @Test
  void nullFixedSizeFieldTest() {
    Sign.Signature sig = new Sign.Signature();
    SecureRandom random = new SecureRandom();
    byte[] r = new byte[20];
    random.nextBytes(r);
    sig.r = new BigInteger(1, r);
    byte[] s = new byte[20];
    random.nextBytes(s);
    sig.s = new BigInteger(1, s);

    AttestationRecord expected3 = new AttestationRecord(
        12412L,
        123,
        Collections.emptyList(),
        null,
        new Bitfield(decode("abcdef45")),
        12400L,
        null,
        sig
    );
    Executable encodeClosure = () -> {
      SSZSerializer.encode(expected3);
    };
    assertThrows(NullPointerException.class, encodeClosure);
  }

  @Test
  void nullListTest() {
    Sign.Signature sig = new Sign.Signature();
    SecureRandom random = new SecureRandom();
    byte[] r = new byte[20];
    random.nextBytes(r);
    sig.r = new BigInteger(1, r);
    byte[] s = new byte[20];
    random.nextBytes(s);
    sig.s = new BigInteger(1, s);

    AttestationRecord expected4 = new AttestationRecord(
        12412L,
        123,
        null,
        Hash.sha2_256("aa".getBytes()),
        new Bitfield(decode("abcdef45")),
        12400L,
        Hash.sha2_256("aa".getBytes()),
        sig
    );
    Executable encodeClosure = () -> {
      SSZSerializer.encode(expected4);
    };

    assertThrows(NullPointerException.class, encodeClosure);
  }
}
