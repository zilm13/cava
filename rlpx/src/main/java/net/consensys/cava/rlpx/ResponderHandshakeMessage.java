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
import net.consensys.cava.crypto.SECP256K1.PublicKey;
import net.consensys.cava.rlp.RLP;

/**
 * The decrypted contents of a handshake response message.
 */
final class ResponderHandshakeMessage implements HandshakeMessage {

  private final PublicKey ephemeralPublicKey;

  private final Bytes32 nonce;

  static ResponderHandshakeMessage create(PublicKey ephemeralPublicKey, Bytes32 nonce) {
    return new ResponderHandshakeMessage(ephemeralPublicKey, nonce);
  }

  static ResponderHandshakeMessage decode(Bytes payload) {
    return RLP.decodeList(
        payload,
        reader -> new ResponderHandshakeMessage(
            PublicKey.fromBytes(reader.readValue()),
            Bytes32.wrap(reader.readValue())));
  }

  private ResponderHandshakeMessage(PublicKey ephemeralPublicKey, Bytes32 nonce) {
    this.ephemeralPublicKey = ephemeralPublicKey;
    this.nonce = nonce;
  }

  /**
   * @return the ephemeral public key included in the response
   */
  @Override
  public PublicKey ephemeralPublicKey() {
    return ephemeralPublicKey;
  }

  /**
   * @return the response nonce
   */
  @Override
  public Bytes32 nonce() {
    return nonce;
  }

  Bytes encode() {
    return RLP.encodeList(writer -> {
      writer.writeValue(ephemeralPublicKey.bytes());
      writer.writeValue(nonce);
      writer.writeInt(InitiatorHandshakeMessage.VERSION);
    });
  }
}
