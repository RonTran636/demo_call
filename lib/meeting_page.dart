import 'package:flutter/material.dart';

class MeetingPage extends StatelessWidget {
  const MeetingPage({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        child: Center(
          child: Text(
            "Call connected",
            style: TextStyle(fontSize: 36,color: Colors.green),
          ),
        ),
      ),
    );
  }
}
