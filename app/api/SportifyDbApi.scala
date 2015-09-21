package api

import play.modules.reactivemongo.ReactiveMongoApi


class SportifyDbApi(reactiveMongoApi: ReactiveMongoApi) {

  val userDb: UserDb = new UserMongoDb(reactiveMongoApi)
  
  
}
