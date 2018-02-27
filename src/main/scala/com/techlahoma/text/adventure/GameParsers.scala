package com.techlahoma.text.adventure

import fastparse.{WhitespaceApi, all, core, noApi}

trait GameParsers {
  val White = WhitespaceApi.Wrapper{
    import fastparse.all._
    NoTrace(" ".rep)
  }
  import fastparse.noApi._
  import White._

  private lazy val commandTarget = P(AnyChar.rep.!)
  private lazy val look = P("look" | "l")
  private lazy val quit = P("quit" | "q")
  private lazy val examine = P(("look" ~ "at") | "examine")
  private lazy val open = P("open")
  private lazy val read = P("read")
  private lazy val save = P("save")
  private lazy val load = P("load")

  //TODO: Implement these keywords!
  private lazy val use = P("use")
  private lazy val take = P("take")
  private lazy val from = P("from")
  private lazy val go = P("go")
  private lazy val close = P("close")
  private lazy val enter = P("enter")
  private lazy val unlock = P("unlock")
  private lazy val north = P("north" | "n")
  private lazy val south = P("south" | "s")
  private lazy val east = P("east" | "e")
  private lazy val west = P("west" | "w")
  private lazy val up = P("up" | "u")
  private lazy val down = P("down" | "d")

  protected val startState: State

  private def targetedCommand = (command: noApi.Parser[Unit], action:Interaction.Value, actionIfInContainer:Option[Interaction.Value]) => P(command ~ commandTarget).map(target => (s:State) => s.attemptOn(target, action, actionIfInContainer))

  private lazy val interactionCommands = P(
    targetedCommand(examine, Interaction.Examine, None) |
    targetedCommand(open, Interaction.Open, None) |
    targetedCommand(read, Interaction.Read, None)
  )

  private lazy val saveCommand = save.map(_ => (s:State) => State.save(s))
  private lazy val loadCommand = load.map(_ => (s:State) => State.restoreWith(startState, parseCommand))

  lazy val parseCommand: P[State => State] = Start ~
    (
      saveCommand |
      loadCommand |
      quit.map(_ => (s:State) => s.copy(continue = false)) |
      interactionCommands
    ) ~ End

  lazy val shouldPersist: P[Boolean] =
    (Start ~ (save | load) ~ End).map(_ => false) |
    parseCommand.map(_ => true)
}
