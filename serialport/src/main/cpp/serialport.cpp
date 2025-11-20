#include <jni.h>
#include <string>
#include <fcntl.h>
#include <termios.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <android/log.h>

#define TAG "SerialPortJNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jobject JNICALL
Java_com_onatakduman_serialport_SerialPortJNI_open(JNIEnv *env, jobject thiz, jstring path, jint flags) {
    const char *path_utf = env->GetStringUTFChars(path, 0);
    if (path_utf == nullptr) {
        return nullptr;
    }

    LOGD("Opening serial port: %s with flags: %d", path_utf, flags);
    int fd = open(path_utf, O_RDWR | flags);
    env->ReleaseStringUTFChars(path, path_utf);

    if (fd == -1) {
        LOGE("Cannot open port");
        return nullptr;
    }

    jclass fileDescriptorClass = env->FindClass("java/io/FileDescriptor");
    jmethodID fileDescriptorConstructor = env->GetMethodID(fileDescriptorClass, "<init>", "()V");
    jobject fileDescriptor = env->NewObject(fileDescriptorClass, fileDescriptorConstructor);
    jfieldID descriptorField = env->GetFieldID(fileDescriptorClass, "descriptor", "I");
    env->SetIntField(fileDescriptor, descriptorField, fd);

    return fileDescriptor;
}

JNIEXPORT void JNICALL
Java_com_onatakduman_serialport_SerialPortJNI_close(JNIEnv *env, jobject thiz, jobject fileDescriptor) {
    jclass fileDescriptorClass = env->FindClass("java/io/FileDescriptor");
    jfieldID descriptorField = env->GetFieldID(fileDescriptorClass, "descriptor", "I");
    int fd = env->GetIntField(fileDescriptor, descriptorField);

    if (fd != -1) {
        LOGD("Closing serial port: %d", fd);
        close(fd);
        env->SetIntField(fileDescriptor, descriptorField, -1);
    }
}

static speed_t getBaudRate(jint baudRate) {
    switch (baudRate) {
        case 0: return B0;
        case 50: return B50;
        case 75: return B75;
        case 110: return B110;
        case 134: return B134;
        case 150: return B150;
        case 200: return B200;
        case 300: return B300;
        case 600: return B600;
        case 1200: return B1200;
        case 1800: return B1800;
        case 2400: return B2400;
        case 4800: return B4800;
        case 9600: return B9600;
        case 19200: return B19200;
        case 38400: return B38400;
        case 57600: return B57600;
        case 115200: return B115200;
        case 230400: return B230400;
        case 460800: return B460800;
        case 500000: return B500000;
        case 576000: return B576000;
        case 921600: return B921600;
        case 1000000: return B1000000;
        case 1152000: return B1152000;
        case 1500000: return B1500000;
        case 2000000: return B2000000;
        case 2500000: return B2500000;
        case 3000000: return B3000000;
        case 3500000: return B3500000;
        case 4000000: return B4000000;
        default: return -1;
    }
}

JNIEXPORT jboolean JNICALL
Java_com_onatakduman_serialport_SerialPortJNI_configure(JNIEnv *env, jobject thiz, jobject fileDescriptor,
                                                        jint baudRate, jint dataBits, jint stopBits, jint parity) {
    jclass fileDescriptorClass = env->FindClass("java/io/FileDescriptor");
    jfieldID descriptorField = env->GetFieldID(fileDescriptorClass, "descriptor", "I");
    int fd = env->GetIntField(fileDescriptor, descriptorField);

    struct termios cfg;
    if (tcgetattr(fd, &cfg)) {
        LOGE("tcgetattr() failed");
        return JNI_FALSE;
    }

    cfmakeraw(&cfg);
    speed_t speed = getBaudRate(baudRate);
    if (speed == -1) {
        LOGE("Invalid baud rate");
        return JNI_FALSE;
    }
    cfsetispeed(&cfg, speed);
    cfsetospeed(&cfg, speed);

    cfg.c_cflag &= ~CSIZE;
    switch (dataBits) {
        case 5: cfg.c_cflag |= CS5; break;
        case 6: cfg.c_cflag |= CS6; break;
        case 7: cfg.c_cflag |= CS7; break;
        case 8: cfg.c_cflag |= CS8; break;
        default: cfg.c_cflag |= CS8; break;
    }

    switch (parity) {
        case 0: cfg.c_cflag &= ~PARENB; break; // None
        case 1: cfg.c_cflag |= (PARODD | PARENB); break; // Odd
        case 2: cfg.c_cflag |= PARENB; cfg.c_cflag &= ~PARODD; break; // Even
        default: cfg.c_cflag &= ~PARENB; break;
    }

    switch (stopBits) {
        case 1: cfg.c_cflag &= ~CSTOPB; break;
        case 2: cfg.c_cflag |= CSTOPB; break;
        default: cfg.c_cflag &= ~CSTOPB; break;
    }

    if (tcsetattr(fd, TCSANOW, &cfg)) {
        LOGE("tcsetattr() failed");
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

}