package mcsa.actors

import java.text.SimpleDateFormat
import java.util.Calendar

import akka.actor.Actor
import mcsa.models.Graph
import mcsa.repositories.CollectionRepository
import mcsa.repositories.GraphRepository
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import reactivemongo.bson.BSONObjectID

/**
 * SnapShotActor takes SnapShots for a set of configured collections. It can only handle one type of message, namely SnapShot.
 * Whenever a SnapShot Message is received, the Actor retrieves the current time and current CollectionData for each configured collection.
 * Using the CollectionData and timestamp, either a new Graph is created, or an existing graph is updated.
 */
class SnapShotActor(collectionNames: Seq[String]) extends Actor {
  val format = new SimpleDateFormat("dd-MM-YY HH:mm")

  // TODO Remove old snapshots, so the data doesnt grow to large
  
  def receive = {
    case SnapShot => {
      val now = format.format(Calendar.getInstance().getTime())

      collectionNames map { collectionName =>
        val snapshots = CollectionRepository.getCollectionData(collectionName)
        snapshots.map { elem =>
          elem.foreach(updateGraph)

          def updateGraph(pair: (String, Int)) = {
            val (description, value) = pair
            val graph = GraphRepository.find(collectionName, description)
            graph.map(graphResult => createOrUpdateGraph(graphResult, value))

            def createOrUpdateGraph(o: Option[Graph], value: Int) = o match {
              case Some(g) => {
                Logger.debug("Snapshot Taken for collection " + collectionName + ", " + description + " value was " + value);
                GraphRepository.save(g.append(now, value))
              }
              case None => {
                Logger.info("Initialized collection " + collectionName + " value " + description);
                GraphRepository.save(Graph(BSONObjectID.generate, collectionName, description, now + "=" + value))
              }
            }
          }
        }
      }
    }
  }
}

case object SnapShot
