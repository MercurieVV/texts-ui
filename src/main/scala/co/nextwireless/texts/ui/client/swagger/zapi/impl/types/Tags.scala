package co.nextwireless.texts.ui.client.swagger.zapi.impl.types

import shapeless.tag._

/**
  * Created with IntelliJ IDEA.
  * User: Victor Mercurievv
  * Date: 10/17/2018
  * Time: 2:30 PM
  * Contacts: email: mercurievvss@gmail.com Skype: 'grobokopytoff' or 'mercurievv'
  */
trait Tags {
  trait ApplicationIdTag
  type ApplicationId = String @@ ApplicationIdTag
  trait PageIdTag
  type PageId = String @@ PageIdTag
}
