/*
 *  Copyright 2009,2010 Martin Roth (mhroth@gmail.com)
 * 
 *  This file is part of JAsioHost.
 *
 *  JAsioHost is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  JAsioHost is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with JAsioHost.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

#include <stdio.h>
#include <string.h>
#include <vector>
#include "asiosys.h"
#include "asio.h"
#include "asiodrivers.h"
#include "iasiothiscallresolver.h"
#include "com_synthbot_jasiohost_AsioDriver.h"

#define JNI_VERSION JNI_VERSION_1_4

// external reference
extern AsioDrivers* asioDrivers;

// global variables
JavaVM *jvm;
jobject jAsioDriver; // a strong global reference to the AsioDriver Java object for use in callbacks

// global references to the callback methods
jmethodID fireBufferSwitchMid;
jmethodID fireLatenciesChangedMid;
jmethodID fireResetRequestMid;
jmethodID fireSampleRateDidChangeMid;
jmethodID fireResyncRequestMid;
jmethodID fireBufferSizeChangedMid;

typedef struct BufferVars {
  ASIOBufferInfo *bufferInfos;
  int numInitedChannels; // length of bufferInfos array
  int bufferSize;
  ASIOCallbacks *callbacks;
  long samplePosition;
};
BufferVars bufferVars = {0};

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *localJvm, void *reserved) {  
  jvm = localJvm; // store the JVM so that it can be used to attach ASIO threads to the JVM during callbacks
  asioDrivers = new AsioDrivers(); // set the global variable
  
  JNIEnv *env = NULL;
  jvm->GetEnv((void **) &env, JNI_VERSION);
  
  // assign global references to callback methods
  fireBufferSwitchMid = env->GetMethodID(
      env->FindClass("com/synthbot/jasiohost/AsioDriver"), 
      "fireBufferSwitch", "(JJI)V");
  fireLatenciesChangedMid = env->GetMethodID(
      env->FindClass("com/synthbot/jasiohost/AsioDriver"), 
      "fireLatenciesChanged", "(II)V");
  fireResetRequestMid = env->GetMethodID(
      env->FindClass("com/synthbot/jasiohost/AsioDriver"), 
      "fireResetRequest", "()V");
  fireSampleRateDidChangeMid = env->GetMethodID(
      env->FindClass("com/synthbot/jasiohost/AsioDriver"),
      "fireSampleRateDidChange", "(D)V");
  fireResyncRequestMid = env->GetMethodID(
      env->FindClass("com/synthbot/jasiohost/AsioDriver"),
      "fireResyncRequest", "()V");
  fireBufferSizeChangedMid = env->GetMethodID(
      env->FindClass("com/synthbot/jasiohost/AsioDriver"),
      "fireBufferSizeChanged", "(I)V");
  	  
  jAsioDriver = NULL;
  
  return JNI_VERSION;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *jvm, void *reserved) {
  if (asioDrivers) {
    delete asioDrivers;
  }
}

#if NATIVE_INT64
  #define ASIO64toLong(a)  (a)
#else
  #define ASIO64toLong(a)  (((unsigned long long int) a.hi) << 32) | (unsigned long long int) a.lo
#endif


/*
 * Driver Callbacks
 */

// from ASIOv2
ASIOTime* bufferSwitchTimeInfo(ASIOTime* asioTime, long bufferIndex, ASIOBool directProcess) {
  JNIEnv *env = NULL;
  jint res = jvm->AttachCurrentThreadAsDaemon((void **) &env, NULL);
  if (res == JNI_OK && env != NULL) {
    env->CallVoidMethod(
        jAsioDriver,
        fireBufferSwitchMid,
        (jlong) ASIO64toLong(asioTime->timeInfo.systemTime),
        (jlong) ASIO64toLong(asioTime->timeInfo.samplePosition),
        (jint) bufferIndex);
  }
  
  ASIOOutputReady();
  
  return asioTime;
}

// from ASIOv1
void bufferSwitch(long bufferIndex, ASIOBool directProcess) {
  ASIOTime asioTime = {0};
  ASIOError errorCode = ASIOGetSamplePosition(&asioTime.timeInfo.samplePosition, &asioTime.timeInfo.systemTime);
  if (errorCode == ASE_OK) {
    asioTime.timeInfo.flags = kSystemTimeValid | kSamplePositionValid;
  }
  bufferSwitchTimeInfo(&asioTime, bufferIndex, directProcess);
}

void sampleRateDidChange(ASIOSampleRate sampleRate) {
  JNIEnv *env = NULL;
  jint res = jvm->AttachCurrentThreadAsDaemon((void **) &env, NULL);
  if (res == JNI_OK && env != NULL) {
    env->CallVoidMethod(
        jAsioDriver,
        fireSampleRateDidChangeMid,
        (jdouble) sampleRate);
  }
}
long asioMessage(long selector, long value, void* message, double* opt) {
  switch (selector) {
    case kAsioSelectorSupported: {
      switch (value) {
        case kAsioEngineVersion:
        case kAsioResetRequest:
        case kAsioResyncRequest:
        case kAsioBufferSizeChange:
        case kAsioLatenciesChanged:
        case kAsioSupportsTimeInfo:
        case kAsioSupportsTimeCode: {
          return 1L;
        }
        default: {
          // no supporting kAsioSupportsInputMonitor yet because I don't know what it is
          return 0L;
        }
      }
    }
      
    case kAsioEngineVersion: {
      return 2L;
    }
      
    case kAsioResetRequest: {
      JNIEnv *env = NULL;
      jint res = jvm->AttachCurrentThreadAsDaemon((void **) &env, NULL);
      if (res == JNI_OK && env != NULL) {
        env->CallVoidMethod(
            jAsioDriver,
			fireResetRequestMid);
      }
      return 1L;
    }
      
    case kAsioResyncRequest: {
      JNIEnv *env = NULL;
      jint res = jvm->AttachCurrentThreadAsDaemon((void **) &env, NULL);
      if (res == JNI_OK && env != NULL) {
        env->CallVoidMethod(
            jAsioDriver,
            fireResyncRequestMid);
      }
      return 1L;
    }
    
    case kAsioBufferSizeChange: {
      JNIEnv *env = NULL;
      jint res = jvm->AttachCurrentThreadAsDaemon((void **) &env, NULL);
      if (res == JNI_OK && env != NULL) {
        env->CallVoidMethod(
            jAsioDriver,
            fireBufferSizeChangedMid,
            (jint) value);
      }
      return 1L; // the request is always accepted
    }
      
    case kAsioLatenciesChanged: {
      JNIEnv *env = NULL;
      jint res = jvm->AttachCurrentThreadAsDaemon((void **) &env, NULL);
      if (res == JNI_OK && env != NULL) {
        env->CallVoidMethod(
            jAsioDriver,
			fireLatenciesChangedMid,
            Java_com_synthbot_jasiohost_AsioDriver_ASIOGetLatencies(env, NULL, JNI_TRUE),
            Java_com_synthbot_jasiohost_AsioDriver_ASIOGetLatencies(env, NULL, JNI_FALSE));
      }
      return 1L;
    }
      
    case kAsioSupportsTimeInfo: {
      return 1L;
    }
    
    case kAsioSupportsTimeCode: {
      return 0L;
    }
      
    default: {
      return 0L;
    }
  }
}


/*
 * JAsioHost
 */

JNIEXPORT void JNICALL Java_com_synthbot_jasiohost_AsioDriver_registerThread
(JNIEnv *env, jclass clazz) {

  CoInitialize(NULL);
}

JNIEXPORT jboolean JNICALL Java_com_synthbot_jasiohost_AsioDriver_loadDriver
(JNIEnv *env, jclass clazz, jstring jdriverName) {

  char *driverName = (char *) env->GetStringUTFChars(jdriverName, NULL);
  bool isLoaded = asioDrivers->loadDriver(driverName);
  env->ReleaseStringUTFChars(jdriverName, driverName);
  return isLoaded ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jobject JNICALL Java_com_synthbot_jasiohost_AsioDriver_ASIOInit
(JNIEnv *env, jobject jobj) {

  ASIODriverInfo asioDriverInfo = {0};
  asioDriverInfo.asioVersion = 2L;
  ASIOError errorCode = ASIOInit(&asioDriverInfo);
  if (errorCode == ASE_OK) {
    jAsioDriver = env->NewGlobalRef(jobj); // store global ref to AsioDriver object for callbacks
    
    // create new AsioDriverInfo object and return it
    return env->NewObject(
        env->FindClass("com/synthbot/jasiohost/AsioDriverInfo"),
        env->GetMethodID(env->FindClass("com/synthbot/jasiohost/AsioDriverInfo"), "<init>", "(IILjava/lang/String;Ljava/lang/String;)V"),
        asioDriverInfo.asioVersion,
        asioDriverInfo.driverVersion,
        env->NewStringUTF(asioDriverInfo.name),
        env->NewStringUTF(asioDriverInfo.errorMessage));
  } else {
    env->ThrowNew(
      env->FindClass("com/synthbot/jasiohost/AsioException"),
      asioDriverInfo.errorMessage);
    return NULL;
  }
}

JNIEXPORT void JNICALL Java_com_synthbot_jasiohost_AsioDriver_ASIOExit
(JNIEnv *env, jobject jobj) {

  env->DeleteGlobalRef(jAsioDriver);
  jAsioDriver = NULL;
  ASIOExit();
}

JNIEXPORT void JNICALL Java_com_synthbot_jasiohost_AsioDriver_removeCurrentDriver
(JNIEnv *env, jclass clazz) {

  asioDrivers->removeCurrentDriver();
}

JNIEXPORT jint JNICALL Java_com_synthbot_jasiohost_AsioDriver_getDriverNames
(JNIEnv *env, jclass clazz, jobjectArray jdriverNames) {

  long maxNames = (long) env->GetArrayLength(jdriverNames);
  char **names = (char **) malloc(sizeof(char *) * maxNames);
  for (int i = 0; i < maxNames; i++) {
    names[i] = (char *) malloc(sizeof(char) * 32);
  }
  
  long numNames = asioDrivers->getDriverNames(names, maxNames);
  for (int i = 0; i < numNames; i++) {
    env->SetObjectArrayElement(jdriverNames, i, env->NewStringUTF(names[i]));
  }
  
  for(int i = 0; i < maxNames; i++) {
    free(names[i]);
  }
  free(names);
  
  return (jint) numNames;
}

/**
 * Deprecated in Java
 *
JNIEXPORT jint JNICALL Java_com_synthbot_jasiohost_AsioDriver_getCurrentDriverIndex
(JNIEnv *env, jclass clazz) {

  return (jint) asioDrivers->getCurrentDriverIndex();
}
 */

/**
 * Deprecated in Java
 *
JNIEXPORT jstring JNICALL Java_com_synthbot_jasiohost_AsioDriver_getCurrentDriverName
(JNIEnv *env, jclass clazz) {

  char *name = (char *) malloc(sizeof(char) * 32);
  asioDrivers->getCurrentDriverName(name);
  jstring jname = env->NewStringUTF(name);
  free(name);
  return jname;
}
 */


/*
 * AsioDriver
 */

JNIEXPORT void JNICALL Java_com_synthbot_jasiohost_AsioDriver_ASIOControlPanel
(JNIEnv *env, jclass clazz) {

  ASIOControlPanel();
}

JNIEXPORT jint JNICALL Java_com_synthbot_jasiohost_AsioDriver_ASIOGetChannels
(JNIEnv *env, jclass clazz, jboolean isInput) {
  
  long numInputs;
  long numOutputs;
  ASIOError errorCode = ASIOGetChannels(&numInputs, &numOutputs);
  if (errorCode == ASE_OK) {
    return (isInput == JNI_TRUE) ? (jint) numInputs : (jint) numOutputs;
  } else {
    return (jint) 0;
  }
}

JNIEXPORT jdouble JNICALL Java_com_synthbot_jasiohost_AsioDriver_ASIOGetSampleRate
(JNIEnv *env, jclass clazz) {
  
  ASIOSampleRate sampleRate;
  ASIOError errorCode = ASIOGetSampleRate(&sampleRate);
  switch (errorCode) {
    case ASE_OK: {
      return (jdouble) sampleRate;
    }
    case ASE_NoClock: {
      env->ThrowNew(
          env->FindClass("com/synthbot/jasiohost/AsioException"),
          "Sample rate not present or unknown.");
      return (jdouble) -1.0;
    }
    case ASE_NotPresent: {
      env->ThrowNew(
          env->FindClass("com/synthbot/jasiohost/AsioException"),
          "No input or output is present.");
      return (jdouble) -1.0;
    }
  }
}

JNIEXPORT jboolean JNICALL Java_com_synthbot_jasiohost_AsioDriver_ASIOCanSampleRate
(JNIEnv *env, jclass clazz, jdouble sampleRate) {
  
  ASIOError errorCode = ASIOCanSampleRate((ASIOSampleRate) sampleRate);
  return (errorCode == ASE_OK) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_com_synthbot_jasiohost_AsioDriver_ASIOSetSampleRate
(JNIEnv *env, jclass clazz, jdouble sampleRate) {
  
  ASIOError errorCode = ASIOSetSampleRate((ASIOSampleRate) sampleRate);
  switch (errorCode) {
    case ASE_OK: {
      return;
    }
    case ASE_NoClock: {
      env->ThrowNew(
          env->FindClass("com/synthbot/jasiohost/AsioException"),
          "Sample rate not present or unknown.");
    }
    case ASE_InvalidMode: {
      env->ThrowNew(
          env->FindClass("com/synthbot/jasiohost/AsioException"),
          "The current clock is external and the given sample rate is non-zero.");
    }
    case ASE_NotPresent: {
      env->ThrowNew(
          env->FindClass("com/synthbot/jasiohost/AsioException"),
          "No input or output is present.");
    }
  }
}

JNIEXPORT jint JNICALL Java_com_synthbot_jasiohost_AsioDriver_ASIOGetBufferSize
(JNIEnv *env, jclass clazz, jint argIndex) {
  
  long minSize;
  long maxSize;
  long preferredSize;
  long granularity;
  ASIOError errorCode = ASIOGetBufferSize(&minSize, &maxSize, &preferredSize, &granularity);
  if (errorCode == ASE_OK) {
    switch (argIndex) {
      case 0: {
        return (jint) minSize;
      }
      case 1: {
        return (jint) maxSize;
      }
      case 2: {
        return (jint) preferredSize;
      }
      case 3: {
        return (jint) granularity;
      }
      default: {
        return -1;
      }
    }
  } else {
    return -1;
  }
}

JNIEXPORT jint JNICALL Java_com_synthbot_jasiohost_AsioDriver_ASIOGetLatencies
(JNIEnv *env, jclass clazz, jboolean isInput) {
  
  long inputLatency;
  long outputLatency;
  ASIOError errorCode = ASIOGetLatencies(&inputLatency, &outputLatency);
  switch (errorCode) {
    case ASE_OK: {
      // normal operation
      return (isInput == JNI_TRUE) ? (jint) inputLatency : (jint) outputLatency;
    }
    case ASE_NotPresent: {
      env->ThrowNew(
          env->FindClass("com/synthbot/jasiohost/AsioException"),
          (isInput == JNI_TRUE) ? "The requested input does not exist." : "The requested output does not exist.");
      return (jint) 0;
    }
    default: {
      env->ThrowNew(
          env->FindClass("com/synthbot/jasiohost/AsioException"),
          "Unknown error code.");
      return (jint) 0;
    }
  }
}

JNIEXPORT jobject JNICALL Java_com_synthbot_jasiohost_AsioDriver_ASIOGetChannelInfo
(JNIEnv *env, jclass clazz, jint index, jboolean isInput) {

  ASIOChannelInfo channelInfo;
  channelInfo.channel = (long) index;
  channelInfo.isInput = (isInput == JNI_TRUE) ? ASIOTrue : ASIOFalse;
  ASIOError errorCode = ASIOGetChannelInfo(&channelInfo);
  
  switch (errorCode) {
    case ASE_OK: {
      return env->NewObject(
          env->FindClass("com/synthbot/jasiohost/AsioChannel"),
          env->GetMethodID(env->FindClass("com/synthbot/jasiohost/AsioChannel"), "<init>", "(IZZILcom/synthbot/jasiohost/AsioSampleType;Ljava/lang/String;)V"),
          (jint) index,
          isInput,
          (channelInfo.isActive == ASIOTrue) ? JNI_TRUE : JNI_FALSE,
          (jint) channelInfo.channelGroup,
          env->CallStaticObjectMethod(
              env->FindClass("com/synthbot/jasiohost/AsioSampleType"),
              env->GetStaticMethodID(env->FindClass("com/synthbot/jasiohost/AsioSampleType"), "getSampleType", "(I)Lcom/synthbot/jasiohost/AsioSampleType;"),
              channelInfo.type),
          env->NewStringUTF(channelInfo.name));
    }
    case ASE_NotPresent: {
      env->ThrowNew(
          env->FindClass("com/synthbot/jasiohost/AsioException"),
          (isInput == JNI_TRUE) ? "The requested input does not exist." : "The requested output does not exist.");
      return NULL;
    }
    default: {
      env->ThrowNew(
          env->FindClass("com/synthbot/jasiohost/AsioException"),
          "Unknown error code.");
      return NULL;
    }
  }
}

JNIEXPORT void JNICALL Java_com_synthbot_jasiohost_AsioDriver_ASIOCreateBuffers
(JNIEnv *env, jclass clazz, jobjectArray channelsToInit, jint bufferSize) {

  bufferVars.numInitedChannels = (int) env->GetArrayLength(channelsToInit);
  bufferVars.bufferInfos = (ASIOBufferInfo *) malloc(sizeof(ASIOBufferInfo) * bufferVars.numInitedChannels);
  std::vector<ASIOSampleType> sampleTypes(bufferVars.numInitedChannels);
  for (int i = 0; i < bufferVars.numInitedChannels; i++) {
    jobject channelInfo = env->GetObjectArrayElement(channelsToInit, (jsize) i);
    jboolean jIsInput = env->CallBooleanMethod(channelInfo,
                                               env->GetMethodID(env->FindClass("com/synthbot/jasiohost/AsioChannel"), "isInput", "()Z"));
    bufferVars.bufferInfos[i].isInput = (jIsInput == JNI_TRUE) ? ASIOTrue : ASIOFalse;
    jint channelNum = env->CallIntMethod(channelInfo,
                                         env->GetMethodID(env->FindClass("com/synthbot/jasiohost/AsioChannel"), "getChannelIndex", "()I"));
    bufferVars.bufferInfos[i].channelNum = (long) channelNum;
    sampleTypes[i] = env->GetIntField(env->CallObjectMethod(
                                          channelInfo,
                                          env->GetMethodID(env->FindClass("com/synthbot/jasiohost/AsioChannel"), "getSampleType", "()Lcom/synthbot/jasiohost/AsioSampleType;")),
                                      env->GetFieldID(env->FindClass("com/synthbot/jasiohost/AsioSampleType"), "nativeEnum", "I"));    
  }
  
  bufferVars.bufferSize = (int) bufferSize;
  
  bufferVars.callbacks = (ASIOCallbacks *) malloc(sizeof(ASIOCallbacks));
  bufferVars.callbacks->bufferSwitch = &bufferSwitch;
  bufferVars.callbacks->sampleRateDidChange = &sampleRateDidChange;
  bufferVars.callbacks->asioMessage = &asioMessage;
  bufferVars.callbacks->bufferSwitchTimeInfo = &bufferSwitchTimeInfo;
  
  ASIOError errorCode = ASIOCreateBuffers(bufferVars.bufferInfos, (long) bufferVars.numInitedChannels, (long) bufferVars.bufferSize, bufferVars.callbacks);
  
  switch (errorCode) {
    case ASE_OK: {
      for (int i = 0; i < bufferVars.numInitedChannels; i++) {
        jobject byteBuffer0 = NULL;
        jobject byteBuffer1 = NULL;
        switch (sampleTypes[i]) {
          case ASIOSTFloat64MSB:
          case ASIOSTFloat64LSB: {
            if (bufferVars.bufferInfos[i].buffers[0] != NULL &&
                bufferVars.bufferInfos[i].buffers[1] != NULL) {
              byteBuffer0 = env->NewDirectByteBuffer(bufferVars.bufferInfos[i].buffers[0], bufferSize * 8);
              byteBuffer1 = env->NewDirectByteBuffer(bufferVars.bufferInfos[i].buffers[1], bufferSize * 8);
            }
            break;
          }
          case ASIOSTFloat32MSB:
          case ASIOSTFloat32LSB:
          case ASIOSTInt32MSB:
          case ASIOSTInt32MSB16:
          case ASIOSTInt32MSB18:
          case ASIOSTInt32MSB20:
          case ASIOSTInt32MSB24:
          case ASIOSTInt32LSB:
          case ASIOSTInt32LSB16:
          case ASIOSTInt32LSB18:
          case ASIOSTInt32LSB20:
          case ASIOSTInt32LSB24: {
            if (bufferVars.bufferInfos[i].buffers[0] != NULL &&
                bufferVars.bufferInfos[i].buffers[1] != NULL) {
              byteBuffer0 = env->NewDirectByteBuffer(bufferVars.bufferInfos[i].buffers[0], bufferSize * 4);
              byteBuffer1 = env->NewDirectByteBuffer(bufferVars.bufferInfos[i].buffers[1], bufferSize * 4);
            }
            break;
          }
          case ASIOSTInt24MSB:
          case ASIOSTInt24LSB: {
            if (bufferVars.bufferInfos[i].buffers[0] != NULL &&
                bufferVars.bufferInfos[i].buffers[1] != NULL) {
              byteBuffer0 = env->NewDirectByteBuffer(bufferVars.bufferInfos[i].buffers[0], bufferSize * 3);
              byteBuffer1 = env->NewDirectByteBuffer(bufferVars.bufferInfos[i].buffers[1], bufferSize * 3);
            }
            break;
          }
          case ASIOSTInt16MSB:
          case ASIOSTInt16LSB: {
            if (bufferVars.bufferInfos[i].buffers[0] != NULL &&
                bufferVars.bufferInfos[i].buffers[1] != NULL) {
              byteBuffer0 = env->NewDirectByteBuffer(bufferVars.bufferInfos[i].buffers[0], bufferSize * 2);
              byteBuffer1 = env->NewDirectByteBuffer(bufferVars.bufferInfos[i].buffers[1], bufferSize * 2);
            }
            break;
          }
          case ASIOSTDSDInt8MSB1:
          case ASIOSTDSDInt8LSB1:
          case ASIOSTDSDInt8NER8: {
            if (bufferVars.bufferInfos[i].buffers[0] != NULL &&
                bufferVars.bufferInfos[i].buffers[1] != NULL) {
              byteBuffer0 = env->NewDirectByteBuffer(bufferVars.bufferInfos[i].buffers[0], bufferSize * 1);
              byteBuffer1 = env->NewDirectByteBuffer(bufferVars.bufferInfos[i].buffers[1], bufferSize * 1);
            }
            break;
          }
          default: {
            env->ThrowNew(
                env->FindClass("com/synthbot/jasiohost/AsioException"),
                "Unknown sample type.");
            break;
          }
        }
        
        if (byteBuffer0 == NULL || byteBuffer1 == NULL) {
          if (env->ExceptionCheck() == JNI_TRUE) {
            env->Throw(env->ExceptionOccurred());
            return;
          } else {
            env->ThrowNew(
                env->FindClass("com/synthbot/jasiohost/AsioException"),
                "JNI access to direct buffers is not supported by this virtual machine.");
            return;
          }
        }
        env->CallVoidMethod(
            env->GetObjectArrayElement(channelsToInit, (jsize) i),
            env->GetMethodID(
                env->FindClass("com/synthbot/jasiohost/AsioChannel"), 
                "setByteBuffers", "(Ljava/nio/ByteBuffer;Ljava/nio/ByteBuffer;)V"),
            byteBuffer0,
            byteBuffer1);
      }
      return;
    }
    case ASE_NoMemory: {
      env->ThrowNew(
          env->FindClass("com/synthbot/jasiohost/AsioException"),
          "Not enough memory is available for the audio buffers to be created.");
      return;
    }
    case ASE_NotPresent: {
      env->ThrowNew(
          env->FindClass("com/synthbot/jasiohost/AsioException"),
          "No input or output is present.");
      return;
    }
    case ASE_InvalidMode: {
      env->ThrowNew(
          env->FindClass("com/synthbot/jasiohost/AsioException"),
          "The buffer size is not supported.");
      return;
    }
    default: {
      env->ThrowNew(
          env->FindClass("com/synthbot/jasiohost/AsioException"),
          "Unknown error code.");
      return;
    }
  }
}

JNIEXPORT void JNICALL Java_com_synthbot_jasiohost_AsioDriver_ASIODisposeBuffers
(JNIEnv *env, jclass clazz) {

  free(bufferVars.bufferInfos);
  free(bufferVars.callbacks);
  
  ASIOError errorCode = ASIODisposeBuffers();
  
  switch (errorCode) {
    case ASE_OK: {
      // normal operation
      return;
    }
    case ASE_InvalidMode: {
      env->ThrowNew(
          env->FindClass("com/synthbot/jasiohost/AsioException"),
          "No buffers to dispose of. None were ever created.");
      return;
    }
    case ASE_NotPresent: {
      env->ThrowNew(
          env->FindClass("com/synthbot/jasiohost/AsioException"),
          "No input or output is present.");
      return;
    }
    default: {
      env->ThrowNew(
          env->FindClass("com/synthbot/jasiohost/AsioException"),
          "Unknown error code.");
    }
  }
}

JNIEXPORT void JNICALL Java_com_synthbot_jasiohost_AsioDriver_ASIOStart
(JNIEnv *env, jclass clazz) {

  ASIOError errorCode = ASIOStart();
  
  switch (errorCode) {
    case ASE_OK: {
      // normal operation
      return;
    }
    case ASE_NotPresent: {
      env->ThrowNew(
          env->FindClass("com/synthbot/jasiohost/AsioException"),
          "No input or output is present.");
      return;
    }
    case ASE_HWMalfunction: {
      env->ThrowNew(
          env->FindClass("com/synthbot/jasiohost/AsioException"),
          "The hardware has malfunctioned.");
      return;
    }
    default: {
      env->ThrowNew(
          env->FindClass("com/synthbot/jasiohost/AsioException"),
          "Unknown error code.");
      return;
    }
  }
}

JNIEXPORT void JNICALL Java_com_synthbot_jasiohost_AsioDriver_ASIOStop
(JNIEnv *env, jclass clazz) {

  ASIOError errorCode = ASIOStop();
  
  switch (errorCode) {
    case ASE_OK: {
      // normal operation
      return;
    }
    case ASE_NotPresent: {
      env->ThrowNew(
          env->FindClass("com/synthbot/jasiohost/AsioException"),
          "No input or output is present.");
      return;
    }
    default: {
      env->ThrowNew(
          env->FindClass("com/synthbot/jasiohost/AsioException"),
          "Unknown error code.");
      return;
    }
  }
}