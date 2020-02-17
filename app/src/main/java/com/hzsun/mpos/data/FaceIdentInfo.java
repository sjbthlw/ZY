package com.hzsun.mpos.data;

import java.util.ArrayList;
import java.util.List;

public class FaceIdentInfo {

    public int iListNum;        //人脸识别名单数量
    public List<String> FaceAccList = new ArrayList<>(); //人脸名单列表
    public List<String> FaceNameList = new ArrayList<>(); //人脸名单全列表
    public int iMaxFaceNum = 1;  // 最大支持的检测人脸数量
    public float fLiveThrehold = 0.5f; //活体检测率
    public int iPupilDistance = 10;  // 瞳距
    public float fFraction = 0.65f;

    public int getiListNum() {
        return iListNum;
    }

    public void setiListNum(int iListNum) {
        this.iListNum = iListNum;
    }

    public List<String> getFaceNameList() {
        return FaceNameList;
    }

    public void setFaceNameList(List<String> faceNameList) {
        FaceNameList = faceNameList;
    }

    public int getiMaxFaceNum() {
        return iMaxFaceNum;
    }

    public void setiMaxFaceNum(int iMaxFaceNum) {
        this.iMaxFaceNum = iMaxFaceNum;
    }

    public float getfLiveThrehold() {
        return fLiveThrehold;
    }

    public void setfLiveThrehold(float fLiveThrehold) {
        this.fLiveThrehold = fLiveThrehold;
    }

    public int getiPupilDistance() {
        return iPupilDistance;
    }

    public void setiPupilDistance(int iPupilDistance) {
        this.iPupilDistance = iPupilDistance;
    }

    public float getFraction() {
        return fFraction;
    }

    public void setFraction(float fraction) {
        fFraction = fraction;
    }

    @Override
    public String toString() {
        return "FaceIdentInfo{" +
                "iListNum=" + iListNum +
                ", FaceNameList=" + FaceNameList +
                ", iMaxFaceNum=" + iMaxFaceNum +
                ", fLiveThrehold=" + fLiveThrehold +
                ", iPupilDistance=" + iPupilDistance +
                ", Fraction=" + fFraction +
                '}';
    }
}
