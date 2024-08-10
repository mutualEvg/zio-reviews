package com.nathan.reviewboard

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import com.raquo.laminar.api.L.Owner

import scala.util.Try
import com.raquo.airstream.ownership.OneTimeOwner

import scala.scalajs.js.timers.*
import com.raquo.laminar.api.features.unitArrows
import com.nathan.reviewboard.components.*
import frontroute.LinkHandler


// todo
//  how to compile
//  sbt
//  project App
//  ~fastOptJS
//  npm run start >>> localhost:1234
object App {

  val app = div(
    Header(),
    Router()
  ).amend(LinkHandler.bind) // amend for internal links
  
  def main(args: Array[String]): Unit = {
    val containerNode = dom.document.querySelector("#app")
//    render(containerNode, div("Azaza girls it's laminar!"))
//    render(containerNode, p("This is an app"))
//    render(containerNode, h3("This is a header"))
    // modifiers
    // css classes
    // styles
    // onClick
    // children

    render(
      containerNode,
//      div(
//        styleAttr := "color:red", // <div style="color:red">
//        p("This is an app")
//      )
//      Tutorial.staticContent,
//      Tutorial.clicksVar
      app
    )
  }
}

// reactive variables
object Tutorial {
  val staticContent =
    div(
      // modifiers
      styleAttr := "color:red", // <div style="color:red">
      p("This is an app"),
      p("rock the JVM but also JS")
    )
  // EventStream - produce values of the same type
  val ticks = EventStream.periodic(1000) // EventStream[Int]
  // ownership
  val subscription = ticks.addObserver(new Observer[Int] {
    override def onError(err: Throwable): Unit = ()

    override def onTry(nextValue: Try[Int]): Unit = ()

    override def onNext(nextValue: Int): Unit = dom.console.log(s"Ticks: $nextValue")
  })(new OneTimeOwner(() => ()))

  setTimeout(10000)(subscription.kill())

  val timeUpdated =
    div(
      span("Time since loaded: "),
      child <-- ticks.map(number => s"$number seconds")
    )


  // EventBus - like EventStreams, but you can push new elements to the stream
  val clickEvents = EventBus[Int]()
  val clickUpdated = div(
    span("Clicks since loaded: "),
    child <-- clickEvents.events.scanLeft(0)(_ + _).map(number => s"$number clicks"),
    button(
      `type` := "button",
      styleAttr := "display: block",
      onClick.map(_ => 1) --> clickEvents,
      "Add a click"
    )
  )

  // Signal - similar to EventStreams, but they have a "current value" (state)
  // can be inspected for the current state (if Laminar/Airstream knows that it has an owner)
  val countSignal = clickEvents.events.scanLeft(0)(_ + _).observe(new OneTimeOwner(() => ()))
  val queryEvents = EventBus[Unit]()

  val clicksQueried = div(
    span("Clicks since loaded: "),
    child <-- queryEvents.events.map(_ => countSignal.now()),
    button(
      `type` := "button",
      styleAttr := "display: block",
      onClick.map(_ => 1) --> clickEvents,
      "Add a click"
    ),
    button(
      `type` := "button",
      styleAttr := "display: block",
      onClick.map(_ => ()) --> queryEvents,
      "Refresh count"
    )
  )


  // Create a reactive variable
  val countVar = Var(0)

  // Create the UI component
  val clicksVar = div(
    span("Clicks so far: "),
    child.text <-- countVar.signal.map(_.toString),
    button(
      `type` := "button",
      styleAttr := "display: block",
      //onClick.mapTo(1) --> { _ => countVar.update(current => current + 1) },
      //onClick --> countVar.writer.contramap(event => countVar.now() + 1),
      onClick --> (_ => countVar.set(countVar.now() + 1)),
      "Click me"
    )
  )

  /**
   *              no state     |    with state
   * ---------------------------------------------
   * read       EventStream    |     Signal
   * ---------------------------------------------
   * write      EventBus       |     Var
   */


}

