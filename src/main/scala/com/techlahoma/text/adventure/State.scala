package com.techlahoma.text.adventure
import fastparse.all
import scala.io.Source
import java.io.File
import java.io.PrintWriter

case class State(
                  currentRoom: Symbol = 'void,
                  prompt: String = "",
                  objects: Map[Symbol, RoomObject] = Map(),
                  rooms: Map[Symbol, Room] = Map(),
                  continue: Boolean = true,
                  inventory: scala.collection.immutable.Set[Symbol] = Set(),
                  commands: List[String] = Nil
                ) {

  def withCommand(command: String) = copy(commands = command :: commands)
  def getcurrentRoom: Room = rooms(currentRoom)
  def +(obj: RoomObject) = copy(objects = objects + (obj.symbol -> obj))
  def withObject(obj: RoomObject) = this + obj
  def withContainer(obj: RoomObject, containedObjects: Set[Symbol]) = {
    val newSymbol = Symbol(obj.symbol.name + "_open")
    val openContainer = obj.copy(
      description = obj.description + " It is open.",
      symbol = newSymbol,
      containedObjects = containedObjects,
      interactions = obj.interactions +
        (Interaction.Close -> ((s: State, o: RoomObject) => State.swapItemInRoom(newSymbol, obj.symbol, s).withPrompt(s"You closed the ${o.name}."))) +
        (Interaction.TakeFromMe -> ((s: State, o: RoomObject) => o.attempt(Interaction.TakeMeFrom, s.objects(newSymbol), s)))
    )

    val closedContainer = obj.copy(
      interactions = obj.interactions +
        (Interaction.Open -> ((s: State, o: RoomObject) => State.swapItemInRoom(obj.symbol, newSymbol, s).withPrompt(
          if(s.objects(newSymbol).containedObjects.map(s.objects).filter(o => !o.hidden).isEmpty) {
            s"You open the ${o.name}. It is empty."
          } else {
            s"You open the ${o.name}. It contains:\n" + s.objects(newSymbol).containedObjects.map(s.objects).filter(o => !o.hidden).map(o => o.describeContained(" ", s)).mkString("")
          }
        )))
    )

    withObject(openContainer)
      .withObject(closedContainer)
  }
  def withRoom(room:Room) = copy(rooms = rooms + (room.symbol -> room))
  def withCurrentRoom(room:Symbol) = {
    val newState = copy(currentRoom = room)
    newState.withPrompt(rooms(room).look(newState))
  }

  def withPrompt(p:String) = copy(prompt = p)

  def attemptOn(target: String, action: Interaction.Value, actionIfInContainer:Option[Interaction.Value] = None) = {
    val objectInRoom = getcurrentRoom.objects.map(objects).find(o => o.name == target)
    lazy val objectInObjectInRoom = getcurrentRoom.objects.map(objects).flatMap(o => o.containedObjects.map(objects).map(oc => (o,oc))).find(o => o._2.name == target)
    lazy val objectInInventory =   inventory.map(objects).find(o => o.name == target)

    objectInRoom.map(o => (o, o, action))
      .orElse(
        actionIfInContainer.flatMap(a => objectInObjectInRoom.map(o => (o._2, o._1, a))).orElse(objectInObjectInRoom.map(o => (o._2,o._2,action)))
      )
      .orElse(objectInInventory.map(o => (o, o,action)))
      .fold(withPrompt(s"I can't find a ${target} here."))(
        (res) => res._2.attempt(res._3, res._1, this)
      )
  }

  def attemptOnIn(target: String, container: String, action: Interaction.Value) =
    getcurrentRoom.objects.map(objects).find(o => o.name == container)
      .fold(withPrompt(s"I can't find a ${container} here."))(matchingContainer =>
        matchingContainer.containedObjects.map(objects).find(o => o.name == target).fold(withPrompt(s"I can't find a ${target} in the ${container}."))(matchingObject =>
          matchingContainer.attempt(action, matchingObject, this)
        )
      )

  def attemptExit(direction: Direction.Value) =
    getcurrentRoom.exits.find(e => e._1 == direction).fold(withPrompt("You can't go that way."))(exit => exit._2(this))

  def changeItem(item: Symbol, newItem: RoomObject) = {
    copy(
      objects = objects + (item -> newItem)
    )
  }
}

object State {
  def save(curState: State): State = {
    val writer = new PrintWriter(new File("save.sav"))
    curState.commands.reverse.foreach(s => writer.println(s))
    writer.close()
    curState.withPrompt("Game saved successfully.")
  }
  def restoreWith(startState: State, gameParser: all.P[State => State]): State = {
    val commands = Source.fromFile("save.sav").mkString.split("\n").map(x => x.trim())
    commands.foldLeft(startState)((curState: State, command: String) => gameParser.parse(command).fold((p, _, _) => curState.withPrompt("Holy failed load, batman!").withCommand(command), (s, _) => s(curState).withCommand(command).withPrompt("Game loaded successfully.")))
  }
  def gotToRoom(room: Symbol): State => State = (state:State) => state.copy(currentRoom = room, prompt = state.rooms(room).look(state))
  def changeCurrentRoom(newRoom: Room, state: State): State = {
    state.copy(
      currentRoom = newRoom.symbol,
      rooms = state.rooms + (newRoom.symbol -> newRoom)
    )
  }
  def takeItem(item: Symbol, state: State): State = {
    val newState = changeCurrentRoom(state.getcurrentRoom.copy(objects = state.getcurrentRoom.objects - item), state)
    newState.copy(inventory = newState.inventory + item)
      .withPrompt(s"You take the ${state.objects(item).name}.")
  }
  def swapItemInRoom(oldItem: Symbol, newItem: Symbol, state: State): State = {
    changeCurrentRoom(state.getcurrentRoom.copy(objects = state.getcurrentRoom.objects.map({
      case o if o == oldItem => newItem
      case o => o
    })), state)
  }
  def takeItemFromContainer(item: Symbol, container:RoomObject, state: State): State = {
    val newState = state.changeItem(container.symbol, container.copy(
      containedObjects = container.containedObjects - item
    ))
    newState.copy(inventory = newState.inventory + item)
      .withPrompt(s"You take the ${state.objects(item).name} from the ${container.name}.")
  }
}