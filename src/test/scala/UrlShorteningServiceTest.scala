import org.scalatest._

class UrlShorteningServiceSpec extends AsyncFunSpec {
  describe("Url shortening service") {
    val email = UserEmail("juanignaciosl@gmail.com")
    val url = Url("https://www.juanignaciosl.com")

    describe("on normal usage") {
      it("saves users from emails") {
        val service = new UrlShorteningService(new UserRepository, new ShortenedUrlRepository)
        service.save(email) map { user => assert(user.email == email) }
      }

      it("allows shortening URLs to saved users") {
        val service = new UrlShorteningService(new UserRepository, new ShortenedUrlRepository)
        val shortenedUrlFuture = service.save(email).flatMap { user => service.save(url, user.apiKey) }
        shortenedUrlFuture map { shortenedUrl => assert(shortenedUrl.userUrl == url) }
      }

      it("returns saved urls given the id") {
        val service = new UrlShorteningService(new UserRepository, new ShortenedUrlRepository)
        val shortenedUrlsFuture = for {
          user <- service.save(email)
          shortenedUrl <- service.save(url, user.apiKey)
          shortenedUrl2 <- service.fetch(shortenedUrl.id)
        } yield (shortenedUrl, shortenedUrl2)
        shortenedUrlsFuture map { shortenedUrls => assert(shortenedUrls._1 == shortenedUrls._2.get) }
      }
    }

    describe("on wrong usage") {
      it("raises an error if the same user is saved twice") {
        val service = new UrlShorteningService(new UserRepository, new ShortenedUrlRepository)
        recoverToSucceededIf[EmailAlreadyRegisteredException] {
          service.save(email) flatMap { _ => service.save(email) }
        }
      }

      it("raises an error if you try to shorten URLs with nonexisting API keys") {
        val service = new UrlShorteningService(new UserRepository, new ShortenedUrlRepository)
        recoverToSucceededIf[ApiKeyNotFoundException] {
          service.save(url, ApiKey("wadus"))
        }
      }
    }
  }
}
