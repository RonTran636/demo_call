import 'dart:io';

import 'package:flutter/material.dart';
import 'package:stringee_flutter_plugin/stringee_flutter_plugin.dart';

import 'constant.dart';

class CallPage extends StatefulWidget {
  const CallPage({Key? key, required this.stringeeClient, required this.token})
      : super(key: key);
  final StringeeClient stringeeClient;
  final String token;

  @override
  State<CallPage> createState() => _CallPageState();
}

class _CallPageState extends State<CallPage> {
  late final _call = StringeeCall(widget.stringeeClient);

  @override
  void initState() {
    super.initState();
    _call.eventStreamController.stream.listen((event) {
      Map<dynamic, dynamic> map = event;
      switch (map['eventType']) {
        case StringeeCallEvents.didChangeSignalingState:
          handleSignalingStateChangeEvent(map['body']);
          break;
        case StringeeCallEvents.didChangeMediaState:
          handleMediaStateChangeEvent(map['body']);
          break;
        case StringeeCallEvents.didReceiveCallInfo:
          handleReceiveCallInfoEvent(map['body']);
          break;
        case StringeeCallEvents.didHandleOnAnotherDevice:
          handleHandleOnAnotherDeviceEvent(map['body']);
          break;
        case StringeeCallEvents.didReceiveLocalStream:
          handleReceiveLocalStreamEvent(map['body']);
          break;
        case StringeeCallEvents.didReceiveRemoteStream:
          handleReceiveRemoteStreamEvent(map['body']);
          break;

      /// This event only for android
        case StringeeCallEvents.didChangeAudioDevice:
          if (Platform.isAndroid) {
            handleChangeAudioDeviceEvent(
                map['selectedAudioDevice'], map['availableAudioDevices']);
          }
          break;
        default:
          break;
      }
    });
    final calleeToken = widget.token == token1 ? token2 : token1;
    final params = MakeCallParams(widget.token, calleeToken, isVideoCall: false,
        videoQuality: VideoQuality.normal);
    _call.makeCallFromParams(params).then((result) {
      bool status = result['status'];
      int code = result['code'];
      String message = result['message'];
      print('MakeCall CallBack --- $status - $code - $message - ${_call.id} - ${_call.from} - ${_call.to}');
    });
  }

  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      body: Center(child: Text("Connecting")),
    );
  }

  /// Invoked when get Signaling state
  void handleSignalingStateChangeEvent(StringeeSignalingState state) {
    print('handleSignalingStateChangeEvent - $state');
  }

  /// Invoked when get Media state
  void handleMediaStateChangeEvent(StringeeMediaState state) {
    print('handleMediaStateChangeEvent - $state');
  }

  /// Invoked when get Call info
  void handleReceiveCallInfoEvent(Map<dynamic, dynamic> info) {
    print('handleReceiveCallInfoEvent - $info');
  }

  /// Invoked when an incoming call is handle on another device
  void handleHandleOnAnotherDeviceEvent(StringeeSignalingState state) {
    print('handleHandleOnAnotherDeviceEvent - $state');
  }

  /// Invoked when get Local stream in video call
  void handleReceiveLocalStreamEvent(String callId) {
    print('handleReceiveLocalStreamEvent - $callId');
  }

  /// Invoked when get Remote stream in video call
  void handleReceiveRemoteStreamEvent(String callId) {
    print('handleReceiveRemoteStreamEvent - $callId');
  }

  /// Invoked when change Audio device in android
  void handleChangeAudioDeviceEvent(AudioDevice audioDevice,
      List<AudioDevice> availableAudioDevices) {
    print('handleChangeAudioDeviceEvent - $audioDevice');
  }
}
