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
package net.consensys.cava.crypto.sodium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import net.consensys.cava.bytes.Bytes;
import net.consensys.cava.crypto.sodium.KeyDerivation.MasterKey;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class KeyDerivationTest {

  @BeforeAll
  static void checkAvailable() {
    assumeTrue(Sodium.isAvailable(), "Sodium native library is not available");
    assumeTrue(KeyDerivation.isAvailable(), "KeyDerivation support is not available (requires >= 10.0.12");
  }

  @Test
  void differentIdsShouldGenerateDifferentKeys() {
    MasterKey masterKey = MasterKey.random();

    Bytes subKey1 = masterKey.deriveKey(40, 1, "abcdefg");
    assertEquals(subKey1, masterKey.deriveKey(40, 1, "abcdefg"));

    assertNotEquals(subKey1, masterKey.deriveKey(40, 2, "abcdefg"));
    assertNotEquals(subKey1, masterKey.deriveKey(40, 1, new byte[KeyDerivation.contextLength()]));
  }
}
