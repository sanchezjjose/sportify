package api

import play.modules.reactivemongo.ReactiveMongoApi


class SportifyDbApi(reactiveMongoApi: ReactiveMongoApi) {

  val userDb: UserDb = new UserMongoDb(reactiveMongoApi)

  val teamDb: TeamDb = new TeamMongoDb(reactiveMongoApi)

  val seasonDb: SeasonDb = new SeasonMongoDb(reactiveMongoApi)

  val scheduleDb: ScheduleDb = new ScheduleMongoDb(reactiveMongoApi)

  val gameDb: GameDb = new GameMongoDb(reactiveMongoApi)
}
