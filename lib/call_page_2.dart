import 'dart:developer' as developer;
import 'dart:io';
import 'dart:ui';

import 'package:flutter/cupertino.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:get/get.dart';
import 'package:stringee_flutter_plugin/stringee_flutter_plugin.dart';

import 'bordered_avatar.dart';
import 'constant.dart';

class CallPage extends StatefulWidget {
  const CallPage({Key? key, required this.stringeeClient, required this.token})
      : super(key: key);
  final StringeeClient stringeeClient;
  final String token;

  @override
  _CallPageState createState() => _CallPageState();
}

class _CallPageState extends State<CallPage> {
  late final _call = StringeeCall(widget.stringeeClient);
  var isCallInitiated = true;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SizedBox(
        width: Get.width,
        height: Get.height,
        child: Stack(
          children: [
            generateBlurredImage(),
            Positioned(
                width: Get.width,
                top: Get.height * 0.27,
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: const [
                    BorderedAvatar(avatarSize: 96, radius: 16),
                    SizedBox(height: 32),
                    Text("Dr. Ngô Minh Tuyết",
                        style: TextStyle(
                            color: Colors.white,
                            fontSize: 24,
                            fontWeight: FontWeight.bold)),
                    SizedBox(height: 16),
                    Text("Connecting ...",
                        style: TextStyle(
                            color: Colors.white,
                            fontSize: 24,
                            fontWeight: FontWeight.bold))
                  ],
                )),
            Positioned(
              width: Get.width,
              bottom: 32,
              child: Center(
                child: Container(
                  padding: const EdgeInsets.all(16),
                  width: 72,
                  height: 72,
                  decoration: BoxDecoration(
                      color: Colors.red,
                      borderRadius: BorderRadius.circular(16)),
                  child: InkWell(
                      onTap: () {
                        isCallInitiated = false;
                        _call.hangup().then((result) {
                          bool status = result['status'];
                          if (status) {
                            ///success
                          } else {
                            ///false
                          }
                        });
                        Navigator.pop(context);
                      },
                      child: SvgPicture.asset('assets/icons/ic_call_reject.svg',
                          color: Colors.white)),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget generateBlurredImage() {
    const ImageProvider<Object> _imageProvider =
        AssetImage('assets/images/avatar_test_2.png');
    return Container(
      decoration: const BoxDecoration(
        image: DecorationImage(
          image: _imageProvider,
          fit: BoxFit.cover,
        ),
      ),
      //I blurred the parent container to blur background image, you can get rid of this part
      child: BackdropFilter(
        filter: ImageFilter.blur(sigmaX: 5.0, sigmaY: 5.0),
        child: Container(
          //you can change opacity with color here(I used black) for background.
          decoration: BoxDecoration(color: Colors.black.withOpacity(0.2)),
        ),
      ),
    );
  }

  void _setupCallTimeout() async {
    Future.delayed(const Duration(minutes: 1), () {
      if (isCallInitiated) {
        isCallInitiated = false;
        _call.hangup().then((result) {
          bool status = result['status'];
          if (status) {
            ///success
          } else {
            ///false
          }
        });
      }
    });
  }

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
    final callerName = widget.token == token1 ? "User01" : "User02";
    final calleeName = widget.token == token1 ? "User02" : "User01";
    print("calling to $calleeName");
    final params = MakeCallParams(callerName, calleeName,
        isVideoCall: false, videoQuality: VideoQuality.normal);
    _call.makeCallFromParams(params).then((result) {
      bool status = result['status'];
      int code = result['code'];
      String message = result['message'];
      print(
          'MakeCall CallBack --- $status - $code - $message - ${_call.id} - ${_call.from} - ${_call.to}');
    });
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
  void handleChangeAudioDeviceEvent(
      AudioDevice audioDevice, List<AudioDevice> availableAudioDevices) {
    print('handleChangeAudioDeviceEvent - $audioDevice');
  }
}
