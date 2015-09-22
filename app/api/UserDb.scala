package api

import java.util.concurrent.TimeUnit

import models.JsonUserFormats._
import models.User
import org.mindrot.jbcrypt.BCrypt
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONDocument

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}


trait UserDb {

  def findOne(query: BSONDocument)(implicit ec: ExecutionContext): Future[Option[User]]

  def find(query: BSONDocument)(implicit ec: ExecutionContext): Future[List[User]]

  def save(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]

  def update(selector: BSONDocument, update: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]

  def remove(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]
}

class UserMongoDb(reactiveMongoApi: ReactiveMongoApi) extends UserDb {

  // BSON-JSON conversions
  import play.modules.reactivemongo.json._, ImplicitBSONHandlers._

  protected def collection = reactiveMongoApi.db.collection[JSONCollection]("users")

  def authenticate(email: String, password: String): Option[User] = {
    Await.result(findOne(BSONDocument("email" -> email, "password" -> password)), Duration(10, TimeUnit.SECONDS))
      .filter(user => BCrypt.checkpw(password, user.password.get))
  }

  override def findOne(query: BSONDocument)(implicit ec: ExecutionContext): Future[Option[User]] = {
    collection.find(query).one[User]
  }

  override def find(query: BSONDocument)(implicit ec: ExecutionContext): Future[List[User]] = {
    collection.find(query).cursor[User].collect[List]()
  }

  override def save(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.save(document)
  }

  override def update(selector: BSONDocument, update: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.update(selector, update)
  }

  override def remove(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.remove(document)
  }
}
