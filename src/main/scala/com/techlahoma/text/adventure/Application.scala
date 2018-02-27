package com.techlahoma.text.adventure
import fastparse.core.Parsed.{Failure, Success}

import scala.annotation.tailrec

object Application extends GameSetup with GameParsers  {

  val wrapRegEx = """(.{1,54})\s""".r
  def printWrapLine(str: String) = println(wrapRegEx.replaceAllIn(str + "\n", m=>m.group(1)+"\n"))

  @tailrec
  def waitForCommand(currentState:State): Unit = {
    val command = scala.io.StdIn.readLine("> ");
    val commandResult = parseCommand.parse(command)

    commandResult match {
      case Success(s, _) =>
        val newState = if (shouldPersist.parse(command).fold((_,_,_) => false, (b,_) => b)) {
          s(currentState).withCommand(command)
        } else {
          s(currentState)
        }

        if(newState.continue) {
          println()
          printWrapLine(newState.prompt)
          waitForCommand(newState)
        }
      case a =>
        println()
        println("I don't know how to do that.")
        println(a)
        waitForCommand(currentState)
    }
  }

  def main(args: Array[String]): Unit = {
    println("Welcome to the Lambda Lunch Text Adventure!")
    println()
    println()
    printWrapLine(startState.getcurrentRoom.look(startState))
    waitForCommand(startState)
  }
}
