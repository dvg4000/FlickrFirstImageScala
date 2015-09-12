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
      r <- result if "ok" == r.stat
      photos = r.photos if photos.total > 0 && photos.photo.nonEmpty
    } yield photos.photo.head.id
  }

  def getSizes(id: Option[String]) : Option[List[Size]] = {
    return None
  }

  // For Argonaut.
  // Parse search result.
  case class Photo(id: String)
  case class Photos(total: Long, photo: List[Photo])
  case class SearchResult(stat: String, photos: Photos)

  object Photo {
    implicit def PhotoCodecJson: CodecJson[Photo]
      = casecodec1(Photo.apply, Photo.unapply)("id")
  }

  object Photos {
    implicit def PhotosCodecJson: CodecJson[Photos]
      = casecodec2(Photos.apply, Photos.unapply)("total", "photo")
  }

  object SearchResult {
    implicit def SearchResultCodecJson: CodecJson[SearchResult]
      = casecodec2(SearchResult.apply, SearchResult.unapply)("stat", "photos")
  }

  // Parse get sizes reuslt.
  case class Size(label: String, width: Long, height: Long, source: String)
  case class Sizes(size: List[Size])
  case class GetSizesResult(stat: String, sizes: Sizes)

  object Size {
    implicit def SizeCodecJson: CodecJson[Size]
      = casecodec4(Size.apply, Size.unapply)("label", "width", "height", "source")
  }

  object Sizes {
    implicit def SizesCodecJson: CodecJson[Sizes]
      = casecodec1(Sizes.apply, Sizes.unapply)("size")
  }

  object GetSizesResult {
    implicit def GetSizesResultCodecJson: CodecJson[GetSizesResult]
      = casecodec2(GetSizesResult.apply, GetSizesResult.unapply)("stat", "sizes")
  }
}

