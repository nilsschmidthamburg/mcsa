package mcsa.models

import play.api.libs.json.Json
import reactivemongo.bson.BSONObjectID
import play.modules.reactivemongo.json.BSONFormats._

/**
 * A simple representation of a graph.
 *
 * @param _id The BSON object id of the message
 * @param collection The name of the collection this data set belongs to
 * @param description The description of this data set
 * @param dataPoints The actual data points
 */
case class Graph(_id: BSONObjectID, collection: String, description: String, dataPoints: String) {
  val rows = dataPoints.split(";").map(p => (p.split("=")(0), p.split("=")(1).toInt)).seq
  
  def append(time: String, value: Int) = {
    val spacer = if (dataPoints == "") "" else ";" 
    Graph(_id, collection, description, dataPoints + spacer + time + "=" + value)
    // TODO Delete old values, so the list doesn't grow infinite
  }
}

object Graph {
  
  /** Format for the graph. Used both by JSON library and reactive mongo to serialise/deserialise a Graph. */
  implicit val graphFormat = Json.format[Graph]
}