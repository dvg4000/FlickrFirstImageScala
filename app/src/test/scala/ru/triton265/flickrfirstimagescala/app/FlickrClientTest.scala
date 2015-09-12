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
      r <- result if "ok" == r.stat
      photos = r.photos if photos.total > 0 && photos.photo.nonEmpty
    } yield photos.photo.head.id

    assertTrue("21127436650" == id2.getOrElse(""))
  }

  @Test def testWrongSearchResultParse() {
    val input = "wrong json string"
    val result = input.decodeOption[FlickrClient.SearchResult]

    val id2 = for {
      r <- result if "ok" == r.stat
      photos = r.photos if photos.total > 0 && photos.photo.nonEmpty
    } yield photos.photo.head.id

    assertTrue(id2.isEmpty)
  }

  @Test def testSearch() {
    val id = FlickrClient.searchFirst(Some("nice butt"))
    assertFalse(id.isEmpty)
  }

  @Test def testNormalGetSizesParse() {
    val input =
      """
        |{ "sizes": { "canblog": 0, "canprint": 0, "candownload": 1,
        |    "size": [
        |      { "label": "Square", "width": 75, "height": 75, "source": "https:\/\/farm1.staticflickr.com\/753\/21127436650_701de65407_s.jpg", "url": "https:\/\/www.flickr.com\/photos\/133459415@N05\/21127436650\/sizes\/sq\/", "media": "photo" },
        |      { "label": "Large Square", "width": "150", "height": "150", "source": "https:\/\/farm1.staticflickr.com\/753\/21127436650_701de65407_q.jpg", "url": "https:\/\/www.flickr.com\/photos\/133459415@N05\/21127436650\/sizes\/q\/", "media": "photo" },
        |      { "label": "Thumbnail", "width": 80, "height": 100, "source": "https:\/\/farm1.staticflickr.com\/753\/21127436650_701de65407_t.jpg", "url": "https:\/\/www.flickr.com\/photos\/133459415@N05\/21127436650\/sizes\/t\/", "media": "photo" },
        |      { "label": "Small", "width": "192", "height": "240", "source": "https:\/\/farm1.staticflickr.com\/753\/21127436650_701de65407_m.jpg", "url": "https:\/\/www.flickr.com\/photos\/133459415@N05\/21127436650\/sizes\/s\/", "media": "photo" },
        |      { "label": "Small 320", "width": "256", "height": "320", "source": "https:\/\/farm1.staticflickr.com\/753\/21127436650_701de65407_n.jpg", "url": "https:\/\/www.flickr.com\/photos\/133459415@N05\/21127436650\/sizes\/n\/", "media": "photo" },
        |      { "label": "Medium", "width": "400", "height": "500", "source": "https:\/\/farm1.staticflickr.com\/753\/21127436650_701de65407.jpg", "url": "https:\/\/www.flickr.com\/photos\/133459415@N05\/21127436650\/sizes\/m\/", "media": "photo" },
        |      { "label": "Medium 640", "width": "512", "height": "640", "source": "https:\/\/farm1.staticflickr.com\/753\/21127436650_701de65407_z.jpg", "url": "https:\/\/www.flickr.com\/photos\/133459415@N05\/21127436650\/sizes\/z\/", "media": "photo" },
        |      { "label": "Medium 800", "width": "640", "height": "800", "source": "https:\/\/farm1.staticflickr.com\/753\/21127436650_701de65407_c.jpg", "url": "https:\/\/www.flickr.com\/photos\/133459415@N05\/21127436650\/sizes\/c\/", "media": "photo" },
        |      { "label": "Large", "width": "819", "height": "1024", "source": "https:\/\/farm1.staticflickr.com\/753\/21127436650_701de65407_b.jpg", "url": "https:\/\/www.flickr.com\/photos\/133459415@N05\/21127436650\/sizes\/l\/", "media": "photo" },
        |      { "label": "Original", "width": "1080", "height": "1350", "source": "https:\/\/farm1.staticflickr.com\/753\/21127436650_ba7cdae1fe_o.jpg", "url": "https:\/\/www.flickr.com\/photos\/133459415@N05\/21127436650\/sizes\/o\/", "media": "photo" }
        |    ] }, "stat": "ok" }
      """.stripMargin

    val result = input.decodeOption[FlickrClient.GetSizesResult]
    assertFalse(result.isEmpty)

    val sizeList = result
      .filter(p => "ok" == p.stat)
      .map(p => p.sizes.size)

    assertFalse(sizeList.isEmpty)

    val source = for {
      list <- sizeList
    } yield list.filter("Medium" == _.label).head.source

    assertFalse(source.isEmpty)
  }

  @Test def testNoMediumGetSizesParse() {
    val input =
      """
        |{ "sizes": { "canblog": 0, "canprint": 0, "candownload": 1,
        |    "size": [
        |      { "label": "Square", "width": 75, "height": 75, "source": "https:\/\/farm1.staticflickr.com\/753\/21127436650_701de65407_s.jpg", "url": "https:\/\/www.flickr.com\/photos\/133459415@N05\/21127436650\/sizes\/sq\/", "media": "photo" },
        |      { "label": "Large Square", "width": "150", "height": "150", "source": "https:\/\/farm1.staticflickr.com\/753\/21127436650_701de65407_q.jpg", "url": "https:\/\/www.flickr.com\/photos\/133459415@N05\/21127436650\/sizes\/q\/", "media": "photo" },
        |      { "label": "Thumbnail", "width": 80, "height": 100, "source": "https:\/\/farm1.staticflickr.com\/753\/21127436650_701de65407_t.jpg", "url": "https:\/\/www.flickr.com\/photos\/133459415@N05\/21127436650\/sizes\/t\/", "media": "photo" },
        |      { "label": "Small", "width": "192", "height": "240", "source": "https:\/\/farm1.staticflickr.com\/753\/21127436650_701de65407_m.jpg", "url": "https:\/\/www.flickr.com\/photos\/133459415@N05\/21127436650\/sizes\/s\/", "media": "photo" },
        |      { "label": "Small 320", "width": "256", "height": "320", "source": "https:\/\/farm1.staticflickr.com\/753\/21127436650_701de65407_n.jpg", "url": "https:\/\/www.flickr.com\/photos\/133459415@N05\/21127436650\/sizes\/n\/", "media": "photo" },
        |      { "label": "Large", "width": "819", "height": "1024", "source": "https:\/\/farm1.staticflickr.com\/753\/21127436650_701de65407_b.jpg", "url": "https:\/\/www.flickr.com\/photos\/133459415@N05\/21127436650\/sizes\/l\/", "media": "photo" },
        |      { "label": "Original", "width": "1080", "height": "1350", "source": "https:\/\/farm1.staticflickr.com\/753\/21127436650_ba7cdae1fe_o.jpg", "url": "https:\/\/www.flickr.com\/photos\/133459415@N05\/21127436650\/sizes\/o\/", "media": "photo" }
        |    ] }, "stat": "ok" }
      """.stripMargin

    val result = input.decodeOption[FlickrClient.GetSizesResult]
    assertFalse(result.isEmpty)

    val sizeList = result
      .filter(p => "ok" == p.stat)
      .map(p => p.sizes.size)

    assertFalse(sizeList.isEmpty)

    val source = for {
      list <- sizeList
      medium = list.filter("Medium" == _.label) if medium.nonEmpty
    } yield medium.head.source

    assertTrue(source.isEmpty)
  }

  @Test def testWrongGetSizesParse() {
    val input = "Wrong json string"

    val result = input.decodeOption[FlickrClient.GetSizesResult]
    assertTrue(result.isEmpty)
  }

  @Test def testGetSizes() {
    val result = FlickrClient.getSizes(Some("21127436650"))
    assertFalse(result.isEmpty)
  }

  @Test def testGetSizes2() {
    val result = FlickrClient.getSizes(None)
    assertTrue(result.isEmpty)
  }

  @Test def testGetSizes3() {
    val result = FlickrClient.getSizes(FlickrClient.searchFirst(Some("flower")))
    assertFalse(result.isEmpty)
  }

  /*
  @Test def testGetSizes4() {
    val result = FlickrClient.getSizes(FlickrClient.searchFirst(None))
    assertTrue(result.isEmpty)
  }
  */
}
