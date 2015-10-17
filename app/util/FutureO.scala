package util

import scala.concurrent.{Future, ExecutionContext}

// http://www.edofic.com/posts/2014-03-07-practical-future-option.html
case class FutureO[+A](future: Future[Option[A]]) extends AnyVal {

  def flatMap[B](f: A => FutureO[B])(implicit ec: ExecutionContext): FutureO[B] = {
    FutureO {
      future.flatMap { optA =>
        optA.map { a =>
          f(a).future
        } getOrElse Future.successful(None)
      }
    }
  }

  def map[B](f: A => B)(implicit ec: ExecutionContext): FutureO[B] = {
    FutureO(future.map(_ map f))
  }
}

object FutureO {

  // wraps a Future[A] into a FutureO[A]
  implicit def liftFO[A](fut: Future[A])(implicit ec: ExecutionContext): FutureO[A] = {

    // convert Future[A] to Future[Option[A]] to conform to FutureO requirements
    val futureOpt: Future[Option[A]] = fut.map(Some(_))
    FutureO(futureOpt)
  }
}
