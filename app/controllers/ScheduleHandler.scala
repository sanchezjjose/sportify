package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import views._
import models._


object ScheduleHandler extends Controller {

  	/**
    * Handle form submission.
    */
  	def submit = Action { 
      Ok(html.schedule("Winter 2013 Season"))
  	}
}