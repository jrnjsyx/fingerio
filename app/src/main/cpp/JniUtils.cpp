#include <jni.h>
#include <string>
#include "fftsg_h.c"
/*
 * Class:     hust_cc_asynchronousacousticlocalization_utils_JniUtils
 * Method:    getCLanguageString
 * Signature: ()Ljava/lang/String;
 */
extern "C" {
JNIEXPORT jstring JNICALL
Java_com_example_jrnjsyx_fingerio_utils_JniUtils_sayHello(JNIEnv *env, jclass jobj) {
    std::string hello = "HelloWorld!!";
    return env->NewStringUTF(hello.c_str());
}


JNIEXPORT jdoubleArray JNICALL
Java_com_example_jrnjsyx_fingerio_utils_JniUtils_fft(JNIEnv *env, jclass jobj,
                                                                 jdoubleArray data, jint len) {
//    jboolean isCopy;
//    jdouble *dataP = static_cast<double*>(env->GetPrimitiveArrayCritical(data, &isCopy));
    jdouble *dataP = env->GetDoubleArrayElements(data, 0);
    jsize n = env->GetArrayLength(data);
    int l = 1;
    while (l < len) {
        l = l * 2;
    }

    jdouble* sig = new jdouble[2*l];
    for(int i=0;i<l;i++) {
        if (i < n) {
            sig[2 * i] = dataP[i];
            sig[2 * i + 1] = 0;
        } else{
            sig[2*i] = 0;
            sig[2*i+1] = 0;
        }

    }
    cdft(2*l,-1,sig);//4096 12288
    jdoubleArray output = env->NewDoubleArray(2*l);
    env->SetDoubleArrayRegion(output,0,2*l,sig);
    env->ReleaseDoubleArrayElements(data, dataP, 0);
//    env->ReleasePrimitiveArrayCritical(data,dataP,0);
    return output;
}


JNIEXPORT jdoubleArray JNICALL
Java_com_example_jrnjsyx_fingerio_utils_JniUtils_ifft(JNIEnv *env, jclass jobj,
                                                                 jdoubleArray data, jint len) {
//    jboolean isCopy;
//    jdouble *dataP = static_cast<double*>(env->GetPrimitiveArrayCritical(data, &isCopy));
    jdouble *dataP = env->GetDoubleArrayElements(data, 0);
    jsize n = env->GetArrayLength(data);
    int l = 1;
    while (l < len) {
        l = l * 2;
    }

    jdouble* sig = new jdouble[2*l];
    for(int i=0;i<l;i++) {
        if (i < n) {
            sig[2 * i] = dataP[i];
            sig[2 * i + 1] = 0;
        } else{
            sig[2*i] = 0;
            sig[2*i+1] = 0;
        }

    }
    cdft(2*l,1,sig);//4096 12288
    for(int i=0;i<2*l;i++){
        sig[i] = sig[i]/l;
    }
    jdoubleArray output = env->NewDoubleArray(2*l);
    env->SetDoubleArrayRegion(output,0,2*l,sig);
    env->ReleaseDoubleArrayElements(data, dataP, 0);
//    env->ReleasePrimitiveArrayCritical(data,dataP,0);
    return output;
}


JNIEXPORT jdoubleArray JNICALL
Java_com_example_jrnjsyx_fingerio_utils_JniUtils_xcorr(JNIEnv *env, jclass jobj,jdoubleArray data1,jdoubleArray data2){
    jdouble *dataP1 = env->GetDoubleArrayElements(data1, 0);
    jdouble *dataP2 = env->GetDoubleArrayElements(data2, 0);
    jsize n = env->GetArrayLength(data1);
    jdouble* res = new jdouble[n];
    jdouble* corr = new jdouble[n/2];
    res[0] = dataP1[0]*dataP2[0];
    res[1] = dataP1[1]*dataP2[1];
    for(int i=1;i<n/2;i++){
        double a = dataP1[2*i];
        double b = dataP1[2*i+1];
        double c = dataP2[2*i];
        double d = dataP2[2*i+1];
        res[2*i] = a*c+b*d;
        res[2*i+1] = b*c-a*d;
    }
    cdft(n,1,res);
    for(int i=0;i<n/2;i++){
        double a = res[2*i];
        double b = res[2*i+1];
        corr[i] = sqrt(a*a+b*b)*2/n;
    }
    delete [] res;
    jdoubleArray output = env->NewDoubleArray(n/2);
    env->SetDoubleArrayRegion(output,0,n/2,corr);
    env->ReleaseDoubleArrayElements(data1, dataP1, 0);
    env->ReleaseDoubleArrayElements(data2, dataP2, 0);
    return output;
}

//JNIEXPORT void JNICALL
//JavaCritical_hust_cc_asynchronousacousticlocalization_utils_JniUtils_realForward(jint length,jdouble* data) {
//    jdouble* sig = new jdouble[2*length];
//    for(int i=0;i<length;i++){
//        sig[2*i] = data[i];
//    }
//    cdft(2*length,-1,sig);
//    for(int i=0;i<length;i++){
//        data[i] = sig[i];
//    }
//}


}