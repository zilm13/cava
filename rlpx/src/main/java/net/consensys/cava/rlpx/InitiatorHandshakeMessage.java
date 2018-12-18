/*
 * Copyright 2018 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package net.consensys.cava.rlpx;

import net.consensys.cava.bytes.Bytes;
import net.consensys.cava.bytes.Bytes32;
import net.consensys.cava.crypto.SECP256K1;
import net.consensys.cava.crypto.SECP256K1.KeyPair;
import net.consensys.cava.crypto.SECP256K1.PublicKey;
import net.consensys.cava.crypto.SECP256K1.SecretKey;
import net.consensys.cava.crypto.SECP256K1.Signature;
import net.consensys.cava.rlp.RLP;

/**
 * The initial message sent during a RLPx handshake.
 */
final class InitiatorHandshakeMessage implements HandshakeMessage {

  static final int VERSION = 4;

  private final PublicKey publicKey;
  private final Signature signature;
  private final PublicKey ephemeralPublicKey;
  private final Bytes32 nonce;

  private InitiatorHandshakeMessage(
      PublicKey publicKey,
      Signature signature,
      PublicKey ephemeralPublicKey,
      Bytes32 nonce) {
    this.publicKey = publicKey;
    this.signature = signature;
    this.ephemeralPublicKey = ephemeralPublicKey;
    this.nonce = nonce;
  }

  static InitiatorHandshakeMessage create(
      PublicKey ourPubKey,
      KeyPair ephemeralKeyPair,
      Bytes32 staticSharedSecret,
      Bytes32 nonce) {
    Bytes toSign = staticSharedSecret.xor(nonce);
    return new InitiatorHandshakeMessage(
        ourPubKey,
        SECP256K1.sign(toSign, ephemeralKeyPair),
        ephemeralKeyPair.publicKey(),
        nonce);
  }

  static InitiatorHandshakeMessage decode(Bytes payload, SecretKey privateKey) {
    return RLP.decodeList(payload, reader -> {
      Signature signature = Signature.fromBytes(reader.readValue());
      PublicKey pubKey = PublicKey.fromBytes(reader.readValue());
      Bytes32 nonce = Bytes32.wrap(reader.readValue());
      Bytes32 staticSharedSecret = SECP256K1.calculateKeyAgreement(privateKey, pubKey);
      Bytes toSign = staticSharedSecret.xor(nonce);
      PublicKey ephemeralPublicKey = PublicKey.recoverFromSignature(toSign, signature);
      return new InitiatorHandshakeMessage(pubKey, signature, ephemeralPublicKey, nonce);
    });
  }

  Bytes encode() {
    return RLP.encodeList(writer -> {
      writer.writeValue(signature.bytes());
      writer.writeValue(publicKey.bytes());
      writer.writeValue(nonce);
      writer.writeInt(VERSION);
    });
  }

  PublicKey publicKey() {
    return publicKey;
  }

  @Override
  public PublicKey ephemeralPublicKey() {
    return ephemeralPublicKey;
  }

  @Override
  public Bytes32 nonce() {
    return nonce;
  }
}
