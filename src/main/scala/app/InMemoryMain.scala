package app

import cats.arrow.FunctionK
import domain._
import zio._
import zio.console._
import zio.interop.catz._

object InMemoryMain extends App {

  def run(args: List[String]): ZIO[Environment, Nothing, Int] =
    for {
      map          <- Ref.make(Map.empty[Int, Foo])
      counter      <- Ref.make(0)
      programUIO   = Program[UIO]
      appEnv       = createAppEnv(map, counter)
      result       <- programUIO.provideSome(appEnv)
      (res1, res2) = result
      failureMsg   = s"Failing result: ${res1.toString}"
      _            <- putStrLn(failureMsg)
      successMsg   = s"Successful result: ${res2.toString}"
      exitCode     <- putStrLn(successMsg).fold(_ => 1, _ => 0)
    } yield exitCode

  /**
    * Creates the whole object graph needed for the program to run.
    */
  private def createAppEnv(map: Ref[Map[Int, Foo]], counter: Ref[Int]) = {
    _: Environment =>
      new Program.Environment[UIO] {
        val transact = Transactor.InMemoryTransactor()
        val functionK = new FunctionK[UIO, UIO] {
          def apply[A](fa: UIO[A]): UIO[A] = fa
        }
        val fooRepository = FooRepository.InMemoryFooRepository(map, counter)
      }
  }
}
