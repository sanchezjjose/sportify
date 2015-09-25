package api

import models.{Game, GameFields}
import org.joda.time.DateTime
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.{BSONDateTime, BSONDocument}

import scala.concurrent.{ExecutionContext, Future}


trait GameDb {

  // TODO: move to separate API
  def findNextGame(gameIds: Set[Long])(implicit ec: ExecutionContext): Future[Option[Game]]

  def findFutureGames()(implicit ec: ExecutionContext): Future[List[Game]]

  def findOne(query: BSONDocument)(implicit ec: ExecutionContext): Future[Option[Game]]

  def find(query: BSONDocument)(implicit ec: ExecutionContext): Future[List[Game]]

  def save(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]

  def update(selector: BSONDocument, update: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]

  def remove(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]
}

class GameMongoDb(reactiveMongoApi: ReactiveMongoApi) extends GameDb {

  // BSON-JSON conversions
  import play.modules.reactivemongo.json._
  import ImplicitBSONHandlers._

  protected def collection = reactiveMongoApi.db.collection[JSONCollection]("games")

  // TODO: move to separate API
  def findNextGame(gameIds: Set[Long])(implicit ec: ExecutionContext): Future[Option[Game]] = {
    findFutureGames().map { futureGames =>
      futureGames.sortBy(_.start_time).headOption
    }
  }

  override def findFutureGames()(implicit ec: ExecutionContext): Future[List[Game]] = {
    collection.find(
      GameFields.StartTime -> BSONDocument("$gt" -> BSONDateTime(DateTime.now().getMillis))
    ).cursor[Game].collect[List]()
  }

  override def findOne(query: BSONDocument)(implicit ec: ExecutionContext): Future[Option[Game]] = {
    collection.find(query).one[Game]
  }

  override def find(query: BSONDocument)(implicit ec: ExecutionContext): Future[List[Game]] = ???

  override def save(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] = ???

  override def update(selector: BSONDocument, update: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] = ???

  override def remove(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] = ???
}

/*

val gameDateFormat = DateTimeFormat.forPattern("E MM/dd/yyyy, H:mm a")

 def getNextGame(gameIds: Set[Long]): Option[Game] = {
   gameIds.flatMap(findById).filter { game =>
     DateTime.now().getMillis < gameDateFormat.parseDateTime(game.start_time).plusDays(1).getMillis
   }.toList.sortBy(g => gameDateFormat.parseDateTime(g.start_time).plusDays(1).getMillis).headOption
 }


 /*
  * MONGO API -- TODO: move to separate DB Trait
  */

 private val mongoManager = MongoManagerFactory.instance

 def findById(id: Long): Option[Game] = {
   val dbObject = mongoManager.games.findOne(MongoDBObject("_id" -> id))
   dbObject.map(o => grater[Game].asObject(o))
 }

 def create(game: Game): Unit = {
   val dbo = grater[Game].asDBObject(game)
   mongoManager.games += dbo
 }

 def remove(id: Long): Unit = {
   mongoManager.games.remove(MongoDBObject("_id" -> id))
 }

 def update(game: Game): Game = {
   val dbo = grater[Game].asDBObject(game)
   mongoManager.games.update(MongoDBObject("_id" -> game._id), dbo)
   game
 }


 */