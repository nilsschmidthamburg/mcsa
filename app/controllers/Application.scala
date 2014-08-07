package controllers

import play.api.libs.concurrent.Execution.Implicits._
import mcsa.models.Graph
import mcsa.repositories.GraphRepository
import play.api.Logger
import play.api.mvc._
import scala.util.Success
import scala.util.Failure
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Main Application object, that collects all Graphs from the database,
 * and then transforms these into a CollectionDataSet (ViewModel),
 * which can be represented as a Google Graph API diagram in the view.
 */
object Application extends Controller {

  def index = Action {
    Logger.debug("Serving index page...")
    val future = GraphRepository.findAll()

    val graphs = Await.result(future, 10.seconds)
    // Logger.debug("Found the following graphs: " + graphs)
    val model = graphs.map(asViewModel)
    // Logger.debug("Transformed into the following view model: " + model)

    val grouped = model.groupBy(_.collectionName).map(p => CollectionDataSet(p._1, p._2.flatMap(_.graphs).toList.sortBy(_.description))).toList

    // TODO Make "pure" and/or "aligned" graph versions
    // For aligned: collect all timestamps from all graphs (into a set) 
    // Check if an existing graph misses one of those timestamp values, and if so create a "0" value element for that timeslot

    Ok(views.html.index(grouped))
  }

  def asViewModel(graph: Graph): CollectionDataSet = {
    val rows = graph.rows
    val graphData = GraphData(graph.description, rows)
    CollectionDataSet(graph.collection, List(graphData))
  }
}

case class CollectionDataSet(collectionName: String, graphs: List[GraphData])

case class GraphData(description: String, rows: Seq[(String, Int)]) {
  val asJsArray = "[" + rows.map(row => "['" + row._1 + "'," + row._2 + "]").mkString(",") + "]"
}