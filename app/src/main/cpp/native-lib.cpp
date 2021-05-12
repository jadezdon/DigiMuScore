#include <jni.h>
#include <string>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/objdetect.hpp>
#include <android/log.h>

#define TAG "native-lib-tag"
#define LOGPRINT(...) __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__)

using namespace std;
using namespace cv;

bool compareContourAreas(const vector<Point> &contour1,
                         const vector<Point> &contour2) {
    double i = fabs(contourArea(Mat(contour1)));
    double j = fabs(contourArea(Mat(contour2)));
    return (i > j);
}

vector<Point> getDocumentPoints(jlong mat_addr) {

    Mat matGrey, edges;
    Mat &src = *(Mat *) mat_addr;

    //Step 1: Detect edges.
    // grayscale image
    cvtColor(src, matGrey, COLOR_RGB2GRAY);
    // blur image
    GaussianBlur(matGrey, matGrey, Size(7, 7), 0);
    // dilate
    Mat kernel = getStructuringElement(MORPH_RECT, Size(9, 9));
    morphologyEx(matGrey, matGrey, MORPH_CLOSE, kernel);
    // edge detection with Canny
    Canny(matGrey, edges, 50, 120);


    //Step 2: Use the edges in the image to find the contour (outline) representing the piece of paper being scanned.
    vector<vector<Point> > contours;
    vector<Vec4i> hierarchy;

    findContours(edges, contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
    // sort contours
    sort(contours.begin(), contours.end(), compareContourAreas);

    vector<vector<Point> > contoursPoly(contours.size());
    vector<Point> biggestContour;

    for (int i = 0; i < contours.size() && i < 5; i++) {
        double peri = arcLength(contours[i], true);
        approxPolyDP(contours[i], contoursPoly[i], 0.02 * peri, true);
        if (contoursPoly[i].size() >= 4 &&
            contourArea(contoursPoly[i]) > src.rows * src.cols * 0.25) {
            biggestContour = contoursPoly[i];
        }
    }
    return biggestContour;
}

// reorder points as 0 (top left), 1 (top right), 2 (bottom left), 3 (bottom right)
vector<Point> reorderPoints(vector<Point> points) {
    vector<Point> orderedPoints;
    vector<int> sum, sub;

    for (int i = 0; i < 4; i++) {
        sum.push_back(points[i].x + points[i].y);
        sub.push_back(points[i].x - points[i].y);
    }

    orderedPoints.push_back(points[min_element(sum.begin(), sum.end()) - sum.begin()]); // 0
    orderedPoints.push_back(points[max_element(sub.begin(), sub.end()) - sub.begin()]); // 1
    orderedPoints.push_back(points[min_element(sub.begin(), sub.end()) - sub.begin()]); // 2
    orderedPoints.push_back(points[max_element(sum.begin(), sum.end()) - sum.begin()]); // 3

    return orderedPoints;
}

Mat getSharpenKernel() {
    Mat kernel(3, 3, CV_32F, Scalar(0));
    kernel.at<float>(1, 1) = 5.0;
    kernel.at<float>(0, 1) = -1.0;
    kernel.at<float>(2, 1) = -1.0;
    kernel.at<float>(1, 0) = -1.0;
    kernel.at<float>(1, 2) = -1.0;
    return kernel;
}

extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_zhouppei_digimuscore_ui_scanner_ScannerActivity_detectEdges(JNIEnv *env, jobject thiz,
                                                                     jlong mat_addr) {
    vector<Point> points = getDocumentPoints(mat_addr);
    jfloatArray result = env->NewFloatArray(points.size() * 2);
    if (points.size() == 4) {
        vector<Point> documentPoints = reorderPoints(points);
        jfloat array[documentPoints.size() * 2];
        for (int i = 0; i < documentPoints.size(); i++) {
            array[2 * i] = documentPoints[i].x;
            array[2 * i + 1] = documentPoints[i].y;
        }
        env->SetFloatArrayRegion(result, 0, documentPoints.size() * 2, array);
    }
    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_zhouppei_digimuscore_ui_scanner_ScannerActivity_setScannedDocumentMat(JNIEnv *env,
                                                                               jobject thiz,
                                                                               jlong mat_addr,
                                                                               jlong mat_result_addr,
                                                                               jfloatArray points) {
    Mat &src = *(Mat *) mat_addr;
    Mat &result = *(Mat *) mat_result_addr;

    jsize size = env->GetArrayLength(points);
    vector<float> initialPoints(size);
    env->GetFloatArrayRegion(points, jsize{0}, size, &initialPoints[0]);
    //Step 3: Apply a perspective transform to obtain the top-down view of the document.
    vector<Point> documentPoints;
    for (int i = 0; i < initialPoints.size(); i += 2) {
        documentPoints.emplace_back(initialPoints[i], initialPoints[i + 1]);
    }

    int w = documentPoints[1].x - documentPoints[0].x;
    int h = documentPoints[2].y - documentPoints[0].y;

    Point2f srcPoint[4] = {documentPoints[0], documentPoints[1], documentPoints[2],
                           documentPoints[3]};
    Point2f dstPoint[4] = {{0.0f,      0.0f},
                           {(float) w, 0.0f},
                           {0.0f,      (float) h},
                           {(float) w, (float) h}};

    Mat matrix = getPerspectiveTransform(srcPoint, dstPoint);
    warpPerspective(src, result, matrix, Point(w, h));
}

extern "C"
JNIEXPORT void JNICALL
Java_com_zhouppei_digimuscore_ui_scanner_ScannerActivity_sharpenMat(JNIEnv *env, jobject thiz,
                                                                    jlong mat_addr) {
    // https://github.com/zishan0215/opencv/blob/master/sharpen.cpp
    Mat &src = *(Mat *) mat_addr;
    filter2D(src, src, src.depth(), getSharpenKernel());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_zhouppei_digimuscore_ui_scanner_ScannerActivity_rotateMat(JNIEnv *env, jobject thiz,
                                                                   jlong mat_addr) {
    Mat &src = *(Mat *) mat_addr;
    transpose(src, src);
    flip(src, src, 1);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_zhouppei_digimuscore_ui_scanner_ScannerActivity_convertToBlackWhite(JNIEnv *env,
                                                                             jobject thiz,
                                                                             jlong mat_addr) {
//    https://stackoverflow.com/questions/44047819/increase-image-brightness-without-overflow/44054699#44054699
    Mat &src = *(Mat *) mat_addr;

    Mat rgbChannels[src.channels()];
    split(src, rgbChannels);
    vector<Mat> resultRGBChannels;
    for (const auto &rgb : rgbChannels) {
        Mat kernel(7, 7, CV_32F, Scalar(1.0));
        Mat dilated, diffImg, resultChannel;

        dilate(rgb, dilated, kernel);
        medianBlur(dilated, dilated, 21);
        absdiff(rgb, dilated, diffImg);
        bitwise_not(diffImg, diffImg);
        normalize(diffImg, resultChannel, 0, 255, NORM_MINMAX);
        resultRGBChannels.push_back(resultChannel);
    }
    merge(resultRGBChannels, src);
    filter2D(src, src, src.depth(), getSharpenKernel());
    threshold(src, src, 230, 0, THRESH_TRUNC);
    normalize(src, src, 0, 255, NORM_MINMAX, CV_8UC1);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_zhouppei_digimuscore_ui_scanner_ScannerActivity_grayscale(JNIEnv *env, jobject thiz,
                                                                   jlong mat_addr) {
    Mat &src = *(Mat *) mat_addr;
    if (src.channels() > 1) {
        cvtColor(src, src, COLOR_RGB2GRAY);
    }
}