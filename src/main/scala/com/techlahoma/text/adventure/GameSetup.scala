package com.techlahoma.text.adventure

import org.backuity.ansi.AnsiFormatter.FormattedHelper

trait GameSetup {

  val mailbox = RoomObject('mailbox, "mailbox", "There is a small mailbox here.")

  val programmer = RoomObject('person, "person", "There is a person sitting at one of the computers here.")
    .withInteraction(Interaction.Examine, (s:State, _) => s.withPrompt(ansi"%bold{Philip Kin} is co-owner of %bold{Berwanger and Kin, LLC} and a software developer at %bold{Zealcon}, a consulting company in Tulsa, OK. He has been doing Functional Programming professionally for a short time. Parsers are near and dear to him, as he grew up playing Infocom games like %bold{ZORK} and %bold{Hitchhiker's Guide}."))

  val computer = RoomObject('computer, "computer", "", hidden = true)
    .withInteraction(Interaction.Examine, (s:State, _) => s.withPrompt(ansi"The computer appears to be running a modified version of an old text adventure game, %bold{ZORK}. It looks like it was modified to teach people how to use %bold{Parser Combinators} in %bold{Scala}.${"\n\n"}A %bold{Parser Combinator} is a %bold{higher-order function} that accepts several parsers as input and returns a new parser as its output."))
    .withInteraction(Interaction.Use, (s:State, _) => {
      val state = s.withCurrentRoom('westOfHouse)
      state.withPrompt("You sit down at the computer, pushing the person out of the way. The game appears to have just begun...\n\n" + state.prompt)
    })

  val computerNote1 = RoomObject('computerNote1, "parser note", "There is a note about parsers here.")
      .withInteraction(Interaction.Read, (s:State, _) => s.withPrompt(ansi"A %bold{parser} is a compiler or interpreter component that breaks data into smaller elements for easy translation into another language. A %bold{parser} takes input in the form of a sequence of tokens or program instructions and usually builds a data structure in the form of a parse tree or an abstract syntax tree."))
  val computerNote2 = RoomObject('computerNote2, "parser combinator note", "There is a note about parser combinators here.")
    .withInteraction(Interaction.Read, (s:State, _) => s.withPrompt(ansi"In between %bold{regex} and %bold{recursive-descent/parser-generator}, there is a gap: what about the cases where you don't need utmost performance, but your data format is too complex to use a regex? Hand-rolled recursive descent parsers are straightforward but tedious to write, while parser generators are finnicky to set up and often require custom build steps and code-generation.${"\n\n"}Why can't there be a way of writing parsers as simple as a regex, but as flexible as hand-rolled recursive-descent parsers or parser generators? It turns out there are such things: they're called %bold{Parser Combinators}."))

  val computerLab = Room('computerLab, "Computer Lab", "You are standing in an old computer lab. The screens flicker in a disconcerting fashion.")
    .withObject('person)
    .withObject('computer)
    .withObject('computerNote1)
    .withObject('computerNote2)

  val note = RoomObject('note, "note", "There is a small crumpled note on the floor.", "a small crumpled note")
    .withInteraction(Interaction.Take, (s:State, _) => State.takeItem('note, s))
    .withInteraction(Interaction.TakeMeFrom, (s:State, o:RoomObject) => State.takeItemFromContainer('note, o, s))
    .withInteraction(Interaction.Read, (s:State, _) => s.withPrompt("""WELCOME TO LAMBDA LUNCH TEXT ADVENTURE! (Totally not a ZORK ripoff)
     |
     |LAMBDA LUNCH TEXT ADVENTURE is a meeting talk of adventure, danger, and low cunning. In this talk you will explore some of the most amazing territory ever seen by mortals. And by territory I of course mean parsing commands using a Parser Combinator. No computer should be without one!""".stripMargin))

  val southOfHouse = Room('southOfHouse, "South of House", "You are facing the south side of a white house. There is no door here, and all the windows are boarded.")
    .withExit('westOfHouse, Direction.West)
    .withExit('behindHouse, Direction.East)

  val northOfHouse = Room('northOfHouse, "North of House", "You are facing the north side of a white house.  There is no door here, and all the windows are barred.")
    .withExit('westOfHouse, Direction.West)
    .withExit('behindHouse, Direction.East)

  val westOfHouse = Room('westOfHouse, "West of House", "You are standing in an open field west of a white house, with a boarded front door.")
    .withObject(mailbox.symbol)
    .withExit('southOfHouse, Direction.South)
    .withExit('northOfHouse, Direction.North)

  val behindHouse = Room('behindHouse, "Behind House", "You are behind the white house. A path leads into the forest to the east. In one corner of the house there is a small window which is slightly ajar.")
    .withObject('window)
    .withObject('behindHouseWindowEntrance)
    .withExit('southOfHouse, Direction.South)
    .withExit('northOfHouse, Direction.North)

  val behindHouseWindowAjar = RoomObject('window, "window", "window", hidden = true)
    .withInteraction(Interaction.Open, (s:State, o:RoomObject) =>
      s.changeItem('window, behindHouseWindowOpen)
        .withRoom(behindHouse.copy(description = "You are behind the white house.  In one corner of the house there is a small window which is open.")
          .withExit('kitchen, Direction.West)
        )
        .withPrompt("With great effort, you open the window far enough to allow entry."))

  val behindHouseWindowOpen = RoomObject('windowOpen, "window", "window", hidden = true)
    .withInteraction(Interaction.Open, (s:State, o:RoomObject) => s.withPrompt("The window is open. What else do you want?"))
    .withInteraction(Interaction.Enter, (s:State, o:RoomObject) => s.withCurrentRoom('kitchen))

  val behindHouseEntrance = RoomObject('behindHouseWindowEntrance, "house", "house", hidden = true)
    .withInteraction(Interaction.Enter, (s:State, o:RoomObject) => s.withCurrentRoom('kitchen))

  val keys = RoomObject('keys, "keys", "There are some keys on a hook on the wall here.")
    .withInteraction(Interaction.Take, (s:State, _) => State.takeItem('keys, s).withObject(doorWithKey))

  val kitchen = Room('kitchen, "Kitchen", "You are in the kitchen of the white house.  A table seems to have been used recently for the preparation of food.  A passage leads to the west and a dark staircase can be seen leading upward.  To the east is a small window which is open.")
    .withExit('behindHouse, Direction.East)
    .withExit('endOfHallway, Direction.West)
    .withExit('topOfStairs, Direction.Up)
    .withObject('keys)

  val door = RoomObject('hallDoor, "door", "door", hidden = true)
    .withInteraction(Interaction.Unlock, (s:State, _) => s.withPrompt("You don't have the key."))

  val doorWithKey = RoomObject('hallDoor, "door", "door", hidden = true)
    .withInteraction(Interaction.Unlock, (s:State, _) => s.withRoom(endOfHallwayDoorOpen).withPrompt("You unlock the door to the north. It looks like it leads to a hall closet."))

  val endOfHallway = Room('endOfHallway, "End of Hallway", "You are at the end of the hallway leading from the kitchen. Two of the doors here are boarded up, and the other appears to be locked. You can see the kitchen back to the east")
    .withExit('kitchen, Direction.East)
    .withObject('hallDoor)

  val endOfHallwayDoorOpen = Room('endOfHallway, "End of Hallway", "You are at the end of the hallway leading from the kitchen. Two of the doors here are boarded up, and the other leads to a hall closet to the north. You can see the kitchen back to the east")
    .withExit('kitchen, Direction.East)
    .withExit('hallCloset, Direction.North)

  val flashlight = RoomObject('flashlight, "flashlight", "There is a flashlight here.")
    .withInteraction(Interaction.Take, (s:State, _) => State.takeItem('flashlight, s).withRoom(litTopOfStairs))
    .withInteraction(Interaction.TakeMeFrom, (s:State, o:RoomObject) => State.takeItemFromContainer('flashlight, o, s))

  val hallCloset = Room('hallCloset, "Hall Closet", "This is a pretty unintereting hall closet.")
    .withExit('endOfHallway, Direction.South)
    .withObject('flashlight)

  val darkTopOfStairs = Room('topOfStairs, "Pitch Black", ansi"It is very dark here. You are likely to be eaten by a grue... well, you would be, but you keep hearing grunts, typing, then ${"\""}I don't know how to do that.${"\""} You get the impression the grue doesn't understand %bold{parser combinators}.")
    .withExit('kitchen, Direction.Down)

  val businessCard = RoomObject('businessCard, "business card", "There is a business card here.")
    .withInteraction(Interaction.Read, (s:State, _) => s.withPrompt(
      """The card reads:
         |Philip Kin                 Senior Software Engineer
         |Emails: philip@thekinfamily.com, pkin@zealcon.com, philip.kin@berwanger-and-kin.com
         |Presentation Github link: https://github.com/pipakin/tulsa-lambda-lunch-text-adventure""".stripMargin))

  val litTopOfStairs = Room('topOfStairs, "End of Game", ansi"You've reached a room which is covered in inspiring posters. You get the impression this is as much content as had been added up to this point.")
    .withExit('kitchen, Direction.Down)
    .withObject('businessCard)

  val startState = State()
    .withObject(note)
    .withObject(behindHouseWindowOpen)
    .withObject(behindHouseWindowAjar)
    .withObject(behindHouseEntrance)
    .withObject(programmer)
    .withObject(computer)
    .withObject(computerNote1)
    .withObject(computerNote2)
    .withObject(keys)
    .withObject(businessCard)
    .withObject(door)
    .withObject(flashlight)
    .withContainer(mailbox, Set(note.symbol))
    .withRoom(westOfHouse)
    .withRoom(southOfHouse)
    .withRoom(behindHouse)
    .withRoom(northOfHouse)
    .withRoom(kitchen)
    .withRoom(computerLab)
    .withRoom(endOfHallway)
    .withRoom(darkTopOfStairs)
    .withRoom(hallCloset)
    .withCurrentRoom(computerLab.symbol)

}
