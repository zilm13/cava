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
package net.consensys.cava.rlpx.wire;

/**
 * Identifier of a subprotocol, comprised of a name and version.
 */
public interface SubProtocolIdentifier {

  static SubProtocolIdentifier of(String name, String version) {
    return new DefaultSubProtocolIdentifier(name, version);
  }

  /**
   *
   * @return the name of the subprotocol
   */
  String name();

  /**
   *
   * @return the version of the subprotocol
   */
  String version();
}
