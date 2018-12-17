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
package net.consensys.cava.ssz.experimental

import net.consensys.cava.bytes.Bytes
import net.consensys.cava.units.bigints.UInt256
import java.math.BigInteger

@ExperimentalUnsignedTypes
internal class BytesSSZWriter(private val delegate: net.consensys.cava.ssz.SSZWriter) : SSZWriter {
  override fun writeSSZ(value: Bytes) =
    delegate.writeSSZ(value)

  override fun writeBytes(value: Bytes) =
    delegate.writeBytes(value)

  override fun writeBytes(value: ByteArray) =
    delegate.writeBytes(value)

  override fun writeString(str: String) =
    delegate.writeString(str)

  override fun writeInt(value: Int, bitLength: Int) =
    delegate.writeInt(value, bitLength)

  override fun writeLong(value: Long, bitLength: Int) =
    delegate.writeLong(value, bitLength)

  override fun writeUInt(value: UInt, bitLength: Int) =
    delegate.writeUInt(value.toInt(), bitLength)

  override fun writeULong(value: ULong, bitLength: Int) =
    delegate.writeULong(value.toLong(), bitLength)

  override fun writeBytesList(vararg elements: Bytes) =
    delegate.writeBytesList(*elements)

  override fun writeStringList(vararg elements: String) =
    delegate.writeStringList(*elements)

  override fun writeIntList(bitLength: Int, vararg elements: Int) =
    delegate.writeIntList(bitLength, *elements)

  override fun writeLongIntList(bitLength: Int, vararg elements: Long) =
    delegate.writeLongIntList(bitLength, *elements)

  override fun writeBigIntegerList(bitLength: Int, vararg elements: BigInteger) =
    delegate.writeBigIntegerList(bitLength, *elements)

  override fun writeUIntList(bitLength: Int, vararg elements: UInt) =
    delegate.writeUIntList(bitLength, *(elements.map { i -> i.toInt() }.toIntArray()))

  override fun writeULongIntList(bitLength: Int, vararg elements: ULong) =
    delegate.writeULongIntList(bitLength, *(elements.map { i -> i.toLong() }.toLongArray()))

  override fun writeUInt256List(vararg elements: UInt256) =
    delegate.writeUInt256List(*elements)

  override fun writeHashList(vararg elements: Bytes) =
    delegate.writeHashList(*elements)

  override fun writeAddressList(vararg elements: Bytes) =
    delegate.writeAddressList(*elements)

  override fun writeBooleanList(vararg elements: Boolean) =
    delegate.writeBooleanList(*elements)
}
