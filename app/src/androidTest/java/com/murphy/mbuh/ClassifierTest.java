package com.murphy.mbuh;

import static com.google.common.truth.Truth.assertThat;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.murphy.mbuh.tflite.Classifier;
import com.murphy.mbuh.tflite.Classifier.Device;
import com.murphy.mbuh.tflite.Classifier.Model;
import com.murphy.mbuh.tflite.Classifier.Recognition;

/** Golden test for Image Classification Reference app. */
@RunWith(AndroidJUnit4.class)
public class ClassifierTest {

  @Rule
  public ActivityTestRule<ClassifierActivity> rule =
      new ActivityTestRule<>(ClassifierActivity.class);

  private static final String[] INPUTS = {"fox.jpg"};
  private static final String[] GOLDEN_OUTPUTS = {"fox-mobilenet_v1_1.0_224.txt"};

  @Test
  public void classificationResultsShouldNotChange() throws IOException {
    ClassifierActivity activity = rule.getActivity();
    Classifier classifier = Classifier.create(activity, Model.FLOAT, Device.CPU, 1);
    for (int i = 0; i < INPUTS.length; i++) {
      String imageFileName = INPUTS[i];
      String goldenOutputFileName = GOLDEN_OUTPUTS[i];
      Bitmap input = loadImage(imageFileName);
      List<Recognition> goldenOutput = loadRecognitions(goldenOutputFileName);

      List<Recognition> result = classifier.recognizeImage(input, 0);
      Iterator<Recognition> goldenOutputIterator = goldenOutput.iterator();

      for (Recognition actual : result) {
        Assert.assertTrue(goldenOutputIterator.hasNext());
        Recognition expected = goldenOutputIterator.next();
        assertThat(actual.getTitle()).isEqualTo(expected.getTitle());
        assertThat(actual.getConfidence()).isWithin(0.01f).of(expected.getConfidence());
      }
    }
  }

  private static Bitmap loadImage(String fileName) {
    AssetManager assetManager =
        InstrumentationRegistry.getInstrumentation().getContext().getAssets();
    InputStream inputStream = null;
    try {
      inputStream = assetManager.open(fileName);
    } catch (IOException e) {
      Log.e("Test", "Cannot load image from assets");
    }
    return BitmapFactory.decodeStream(inputStream);
  }

  private static List<Recognition> loadRecognitions(String fileName) {
    AssetManager assetManager =
        InstrumentationRegistry.getInstrumentation().getContext().getAssets();
    InputStream inputStream = null;
    try {
      inputStream = assetManager.open(fileName);
    } catch (IOException e) {
      Log.e("Test", "Cannot load probability results from assets");
    }
    Scanner scanner = new Scanner(inputStream);
    List<Recognition> result = new ArrayList<>();
    while (scanner.hasNext()) {
      String category = scanner.next();
      category = category.replace('_', ' ');
      if (!scanner.hasNextFloat()) {
        break;
      }
      float probability = scanner.nextFloat();
      Recognition recognition = new Recognition(null, category, probability, null);
      result.add(recognition);
    }
    return result;
  }
}
