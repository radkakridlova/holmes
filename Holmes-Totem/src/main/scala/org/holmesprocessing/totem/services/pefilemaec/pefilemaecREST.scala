package org.holmesprocessing.totem.services.pefilemaec

import dispatch.Defaults._
import dispatch.{url, _}
import org.json4s.JsonAST.{JString, JValue}
import org.holmesprocessing.totem.types.{TaskedWork, WorkFailure, WorkResult, WorkSuccess}
import collection.mutable


case class pefilemaecWork(key: Long, filename: String, TimeoutMillis: Int, WorkType: String, Worker: String, Arguments: List[String]) extends TaskedWork {
  def doWork()(implicit myHttp: dispatch.Http): Future[WorkResult] = {

    val uri = pefilemaecREST.constructURL(Worker, filename, Arguments)
    val requestResult = myHttp(url(uri) OK as.String)
      .either
      .map({
      case Right(content) =>
        pefilemaecSuccess(true, JString(content), Arguments)

      case Left(StatusCode(404)) =>
        pefilemaecFailure(false, JString("Not found (File already deleted?)"), Arguments)

      case Left(StatusCode(500)) =>
        pefilemaecFailure(false, JString("pefilemaec service failed, check local logs"), Arguments) //would be ideal to print response body here

      case Left(StatusCode(code)) =>
        pefilemaecFailure(false, JString("Some other code: " + code.toString), Arguments)

      case Left(something) =>
        pefilemaecFailure(false, JString("wildcard failure: " + something.toString), Arguments)
    })
    requestResult
  }
}


case class pefilemaecSuccess(status: Boolean, data: JValue, Arguments: List[String], routingKey: String = "pefilemaec.result.static.totem", WorkType: String = "PEFILEMAEC") extends WorkSuccess
case class pefilemaecFailure(status: Boolean, data: JValue, Arguments: List[String], routingKey: String = "", WorkType: String = "PEFILEMAEC") extends WorkFailure


object pefilemaecREST {
  def constructURL(root: String, filename: String, arguments: List[String]): String = {
    arguments.foldLeft(new mutable.StringBuilder(root+filename))({
      (acc, e) => acc.append(e)}).toString()
  }
}
