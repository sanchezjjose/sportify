package api

import play.modules.reactivemongo.ReactiveMongoApi


class SportifyDbApi(reactiveMongoApi: ReactiveMongoApi) {

  val gameDb: GameDb = new GameMongoDb(reactiveMongoApi)
  val emailMessageDb: EmailMessageDb = new EmailMessageMongoDb(reactiveMongoApi)
  val playerDb: PlayerDb = new PlayerMongoDb(reactiveMongoApi)
  val seasonDb: SeasonDb = new SeasonMongoDb(reactiveMongoApi)
  val teamDb: TeamDb = new TeamMongoDb(reactiveMongoApi)
  val userDb: UserDb = new UserMongoDb(reactiveMongoApi)
}
