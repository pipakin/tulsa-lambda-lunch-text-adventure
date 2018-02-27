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
  private lazy val use = P("use")
  private lazy val open = P("open")
  private lazy val read = P("read")
  private lazy val save = P("save")
  private lazy val load = P("load")

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
  private def parseDirection = (exit: noApi.Parser[Unit], direction:Direction.Value) => P(exit).map(_ => (s:State) => s.attemptExit(direction))

  private lazy val movementCommands = P(
    north.map(_ => (s:State) => s.attemptExit(Direction.North)) |
    south.map(_ => (s:State) => s.attemptExit(Direction.South)) |
    east.map(_ => (s:State) => s.attemptExit(Direction.East)) |
    west.map(_ => (s:State) => s.attemptExit(Direction.West)) |
    up.map(_ => (s:State) => s.attemptExit(Direction.Up)) |
    down.map(_ => (s:State) => s.attemptExit(Direction.Down))
  )

  private lazy val inventoryCommands =
    P(
      (take ~ (AnyChar ~ !from).rep.! ~ from ~ commandTarget).map(res => (s:State) => s.attemptOnIn(res._1.trim(), res._2.trim(), Interaction.TakeFromMe)) |
      targetedCommand(take, Interaction.Take, Some(Interaction.TakeFromMe))
    )

  private lazy val interactionCommands = P(
    targetedCommand(examine, Interaction.Examine, None) |
    targetedCommand(use, Interaction.Use, None) |
    targetedCommand(open, Interaction.Open, None) |
    targetedCommand(enter, Interaction.Enter, None) |
    targetedCommand(close, Interaction.Close, None) |
    targetedCommand(read, Interaction.Read, None) |
    targetedCommand(unlock, Interaction.Unlock, None)
  )

  private lazy val saveCommand = save.map(_ => (s:State) => State.save(s))
  private lazy val loadCommand = load.map(_ => (s:State) => State.restoreWith(startState, parseCommand))

  lazy val parseCommand: P[State => State] = Start ~
    (saveCommand |
    loadCommand |
    quit.map(_ => (s:State) => s.copy(continue = false)) |
    interactionCommands |
  // - Remove these before talk!
    look.map(_ => (s:State) => s.copy(prompt = s.getcurrentRoom.look(s))) |
    inventoryCommands |
    P(go ~/ movementCommands) |
    movementCommands) ~ End

  lazy val shouldPersist: P[Boolean] =
    (Start ~ (save | load) ~ End).map(_ => false) |
    parseCommand.map(_ => true)
}
