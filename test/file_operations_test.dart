import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:file_operations/file_operations.dart';

void main() {
  const MethodChannel channel = MethodChannel('file_operations');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await FileOperations.platformVersion, '42');
  });
}
