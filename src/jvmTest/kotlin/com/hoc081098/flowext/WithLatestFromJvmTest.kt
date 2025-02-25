/*
 * MIT License
 *
 * Copyright (c) 2021-2023 Petrus Nguyễn Thái Học
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.hoc081098.flowext

import com.hoc081098.flowext.utils.TestException
import com.hoc081098.flowext.utils.test
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking

@Ignore("Ignore JVM tests. Run only locally.")
@InternalCoroutinesApi
@ExperimentalCoroutinesApi
class WithLatestFromJvmTest {
  @Test
  fun basic() = runBlocking {
    val f1 = flowOf(1, 2, 3, 4)
    val f2 = flowOf("a", "b", "c", "d", "e")
    assertEquals(
      f2.withLatestFrom(f1).toList(),
      listOf(
        "a" to 4,
        "b" to 4,
        "c" to 4,
        "d" to 4,
        "e" to 4,
      ),
    )
  }

  @Test
  fun basicWithNull() = runBlocking {
    val f1 = flowOf(1, 2, 3, 4, null)
    val f2 = flowOf("a", "b", "c", "d", "e")
    assertEquals(
      f2.withLatestFrom(f1).toList(),
      listOf(
        "a" to null,
        "b" to null,
        "c" to null,
        "d" to null,
        "e" to null,
      ),
    )
  }

  @Test
  fun basic2() = runBlocking {
    val f1 = flowOf(1, 2, 3, 4).onEach { delay(300) }
    val f2 = flowOf("a", "b", "c", "d", "e").onEach { delay(100) }
    assertEquals(
      f2.withLatestFrom(f1).toList(),
      listOf(
        "c" to 1,
        "d" to 1,
        "e" to 1,
      ),
    )
  }

  @Test
  fun testWithLatestFrom_failureUpStream() = runBlocking {
    assertFailsWith<TestException> {
      flow<Int> { throw TestException() }
        .withLatestFrom(neverFlow())
        .collect()
    }

    assertFailsWith<TestException> {
      neverFlow()
        .withLatestFrom(flow<Int> { throw TestException() })
        .collect()
    }

    Unit
  }

  @Test
  fun testWithLatestFrom_cancellation() = runBlocking {
    assertFailsWith<CancellationException> {
      flow {
        emit(1)
        throw CancellationException("")
      }
        .withLatestFrom(emptyFlow<Nothing>())
        .collect()
    }

    flowOf(1)
      .withLatestFrom(
        flow {
          emit(2)
          throw CancellationException("")
        },
      )
      .test(
        listOf(
          Event.Value(1 to 2),
          Event.Complete,
        ),
      )
  }
}
