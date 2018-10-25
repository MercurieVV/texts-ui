package co.nextwireless.texts.ui.client.swagger.zapi.impl.types

import cats.Show
import co.nextwireless.texts.ui.client.swagger.model.Language
import eu.timepit.refined.api.Validate
//import eu.timepit.refined.api.Validate
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._
import io.circe.generic.semiauto._
import io.circe.refined.refinedEncoder
import io.circe.generic.encoding.DerivedObjectEncoder
//import org.http4s.QueryParamEncoder
//import org.http4s.QueryParamEncoder.fromShow
import shapeless.{Lazy, tag}
//import io.circe.java8.time._
import io.circe.Json
import io.circe.syntax._
import eu.timepit.refined.{refineMT, refineMV}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import io.circe.Encoder
import io.circe.refined._
import io.circe.generic.semiauto._
import io.circe.syntax._
import shapeless.tag.@@

/**
  * Created with IntelliJ IDEA.
  * User: Victor Mercurievv
  * Date: 10/17/2018
  * Time: 3:18 PM
  * Contacts: email: mercurievvss@gmail.com Skype: 'grobokopytoff' or 'mercurievv'
  */
trait TagConverters extends Tags {
  implicit val pageIdE: Encoder[PageId] = refinedEncoder[String, PageIdTag, @@]

  implicit val pageIdV: Validate[String, PageIdTag] = Validate.alwaysPassed("")

  implicit val pageIdD: Decoder[PageId] = refinedDecoder[String, PageIdTag, @@]
  implicit def languageFromString(i: String): Language = Language.withName(i)
  implicit def stringToApplicationId(s: String): ApplicationId = tag[ApplicationIdTag][String](s)

}
