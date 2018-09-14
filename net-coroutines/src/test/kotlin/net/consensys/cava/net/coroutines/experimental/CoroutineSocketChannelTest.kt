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
package net.consensys.cava.net.coroutines.experimental

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8

internal class CoroutineSocketChannelTest {

  @Test
  fun shouldSuspendServerSocketChannelWhileAccepting() {
    val listenChannel = CoroutineServerSocketChannel.open()
    Assertions.assertNull(listenChannel.localAddress)
    assertEquals(0, listenChannel.localPort)

    listenChannel.bind(null)
    assertNotNull(listenChannel.localAddress)
    assertTrue(listenChannel.localPort > 0)
    val addr = InetSocketAddress(InetAddress.getLocalHost(), listenChannel.localPort)

    var didBlock = false
    val job = async {
      val serverChannel = listenChannel.accept()
      assertNotNull(serverChannel)
      assertTrue(didBlock)
    }

    Thread.sleep(100)
    didBlock = true

    val clientChannel = CoroutineSocketChannel.open()
    runBlocking {
      clientChannel.connect(addr)
      job.await()
    }
  }

  @Test
  fun shouldBlockSocketChannelWhileReading() {
    val listenChannel = CoroutineServerSocketChannel.open()
    listenChannel.bind(null)
    val addr = InetSocketAddress(InetAddress.getLocalHost(), (listenChannel.localAddress as InetSocketAddress).port)

    val serverJob = async {
      val serverChannel = listenChannel.accept()
      assertNotNull(serverChannel)

      assertTrue(serverChannel.isConnected)
      val dst = ByteBuffer.allocate(1024)
      serverChannel.read(dst)

      dst.flip()
      val chars = ByteArray(dst.limit())
      dst.get(chars, 0, dst.limit())
      assertEquals("testing123456", String(chars, UTF_8))

      serverChannel.write(ByteBuffer.wrap("654321abcdefg".toByteArray(UTF_8)))

      serverChannel.close()
    }

    val clientJob = async {
      val clientChannel = CoroutineSocketChannel.open()
      clientChannel.connect(addr)

      clientChannel.write(ByteBuffer.wrap("testing123456".toByteArray(UTF_8)))

      val dst = ByteBuffer.allocate(1024)
      clientChannel.read(dst)

      dst.flip()
      val chars = ByteArray(dst.limit())
      dst.get(chars, 0, dst.limit())
      assertEquals("654321abcdefg", String(chars, UTF_8))

      clientChannel.close()
    }

    runBlocking {
      serverJob.await()
      clientJob.await()
    }
  }

  @Test
  fun shouldCloseSocketChannelWhenRemoteClosed() {
    val listenChannel = CoroutineServerSocketChannel.open()
    listenChannel.bind(null)
    val addr = InetSocketAddress(InetAddress.getLocalHost(), (listenChannel.localAddress as InetSocketAddress).port)

    val serverJob = async {
      val serverChannel = listenChannel.accept()
      assertNotNull(serverChannel)
      assertTrue(serverChannel.isConnected)

      val dst = ByteBuffer.allocate(1024)
      serverChannel.read(dst)

      dst.flip()
      val chars = ByteArray(dst.limit())
      dst.get(chars, 0, dst.limit())
      assertEquals("testing123456", String(chars, UTF_8))

      serverChannel.close()
    }

    val clientJob = async {
      val clientChannel = CoroutineSocketChannel.open()
      clientChannel.connect(addr)

      clientChannel.write(ByteBuffer.wrap("testing123456".toByteArray(UTF_8)))

      val dst = ByteBuffer.allocate(1024)
      assertTrue(clientChannel.read(dst) < 0)

      clientChannel.close()
    }

    runBlocking {
      serverJob.await()
      clientJob.await()
    }
  }
}