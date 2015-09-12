package ru.triton265.flickrfirstimagescala.app

import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.view.View.OnKeyListener
import android.view.inputmethod.InputMethodManager
import android.view.{KeyEvent, View, ViewGroup, LayoutInflater}
import android.widget.{EditText, ImageView, Toast}
import ru.triton265.flickrfirstimagescala.app.FlickrClient.Size
import rx.android.schedulers.AndroidSchedulers
import rx.lang.scala.schedulers.IOScheduler
import rx.lang.scala.{JavaConversions, Observable, Subscription}

object MainFragment {
  private val EXTRA_IMAGE_URL: String = "EXTRA_IMAGE_URL"
}

class MainFragment extends Fragment {
  private var _subscription: Option[Subscription] = None
  private var _imageView: Option[ImageView] = None
  private var _imageUrl: Option[String] = None

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setRetainInstance(true)
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    val rootView: View = inflater.inflate(R.layout.fragment_main, container, false)

    _imageView = Some(rootView.findViewById(R.id.imageView).asInstanceOf[ImageView])

    val editText = rootView.findViewById(R.id.editText).asInstanceOf[EditText]
    editText.setOnKeyListener(new OnKeyListener {
      override def onKey(view: View, keyCode: Int, keyEvent: KeyEvent): Boolean = {
        if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (KeyEvent.KEYCODE_ENTER == keyCode)) {
          // Search for entered text.
          findImage(Some(view.asInstanceOf[EditText].getText.toString))

          // Hide soft keyboard.
          val imm = getActivity().getSystemService(Context.INPUT_METHOD_SERVICE).asInstanceOf[InputMethodManager]
          imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

          return true
        }
        false
      }
    })

    rootView
  }

  override def onDestroy() {
    super.onDestroy()
    _subscription.foreach(_.unsubscribe())
  }

  private def findImage(searchTextOption: Option[String]) {
    /*
    searchTextOption.map( text => {
      Observable[Option[String]](subscriber => {
        if (!subscriber.isUnsubscribed) {
          subscriber.onNext(FlickrClient.searchFirst(Some(text))) // Do flickr search request
          subscriber.onCompleted()
        }
      }).map( searchResultOption => {
        searchResultOption.map( searchResult => {
          Observable[Option[List[Size]]] (subscriber => {
            if (!subscriber.isUnsubscribed) {
              subscriber.onNext(FlickrClient.getSizes(Some(searchResult))) // Do flickr getsizes request
              subscriber.onCompleted()
            }
          })
        }).getOrElse(
            Toast.makeText(getActivity, R.string.error_nothing_found, Toast.LENGTH_SHORT).show()
        )
      }).subscribeOn(IOScheduler())
        .observeOn(JavaConversions.javaSchedulerToScalaScheduler(AndroidSchedulers.mainThread()))
    }).getOrElse(
        Toast.makeText(getActivity, R.string.error_empty_search_text, Toast.LENGTH_SHORT).show()
    )
    */
    val s = createSearchObservable(searchTextOption)
      .flatMap(createGetSizesObservable)
      .subscribeOn(IOScheduler())
      .observeOn(JavaConversions.javaSchedulerToScalaScheduler(AndroidSchedulers.mainThread()))
      .subscribe(sizes => {
        // Get source of medium size.
        val mediumSizeImageUrl = for {
          list <- sizes
          medium = list.filter("Medium" == _.label) if medium.nonEmpty
        } yield medium.head.source

        setImageUrl(mediumSizeImageUrl)
        updateImageView()
      })

    _subscription = Some(s)
  }

  private def createSearchObservable(searchTextOption: Option[String]): Observable[Option[String]] = {
    searchTextOption
      .map(searchText => {
        Observable[Option[String]](subscriber => {
          if (!subscriber.isUnsubscribed) {
            subscriber.onNext(FlickrClient.searchFirst(Some(searchText))) // Do flickr search request
            subscriber.onCompleted()
          }
        })
    }).getOrElse({
      Toast.makeText(getActivity, R.string.error_empty_search_text, Toast.LENGTH_SHORT).show()
      Observable.empty
    })
  }

  private def createGetSizesObservable(photoIdOption: Option[String]): Observable[Option[List[Size]]] = {
    photoIdOption
      .map(photoId => {
        Observable[Option[List[Size]]](subscriber => {
          if (!subscriber.isUnsubscribed) {
            subscriber.onNext(FlickrClient.getSizes(Some(photoId))) // Do flickr getsizes request
            subscriber.onCompleted()
          }
        })
    }).getOrElse({
      Toast.makeText(getActivity, R.string.error_nothing_found, Toast.LENGTH_SHORT).show()
      Observable.empty
    })
  }

  private def setImageUrl(urlOption: Option[String]) = _imageUrl = urlOption

  private def updateImageView() = ???
}
