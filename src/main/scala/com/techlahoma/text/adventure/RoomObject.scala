package com.techlahoma.text.adventure

case class RoomObject(symbol:Symbol, name: String, description: String, describeContained: String = "???", hidden:Boolean = false, containedObjects: scala.collection.immutable.Set[Symbol] = Set(), interactions: Map[Interaction.Value, (State, RoomObject) => State] = Map()) {

  def getSubDescription(indent: String, state: State): String =
    if (containedObjects.map(state.objects).filter(o => !o.hidden).isEmpty) {
      ""
    } else {
      s"\n${indent}The $name contains:\n" + containedObjects.map(state.objects).filter(o => !o.hidden).map(o => o.describeContained(indent + " ", state)).mkString("")
    }

  def describe(indent: String, state: State): String = indent + description + getSubDescription(indent, state)
  def describeContained(indent: String, state: State): String = indent + describeContained + getSubDescription(indent, state)
  def attempt(action: Interaction.Value, obj: RoomObject, state:State) = interactions.find(a => a._1 == action).fold(state.withPrompt("I'm sorry, you can't do that."))(i => i._2(state, obj))
  def withInteraction(action: Interaction.Value, fn: (State, RoomObject) => State) = copy(interactions = interactions + (action -> fn))
}
