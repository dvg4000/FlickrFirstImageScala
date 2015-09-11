package ru.triton265.flickrfirstimagescala.app

import android.app.{Fragment, Activity}
import android.os.Bundle
import android.view.{ViewGroup, LayoutInflater, View}

class MainActivity extends Activity {

  protected override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_main)

    if (savedInstanceState == null) {
      getFragmentManager.beginTransaction.add(R.id.container, new MainFragment()).commit
    }
  }
}
