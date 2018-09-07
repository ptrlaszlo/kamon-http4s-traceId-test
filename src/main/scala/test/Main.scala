package test

import java.util.Collections
import java.util.concurrent.{AbstractExecutorService, TimeUnit}

import cats.effect.IO
import fs2.StreamApp.ExitCode
import fs2.{Stream, StreamApp}
import kamon.executors.util.ContextAwareExecutorService
import kamon.http4s.middleware._
import org.http4s.{HttpService, Response, Status}
import org.http4s.dsl.io._
import org.http4s.server.{Router, ServiceErrorHandler}
import org.http4s.server.blaze.BlazeBuilder
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

object Main extends StreamApp[IO] {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(
    ContextAwareExecutorService(ExecutionContextExecutorServiceBridge(ExecutionContext.global))
  )

  override def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, ExitCode] = {
    val service: HttpService[IO] = HttpService {
      case GET -> Root / "ok" =>
        logger.info("test ok")
        Ok("OK")

      case GET -> Root / "iofail" =>
        logger.info("test iofail")
        IO.raiseError(new Exception("Fail with IO.raiseError"))

      case GET -> Root / "exception" =>
        logger.info("test exception")
        throw new Exception("Fail with throwing an exception")
    }

    val router = Router("/" -> service)
    BlazeBuilder[IO]
      .mountService(server.KamonSupport(router))
      .withServiceErrorHandler(errorHandler)
      .serve
  }

  val errorHandler: ServiceErrorHandler[IO] = request => {
    case e: Throwable =>
      logger.warn(s"Error ${e.getMessage}")
      IO(Response[IO](Status.InternalServerError))
  }
}

/**
 * In order for Kamon to propagate context over threads, we need to use the custom
 * [[kamon.executors.util.ContextAwareExecutorService]] as provided by the `kamon-executors` library.
 * However, to construct this class we need the underlying [[java.util.concurrent.ExecutorService]] which is not
 * straightforward to obtain from an [[scala.concurrent.ExecutionContext]].
 *
 * The solution used here is to transform the [[scala.concurrent.ExecutionContext]] into an
 * [[scala.concurrent.ExecutionContextExecutorService]], an interface that implements both. The solution was
 * mentioned in the scala user group by Victor Klang:
 * https://groups.google.com/forum/#!topic/scala-user/ZyHrfzD7eX8
 */
object ExecutionContextExecutorServiceBridge {
  def apply(ec: ExecutionContext): ExecutionContextExecutorService = ec match {
    case null => throw null
    case eces: ExecutionContextExecutorService => eces
    case other => new AbstractExecutorService with ExecutionContextExecutorService {
      override def prepare(): ExecutionContext = other
      override def isShutdown = false
      override def isTerminated = false
      override def shutdown() = ()
      override def shutdownNow() = Collections.emptyList[Runnable]
      override def execute(runnable: Runnable): Unit = other execute runnable
      override def reportFailure(t: Throwable): Unit = other reportFailure t
      override def awaitTermination(length: Long, unit: TimeUnit): Boolean = false
    }
  }
}
