package ru.triton265.flickrfirstimagescala.app

import android.app.Fragment
import android.os.Bundle
import android.support.annotation.Nullable
import android.view.{View, ViewGroup, LayoutInflater}

class MainFragment extends Fragment {

  override def onCreate(@Nullable savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setRetainInstance(true)
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    val rootView: View = inflater.inflate(R.layout.fragment_main, container, false)
    return rootView;
  }
}
