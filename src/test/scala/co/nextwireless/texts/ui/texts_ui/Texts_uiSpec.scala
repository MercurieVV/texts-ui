package co.nextwireless.texts.ui.texts_ui

import org.scalatest._
import org.scalajs.dom._
import outwatch.dom._
import outwatch.dom.dsl._
import monix.execution.Scheduler.Implicits.global

class Texts_uiSpec extends JSDomSpec {

  "You" should "probably add some tests" in {

    val message = "Hello World!"
    OutWatch.render("#app", h1(message)).unsafeRunSync()

    document.body.innerHTML.contains(message) shouldBe true
  }
}
