import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class EmailAlreadyRegisteredException extends RuntimeException

class ApiKeyNotFoundException extends RuntimeException

class UrlShorteningService(userRepository: UserRepository, shortenedUrlRepository: ShortenedUrlRepository) {
  def save(email: UserEmail)(implicit ec: ExecutionContext): Future[User] = {
    val user = User(email, ApiKey(Random.alphanumeric.take(10).mkString))
    val existingUserFuture = userRepository.byEmail(email)
    existingUserFuture.flatMap {
      case Some(_) => Future.failed(new EmailAlreadyRegisteredException)
      case None => userRepository.save(user).map { _ => user }
    }
  }

  def save(userUrl: Url, apiKey: ApiKey)(implicit ec: ExecutionContext): Future[ShortenedUrl] = {
    val shortenedUrl = ShortenedUrl(UrlId(Random.alphanumeric.take(3).mkString), userUrl)
    userRepository.byApiKey(apiKey).flatMap {
      case Some(_) => shortenedUrlRepository.save(shortenedUrl).map { _ => shortenedUrl }
      case None => Future.failed(new ApiKeyNotFoundException)
    }
  }

  def fetch(id: UrlId): Future[Option[ShortenedUrl]] = {
    shortenedUrlRepository.byId(id)
  }
}

case class ApiKey(apiKey: String) extends AnyVal

case class UserEmail(email: String) extends AnyVal

case class User(email: UserEmail, apiKey: ApiKey)

class UserRepository {
  private var usersByApiKey: Map[ApiKey, User] = Map()

  def byEmail(email: UserEmail): Future[Option[User]] = {
    Future.successful(usersByApiKey.values.find(_.email == email))
  }

  def byApiKey(apiKey: ApiKey): Future[Option[User]] = {
    Future.successful(usersByApiKey.get(apiKey))
  }

  def save(user: User): Future[Unit] = {
    usersByApiKey = usersByApiKey + (user.apiKey -> user)
    Future.successful()
  }
}

case class Url(url: String) extends AnyVal

case class UrlId(id: String) extends AnyVal

case class ShortenedUrl(id: UrlId, userUrl: Url)

class ShortenedUrlRepository {
  private var shortenedUrls: Map[UrlId, ShortenedUrl] = Map()

  def byId(id: UrlId): Future[Option[ShortenedUrl]] = Future.successful(shortenedUrls.get(id))

  def save(shortenedUrl: ShortenedUrl): Future[Unit] = {
    shortenedUrls = shortenedUrl match {
      case su: ShortenedUrl => shortenedUrls + (su.id -> su)
      case _ => shortenedUrls
    }
    Future.successful()
  }

}
