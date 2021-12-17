import 'package:flutter/material.dart';

class BorderedAvatar extends StatelessWidget {
  const BorderedAvatar({
    Key? key,
    this.imageUrl,
    this.avatarSize = 40,
    required this.radius,
  }) : super(key: key);
  final String? imageUrl;
  final double avatarSize;
  final double radius;

  @override
  Widget build(BuildContext context) {
    return Container(
      height: avatarSize,
      width: avatarSize,
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(radius),
      ),
      child: Container(
        margin: EdgeInsets.all(avatarSize * 0.05),
        child: ClipRRect(
          borderRadius: BorderRadius.circular(radius * 0.75),
          child: Image.asset('assets/images/avatar_test_2.png')
        ),
      ),
    );
  }
}