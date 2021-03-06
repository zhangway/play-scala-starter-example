package controllers


import javax.inject._
import play.api.Logger
import play.api.i18n._
import play.api.libs.functional.syntax.unlift
import play.api.libs.json.{JsPath, Json, Writes}
import play.api.mvc._
import play.api.libs.functional.syntax._

import scala.io.Source

case class InitNode(
  fish: String,
  lice: String,
  bicor: String
)


case class Node(
  id: String,
  label: String
)

case class Edge(
  id: String,
  source: String,
  target: String,
  bicor: Double
)




/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(
  cc: ControllerComponents)
  extends AbstractController(cc)
  with I18nSupport{


  implicit val nodeWrites: Writes[Node] =
    ((JsPath \ "id").write[String] and
      (JsPath \ "label").write[String]) (unlift(Node.unapply))


  implicit val edgeWrites: Writes[Edge] =
    ((JsPath \ "id").write[String] and
      (JsPath \ "source").write[String] and
      (JsPath \ "target").write[String] and
      (JsPath \ "bicor").write[Double]) (unlift(Edge.unapply))

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action { implicit request =>
    //Ok(views.html.index("Your new application is ready."))
//    val messages: Messages = request.messages
//    val message: String = messages("info.error")
    val lines = Source.fromFile("globalNetwk.txt").getLines().drop(1)

    val nodes = lines.map ( line => {
      val items = line.split("\\s+")
      val fish = items(0)
      val lice = items(1)
      val bicor = items(2)
      InitNode(fish, lice, bicor)
    }).toList




    val fishes = nodes.map(_.fish)
    Logger.debug(s"original fish count:${fishes.size}")

    val uniqueFishes = fishes.distinct

    Logger.debug(s"distinct fish count:${uniqueFishes.size}")

    val edges = uniqueFishes.flatMap(fish => {
      val mappings = nodes.filter(node => node.fish.equals(fish)).distinct
      mappings
      //mappings.foreach(l => Logger.logger.debug(s"edge: ${l.fish} -- ${l.lice}"))

    })

    val lice = nodes.map(_.lice)

    val uniqueLice = lice.distinct

    Logger.debug(s"original lice count:${lice.size}, distinct lice count:${uniqueLice.size}")

    val bicor = nodes.map(_.bicor)

    val uniqueBicor = bicor.distinct

    Logger.debug(s"original bicor count:${bicor.size}, distinct bicor count:${uniqueBicor.size}")

    Ok(views.html.cyto("Cyto"))
  }


  def cyto2 = Action { implicit request =>




    val a = Node("a", "labelA")
    val b = Node("b", "labelB")
    val c = Node("c", "labelC")
    val d = Node("d", "labelD")
    val e = Node("e", "labelE")
    val f = Node("f", "labelF")

    val nodes = Seq(a, b, c, d, e, f)

    val edges = Seq(
      Edge("ab", "a", "b", 10),
      Edge("cd", "c", "d", 20),
      Edge("ef", "e", "f", 5),
      Edge("ac", "a", "c", 50),
      Edge("be", "b", "e", 44)
    )

    val nodesJson = nodes.map(node => {
      Json.obj("data" -> Json.toJson(node))
    })

    val edgesJson = edges.map(edge => {
      Json.obj("data" -> Json.toJson(edge))
    })




    val response = Json.obj(
      "nodes" -> nodesJson,
      "edges" -> edgesJson
    )

    Logger.logger.debug(s"response:${response}")


    Ok(response)
  }

  def cyto = Action { implicit  request =>
    val lines = Source.fromFile("globalNetwk.txt").getLines().drop(1)

    val nodes = lines.map ( line => {
      val items = line.split("\\s+")
      val fish = items(0)
      val lice = items(1)
      val bicor = items(2)
      InitNode(fish, lice, bicor)
    }).toList

    val fishes = nodes.map(_.fish)
    Logger.debug(s"original fish count:${fishes.size}")

    val uniqueFishes = fishes.distinct

    Logger.debug(s"distinct fish count:${uniqueFishes.size}")

    val edges = uniqueFishes.flatMap(fish => {
      val mappings = nodes.filter(node => node.fish.equals(fish)).distinct
      mappings
      //mappings.foreach(l => Logger.logger.debug(s"edge: ${l.fish} -- ${l.lice}"))

    })

    val lice = nodes.map(_.lice)

    val uniqueLice = lice.distinct

    Logger.debug(s"original lice count:${lice.size}, distinct lice count:${uniqueLice.size}")

    val maximum = nodes.map(_.bicor.toDouble).max
    val minimum = nodes.map(_.bicor.toDouble).min

    Logger.logger.debug(s"maximum:$maximum, min:$minimum")

    val cyNodes = (uniqueFishes ::: uniqueLice).map(s => Node(s, s))

    Logger.logger.debug(s"nodes count:${cyNodes.size}")

    val cyEdges = edges.map(edge => {

      Edge(edge.fish+edge.lice, edge.fish, edge.lice, edge.bicor.toDouble)
    })

    val nodesJson = cyNodes.map(node => {
      Json.obj("data" -> Json.toJson(node))
    })

    val edgesJson = cyEdges.map(edge => {
      Json.obj("data" -> Json.toJson(edge))
    })
    val response = Json.obj(
      "nodes" -> nodesJson,
      "edges" -> edgesJson
    )
    Ok(response)
  }



}
