#include <jni.h>
#include <string>
#include <fcntl.h>
#include <termios.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <android/log.h>
#include <cerrno>
#include <cstring>

#define TAG "SerialPortJNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jobject JNICALL
Java_com_onatakduman_kserialport_SerialPortJNI_open(JNIEnv *env, jobject thiz, jstring path, jint flags) {
    if (path == nullptr) {
        LOGE("Path is null");
        return nullptr;
    }

    const char *path_utf = env->GetStringUTFChars(path, nullptr);
    if (path_utf == nullptr) {
        LOGE("Failed to get path string");
        return nullptr;
    }

    LOGD("Opening serial port: %s with flags: %d", path_utf, flags);
    int fd = open(path_utf, O_RDWR | O_NOCTTY | O_NONBLOCK | flags);
    env->ReleaseStringUTFChars(path, path_utf);

    if (fd == -1) {
        LOGE("Cannot open port: %s", strerror(errno));
        return nullptr;
    }

    // Clear non-blocking flag after open (used only to prevent blocking on open)
    int currentFlags = fcntl(fd, F_GETFL);
    if (currentFlags != -1) {
        fcntl(fd, F_SETFL, currentFlags & ~O_NONBLOCK);
    }

    jclass fileDescriptorClass = env->FindClass("java/io/FileDescriptor");
    if (fileDescriptorClass == nullptr) {
        LOGE("Cannot find FileDescriptor class");
        close(fd);
        return nullptr;
    }

    jmethodID fileDescriptorConstructor = env->GetMethodID(fileDescriptorClass, "<init>", "()V");
    if (fileDescriptorConstructor == nullptr) {
        LOGE("Cannot find FileDescriptor constructor");
        close(fd);
        return nullptr;
    }

    jobject fileDescriptor = env->NewObject(fileDescriptorClass, fileDescriptorConstructor);
    if (fileDescriptor == nullptr) {
        LOGE("Cannot create FileDescriptor object");
        close(fd);
        return nullptr;
    }

    jfieldID descriptorField = env->GetFieldID(fileDescriptorClass, "descriptor", "I");
    if (descriptorField == nullptr) {
        LOGE("Cannot find descriptor field");
        close(fd);
        return nullptr;
    }

    env->SetIntField(fileDescriptor, descriptorField, fd);
    return fileDescriptor;
}

JNIEXPORT void JNICALL
Java_com_onatakduman_kserialport_SerialPortJNI_close(JNIEnv *env, jobject thiz, jobject fileDescriptor) {
    if (fileDescriptor == nullptr) {
        LOGE("FileDescriptor is null");
        return;
    }

    jclass fileDescriptorClass = env->FindClass("java/io/FileDescriptor");
    if (fileDescriptorClass == nullptr) {
        LOGE("Cannot find FileDescriptor class");
        return;
    }

    jfieldID descriptorField = env->GetFieldID(fileDescriptorClass, "descriptor", "I");
    if (descriptorField == nullptr) {
        LOGE("Cannot find descriptor field");
        return;
    }

    int fd = env->GetIntField(fileDescriptor, descriptorField);
    if (fd != -1) {
        LOGD("Closing serial port: %d", fd);
        if (close(fd) == -1) {
            LOGE("Error closing fd %d: %s", fd, strerror(errno));
        }
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
Java_com_onatakduman_kserialport_SerialPortJNI_configure(JNIEnv *env, jobject thiz, jobject fileDescriptor,
                                                        jint baudRate, jint dataBits, jint stopBits, jint parity) {
    if (fileDescriptor == nullptr) {
        LOGE("FileDescriptor is null");
        return JNI_FALSE;
    }

    jclass fileDescriptorClass = env->FindClass("java/io/FileDescriptor");
    if (fileDescriptorClass == nullptr) {
        LOGE("Cannot find FileDescriptor class");
        return JNI_FALSE;
    }

    jfieldID descriptorField = env->GetFieldID(fileDescriptorClass, "descriptor", "I");
    if (descriptorField == nullptr) {
        LOGE("Cannot find descriptor field");
        return JNI_FALSE;
    }

    int fd = env->GetIntField(fileDescriptor, descriptorField);
    if (fd < 0) {
        LOGE("Invalid file descriptor: %d", fd);
        return JNI_FALSE;
    }

    struct termios cfg;
    if (tcgetattr(fd, &cfg) == -1) {
        LOGE("tcgetattr() failed: %s", strerror(errno));
        return JNI_FALSE;
    }

    cfmakeraw(&cfg);
    speed_t speed = getBaudRate(baudRate);
    if (speed == (speed_t)-1) {
        LOGE("Invalid baud rate: %d", baudRate);
        return JNI_FALSE;
    }
    cfsetispeed(&cfg, speed);
    cfsetospeed(&cfg, speed);

    // Data bits
    cfg.c_cflag &= ~CSIZE;
    switch (dataBits) {
        case 5: cfg.c_cflag |= CS5; break;
        case 6: cfg.c_cflag |= CS6; break;
        case 7: cfg.c_cflag |= CS7; break;
        case 8: cfg.c_cflag |= CS8; break;
        default: cfg.c_cflag |= CS8; break;
    }

    // Parity
    switch (parity) {
        case 0: cfg.c_cflag &= ~PARENB; break; // None
        case 1: cfg.c_cflag |= (PARODD | PARENB); break; // Odd
        case 2: cfg.c_cflag |= PARENB; cfg.c_cflag &= ~PARODD; break; // Even
        default: cfg.c_cflag &= ~PARENB; break;
    }

    // Stop bits
    switch (stopBits) {
        case 1: cfg.c_cflag &= ~CSTOPB; break;
        case 2: cfg.c_cflag |= CSTOPB; break;
        default: cfg.c_cflag &= ~CSTOPB; break;
    }

    // Enable receiver and ignore modem control lines
    cfg.c_cflag |= (CLOCAL | CREAD);

    // Configure read timeout: VMIN=1, VTIME=1 (100ms timeout after first byte)
    cfg.c_cc[VMIN] = 1;
    cfg.c_cc[VTIME] = 1;

    if (tcsetattr(fd, TCSANOW, &cfg) == -1) {
        LOGE("tcsetattr() failed: %s", strerror(errno));
        return JNI_FALSE;
    }

    // Flush any pending data
    tcflush(fd, TCIOFLUSH);

    return JNI_TRUE;
}

}