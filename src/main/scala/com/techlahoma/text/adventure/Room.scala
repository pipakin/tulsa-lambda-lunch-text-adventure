package com.techlahoma.text.adventure

import org.backuity.ansi.AnsiFormatter.FormattedHelper

case class Room (symbol: Symbol, name: String, description: String, exits: Map[Direction.Value, State => State] = Map(), objects: scala.collection.immutable.Set[Symbol] = Set()) {
  def look(state: State): String = ansi"%bold{$name}${"\n"}$description${objects.map(state.objects).filter(o => !o.hidden).map(o => "\n" +o.describe("", state)).mkString("")}"
  def withObject(obj: Symbol) = copy(objects = objects + obj)
  def withExit(room:Symbol, direction: Direction.Value) = copy(exits = exits + (direction -> ((s:State) => s.withCurrentRoom(room))))
}
