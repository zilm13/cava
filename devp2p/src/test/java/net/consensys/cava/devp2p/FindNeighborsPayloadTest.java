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
package net.consensys.cava.devp2p;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.consensys.cava.bytes.Bytes;

import org.junit.jupiter.api.Test;

class FindNeighborsPayloadTest {

  @Test
  void testToBytesAndBack() {
    FindNeighborsPayload payload = new FindNeighborsPayload(Bytes.of(1, 2, 3), 20L);
    FindNeighborsPayload read = FindNeighborsPayload.decode(payload.createPayloadBytes());
    assertEquals(payload, read);
  }
}
