package api

import play.modules.reactivemongo.ReactiveMongoApi


class MongoManager(reactiveMongoApi: ReactiveMongoApi) {

  val games: GameDao = new GameMongoDao(reactiveMongoApi)
  val emailMessages: EmailMessageDao = new EmailMessageMongoDao(reactiveMongoApi)
  val players: PlayerDao = new PlayerMongoDao(reactiveMongoApi)
  val seasons: SeasonDao = new SeasonMongoDao(reactiveMongoApi)
  val teams: TeamDao = new TeamMongoDao(reactiveMongoApi)
  val users: UserDao = new UserMongoDao(reactiveMongoApi)
}
