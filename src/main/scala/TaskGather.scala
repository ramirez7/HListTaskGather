import scalaz.{Nondeterminism, Scalaz}, Scalaz._
import scalaz.concurrent.Task
import shapeless._

trait TaskGather[In <: HList] {
  type Out <: HList

  def apply(l: In): Task[Out]
}

object TaskGather {
  type Aux[In <: HList, Out0 <: HList] = TaskGather[In] { type Out = Out0 }

  implicit def hnilTaskGather: Aux[HNil, HNil] = 
    new TaskGather[HNil] {
      type Out = HNil
      def apply(l: HNil): Task[Out] = HNil.point[Task]
    }

  implicit def hconsTaskGather[InH, InT <: HList](implicit gt: TaskGather[InT]): Aux[Task[InH] :: InT, InH :: gt.Out] = 
    new TaskGather[Task[InH] :: InT] {
      type Out = InH :: gt.Out
      def apply(l: Task[InH] :: InT): Task[Out] = {
        val gatheredTail = gt.apply(l.tail)
        Nondeterminism[Task].mapBoth(l.head, gatheredTail){_ :: _}
      }
    }

  implicit final class TaskGatherOps[L <: HList](val l: L) extends AnyVal {
    def gather(implicit gt: TaskGather[L]): Task[gt.Out] = gt.apply(l)
  }
}
