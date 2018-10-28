package co.nextwireless.texts.ui

import cats.effect.IO
import cats.free.Free
import hammock.HammockF
import hammock.js.Interpreter
import monix.reactive.Observable

/**
  * Created with IntelliJ IDEA.
  * User: Victor Mercurievv
  * Date: 10/28/2018
  * Time: 3:51 AM
  * Contacts: email: mercurievvss@gmail.com Skype: 'grobokopytoff' or 'mercurievv'
  */
object HammockExecuter {
  def hammockObservableExec[T](textReq: Observable[Free[HammockF, T]]): IO[Observable[T]] = {
    IO {
      implicit val interpreter: Interpreter[IO] = Interpreter[IO]
      textReq.flatMap(
        r => Observable.fromFuture(r.exec[IO].unsafeToFuture())
      )
    }
  }
}
