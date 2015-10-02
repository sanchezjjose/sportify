package api

import models.Game
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.commands.WriteResult
import scala.concurrent.{ExecutionContext, Future}

trait GameDao {

  // TODO: move to separate API
  def findNextGame(gameIds: Set[Long])(implicit ec: ExecutionContext): Future[Option[Game]]

  // TODO: move to separate API
  def findFutureGames()(implicit ec: ExecutionContext): Future[List[Game]]

  def findOne(query: JsObject)(implicit ec: ExecutionContext): Future[Option[Game]]

  def find(query: JsObject)(implicit ec: ExecutionContext): Future[List[Game]]

  def insert(game: Game)(implicit ec: ExecutionContext): Future[WriteResult]

  def update(selector: JsObject, update: JsObject)(implicit ec: ExecutionContext): Future[WriteResult]

  def remove(document: JsObject)(implicit ec: ExecutionContext): Future[WriteResult]
}

class GameMongoDao(reactiveMongoApi: ReactiveMongoApi) extends GameDao {

  // BSON-JSON conversions
  import play.modules.reactivemongo.json._
  import models.JsonFormats._

  protected def collection = reactiveMongoApi.db.collection[JSONCollection]("games")

  override def findNextGame(gameIds: Set[Long])(implicit ec: ExecutionContext): Future[Option[Game]] = {
    findFutureGames().map { futureGames =>
      futureGames.sortBy(_.start_time).headOption
    }
  }

  override def findFutureGames()(implicit ec: ExecutionContext): Future[List[Game]] = {
    collection.find(Json.obj()).cursor[Game].collect[List]()
  }

  override def findOne(query: JsObject)(implicit ec: ExecutionContext): Future[Option[Game]] = {
    collection.find(query).one[Game]
  }

  override def find(query: JsObject)(implicit ec: ExecutionContext): Future[List[Game]] = {
    collection.find(query).cursor[Game].collect[List]()
  }

  override def insert(game: Game)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.insert(game)
  }

  override def update(selector: JsObject, update: JsObject)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.update(selector, update)
  }

  override def remove(document: JsObject)(implicit ec: ExecutionContext): Future[WriteResult] = {
    collection.remove(document)
  }
}
