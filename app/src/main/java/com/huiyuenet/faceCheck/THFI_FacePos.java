package com.huiyuenet.faceCheck;


public class THFI_FacePos {
    public THFI_FacePos () {
        rcFace = new RECT();
        ptLeftEye = new POINT();
        ptRightEye = new POINT();
        ptMouth = new POINT();
        ptNose = new POINT();
        fAngle = new FaceAngle();
        pFacialData = new byte[512];
    }

    public RECT		rcFace;//coordinate of face
    public POINT ptLeftEye;//coordinate of left eye
    public POINT ptRightEye;//coordinate of right eye
    public POINT ptMouth;//coordinate of mouth
    public POINT ptNose;//coordinate of nose
    public FaceAngle fAngle;//value of face angle
    public int			nQuality;//quality of face(from 0 to 100)
    public byte[]      pFacialData;//facial data
	
}
