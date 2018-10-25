package co.nextwireless.texts.ui.client.swagger.zapi

import cats.effect.IO
import cats.free.Free
import co.nextwireless.texts.ui.client.swagger.model.{Page, Texts}
import co.nextwireless.texts.ui.client.swagger.zapi.impl._
import hammock.Method.GET
import io.circe.generic.auto._
import hammock._
import co.nextwireless.texts.ui.client.swagger.zapi.impl._
import hammock.marshalling._
import hammock.js.Interpreter
import hammock.circe.implicits._
import hammock.js.Interpreter
import hammock.marshalling._
import io.circe.generic.auto._

object TextsWsApi {
  implicit val interpreter = Interpreter[IO]

  val response: IO[List[String]] = Hammock
    .request(Method.GET, uri"https://api.fidesmo.com/apps", Map()) // In the `request` method, you describe your HTTP request
    .as[List[String]]
    .exec[IO]

  def pages(applicationId: String): IO[List[Page]] = Hammock
    .request(GET, uri = uri"https://texts.internal.next-wireless.co/api/keys/application" / applicationId, Map())
    .as[List[Page]]
    .exec[IO]

  def texts(appId: String, pageId: PageId, textId: String): Free[HammockF, Texts] = Hammock
    .request(GET, uri = uri"https://texts.internal.next-wireless.co/api/texts" / s"$appId.$pageId.$textId", Map())
    .as[Texts]
//    .exec[IO]
}
class TextsWsApi {

}
