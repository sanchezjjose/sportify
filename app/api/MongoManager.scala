package api

import play.modules.reactivemongo.ReactiveMongoApi


class MongoManager(reactiveMongoApi: ReactiveMongoApi) {

  val gameDb: GameDao = new GameMongoDao(reactiveMongoApi)
  val emailMessageDb: EmailMessageDao = new EmailMessageMongoDao(reactiveMongoApi)
  val playerDb: PlayerDao = new PlayerMongoDao(reactiveMongoApi)
  val seasonDb: SeasonDao = new SeasonMongoDao(reactiveMongoApi)
  val teamDb: TeamDao = new TeamMongoDao(reactiveMongoApi)
  val userDb: UserDao = new UserMongoDao(reactiveMongoApi)
}
