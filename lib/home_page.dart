import 'dart:io';

import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:stringee_flutter_plugin/stringee_flutter_plugin.dart';

import 'call_page_2.dart';
import 'constant.dart';

class HomePage extends StatefulWidget {
  const HomePage({Key? key}) : super(key: key);

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  final _client = StringeeClient();
  String _token = token1;
  bool isConnected = false;
  String message = "";

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Column(
        children: [
          SizedBox(height: Get.height * 0.2),
          ListTile(
            title: const Text('Token 01'),
            leading: Radio(
                value: token1,
                groupValue: _token,
                onChanged: (String? value) {
                  print(value);
                  setState(() {
                    _token = value!;
                  });
                }),
          ),
          ListTile(
            title: const Text('Token 02'),
            leading: Radio(
              value: token2,
              groupValue: _token,
              onChanged: (String? value) {
                print("token 2 $value");
                setState(() {
                  _token = value!;
                });
              },
            ),
          ),
          const SizedBox(height: 25),
          ElevatedButton(
              onPressed: () => connectToStringeeServer(_token),
              child: const Text("Log in")),
          const SizedBox(height: 8),
          Visibility(visible: isConnected, child: Text(message)),
          const SizedBox(height: 40),
          ElevatedButton(
              onPressed: () {
                if (isConnected) {
                  Get.to(
                      () => CallPage(stringeeClient: _client, token: _token));
                } else {
                  null;
                }
              },
              child: const Text("Start a call"))
        ],
      ),
    );
  }

  connectToStringeeServer(String token) {
    _client.connect(token).then((value) {
      if (value['message'] == 'Success') {
        setState(() {
          print(value);
          isConnected = true;
          registerForStringeeEvents(_client);
          message = value['message'];
          //Send fcm token or voip token to Stringee server
          sendTokenToServer();
        });
      } else {
        setState(() {
          isConnected = true;
          message = value['message'];
        });
      }
    });
  }

  registerForStringeeEvents(StringeeClient stringeeClient) {
    stringeeClient.eventStreamController.stream.listen((event) {
      Map<dynamic, dynamic> map = event;
      switch (map['eventType']) {
        case StringeeClientEvents.didConnect:
          handleDidConnectEvent();
          break;
        case StringeeClientEvents.didDisconnect:
          handleDidDisconnectEvent();
          break;
        case StringeeClientEvents.didFailWithError:
          int code = map['body']['code'];
          String msg = map['body']['message'];
          handleDidFailWithErrorEvent(code, msg);
          break;
        case StringeeClientEvents.requestAccessToken:
          handleRequestAccessTokenEvent();
          break;
        case StringeeClientEvents.didReceiveCustomMessage:
          handleDidReceiveCustomMessageEvent(map['body']);
          break;
        case StringeeClientEvents.incomingCall:
          StringeeCall call = map['body'];
          handleIncomingCallEvent(call);
          break;
        case StringeeClientEvents.incomingCall2:
          StringeeCall2 call = map['body'];
          handleIncomingCall2Event(call);
          break;
        default:
          break;
      }
    });
  }

  void sendTokenToServer() {
    if (Platform.isAndroid) {
      FirebaseMessaging.instance.getToken().then((token) {
        print("fcm token: $token");
        _client
            .registerPush(token!)
            .then((value) => print('Register push ' + value['message']));
      });
    } else {}
  }

  handleDidConnectEvent() {}

  handleDidDisconnectEvent() {}

  handleDidFailWithErrorEvent(int code, String msg) {}

  handleRequestAccessTokenEvent() {}

  handleUserBeginTypingEvent(Map<dynamic, dynamic> map) {}

  handleDidReceiveCustomMessageEvent(Map<dynamic, dynamic> map) {}

  /// Invoked when receive an incoming of StringeeCall
  void handleIncomingCallEvent(StringeeCall call) {
    final _call = StringeeCall(_client);
    _call.initAnswer().then((event) {
      bool status = event['status'];
      print("init answer status : ${event['status']}");
      if (status) {
        ///success
      } else {
        ///false
      }
    });
    _call.answer().then((result) {
      bool status = result['status'];
      if (status) {
        ///success
      } else {
        ///false
      }
    });
  }

  /// Invoked when receive an incoming of StringeeCall2
  void handleIncomingCall2Event(StringeeCall2 call) {}
}
