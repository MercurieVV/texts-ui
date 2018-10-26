package co.nextwireless.texts.ui

import cats.effect.IO
import cats.free.Free
import co.nextwireless.texts.ui.client.swagger.model.{Page, Texts}
import co.nextwireless.texts.ui.client.swagger.zapi.TextsWsApi
import co.nextwireless.texts.ui.client.swagger.zapi.impl._
import co.nextwireless.texts.ui.client.swagger.model._
import hammock.HammockF
import hammock.js.Interpreter
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
      onMenuItemClick <- Handler.create[Item]
      onTextEdit <- Handler.create[Texts]
      saveText <- Handler.create[(PageId, String, Texts)]
      ps <- TextsWsApi.pages("mde")
      textReq <- IO.pure(onMenuItemClick.map(pg => TextsWsApi.texts("mde", pg.id._1, pg.id._2)))
      text <- hammockObservableExec(textReq)
      ns <- IO.pure(createWorkflow(onMenuItemClick, ps, text, onTextEdit, saveText))
      st <- IO {saveText.doOnNext(t => println(t)).subscribe()}
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

    val value: Observable[(PageId, String, Texts)] = onTextEdit.zipMap(textIdO)((text, item) => (item.id._1, item.id._2, text))
    <.div(
      listItems(listItemsObs, textIdO),
      <.div(^.child <-- textIdO.map(_.title)),
      <.div(<.textArea(^.value <-- texts.map(_.en.getOrElse("").toString), ^.onInput.value --> onTextEdit.transformSink(sk => sk.flatMap(s => texts.map(_.copy(en = Some(s))))))),
      <.button("Save", ^.onClick(value) --> saveText),
//      <.button("Save", ^.onClick(onTextEdit.zipMap(oc)((text, page) => (page._1, page._2, text))) --> saveText)

      /*
            <.div(^.child <-- texts.zip(textIdO)
              .map(t => {
                println("list te")
                <.div(
                  <.div(<.textArea(t._1.en.getOrElse("").toString, ^.onInput.value --> onTextEdit.mapSink(s => {
                    val tc = t._1.copy(en = Some(s))
                    println(tc)
                    tc
                  }))),
                )
              }),
            )
      */
    )
    // menuItemsView
  }

  case class Item(id: (PageId, String), title: String)

  val < = dsl.tags
  val ^ = dsl.attributes

  private def listItems(menuItems: Observable[List[Item]], onClick: Handler[Item]) = {
    <.ul(
      ^.children <-- menuItems.doOnNext(t => println(t + " gg")).map(items => items.map(item =>
        <.li(
          ^.onClick(item) --> onClick.mapSink(f=>{
            println("xxx")
            f
          }),
          item.title,
        )
      ))
    )
  }

}
