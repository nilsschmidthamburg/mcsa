package mcsa.repositories

import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.Play.current
import mcsa.models.Graph
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import reactivemongo.core.commands.Count

/** A data access object for Graphs. */
object GraphRepository {
  
  /** The graphs collection */
  private def collection = ReactiveMongoPlugin.db.collection[JSONCollection]("graphs")

  /**
   * Saves a graph.
   *
   * @return The saved graph, once saved.
   */
  def save(graph: Graph): Future[Graph] = {
    collection.save(graph).map {
      case ok if ok.ok =>
        // TODO: Add Event support later (update UI with Web Sockets)
        // EventDao.publish("graph", graph)
        graph
      case error => throw new RuntimeException(error.message)
    }
  }

  /**
   * Find a graphs by its collection name and description.
   *
   * @param collection The collection name.
   * @param description The description.
   * @return The matching graph.
   */
  def find(collectionName: String, description: String): Future[Option[Graph]] = {
    collection.find(Json.obj("collection" -> collectionName, "description" -> description)).one[Graph]
  }
  
  /**
   * Find all the graphs.
   * @return All of the graphs.
   */
  def findAll(): Future[Seq[Graph]] = {
    collection.find(Json.obj())
      .sort(Json.obj("collection" -> -1))
      .cursor[Graph]
      .collect[Seq]()
  }

  /** The total number of graphs */
  def count: Future[Int] = {
    ReactiveMongoPlugin.db.command(Count(collection.name))
  }

}
