package co.nextwireless.texts.ui

import cats.effect.IO
import cats.free.Free
import co.nextwireless.texts.ui.client.swagger.model.{Page, Texts}
import co.nextwireless.texts.ui.client.swagger.zapi.TextsWsApi
import co.nextwireless.texts.ui.client.swagger.zapi.impl._
import co.nextwireless.texts.ui.client.swagger.model._
import hammock.HammockF
import hammock.js.Interpreter
import outwatch.dom.{Handler, OutWatch, Sink, VNode, dsl}
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
      enabledEdditing <- Handler.create[Boolean](true)
      onTextEdit <- Handler.create[Texts]
      saveText <- Handler.create[(PageId, String, Texts)]
      pages <- TextsWsApi.pages("mde")
      textReq <- IO.pure(onMenuItemClick.map(pg => TextsWsApi.texts("mde", pg.id._1, pg.id._2)))
      text <- HammockExecuter.hammockObservableExec(textReq)
      ns <- IO.pure(createUIWorkflow(onMenuItemClick, pages, text, onTextEdit, saveText))
      st <- IO {
        saveText.doOnNext(t => println("save " + t._2 + " " + t._3.en)).subscribe()
      }
      ow <- OutWatch.renderInto("#app", ns)
    } yield ow

    flow.unsafeRunAsync(eith => println(eith))
  }



  case class Item(id: (PageId, String), title: String)

  val < = dsl.tags
  val ^ = dsl.attributes

  private def createUIWorkflow(
                              currentTextId: Handler[Item],
                              ps: List[Page],
                              texts: Observable[Texts],
                              onTextEdit: Handler[Texts],
                              saveText: Handler[(PageId, String, Texts)]
                            ) = {
    val listItemsObs = Observable(for {
      page <- ps
      textId <- page.text_ids
    } yield Item((page.page_id, textId), s"${page.page_id}.$textId"))

    val combineTextAndItsIdToSave: Observable[(PageId, String, Texts)] = onTextEdit.combineLatestMap(currentTextId)((text, item) => (item.id._1, item.id._2, text))

    <.div(^.className := "full height",
      listItems(listItemsObs, currentTextId),
      <.div(^.className := "ui container")(
        <.form(
          ^.className := "ui form",
          <.div(^.className := "ui header", currentTextId.map(_.title)),
          editorForLang("eng", texts, onTextEdit, _.en, (t, s) => t.copy(en = s)),
          <.button(^.className := "ui teal large submit button", "Save", ^.onClick(combineTextAndItsIdToSave) --> saveText),
        )
      )
    )
  }

  private def editorForLang(label: String, texts: Observable[Texts], onTextEdit: Handler[Texts], stringForLang: Texts => Option[String], changeTextLang: (Texts, Option[String]) => Texts): VNode = {
    editorForLang2(label, textForLang(texts, stringForLang), changeText(texts, changeTextLang), onTextEdit)
  }

  private def editorForLang2(label: String, langTextVal: Observable[String], langTextEdit: Observable[String] => Observable[Texts], onTextEdit: Handler[Texts]) = {
    <.div(
      ^.className := "field",
      <.label(label),
      <.textArea(^.value <-- langTextVal, ^.onInput.value.transform(langTextEdit) --> onTextEdit)
    )
  }

  private def listItems(menuItems: Observable[List[Item]], onClick: Handler[Item]) = {
    <.div(^.className := "toc",
      <.div(^.className := "ui sidebar inverted vertical menu visible",
        menuItems.map(_.map(item =>
          <.a(^.className := "item",
            ^.onClick(item) --> onClick,
            item.title,
          )
        ))
      )
    )
  }


  private def changeText(texts: Observable[Texts], changeText: (Texts, Option[String]) => Texts): Observable[String] => Observable[Texts] = {
    strO =>
      strO
        .map(Some(_))
        .flatMap(s => {
          texts.map(changeText(_, s))
        })
  }

  private def textForLang(texts: Observable[Texts], lang: Texts => Option[String]): Observable[String] = {
    texts.map(lang).map(_.getOrElse("").toString)
  }
}
