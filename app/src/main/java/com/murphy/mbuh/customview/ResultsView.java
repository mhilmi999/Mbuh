package com.murphy.mbuh.customview;

import java.util.List;
import com.murphy.mbuh.tflite.Classifier.Recognition;

public interface ResultsView {
  public void setResults(final List<Recognition> results);
}
