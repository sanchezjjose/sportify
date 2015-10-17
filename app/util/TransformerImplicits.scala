package util

import scala.concurrent.Future

/**
 * Transforms Option[T] to a Future[T]
 */
class OptionT[T](opt: Option[T]) {
  
  def toFuture: Future[T] = opt match {
    case Some(t) => Future.successful(t)
    case None => Future.failed(new Exception("Option was None"))
  }
}

object TransformerImplicits {

  implicit def optionT[T](opt: Option[T]): OptionT[T] = new OptionT[T](opt)
}