package mcsa.repositories

import play.modules.reactivemongo.ReactiveMongoPlugin
import play.api.Play.current
import scala.concurrent.Future
import play.api.Play
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import reactivemongo.core.commands.CollStats

/** A data access object for collection metadata (CollStats). */
object CollectionRepository {

  /**
   * Returns collection stats for a given collection name.
   *
   * @return Collection stats containging ["Count", "StorageSize (mb)", "Size (mb)", "Avarage Object Size (bytes)", "Total Index Size (kb)"].
   */
  def getCollectionData(collection: String): Future[Map[String, Int]] = {
    val result = ReactiveMongoPlugin.db.command(new CollStats(collection))
    result map { csr =>
      Map(
        "Count" -> csr.count,
        "StorageSize (mb)" -> (csr.storageSize / 1048576).toInt,
        "Size (mb)" -> (csr.size / 1048576).toInt,
        "Avarage Object Size (bytes)" -> csr.averageObjectSize.map(_.toInt).getOrElse(0),
        "Total Index Size (kb)" -> csr.totalIndexSize / 1024)
    }
  }
}
