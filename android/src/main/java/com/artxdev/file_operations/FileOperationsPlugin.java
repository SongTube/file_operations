package com.artxdev.file_operations;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** FileOperationsPlugin */
public class FileOperationsPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;
  private Activity activity;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "file_operations");
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onMethodCall(@NonNull final MethodCall call, @NonNull final Result result) {
    final String[] resultMessage = {"Not implemented"};
    final ExecutorService executor = Executors.newSingleThreadExecutor();
    final Handler handler = new Handler(Looper.getMainLooper());
    executor.execute(new Runnable() {
      @Override
      public void run() {
      if (call.method.equals("moveFile")) {
        String input = call.argument("inputFile");
        String output = call.argument("outputFile");
        File inputFile = new File(input);
        File outputFile = new File(output);
        resultMessage[0] = moveFile(inputFile, outputFile);
      }
      handler.post(new Runnable() {
        @Override
        public void run() {
          result.success(resultMessage[0]);
        }
      });
      }
    });
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  private String moveFile(File inputFile, File outputFile) {

    // Check path
    if (!outputFile.getParentFile().exists()) {
      if (!outputFile.getParentFile().mkdirs()) {
        return "file_operations_error: couldn't create output path: "
                + outputFile.getParentFile();
      }
    }

    // Create new file
    try {
      outputFile.createNewFile();
    } catch (Exception e) {
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      String exceptionAsString = sw.toString();
      return "file_operations_error: "+inputFile.getPath()+" "+exceptionAsString;
    }

    // File Channels
    FileChannel inChannel = null;
    FileChannel outChannel = null;

    try {
      inChannel = new FileInputStream(inputFile).getChannel();
      outChannel = new FileOutputStream(outputFile).getChannel();

      // Move our file
      inChannel.transferTo(0, inChannel.size(), outChannel);

      // delete the original file
      try {
        inputFile.delete();
      } catch (Exception ignored) {}

      // Close channels
      inChannel.close();
      if (outChannel != null)
        outChannel.close();
    }
    catch (Exception e) {
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      String exceptionAsString = sw.toString();
      return "file_operations_error: "+inputFile.getPath()+" "+exceptionAsString;
    }
    return outputFile.getPath();
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    activity = binding.getActivity();
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {}

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    activity = binding.getActivity();
  }

  @Override
  public void onDetachedFromActivity() {}
}
