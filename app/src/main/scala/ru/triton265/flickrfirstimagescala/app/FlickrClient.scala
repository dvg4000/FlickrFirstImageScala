package ru.triton265.flickrfirstimagescala.app

import argonaut.Argonaut._
import argonaut.CodecJson

import scalaj.http.{Http, HttpResponse}

object FlickrClient {
  private val FLICKR_BASEURL: String = "https://api.flickr.com/services/rest"

  private val OPTIONS = Map(
    "api_key" -> "96635d88513246ce894292de45c8f704",
    "format" -> "json",
    "nojsoncallback" -> "1")

  def searchFirst(searchText: Option[String]) : Option[String] = {
    val response: HttpResponse[String] = Http(FlickrClient.FLICKR_BASEURL)
      .params(FlickrClient.OPTIONS)
      .param("method", "flickr.photos.search")
      .param("per_page", "1")
      .param("text", searchText.getOrElse(""))
      .asString

    if (response.isError) {
      return None
    }

    val result = response.body.decodeOption[FlickrClient.SearchResult]
    for {
      r <- result if r.stat == "ok"
      photos = r.photos if photos.total > 0 && photos.photo.nonEmpty
    } yield photos.photo.head.id
  }

  // For Argonaut.
  // Parse search result
  case class Photo(id: String)
  object Photo {
    implicit def PhotoCodecJson: CodecJson[Photo]
      = casecodec1(Photo.apply, Photo.unapply)("id")
  }

  case class Photos(total: Long, photo: List[Photo])
  object Photos {
    implicit def PhotosCodecJson: CodecJson[Photos]
      = casecodec2(Photos.apply, Photos.unapply)("total", "photo")
  }

  case class SearchResult(stat: String, photos: Photos)
  object SearchResult {
    implicit def SearchResultCodecJson: CodecJson[SearchResult]
      = casecodec2(SearchResult.apply, SearchResult.unapply)("stat", "photos")
  }
}

