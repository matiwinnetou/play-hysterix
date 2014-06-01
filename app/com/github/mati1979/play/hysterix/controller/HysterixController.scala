package com.github.mati1979.play.hysterix.controller

import play.api.mvc.Controller

/**
 * Created by mszczap on 01.06.14.
 */
class HysterixController extends Controller {

//  implicit val dataFmt = Json.format[HysterixCommand]
//
////  def stream = Action {
////    val datas: Enumerator[HysterixCommand] = Enumerator.generateM[HysterixCommand] {
////      Promise.timeout(
////        Some(Data(UUID.randomUUID().toString, "Hello World")), Random.nextInt(500)
////      )
////    }
////
////    Ok.chunked()
////  }
//
//  def index = Action {
//
//    val data = getDataStream
//    val dataContent: Enumerator[String] = Enumerator.fromStream(data)
//
//    Ok.chunked(dataContent)
//  }

}
