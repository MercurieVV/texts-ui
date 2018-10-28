package co.nextwireless.texts.ui

import cats.effect.IO
import cats.free.Free
import co.nextwireless.texts.ui.client.swagger.model.{Page, Texts}
import co.nextwireless.texts.ui.client.swagger.zapi.TextsWsApi
import co.nextwireless.texts.ui.client.swagger.zapi.impl._
import co.nextwireless.texts.ui.client.swagger.model._
import hammock.HammockF
import hammock.js.Interpreter
import outwatch.dom.{Handler, OutWatch, Sink, dsl}
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import outwatch.Pipe

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
      onMenuItemClick <- Handler.create[Item]
      onTextEdit <- Handler.create[Texts]
      saveText <- Handler.create[(PageId, String, Texts)]
      ps <- TextsWsApi.pages("mde")
      textReq <- IO.pure(onMenuItemClick.map(pg => TextsWsApi.texts("mde", pg.id._1, pg.id._2)))
      text <- hammockObservableExec(textReq)
      ns <- IO.pure(createWorkflow(onMenuItemClick, ps, text, onTextEdit, saveText))
      st <- IO {saveText.doOnNext(t => println("save " + t._2 + " " + t._3.en)).subscribe()}
      ow <- OutWatch.renderInto("#app", ns)
    } yield ow

    flow.unsafeRunAsync(eith => println(eith))
  }


  private def hammockObservableExec[T](textReq: Observable[Free[HammockF, T]]) = {
    IO {
      implicit val interpreter: Interpreter[IO] = Interpreter[IO]
      textReq.flatMap(
        r => Observable.fromFuture(r.exec[IO].unsafeToFuture())
      )
    }
  }

  private def createWorkflow(
                              textIdO: Handler[Item],
                              ps: List[Page],
                              texts: Observable[Texts],
                              onTextEdit: Handler[Texts],
                              saveText: Handler[(PageId, String, Texts)]
                            ) = {
    val listItemsObs = Observable(for {
      page <- ps
      textId <- page.text_ids
    } yield Item((page.page_id, textId), s"${page.page_id}.$textId"))

//    val updateEngText: ProHandler[String, Texts] = onTextEdit.transformSink(sk => sk.flatMap(s => texts.map(_.copy(en = Some(s)))))
    val combineDataToSave: Observable[(PageId, String, Texts)] = onTextEdit.combineLatestMap(textIdO)((text, item) => (item.id._1, item.id._2, text))
    <.div(^.className := "full height",
      listItems(listItemsObs, textIdO),
      <.div(^.className := "ui container")(
        <.form(
          ^.className := "ui form",
          <.div(^.className := "ui header", ^.child <-- textIdO.map(_.title)),
          <.div(
            ^.className := "field",
            <.label("eng"),
            <.textArea(^.value <-- texts.map(_.en.getOrElse("").toString), ^.onInput.value.transform(sk => sk.flatMap(s => texts.map(_.copy(en = Some(s))))) --> onTextEdit)
          ),
          <.button(^.className := "ui teal large submit button", "Save", ^.onClick(combineDataToSave) --> saveText),
        )
      )
    )
    // menuItemsView
  }

  case class Item(id: (PageId, String), title: String)

  val < = dsl.tags
  val ^ = dsl.attributes

  private def listItems(menuItems: Observable[List[Item]], onClick: Handler[Item]) = {
    <.div(^.className := "toc",
      <.div(^.className := "ui sidebar inverted vertical menu visible",
        ^.children <-- menuItems.doOnNext(t => println(t + " gg")).map(items => items.map(item =>
          <.a(^.className := "item",
            ^.onClick(item) --> onClick,
            item.title,
          )
        ))
      )
    )
  }

}
