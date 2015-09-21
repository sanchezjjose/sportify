import javax.inject.Inject

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.Json
import play.api.{ Logger, Application, GlobalSettings }

import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json.collection.JSONCollection

class Global @Inject() (val reactiveMongoApi: ReactiveMongoApi) extends GlobalSettings {

  def collection = reactiveMongoApi.db.collection[JSONCollection]("posts")

  val posts = List(
    Json.obj(
      "text" -> "Have you heard about the Web Components revolution?",
      "username" -> "Eric",
      "avatar" -> "../images/avatar-01.svg",
      "favorite" -> false
    ),
    Json.obj(
      "text" -> "Loving this Polymer thing.",
      "username" -> "Rob",
      "avatar" -> "../images/avatar-02.svg",
      "favorite" -> false
    ),
    Json.obj(
      "text" -> "So last year...",
      "username" -> "Dimitri",
      "avatar" -> "../images/avatar-03.svg",
      "favorite" -> false
    ),
    Json.obj(
      "text" -> "Pretty sure I came up with that first.",
      "username" -> "Ada",
      "avatar" -> "../images/avatar-07.svg",
      "favorite" -> false
    ),
    Json.obj(
      "text" -> "Yo, I heard you like components, so I put a component in your component.",
      "username" -> "Grace",
      "avatar" -> "../images/avatar-08.svg",
      "favorite" -> false
    ),
    Json.obj(
      "text" -> "Centralize, centrailize.",
      "username" -> "John",
      "avatar" -> "../images/avatar-04.svg",
      "favorite" -> false
    ),
    Json.obj(
      "text" -> "Has anyone seen my cat?",
      "username" -> "Zelda",
      "avatar" -> "../images/avatar-06.svg",
      "favorite" -> false
    ),
    Json.obj(
      "text" -> "Decentralize!",
      "username" -> "Norbert",
      "avatar" -> "../images/avatar-05.svg",
      "favorite" -> false
    )
  )

  override def onStart(app: Application) {
    Logger.info("Application has started")

    collection.bulkInsert(posts.toStream, ordered = true).
      foreach(i => Logger.info("Database was initialized"))
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")

    collection.drop().onComplete {
      case _ => Logger.info("Database collection dropped")
    }
  }
}
