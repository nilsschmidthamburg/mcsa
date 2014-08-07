package mcsa

import scala.concurrent.duration._
import play.api.Play.current
import akka.actor.Props
import play.api.GlobalSettings
import play.api.libs.concurrent.Akka
import play.api.GlobalSettings
import play.api.templates.Html
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import mcsa.actors.SnapShotActor
import mcsa.actors.SnapShot
import play.api.Play
import play.api.Logger

/**
 * Reads global configuration values and kicks of the SnapShotActor.
 */
object Global extends GlobalSettings {
  val errorMsg = "NONE (Please check your application.conf)"

  override def onStart(application: play.api.Application) {
    Logger.info("Initalizing data collection jobs...")

    val minutes = Play.current.configuration.getString("mcsa.snapshot.interval.minutes")
    Logger.info("Snapshot interval is set to " + minutes.map(_ + " minutes").getOrElse(errorMsg))

    val collections = Play.current.configuration.getStringSeq("mcsa.collections")
    Logger.info("Using following collections: " + collections.getOrElse(errorMsg))

    collections.map(c => {
      val actor = Akka.system.actorOf(Props(new SnapShotActor(c)))
      Akka.system.scheduler.schedule(0.seconds, 1.minutes, actor, SnapShot)
    })
  }
}
