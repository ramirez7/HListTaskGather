Kick off a bunch of parallel Tasks and then combine the results by operating on the resulting HList. If any of the Tasks fails, the combined Task will fail fast, but the other Tasks' work will still run to completion and is not cancelled.

The code here is specialized to Task, but it could be made to work for all types with a scalaz.Nondeterminism instance with some work.

Example usage:

``` scala
scala> import scalaz._, Scalaz._
import scalaz._
import Scalaz._

scala> import shapeless._
import shapeless._

scala> import scalaz.concurrent._
import scalaz.concurrent._

scala> import TaskGather._
import TaskGather._

scala> import scala.concurrent.duration._
import scala.concurrent.duration._

scala> val t1 = Task.delay{println("hi"); 2}.after(2.seconds)
t1: scalaz.concurrent.Task[Int] = scalaz.concurrent.Task@7662f235

scala> val t2 = Task.now("hi")
t2: scalaz.concurrent.Task[String] = scalaz.concurrent.Task@4efcf128

scala> val t3: Task[Boolean] = Task.delay(throw new Exception("NUUUU"))
t3: scalaz.concurrent.Task[Boolean] = scalaz.concurrent.Task@df3edaf

scala> t1 :: t2 :: t3 :: HNil
res0: shapeless.::[scalaz.concurrent.Task[Int],shapeless.::[scalaz.concurrent.Task[String],shapeless.::[scalaz.concurrent.Task[Boolean],shapeless.HNil]]] = scalaz.concurrent.Task@7662f235 :: scalaz.concurrent.Task@4efcf128 :: scalaz.concurrent.Task@df3edaf :: HNil

scala> res0.gather.run
java.lang.Exception: NUUUU
  /* stack trace elided */

scala> hi // This is the println from t1. We don't cancel the other Tasks, so their effects still occur. However, we do fail fast.

scala> val tt1 = Task.delay(2).after(1.seconds)
tt1: scalaz.concurrent.Task[Int] = scalaz.concurrent.Task@4a6183e2

scala> val tt2 = Task.delay("hi").after(3.seconds)
tt2: scalaz.concurrent.Task[String] = scalaz.concurrent.Task@56096a1

scala> val tt3 = Task.delay(false).after(2.5.seconds)
tt3: scalaz.concurrent.Task[Boolean] = scalaz.concurrent.Task@2266c12

scala> tt1 :: tt2 :: tt3 :: HNil
res1: shapeless.::[scalaz.concurrent.Task[Int],shapeless.::[scalaz.concurrent.Task[String],shapeless.::[scalaz.concurrent.Task[Boolean],shapeless.HNil]]] = scalaz.concurrent.Task@4a6183e2 :: scalaz.concurrent.Task@56096a1 :: scalaz.concurrent.Task@2266c12 :: HNil

scala> res1.gather
res2: scalaz.concurrent.Task[shapeless.::[Int,shapeless.::[String,shapeless.::[Boolean,shapeless.HNil]]]] = scalaz.concurrent.Task@77b1b495
// Note how we have a Task[HList] now

scala> res1.gather.run
res3: shapeless.::[Int,shapeless.::[String,shapeless.::[Boolean,shapeless.HNil]]] = 2 :: hi :: false :: HNil
```