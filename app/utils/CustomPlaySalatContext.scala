package utils

import com.novus.salat.Context
import play.api.Play

object CustomPlaySalatContext {

  implicit val ctx = new Context {
    val name = "Custom_Classloader"
  }
  ctx.registerClassLoader(Play.classloader(Play.current))
}
