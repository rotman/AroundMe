// Generated code from Butter Knife. Do not modify!
package com.shenkar.aroundme;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class MapActivity$$ViewBinder<T extends com.shenkar.aroundme.MapActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131296373, "field 'radiusBar'");
    target.radiusBar = finder.castView(view, 2131296373, "field 'radiusBar'");
  }

  @Override public void unbind(T target) {
    target.radiusBar = null;
  }
}
