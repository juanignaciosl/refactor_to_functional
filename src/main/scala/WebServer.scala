import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import spray.json.deserializationError
import spray.json.{JsString, JsValue, JsonFormat}

import scala.io.StdIn
import scala.concurrent.Future
import scala.util.{Failure, Success}

// curl -X POST -H "Content-Type: application/json" -d '{ "email": "juanignaciosl@gmail.com" }' http://localhost:8080/users
// curl -X POST -H "Content-Type: application/json" -d '{ "url": "https://www.google.com", "apiKey": "yyy" }' http://localhost:8080/urls
// curl http://localhost:8080/urls/wLP
// curl -v http://localhost:8080/u/wLP

object WebServer {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  private implicit val executionContext = system.dispatcher

  final case class ShortenedUrlRequest(url: String, apiKey: String)

  final case class CreateUserRequest(email: String)

  implicit object UrlIdFormat extends JsonFormat[UrlId] {
    def write(urlId: UrlId) = JsString(urlId.id)

    def read(json: JsValue) = json match {
      case JsString(id) => UrlId(id)
      case _ => deserializationError("String expected")
    }
  }

  implicit object UrlFormat extends JsonFormat[Url] {
    def write(url: Url) = JsString(url.url)

    def read(json: JsValue) = json match {
      case JsString(url) => Url(url)
      case _ => deserializationError("String expected")
    }
  }

  private implicit val shortenedUrlRequestFormat = jsonFormat2(ShortenedUrlRequest)
  private implicit val shortenedUrlFormat = jsonFormat2(ShortenedUrl)
  private implicit val createUserFormat = jsonFormat1(CreateUserRequest)

  private val urlShorteningService = new UrlShorteningService(new UserRepository, new ShortenedUrlRepository)

  def main(args: Array[String]) {
    val route: Route = getShortenedUrl ~ createShortenedUrl ~ goToShortenedUrl ~ createUser

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture.flatMap(_.unbind()).onComplete(_ ⇒ system.terminate())
  }

  private def goToShortenedUrl = get {
    pathPrefix("u" / Segment) { id =>
      val maybeShortenedUrl: Future[Option[ShortenedUrl]] = urlShorteningService.fetch(UrlId(id))

      onSuccess(maybeShortenedUrl) {
        case Some(shortenedUrl) => redirect(shortenedUrl.userUrl.url, StatusCodes.TemporaryRedirect)
        case None => complete(StatusCodes.NotFound)
      }
    }
  }

  private def getShortenedUrl = get {
    pathPrefix("urls" / Segment) { id =>
      val maybeShortenedUrl: Future[Option[ShortenedUrl]] = urlShorteningService.fetch(UrlId(id))

      onSuccess(urlShorteningService.fetch(UrlId(id))) {
        case Some(shortenedUrl) => complete(shortenedUrl)
        case None => complete(StatusCodes.NotFound)
      }
    }
  }

  private def createShortenedUrl = post {
    path("urls") {
      entity(as[ShortenedUrlRequest]) { shortenedUrlRequest =>
        val saved = urlShorteningService.save(Url(shortenedUrlRequest.url), ApiKey(shortenedUrlRequest.apiKey))
        onComplete(saved) {
          case Success(shortenedUrl) => complete(s"shortened url created. Id: ${shortenedUrl.id.id}")
          case Failure(_: ApiKeyNotFoundException) => complete(StatusCodes.Unauthorized)
          case Failure(e) => failWith(e)
        }
      }
    }
  }

  private def createUser = post {
    path("users") {
      entity(as[CreateUserRequest]) { createUserRequest =>
        val saved = urlShorteningService.save(UserEmail(createUserRequest.email))
        onComplete(saved) {
          case Success(user) => complete(s"user created. Api key: ${user.apiKey.apiKey}")
          case Failure(e) => failWith(e)
        }
      }
    }
  }
}