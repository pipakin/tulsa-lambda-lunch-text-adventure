package com.techlahoma.text.adventure

object Interaction extends Enumeration {
  type Interaction = Value
  val Open, Close, Take, TakeFromMe, TakeMeFrom, Read, Enter, Examine, Use, Unlock = Value
}
