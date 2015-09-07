// Generated code from Butter Knife. Do not modify!
package com.shenkar.aroundme;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;

public class BgPickerActivity$$ViewBinder<T extends com.shenkar.aroundme.BgPickerActivity> implements ViewBinder<T> {
  @Override public void bind(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131296364, "field 'gridview'");
    target.gridview = finder.castView(view, 2131296364, "field 'gridview'");
  }

  @Override public void unbind(T target) {
    target.gridview = null;
  }
}
