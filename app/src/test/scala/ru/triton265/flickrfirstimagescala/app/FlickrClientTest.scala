package ru.triton265.flickrfirstimagescala.app

import argonaut.Argonaut._
import org.junit.Assert._
import org.junit.Test

class FlickrClientTest {

  @Test def testNormalSearchResultParse() {
    val input =
      """
        |{ "photos": { "page": 1, "pages": "763543", "perpage": 1, "total": "763543",
        |    "photo": [
        |      { "id": "21127436650", "owner": "133459415@N05", "secret": "701de65407", "server": "753" }
        |    ] }, "stat": "ok" }
      """.stripMargin

    val result = input.decodeOption[FlickrClient.SearchResult]

    /*
    val id = result
      .filter(r => "ok" == r.stat)
      .map(r => r.photos)
      .filter(p => p.total > 0 && p.photo.nonEmpty)
      .map(p => p.photo.head.id)
    assertTrue("21127436650" == id.getOrElse(""))
    */

    val id2 = for {
      r <- result if r.stat == "ok"
      photos = r.photos if photos.total > 0 && photos.photo.nonEmpty
    } yield photos.photo.head.id

    assertTrue("21127436650" == id2.getOrElse(""))
  }

  @Test def testWrongSearchResultParse() {
    val input = "wrong json string"
    val result = input.decodeOption[FlickrClient.SearchResult]

    /*
    val id = result
      .filter(r => "ok" == r.stat)
      .map(r => r.photos)
      .filter(p => p.total > 0 && p.photo.nonEmpty)
      .map(p => p.photo.head.id)
    assertTrue(id.isEmpty)
    */

    val id2 = for {
      r <- result if r.stat == "ok"
      photos = r.photos if photos.total > 0 && photos.photo.nonEmpty
    } yield photos.photo.head.id

    assertTrue(id2.isEmpty)
  }

  @Test def testSearch() {
    val id = FlickrClient.searchFirst(Some("nice butt"))
    assertFalse(id.isEmpty)
  }
}
