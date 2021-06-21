
import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';

class FileOperations {
  static const MethodChannel _channel =
      const MethodChannel('file_operations');

  static Future<dynamic> moveFile(String inputFile, String outputFile) async {
    if (await File(inputFile).exists()) {
      String result = await (_channel.invokeMethod(
        "moveFile", { "inputFile": inputFile, "outputFile": outputFile }
      ) as FutureOr<String>);
      if (result.contains("file_operations_error")) {
        return result;
      } else {
        return File(result);
      }
    } else {
      return "Input file not found";
    }
  }
}
