package ru.triton265.flickrfirstimagescala.app

import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View.OnKeyListener
import android.view.inputmethod.InputMethodManager
import android.view.{KeyEvent, View, ViewGroup, LayoutInflater}
import android.widget.{EditText, ImageView, Toast}
import com.bumptech.glide.Glide
import ru.triton265.flickrfirstimagescala.app.FlickrClient.Size
import rx.android.schedulers.AndroidSchedulers
import rx.lang.scala.schedulers.IOScheduler
import rx.lang.scala.{JavaConversions, Observable, Subscription}

object MainFragment {
  private val TAG_DEBUG = MainFragment.getClass.getSimpleName
  private val EXTRA_IMAGE_URL: String = "EXTRA_IMAGE_URL"
}

class MainFragment extends Fragment {
  private var _subscriptionOption: Option[Subscription] = None
  private var _imageViewOption: Option[ImageView] = None
  private var _imageUrlOption: Option[String] = None

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setRetainInstance(true)
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    val rootView: View = inflater.inflate(R.layout.fragment_main, container, false)

    _imageViewOption = Option(rootView.findViewById(R.id.imageView).asInstanceOf[ImageView])
    if (null != savedInstanceState) {
      setImageUrl(Option(savedInstanceState.getString(MainFragment.EXTRA_IMAGE_URL)))
    }
    updateImageView()

    val editText = rootView.findViewById(R.id.editText).asInstanceOf[EditText]
    editText.setOnKeyListener(new OnKeyListener {
      override def onKey(view: View, keyCode: Int, keyEvent: KeyEvent): Boolean = {
        if ((keyEvent.getAction == KeyEvent.ACTION_DOWN) && (KeyEvent.KEYCODE_ENTER == keyCode)) {
          // Search for entered text.
          findImage(Option(view.asInstanceOf[EditText].getText.toString))

          // Hide soft keyboard.
          val imm = getActivity.getSystemService(Context.INPUT_METHOD_SERVICE).asInstanceOf[InputMethodManager]
          imm.hideSoftInputFromWindow(view.getApplicationWindowToken, InputMethodManager.HIDE_NOT_ALWAYS)

          return true
        }
        false
      }
    })

    rootView
  }

  override def onSaveInstanceState(outState: Bundle): Unit = {
    super.onSaveInstanceState(outState)
    _imageUrlOption.foreach(outState.putString(MainFragment.EXTRA_IMAGE_URL, _))
  }

  override def onDestroy(): Unit = {
    super.onDestroy()
    _subscriptionOption.foreach(_.unsubscribe())
  }

  private def findImage(searchTextOption: Option[String]) {
    /*
    searchTextOption.map( text => {
      Observable[Option[String]](subscriber => {
        if (!subscriber.isUnsubscribed) {
          subscriber.onNext(FlickrClient.searchFirst(Option(text))) // Do flickr search request
          subscriber.onCompleted()
        }
      }).map( searchResultOption => {
        searchResultOption.map( searchResult => {
          Observable[Option[List[Size]]] (subscriber => {
            if (!subscriber.isUnsubscribed) {
              subscriber.onNext(FlickrClient.getSizes(Option(searchResult))) // Do flickr getsizes request
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
      .subscribe(
        sizes => {
          // Get source of medium size.
          val mediumSizeImageUrl = for {
            list <- sizes
            medium = list.filter("Medium" == _.label) if medium.nonEmpty
          } yield medium.head.source

          setImageUrl(mediumSizeImageUrl)
          updateImageView()},
        e => {
          Toast.makeText(getActivity, R.string.error_unknown, Toast.LENGTH_SHORT).show()
          Log.d(MainFragment.TAG_DEBUG, "onError: " + Log.getStackTraceString(e), e);
        }
      )

    _subscriptionOption = Option(s)
  }

  private def createSearchObservable(searchTextOption: Option[String]): Observable[Option[String]] = {
    searchTextOption
      .map(searchText => {
        Observable[Option[String]](subscriber => {
          if (!subscriber.isUnsubscribed) {
            subscriber.onNext(FlickrClient.searchFirst(Option(searchText))) // Do flickr search request
            subscriber.onCompleted()
          }
        })
    }).getOrElse({
      runOnUiThread( () => {
        Toast.makeText(getActivity, R.string.error_empty_search_text, Toast.LENGTH_SHORT).show() })
      Observable.empty
    })
  }

  private def createGetSizesObservable(photoIdOption: Option[String]): Observable[Option[List[Size]]] = {
    photoIdOption
      .map(photoId => {
        Observable[Option[List[Size]]](subscriber => {
          if (!subscriber.isUnsubscribed) {
            subscriber.onNext(FlickrClient.getSizes(Option(photoId))) // Do flickr getsizes request
            subscriber.onCompleted()
          }
        })
    }).getOrElse({
      runOnUiThread( () => {
        Toast.makeText(getActivity, R.string.error_nothing_found, Toast.LENGTH_SHORT).show() })
      Observable.empty
    })
  }

  private def setImageUrl(urlOption: Option[String]) = _imageUrlOption = urlOption

  private def updateImageView() {
    val res = for {
      imageUrl <- _imageUrlOption
      imageView <- _imageViewOption
    } yield {
        Glide
          .`with`(this)
          .load(imageUrl)
          .centerCrop()
          .placeholder(android.R.drawable.stat_sys_download)
          .into(imageView)
    }
    res.getOrElse(Toast.makeText(getActivity, R.string.error_empty_image_url, Toast.LENGTH_SHORT).show())
  }

  private def runOnUiThread(task: () => Unit): Unit = {
    Option(getActivity).foreach(
      _.runOnUiThread(new Runnable { override def run(): Unit = { task() } })
    )
  }
}
