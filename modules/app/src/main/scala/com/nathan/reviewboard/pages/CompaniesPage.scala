package com.nathan.reviewboard.pages

import com.nathan.reviewboard.common.Constants.companyLogoPlaceholder
import com.nathan.reviewboard.components.*
import com.nathan.reviewboard.core.ZJS.*
import com.nathan.reviewboard.domain.data.*
import com.raquo.laminar.api.L.{*, given}
import sttp.client3.*

object CompaniesPage {

  val filterPanel = new FilterPanel()

  val firstBatch = EventBus[List[Company]]() 

  val dummyCompany = Company(
    1L,
    "dummy-company",
    "Simple company",
    "http://dummy.com",
    Some("Anywhere"),
    Some("On Mars"),
    Some("space travel"),
    None,
    List("space", "scala")
  )

  val companyEvents: EventStream[List[Company]] =
    firstBatch.events.mergeWith {
      filterPanel.triggerFilters.flatMap { newFilter =>
        useBackend(_.company.searchEndpoint(newFilter)).toEventStream
      }
    }
    
//    val companyEvents: EventStream[List[Company]] =
//    useBackend(_.company.getAllEndpoint(())).toEventStream.mergeWith {
//      filterPanel.triggerFilters.flatMap { newFilter =>
//        useBackend(_.company.searchEndpoint(newFilter)).toEventStream
//      }
//    }


  //  val companiesBus = EventBus[List[Company]]()
//
//  def performBackendCall(): Unit = {
//    val companiesZIO = useBackend(_.company.getAllEndpoint(()))
//    companiesZIO.emitTo(companiesBus)
//  }

  def apply() =
    sectionTag(
      //onMountCallback(_ => performBackendCall()),
      onMountCallback(_ => useBackend(_.company.getAllEndpoint(())).emitTo(firstBatch)),
      cls := "section-1",
      div(
        cls := "container company-list-hero",
        h1(
          cls := "company-list-title",
          "JVM Companies Board"
        )
      ),
      div(
        cls := "container",
        div(
          cls := "row jvm-recent-companies-body",
          div(
            cls := "col-lg-4",
            filterPanel()
          ),
          div(
            cls := "col-lg-8",
//            children <-- companiesBus.events.map {
//              _.map {
//                comp =>
//                  println(s"comp = $comp")
//                  renderCompany(comp)
//              }
            children <-- companyEvents.map {
              _.map {
                comp =>
                  println(s"comp = $comp")
                  renderCompany(comp)
              }
            }
//            renderCompany(dummyCompany),
//            renderCompany(dummyCompany)
          )
        )
      )
    )

  private def renderCompanyPicture(company: Company) =
    img(
      cls := "img-fluid",
      src := company.image.getOrElse(companyLogoPlaceholder),
      alt := company.name
    )

  private def renderDetail(icon: String, value: String) =
    div(
      cls := "company-detail",
      i(cls := s"fa fa-$icon company-detail-icon"),
      p(
        cls := "company-detail-value",
        value
      )
    )

  private def fullLocationString(company: Company): String =
    (company.location, company.country) match {
      case (Some(location), Some(country)) => s"$location, $country"
      case (Some(location), None)          => location
      case (None, Some(country))           => country
      case (None, None)                    => "N/A"
    }

  private def renderOverview(company: Company) =
    div(
      cls := "company-summary",
      renderDetail("location-dot", fullLocationString(company)),
      renderDetail("tags", company.tags.mkString(", "))
    )

  private def renderAction(company: Company) =
    div(
      cls := "jvm-recent-companies-card-btn-apply",
      a(
        href   := company.url,
        target := "blank",
        button(
          `type` := "button",
          cls    := "btn btn-danger rock-action-btn",
          "Website"
        )
      )
    )

  def renderCompany(company: Company) =
    div(
      cls := "jvm-recent-companies-cards",
      div(
        cls := "jvm-recent-companies-card-img",
        renderCompanyPicture(company)
      ),
      div(
        cls := "jvm-recent-companies-card-contents",
        h5(
          Anchors.renderNavLink(
            company.name,
            s"/company/${company.id}",
            "company-title-link"
          )
        ),
        renderOverview(company)
      ),
      renderAction(company)
    )
}
