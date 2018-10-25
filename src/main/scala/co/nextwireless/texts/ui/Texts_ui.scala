package co.nextwireless.texts.ui

import cats.effect.IO
import cats.free.Free
import co.nextwireless.texts.ui.client.swagger.model.{Page, Texts}
import co.nextwireless.texts.ui.client.swagger.zapi.TextsWsApi
import co.nextwireless.texts.ui.client.swagger.zapi.impl._
import co.nextwireless.texts.ui.client.swagger.model._
import hammock.HammockF
import hammock.js.Interpreter
import outwatch.dom.dsl.{child, children}
import outwatch.dom.{Handler, Observable, OutWatch, Sink, dsl}
import monix.execution.Scheduler.Implicits.global

/**
  * Created with IntelliJ IDEA.
  * User: Victor Mercurievv
  * Date: 10/25/2018
  * Time: 2:52 PM
  * Contacts: email: mercurievvss@gmail.com Skype: 'grobokopytoff' or 'mercurievv'
  */
object Texts_ui {
  def main(args: Array[String]): Unit = {

    val flow: IO[Unit] = for {
      onMenuItemClick <- Handler.create[(PageId, String)]
      ps <- TextsWsApi.pages("mde")
      textReq <- IO.pure(onMenuItemClick.map(pg => TextsWsApi.texts("mde", pg._1, pg._2)))
      text <- hammockObservableExec(textReq)
      ns <- IO.pure(createWorkflow(onMenuItemClick, ps, text))
      ow <- OutWatch.renderInto("#app", ns)
    } yield ow

    flow.unsafeRunAsync(eith => println(eith))
  }


  private def hammockObservableExec[T](textReq: Observable[Free[HammockF, T]]) = {
    IO {
      implicit val interpreter = Interpreter[IO]
      textReq.flatMap(
        r => Observable.fromFuture(r.exec[IO].unsafeToFuture())
      )
    }
  }

  private def createWorkflow(oc: Handler[(PageId, String)], ps: List[Page], texts: Observable[Texts]) = {
    val listItemsObs = Observable(for {
      page <- ps
      textId <- page.text_ids
    } yield Item((page.page_id, textId), s"${page.page_id}.$textId")) //ps.flatMap(_.text_ids).map(pg => Item(pg, pg)))
    val menuItemsView = listItems(listItemsObs, oc)
    <.div(
      menuItemsView,
      <.div(child <-- texts.map(t => <.div(t.toString)))
    )
    // menuItemsView
  }

  case class Item(id: (PageId, String), title: String)

  val < = dsl.tags
  val ^ = dsl.attributes

  private def listItems(menuItems: Observable[List[Item]], onClick: Sink[(PageId, String)]) = {
    val value = menuItems.map(items => items.map(item => <.li(
      ^.onClick(item.id) --> onClick,
      item.title,
    )))
    <.ul(
      //      Sstyle.container,
      children <-- value
    )
  }

}
